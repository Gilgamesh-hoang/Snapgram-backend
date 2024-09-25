package org.snapgram;

import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.kafka.producer.MailProducer;
import org.snapgram.service.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.Timestamp;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }

    @Autowired
    MailProducer mailProducer;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
//            UserInfoDTO user = new UserInfoDTO();
//            user.setEmail("21130363@st.hcmuaf.edu.vn");
//            user.setFullName("Nguyen Van A");
//            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//            user.setActiveCode(UUID.randomUUID().toString());
//            System.out.println("chuan bi gui mail");
//            long a = System.currentTimeMillis();
//            mailProducer.sendVerificationEmail(user);
//            System.out.println("Message sent to Kafka successfully! " + (System.currentTimeMillis() - a));
//            Thread.sleep(5000);
//            System.out.println("done");
        };
    }
}
// ./gradlew sonar -D sonar.projectKey=Snapgram-backend -D sonar.host.url=http://localhost:8998 -D sonar.login=sqp_846a015e770ec5b9ec749641e0359e14434386e4