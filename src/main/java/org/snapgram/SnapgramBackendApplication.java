package org.snapgram;

import org.snapgram.kafka.producer.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }

    @Autowired
    NotificationProducer notificationProducer;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            UUID id = UUID.randomUUID();
//            NotificationDTO notification = NotificationDTO.builder()
//                    .id(id)
//                    .actor(CreatorDTO.builder()
//                            .nickname("123")
//                            .avatarUrl(null)
//                            .build())
//                    .type(NotificationType.COMMENT_POST)
//                    .entityId(UUID.fromString("1ce28758-516a-3ec6-8663-d7ceeca86c6e"))
//                    .createdAt(new Timestamp(2024, 1, 1, 0, 0, 0, 0))
//                    .recipientId(UUID.fromString("f4aea8f0-967c-4700-aea8-f0967c8700b8"))
//                    .content("This is a test notification1")
//                    .options(Map.of("1","1"))
//                    .build();
//            notificationProducer.sendNotificationMessage(notification);
//
//            notification.setContent("This is a test notification2");
//            notificationProducer.sendNotificationMessage(notification);
//
//            notification = NotificationDTO.builder()
//                    .id(UUID.randomUUID())
//                    .actor(CreatorDTO.builder()
//                            .nickname("123")
//                            .avatarUrl(null)
//                            .build())
//                    .type(NotificationType.COMMENT_POST)
//                    .entityId(UUID.fromString("1ce28758-516a-3ec6-8663-d7ceeca86c6e"))
//                    .createdAt(new Timestamp(2024, 1, 1, 2, 0, 0, 0))
//                    .recipientId(UUID.fromString("f4aea8f0-967c-4700-aea8-f0967c8700b8"))
//                    .content("This is a test notification3")
//                    .options(Map.of("1","1"))
//                    .build();
//            notificationProducer.sendNotificationMessage(notification);
        };
    }

//    void generateTimeline() {
//        @Autowired
//        UserRepository userRepository;
//        @Autowired
//        IFollowService followService;
//        @Autowired
//        PostRepository postRepository;
//        @Autowired
//        NewsfeedRepository timelineRepository;
//        List<Timeline> timelines = new ArrayList<>();
//        List<User> all = userRepository.findAll();
//        System.out.println("User count: " + all.size());
//
//        for (User user : all) {
//            followService.getFolloweesByUser(user.getId(), Pageable.unpaged()).forEach(followee -> {
//                Example<Post> example = Example.of(
//                        Post.builder().user(User.builder().id(followee.getId()).build()).isDeleted(false).build()
//                );
//                postRepository.findAll(example).forEach(post -> {
//                    timelines.add(Timeline.builder().userId(user.getId()).postId(post.getId()).postCreatedAt(post.getCreatedAt()).build());
//                });
//            });
//        }
//
//        List<Timeline> timelines1 = timelineRepository.saveAllAndFlush(timelines);
//        System.out.println("Timeline1 count: " + timelines1.size());
//    }
}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4