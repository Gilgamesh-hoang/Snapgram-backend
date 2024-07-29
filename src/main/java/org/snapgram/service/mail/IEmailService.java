package org.snapgram.service.mail;

import org.snapgram.dto.response.UserDTO;

public interface IEmailService {
	void sendVerificationEmail(UserDTO user);

    void sendForgotPasswordEmail(String email, String newPassword);
}
