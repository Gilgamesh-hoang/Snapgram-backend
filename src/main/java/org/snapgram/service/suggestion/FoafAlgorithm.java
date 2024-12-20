package org.snapgram.service.suggestion;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FoafAlgorithm {
    IUserService userService;

    // Method implementing the Friends-of-a-Friend (FOAF) algorithm
    public Map<UUID, Double> getInstance(UUID userId) {
        // Get the direct friends of the user
        List<UserDTO> directFriends = userService.getFriendsByUserId(userId);
        // Map to store the FOAF candidates and their scores
        Map<UUID, Double> foafCandidates = new ConcurrentHashMap<>();

        // For each direct friend, asynchronously get their friends and update the scores of the candidates
        List<CompletableFuture<Void>> futures = directFriends.stream()
                .map(friend -> CompletableFuture.runAsync(() -> {
                    List<UserDTO> friendsOfFriend = userService.getFriendsByUserId(friend.getId());
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
}
