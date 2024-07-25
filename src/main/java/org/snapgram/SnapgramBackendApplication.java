package org.snapgram;

import org.snapgram.model.response.UserDTO;
import org.snapgram.service.mail.IEmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
public class SnapgramBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapgramBackendApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(IEmailService myService) {
//        return args -> {
//            myService.sendVerificationEmail(UserDTO.builder().email("vophihoang252003@gmail.com")
//                    .fullName("nguyen van a").activeCode(UUID.randomUUID().toString())
//                    .createdAt(new Timestamp(System.currentTimeMillis())).build());

//            LocalDateTime userCreationTime = new Timestamp(2024+1900,6,22,0,0,0,0).toLocalDateTime();
//            LocalDateTime currentTime = new Timestamp(System.currentTimeMillis()).toLocalDateTime();
//            if (userCreationTime.plusDays(3).isBefore(currentTime)) {
//                System.out.println("het han");
//            }else {
//                System.out.println("con han");
//            }
//        };
//    }
}
