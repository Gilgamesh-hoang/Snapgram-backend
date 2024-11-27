package org.snapgram.kafka.producer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.ForgotPasswordRequest;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MailProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void sendVerificationEmail(UserInfoDTO user) {
        kafkaTemplate.send(KafkaTopicConstant.EMAIL_VERIFICATION_TOPIC, user);
        log.info("Sent verification email event: {}", user);
    }

    public void sendForgotPasswordEmail(String email, String newPassword) {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email(email)
                .newPassword(newPassword)
                .build();
        kafkaTemplate.send(KafkaTopicConstant.FORGOT_PASSWORD_TOPIC, request);
        log.info("Sent forgot password email event for: {}", email);
    }
}
