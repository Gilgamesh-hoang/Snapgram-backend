package org.snapgram.service.suggestion;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.exception.MultiThreadException;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HybridFriendSuggestionService implements FriendSuggestionService {
    /*
     * - RWR mô phỏng một người đi bộ ngẫu nhiên trên đồ thị, bắt đầu từ một nút (người dùng) và có xác suất nhất định
     * quay trở lại nút bắt đầu. Các nút được ghé thăm nhiều lần có khả năng là những đề xuất tốt.
     * - sử dụng thuật toán lọc cộng tác
     * */

    IUserService userService;
    static final double RESET_PROBABILITY = 0.4; // Probability to return to the starting point (random jump)
    static final double WEIGHT = 0.5; // Weight for combining scores from different algorithms
    static final int NUM_ITERATIONS = 1000; // Number of steps (iterations) of the Random Walk

    // Method implementing the Friends-of-a-Friend (FOAF) algorithm
    public Map<UUID, Double> foafAlgorithm(UUID userId) {
        // Get the direct friends of the user
        List<UserDTO> directFriends = userService.findFriendsByUserId(userId);
        // Map to store the FOAF candidates and their scores
        Map<UUID, Double> foafCandidates = new ConcurrentHashMap<>();

        // For each direct friend, asynchronously get their friends and update the scores of the candidates
        List<CompletableFuture<Void>> futures = directFriends.stream()
                .map(friend -> CompletableFuture.runAsync(() -> {
                    List<UserDTO> friendsOfFriend = userService.findFriendsByUserId(friend.getId());
                    for (UserDTO foaf : friendsOfFriend) {
                        if (!foaf.getId().equals(userId) && !directFriends.contains(foaf)) {
                            foafCandidates.compute(foaf.getId(), (key, val) -> (val == null) ? 1 : val + 1);
                        }
                    }
                }))
                .toList();

        // Wait for all asynchronous tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // Normalize the scores
        double totalScore = foafCandidates.values().stream().mapToDouble(Double::doubleValue).sum();
        foafCandidates.replaceAll((key, value) -> value / totalScore);
        return foafCandidates;
    }

    // Method to recommend friends to a user
    @Override
    public List<UserDTO> recommendFriends(UUID userId) {
        // Step 1: Use FOAF to generate candidates
        CompletableFuture<Map<UUID, Double>> foafFuture = CompletableFuture.supplyAsync(() -> foafAlgorithm(userId));

        // Step 2: Use Random Walk to rank candidates
        CompletableFuture<Map<UUID, Double>> randomWalkFuture = CompletableFuture.supplyAsync(() -> randomWalk(userId));

        try {
            // Get the results of FOAF and Random Walk
            Map<UUID, Double> foafCandidates = foafFuture.get();
            Map<UUID, Double> randomWalkScores = randomWalkFuture.get();

            // Combine the results from both algorithms
            List<UserDTO> combineResults = combineResults(foafCandidates, randomWalkScores);

            // If there are less than 30 results, add random users
            int missingNum = Math.abs(combineResults.size() - 30);
            if (missingNum > 0) {
                List<UserDTO> randomUsers = userService.findRandomUsers(missingNum, combineResults.stream().map(UserDTO::getId).toList());
                combineResults.addAll(randomUsers);
            }
            return combineResults;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new MultiThreadException("Error occurred during asynchronous processing", e);
        }
    }

    // Method to combine the results from FOAF and Random Walk
    private List<UserDTO> combineResults(Map<UUID, Double> foafCandidates, Map<UUID, Double> randomWalkScores) {
        Map<UUID, Double> combinedScores = new HashMap<>();
        for (Map.Entry<UUID, Double> entry : foafCandidates.entrySet()) {
            UUID key = entry.getKey();
            Double value = entry.getValue();
            double foafScore = value * WEIGHT;
            double randomWalkScore = randomWalkScores.getOrDefault(key, 0.0) * WEIGHT;
            combinedScores.put(key, foafScore + randomWalkScore);
        }

        // Sort the combined scores and limit the results to the top 30
        LinkedHashMap<UUID, Double> results = combinedScores.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        // Convert the user IDs to UserDTO objects
        return results.keySet().stream()
                .map(userService::findById)
                .toList();
    }

    // Method implementing the Random Walk algorithm
    public Map<UUID, Double> randomWalk(UUID userId) {
        Map<UUID, Double> walkScores = new HashMap<>();
        Map<UUID, List<UserDTO>> cache = new HashMap<>();
        walkScores.put(userId, 1.0); // Initial score at the starting node is 1
        SecureRandom random = new SecureRandom();
        UUID currentNode = userId;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            if (random.nextDouble() < RESET_PROBABILITY) {
                // With a probability of `RESET_PROBABILITY`, reset to the starting node
                currentNode = userId;
            } else {
                // Get the friends of the current node
                List<UserDTO> friends = null;
                if (!cache.containsKey(currentNode)) {
                    friends = userService.findFriendsByUserId(currentNode);
                    cache.put(currentNode, friends);
                } else {
                    friends = cache.get(currentNode);
                }

                if (friends.isEmpty()) {
                    // If there are no friends, reset to the starting node
                    currentNode = userId;
                } else {
                    // Randomly select a friend
                    currentNode = friends.get(random.nextInt(friends.size())).getId();
                }
            }

            // Update the score of the current node
            walkScores.merge(currentNode, 1.0, Double::sum);
        }

        // Remove the original user
        walkScores.remove(userId);

        // Normalize the scores
        double totalScore = walkScores.values().stream().mapToDouble(Double::doubleValue).sum();
        walkScores.replaceAll((key, value) -> value / totalScore);
        return walkScores;
    }

}
