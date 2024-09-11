package org.snapgram.service.mail;

import org.snapgram.dto.response.UserDTO;
import org.snapgram.dto.response.UserInfoDTO;

public interface IEmailService {
	void sendVerificationEmail(UserInfoDTO user);

    void sendForgotPasswordEmail(String email, String newPassword);
}
