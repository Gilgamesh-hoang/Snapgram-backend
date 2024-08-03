package org.snapgram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnapgramBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);

    }

//    @Bean
//    public CommandLineRunner commandLineRunner() {
//        return args -> {
//        };
//    }

}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:9000 -D sonar.login=sqp_d7c21f84c155366567f0566185617822378096d0