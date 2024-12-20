package org.snapgram.service.suggestion;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.exception.MultiThreadException;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    FoafAlgorithm foafAlgorithm;
    RandomWalkAlgorithm randomWalkAlgorithm;
    static final double WEIGHT = 0.5; // Weight for combining scores from different algorithms

    // Method to recommend friends to a user
    @Override
    public List<UserDTO> recommendFriends(UUID userId) {
        // Step 1: Use FOAF to generate candidates
        CompletableFuture<Map<UUID, Double>> foafFuture = CompletableFuture.supplyAsync(() -> foafAlgorithm.getInstance(userId));

        // Step 2: Use Random Walk to rank candidates
        CompletableFuture<Map<UUID, Double>> randomWalkFuture = CompletableFuture.supplyAsync(() -> randomWalkAlgorithm.getInstance(userId));

        try {
            // Get the results of FOAF and Random Walk
            Map<UUID, Double> foafCandidates = foafFuture.get();
            Map<UUID, Double> randomWalkScores = randomWalkFuture.get();

            // Combine the results from both algorithms
            List<UserDTO> combineResults = combineResults(foafCandidates, randomWalkScores);

            // If there are less than 30 results, add random users
            int missingNum = Math.abs(combineResults.size() - 30);
            if (missingNum > 0) {
                List<UserDTO> randomUsers = userService.getRandomUsers(missingNum, combineResults.stream().map(UserDTO::getId).toList());
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
                .map(userService::getById)
                .toList();
    }

}
