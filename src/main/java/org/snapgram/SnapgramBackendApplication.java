package org.snapgram;

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

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

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
//        TimelineRepository timelineRepository;
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