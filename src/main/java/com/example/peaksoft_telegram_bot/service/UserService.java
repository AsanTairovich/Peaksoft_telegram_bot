package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.entity.User;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private  UserRepository userRepository;

    public String registerUser(String userEmail, String userName){

        Optional<User> user1 = Optional.ofNullable(userRepository.findByEmail(userEmail)).get();

        if (!emailValidation(userEmail).equals("good")) {
            return "Email is not correct \n" +
                    "Электронная почта не правильно";
        } else if (user1.isPresent()) {
            return "User with this mail already exists in the database\n" + "" +
                    "Пользователь с этой почтой уже существует в базе данных";
        } else if (userEmail.contains("@")) {
            User user = new User();
            user.setUserName(userName);
            user.setEmail(userEmail);
            user.setCount(1);
            userRepository.save(user);
            return "Успешно зарегистрирован"+"\n" +
                    "Вы готовы пройти тест, что бы проверить свои знаний.\n" +
                    "Если готов нажмите >> /test <<";
        }
        return "!";
    }
    public String emailValidation(String email) {
        if (email.length() > 30) {
            return " fgskjfgjsf";
        } else if (email.isEmpty()) {
            return "Email must not be empty!";
        } else if (!email.contains("@")) {
            return "Incorrect email address";
        }
        return "good";
    }
}
