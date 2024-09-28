package org.snapgram.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.UserInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(UserInfoDTO user) {
        LocalDateTime datePlusDays = user.getCreatedAt().toLocalDateTime().plusDays(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedString = datePlusDays.format(formatter);

        Context context = new Context();
        context.setVariable("fullName", user.getFullName());
        context.setVariable("email", user.getEmail());
        context.setVariable("url", frontendUrl+"/sign-in?action=verify-email&code="+user.getActiveCode()+"&email="+user.getEmail());
        context.setVariable("expired", formattedString);

        String process = templateEngine.process("verify-email-template.html", context);
        sendMail(user.getEmail(), "Snapgram - Xác thực tài khoản", process);
    }

    @Override
    public void sendForgotPasswordEmail(String email, String newPassword) {
        Context context = new Context();
        context.setVariable("newPassword", newPassword);
        context.setVariable("email", email);
        String process = templateEngine.process("forgot-password.html", context);
        sendMail(email, "Snapgram - Quên mật khẩu", process);
    }

    private void sendMail(String to, String subject, String content) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
                helper.setFrom(new InternetAddress(username));
                helper.setTo(new InternetAddress(to));
                helper.setSubject(subject);
                helper.setText(content, true);

                mailSender.send(message);
            } catch (MessagingException ex) {
                log.error(ex.getMessage());
            }
    }
}
