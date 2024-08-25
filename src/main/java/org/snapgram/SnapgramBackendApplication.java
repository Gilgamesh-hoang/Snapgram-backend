package org.snapgram;

import org.snapgram.repository.database.CommentRepository;
import org.snapgram.repository.database.PostLikeRepository;
import org.snapgram.repository.database.PostRepository;
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
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PostLikeRepository likeRepository;


    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            ========================
//            ========================
        };
    }

}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:9000 -D sonar.login=sqp_d7c21f84c155366567f0566185617822378096d0