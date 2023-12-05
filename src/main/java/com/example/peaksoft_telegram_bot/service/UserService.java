package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.entity.User;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public String registerUser(String userEmail, String userName) {

        Optional<User> user1 = Optional.ofNullable(userRepository.findByEmail(userEmail)).get();
        Optional<User> user2 = Optional.ofNullable(userRepository.findByUserName(userName)).get();

        if (!emailValidation(userEmail).equals("good")) {
            return "Email is not correct \n" +
                    "Неверная электронная почта";
        } else if (user1.isPresent()) {
            return "User with this mail already exists in the database\n" + "" +
                    "Пользователь с этой почтой уже существует в базе данных";
        } else if (user2.isPresent()) {
            return "Пользователь с этим именем >>" + userName + "<< уже существует в базе данных";

        } else if (userEmail.contains("@")) {
            User user = new User();
            user.setUserName(userName);
            user.setEmail(userEmail);
            userRepository.save(user);
            return "Успешно зарегистрированы" + "\n" +
                    "Вы готовы пройти тест чтобы проверить свои знания?\n" +
                    "Если готовы, нажмите >> /test <<";
        }
        return "!";
    }

    public String emailValidation(String email) {
        if (email.length() > 30) {
            return " fgskjfgjsf";
        } else if (email.isEmpty()) {
            return "Email must not be empty!";
          //  "  ";
        } else if (!email.contains("@")) {
            return "Incorrect email address";
            //  " Неверная электронная почта ";
        }
        return "good";
    }
}
