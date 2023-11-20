package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.config.TelegramBotConfig;
import com.example.peaksoft_telegram_bot.entity.Question;
import com.example.peaksoft_telegram_bot.entity.User;
import com.example.peaksoft_telegram_bot.repository.QuestionRepository;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig telegramBotConfig;
    @Autowired
    private final EmailService emailService;
    @Autowired
    private UserService userService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    static final String HELP_TEXT = "This bot is create to demonstrate Spring capabilities. \n\n" +
            "You can execute commands from the main menu on the left or by typing a command: \n\n" +
            "Type /start to see a welcome message \n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";
    static final String RIGHT = "ВЕРНО " + "✅";
    static final String WRONG = "НЕПРАВИЛЬНЫЙ " + "❌";


    public TelegramBotService(TelegramBotConfig telegramBotConfig, EmailService emailService, QuestionRepository questionRepository) {
        this.telegramBotConfig = telegramBotConfig;
        this.emailService = emailService;
        this.questionRepository = questionRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/register", " you this register"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/test", "test "));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                case "/register" -> userRegister(chatId);
            }

            if (messageText.contains("@")) {
                sed(chatId, update);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText2 = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            if (messageText2.equals("/test") || messageText2.equals("A") || messageText2.equals("B")
                    || messageText2.equals("C") || messageText2.equals("D")) {
                test(chatId, replyKeyboardMarkup, update.getMessage().getChat().getFirstName(), messageText2);
            }
        }
    }

    public void buttonRep(Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setText("Peaksoft Moscow");

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add("A");
        keyboardRow.add("B");
        keyboardRow.add("C");
        keyboardRow.add("D");
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public  void test(Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup, String userName, String text) {
        User user = userRepository.findByUserName(userName).get();

        if (user.getRandom() >= 1) {
            test2(chatId, user, text);
        }

        if (user.getCount() >= 1 && user.getCount() <= 30) {
            Question question = questionRepository.findById((long) user.getCount()).get();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode(ParseMode.MARKDOWN);
            message.setText("Вопрос: " + user.getCount() + ") " + test4(question, user)+"\n");

            user.setCount(user.getCount() + 1);
            userRepository.save(user);
            buttonRep(chatId, replyKeyboardMarkup);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        } else if (user.getCount() == 31) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode(ParseMode.MARKDOWN);
            message.setText(" \uD83E\uDD73 " + "Сиздин упайыныз -> " + user.getBall() +"\uD83C\uDDF0\uD83C\uDDEC");
            user.setCount(1);
            user.setRandom(0);
            userRepository.save(user);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        }
    }

    public String test4(Question question, User user) {
        Random random = new Random();
        int random_one = random.nextInt(1, 5);
        user.setRandom(random_one);
        userRepository.save(user);
        switch (random_one) {
            case 1:
                return question.getQuestionTest() + "\n\n" +
                        "A:  " + question.getCorrectAnswer() + "\n\n" +
                        "B:  " + question.getIncorrectAnswerOne() + "\n\n" +
                        "C:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                        "D:  " + question.getIncorrectAnswerThree();
            case 2:
                return question.getQuestionTest() + "\n\n" +
                        "A:  " + question.getIncorrectAnswerOne() + "\n\n" +
                        "B:  " + question.getCorrectAnswer() + "\n\n" +
                        "C:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                        "D:  " + question.getIncorrectAnswerThree();

            case 3:
                return question.getQuestionTest() + "\n\n" +
                        "A:  " + question.getIncorrectAnswerOne() + "\n\n" +
                        "B:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                        "C:  " + question.getCorrectAnswer() + "\n\n" +
                        "D:  " + question.getIncorrectAnswerThree();

            case 4:
                return question.getQuestionTest() + "\n\n" +
                        "A:  " + question.getIncorrectAnswerOne() + "\n\n\n" +
                        "B:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                        "C:  " + question.getIncorrectAnswerThree() + "\n\n" +
                        "D:  " + question.getCorrectAnswer();

        }
        return " ";
    }


    public void test2(Long chatId, User user, String text) {
        Question question = questionRepository.findById((long) user.getCount() - 1).get();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        if (user.getRandom() == 1 && text.equals("A")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setBall(user.getBall() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 2 && text.equals("B")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setBall(user.getBall() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 3 && text.equals("C")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setBall(user.getBall() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 4 && text.equals("D")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setBall(user.getBall() + 10);
            userRepository.save(user);
        } else {
            sendMessage.setText(WRONG + "\nТуура жооп ->  " + question.getCorrectAnswer());
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfig.getToken();
    }

    public void pinCode(Long chatId, String userName) {
        Random random = new Random();
        int code = random.nextInt(1000, 9999);
        User user = userRepository.findByUserName(userName).get();
        user.setPinCode(String.valueOf(code));
        user.setCount(1);
        userRepository.save(user);
        emailService.sendSimpleMessage(code, user.getEmail());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вам отправили 4 цифровые код " +
                "отпрапте эти код нам");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }


    public void userRegister(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setText("Электронной почтанызды жазыныз.");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void sed(Long chatId, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setText(userService.registerUser(update.getMessage().getText(),
                update.getMessage().getChat().getFirstName()));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Hi,\uD83C\uDDF0\uD83C\uDDEC " + name + ", Таанышканыма кубанычтамын!" +
                "Бул бот Java программалоо тили боюнча оз билимин текшеруу учун тузулгон.\n" +
                " Нажмите >> /register << ";
        log.info("Replied t user " + name);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void sendPhoto(Long id) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(new File("docs/img_telegram-bot.jpeg")));
        sendPhoto.setChatId(id);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

    }
}
