package com.example.peaksoft_telegram_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;

    public void sendSimpleMessage(int stringPinCode, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        String pinCode = String.valueOf(stringPinCode);
        message.setTo(email);
        message.setFrom("tairovasan11@gmail.com");
        message.setSubject("Peaksoft Moscow java ");
        message.setText(pinCode);
        emailSender.send(message);
    }
}
