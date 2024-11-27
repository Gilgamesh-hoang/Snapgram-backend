package org.snapgram.kafka.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.ForgotPasswordRequest;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.service.mail.IEmailService;
import org.snapgram.util.KafkaTopicConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MailConsumer {
    IEmailService emailService;

    @KafkaListener(topics = KafkaTopicConstant.EMAIL_VERIFICATION_TOPIC)
    public void handleVerificationEmail(UserInfoDTO user) {
        emailService.sendVerificationEmail(user);
        log.info("Processing verification email for user: {}", user);
    }

    @KafkaListener(topics = KafkaTopicConstant.FORGOT_PASSWORD_TOPIC)
    public void handleForgotPasswordEmail(ForgotPasswordRequest message) {
        String email = message.getEmail();
        String newPassword = message.getNewPassword();
        emailService.sendForgotPasswordEmail(email, newPassword);
        log.info("Processing forgot password email for: {}", email);
    }
}
