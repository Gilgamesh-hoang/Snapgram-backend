package org.snapgram;

import org.snapgram.dto.response.UserDTO;
import org.snapgram.repository.database.IFollowRepository;
import org.snapgram.repository.database.IUserRepository;
import org.snapgram.repository.elasticsearch.user.CustomUserElasticRepo;
import org.snapgram.service.suggestion.FriendSuggestionService;
import org.snapgram.service.suggestion.HybridFriendSuggestionService;
import org.snapgram.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
public class SnapgramBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);

    }

    @Autowired
    IUserService userService;
    @Autowired
    IFollowRepository followRepository;
    @Autowired
    HybridFriendSuggestionService hybridFriendSuggestionService;
    @Autowired
    FriendSuggestionService friendSuggestionService;
    @Autowired
    CustomUserElasticRepo userElasticRepo;
    @Autowired
    IUserRepository userRepository;

    public int countCommonFriends(UUID userId1, UUID userId2) {
        List<UserDTO> friendsOfUser1 = userService.findFriendsByUserId(userId1);
        List<UserDTO> friendsOfUser2 = userService.findFriendsByUserId(userId2);

        // Convert the lists to sets for efficient intersection
        Set<UserDTO> set1 = new HashSet<>(friendsOfUser1);
        Set<UserDTO> set2 = new HashSet<>(friendsOfUser2);

        // Find the intersection of the two sets
        set1.retainAll(set2);

        // The size of the intersection set is the number of common friends
        return set1.size();
    }
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            UUID id = UUID.fromString("fe7abe91-b11e-3655-ae6c-93ff85a78579");
//            friendSuggestionService.recommendFriends(id).forEach((user) -> {
//                System.out.println(user.getNickname() + ": " + countCommonFriends(id, user.getId()));
//            });
//            for (int i = 0; i < 3; i++) {
//                userElasticRepo.findRandomUsers(5, List.of(
//                                        UUID.fromString("e15695d4-1389-30bd-94da-37188c8813a3"),
//                                        UUID.fromString("ec8609cb-1302-4abc-8609-cb13025abc3d"),
//                                        UUID.fromString("1e0a3b53-7673-37d4-ad0a-0d9777b20fc1"),
//                                        UUID.fromString("56f62d46-f217-3417-8947-dc68623267e7")
//                                )
//                        )
//                        .forEach(System.out::println);
//                System.out.println("====================================");
//            }
////
//            long start = System.currentTimeMillis();
//            List<UserDTO> a = hybridFriendSuggestionService.recommendFriends(id);
//            System.out.println("Time1: " + (System.currentTimeMillis() - start));
//
//            a.forEach((user) -> {
//                System.out.println(user.getNickname() + ": " + countCommonFriends(id, user.getId()));
//            });
//            System.out.println("size = " + a.size());

//            System.out.println("====================================");
//
//            Map<UUID, Double> b = hybridFriendSuggestionService.recommendFriends2(id);
//            b.forEach((uuid, dob) -> {
//                System.out.println(uuid + ": " + dob);
//            });
//            System.out.println("size = " + b.size());

//            hybridFriendSuggestionService.foafAlgorithm(UUID.fromString("fe7abe91-b11e-3655-ae6c-93ff85a78579")).forEach((uuid, integer) -> {
//                System.out.println(uuid.toString() + " : " + integer);
//            });

//            hybridFriendSuggestionService.foafAlgorithm(UUID.fromString("fe7abe91-b11e-3655-ae6c-93ff85a78579"));
//            hybridFriendSuggestionService.randomWalk(UUID.fromString("fe7abe91-b11e-3655-ae6c-93ff85a78579"))
//                    .forEach((uuid, aDouble) -> {
//                        System.out.println(uuid.toString() + " : " + aDouble);
//                    });

//            userRepository.findActiveFollowers(UUID.fromString("fe7abe91-b11e-3655-ae6c-93ff85a78579")).forEach(System.out::println);
//            System.out.println("===========");
//            userRepository.findAll().forEach(user -> {
//                // random form 10 to 30
//                int rand = new Random().nextInt(15) + 10;
//                List<Follow> follows = new ArrayList<>();
//                for (User us : userRepository.findRandomUsers(rand)) {
//                    if (us.getId().equals(user.getId())) {
//                        continue;
//                    }
//
//                    Follow follow = new Follow();
//                    follow.setFollowingUser(us);
//                    follow.setFollowedUser(user);
//                    follows.add(follow);
//
//
//                }
//                CompletableFuture.runAsync(() -> {
//                    followRepository.saveAllAndFlush(follows);
//                });
//            });
        };
    }

}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:9000 -D sonar.login=sqp_d7c21f84c155366567f0566185617822378096d0