package org.snapgram;

import org.snapgram.service.face.FaceRecognitionService;
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

//    @Autowired
//    FaceRecognitionService faceRecognitionService;
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            faceRecognitionService.identify("https://res.cloudinary.com/dqjwkvfko/image/upload/v1732349992/snapgram/file_40628927-f440-4024-a289-27f4400024a3.jpg");
        };
    }
}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4