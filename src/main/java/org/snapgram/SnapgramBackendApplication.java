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
}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4