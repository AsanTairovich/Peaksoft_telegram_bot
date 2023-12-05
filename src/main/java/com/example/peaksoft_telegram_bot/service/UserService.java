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
            return "❌ Неверный формат электронной почты.\nПожалуйста, введите корректный адрес электронной почты.";
        } else if (user1.isPresent()) {
            return "❌ Пользователь с таким адресом электронной почты уже зарегистрирован в базе данных.";
        } else if (user2.isPresent()) {
            return "Пользователь с именем >>" + userName + "<< уже зарегистрирован в базе данных";

        } else if (userEmail.contains("@")) {
            User user = new User();
            user.setUserName(userName);
            user.setEmail(userEmail);
            userRepository.save(user);
            return "✅ Регистрация прошла успешно!\n" +
                    "Вы готовы пройти тест и проверить свои знания?\n" +
                    "Если готовы, нажмите /test.";
        }
        return "❌ Произошла неизвестная ошибка.";
    }

    public String emailValidation(String email) {
        if (email.length() > 30) {
            return "❌ Длина электронной почты слишком длинная. Пожалуйста, введите корректный адрес.";
        } else if (email.isEmpty()) {
            return "❌ Электронная почта не должна быть пустой. Пожалуйста, введите адрес электронной почты.";
          //  "  ";
        } else if (!email.contains("@")) {
            return "❌ Некорректный адрес электронной почты. Пожалуйста, введите правильный адрес.";
            //  " Неверная электронная почта ";
        }
        return "good";
        //✅ Валидация электронной почты прошла успешно.
    }
}
