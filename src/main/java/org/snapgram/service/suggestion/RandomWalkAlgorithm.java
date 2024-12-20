package org.snapgram.service.suggestion;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RandomWalkAlgorithm {
    IUserService userService;
    static final double RESET_PROBABILITY = 0.4; // Probability to return to the starting point (random jump)
    static final int NUM_ITERATIONS = 1000; // Number of steps (iterations) of the Random Walk

    public Map<UUID, Double> getInstance(UUID userId) {
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
                    friends = userService.getFriendsByUserId(currentNode);
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
