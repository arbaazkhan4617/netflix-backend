package com.netflix.backend.common.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.netflix.backend.exception.EmailSendingException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;

	@Override
	public void sendOtp(String to, String otp) {

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);
			helper.setSubject("Verify your account");

			String html = """
					    <h2>Your OTP</h2>
					    <h1 style='color: red;'>%s</h1>
					    <p>This OTP expires in 5 minutes.</p>
					""".formatted(otp);

			helper.setText(html, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new EmailSendingException("Failed to send email", e);
		}
	}
}