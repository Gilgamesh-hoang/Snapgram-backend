package org.snapgram;

import org.snapgram.dto.request.CommentRequest;
import org.snapgram.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }

    @Autowired
    CommentService commentService;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            CommentRequest commentRequest = new CommentRequest();
//            commentRequest.setContent("Hello World");
//            commentRequest.setPostId(UUID.randomUUID());
//            commentService.createComment(UUID.randomUUID(),commentRequest);
        };
    }
}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4