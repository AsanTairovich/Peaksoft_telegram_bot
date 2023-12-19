package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.model.entity.User;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;

    public void sendSimpleMessage(int stringPinCode, String email, String userName) {
        User user = userRepository.findByUserName(userName);
        SimpleMailMessage message = new SimpleMailMessage();
        String pinCode = String.valueOf(stringPinCode);
        message.setFrom("tairovasan11@gmail.com");
        message.setSubject("Peaksoft Moscow java ");
        message.setText(pinCode);
        message.setTo(email);
        user.setPin(stringPinCode);
        user.setEmail(email);
        user.setPinExpiration(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailSender.send(message);
    }
    public void sendResult(String result, String email){
        SimpleMailMessage mailMessage =new SimpleMailMessage();
        User user  = userRepository.findByEmail(email);
        mailMessage.setFrom("tairovasan11@gmail.com");
        mailMessage.setSubject("Peaksoft Moscow java");
        mailMessage.setText(result);
        mailMessage.setTo(user.getEmail());
        emailSender.send(mailMessage);
    }
}
