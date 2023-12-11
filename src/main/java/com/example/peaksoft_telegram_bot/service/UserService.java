package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.entity.Result;
import com.example.peaksoft_telegram_bot.entity.User;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String registerUser(String userName) {
        User user = new User();
        addQuestion(user);
        user.setUserName(userName);
        userRepository.save(user);

        return "Вам нужно отправить электронное почту, чтобы получить результат теста!";
    }

    public String a(String userEmail, String userName) {
        Optional<User> user1 = Optional.ofNullable(userRepository.findByEmail(userEmail)).get();
        User user = userRepository.findByUserName(userName).get();

        if (!emailValidation(userEmail).equals("good")) {
            return "Электронная почта не правильно";
        } else if (user1.isPresent()) {
            return "Пользователь с этой почтой уже существует в базе данных";
        } else {
            user.setEmail(userEmail);
            userRepository.save(user);
            return "";
        }
    }

    public void addQuestion(User user) {
        List<Result> resultList = new ArrayList<>();
        Result javaCore1 = new Result();
        Result javaCore2 = new Result();
        Result sqlQuestion = new Result();
        Result springQuestion = new Result();
        Result hibernateQuestion = new Result();
        javaCore1.setQuestionName("Java Core 1");
        javaCore2.setQuestionName("Java Core 2");
        sqlQuestion.setQuestionName("SQL Question");
        springQuestion.setQuestionName("Spring Question");
        hibernateQuestion.setQuestionName("Hibernate Question");
        resultList.add(javaCore1);
        resultList.add(javaCore2);
        resultList.add(sqlQuestion);
        resultList.add(springQuestion);
        resultList.add(hibernateQuestion);
        javaCore1.setUser(user);
        javaCore2.setUser(user);
        sqlQuestion.setUser(user);
        springQuestion.setUser(user);
        hibernateQuestion.setUser(user);
        user.setResultList(resultList);
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
