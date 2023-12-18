package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.config.TelegramBotConfig;
import com.example.peaksoft_telegram_bot.model.entity.Question;
import com.example.peaksoft_telegram_bot.model.entity.Result;
import com.example.peaksoft_telegram_bot.model.entity.Test;
import com.example.peaksoft_telegram_bot.model.entity.User;
import com.example.peaksoft_telegram_bot.model.enums.Emojis;
import com.example.peaksoft_telegram_bot.repository.ResultRepository;
import com.example.peaksoft_telegram_bot.repository.TestRepository;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final UserService userService;
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final ResultRepository resultRepository;
    private final EmailService emailService;
    static final String HELP_TEXT = "This bot is create to demonstrate Spring capabilities. \n\n" +
            "You can execute commands from the main menu on the left or by typing a command: \n\n" +
            "Type /start to see a welcome message \n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";
    static final String option = "A B C D";
    static final String RIGHT = "ПРАВИЛЬНО " + "✅";
    static final String WRONG = "НЕПРАВИЛЬНЫЙ " + "❌";

    private static boolean isRegistered = false;

    public TelegramBotService(TelegramBotConfig telegramBotConfig, EmailService emailService, UserService userService,
                              TestRepository testRepository, UserRepository userRepository,
                              ResultRepository resultRepository) {
        this.telegramBotConfig = telegramBotConfig;
        this.emailService = emailService;
        this.userService = userService;
        this.testRepository = testRepository;
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message!"));
        listOfCommands.add(new BotCommand("/register", " you this register!"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot!"));
        listOfCommands.add(new BotCommand("/test", "test "));
        listOfCommands.add(new BotCommand("/delete", "delete user! "));
        listOfCommands.add(new BotCommand("/result", "get the result!"));
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
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

            if (messageText.equals("/start")) {
                try {
                    photo(chatId);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals("/register")) {
                saveUser(chatId, update);
            } else if (messageText.contains("@")) {
                isRegistered = true;
                emailService.sendSimpleMessage(new Random().nextInt(1000, 9999), messageText, update.getMessage().getChat().getUserName());
                sendTextToUser(chatId, Emojis.EARTH_ASIA + " На ваш email выслан пинкод!\n  напишите пинкод");
            } else if (isRegistered && (messageText.length() == 4 && StringUtils.isNumeric(messageText))) {
                registrationConfirm(Integer.parseInt(messageText), update.getMessage().getChat().getUserName(), chatId);
                isRegistered = false;
            } else if (messageText.equals("Java Core 1") || messageText.equals("Java Core 2") || messageText.equals("SQL Question") ||
                    messageText.equals("Spring Question") || messageText.equals("Hibernate Question") || option.contains(messageText)) {
                userQuestionName(update.getMessage().getChat().getUserName(), messageText);
                test(update.getMessage().getChat().getUserName(), chatId, messageText, replyKeyboardMarkup);
            } else if (messageText.equals("/test")) {
                buttonTest(chatId, replyKeyboardMarkup);
            } else if (messageText.equals("/stop")) {
                stopTest(chatId, update.getMessage().getChat().getUserName(), replyKeyboardMarkup);
            } else if (messageText.equals("/result")) {
                result(chatId, update.getMessage().getChat().getUserName());
                sendTextToUser(chatId, Emojis.EARTH_ASIA + " на ваш email выслан результат теста");
            } else {
                sendTextToUser(chatId, Emojis.EARTH_ASIA + " ❌ Бот не принимает это слово ❌");
            }
        }
    }

    public void result(Long chatId, String userName) {
        User user = userRepository.findByUserName(userName).get();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String res = Emojis.TROPHY + " результат вашего теста : " +
                user.getResultList().get(0).getQuestionName() + " -> " + user.getResultList().get(0).getResultQuestion() + "  " + Emojis.COMPUTER +
                user.getResultList().get(1).getQuestionName() + " -> " + user.getResultList().get(1).getResultQuestion() + "  " + Emojis.COMPUTER +
                user.getResultList().get(2).getQuestionName() + " -> " + user.getResultList().get(2).getResultQuestion() + "  " + Emojis.COMPUTER +
                user.getResultList().get(3).getQuestionName() + " -> " + user.getResultList().get(3).getResultQuestion() + "  " + Emojis.COMPUTER +
                user.getResultList().get(4).getQuestionName() + " -> " + user.getResultList().get(4).getResultQuestion();

        emailService.sendResult(res, user.getEmail());
    }

    public void stopTest(Long chatId, String userName, ReplyKeyboardMarkup replyKeyboardMarkup) {
        User user = userRepository.findByUserName(userName).get();
        Test test = testRepository.findByName(user.getQuestionName()).get();
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);

        Result result = new Result();
        for (int i = 0; i < user.getResultList().size(); i++) {
            if (user.getResultList().get(i).getQuestionName().equals(user.getQuestionName())) {
                result = user.getResultList().get(i);
            }
        }
        sendmessage.setText(resultText(result, test));
        user.setCount(0);
        user.setRandom(0);
        user.setTestResult(0);
        userRepository.save(user);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();

        keyboardRow1.add("/test");
        keyboardRowList.add(keyboardRow1);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        sendmessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(sendmessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public String resultText(Result result, Test test) {
        int sum = test.getQuestionList().size() * 10 - 50;
        if (result.getResultQuestion() >= sum) {
            return "\\uD83E\\uDD73 " + Emojis.TROPHY + " Ваш результат равен : -> " + result.getResultQuestion() + "очков" + "\\uD83C\\uDDF0\\uD83C\\uDDEC";
        } else {
            return " Ваш результат равен : -> " + result.getResultQuestion() + "очков" + "\n Всегда есть возможность сдать заново!";
        }
    }

    public void userQuestionName(String userName, String questionName) {
        User user = userRepository.findByUserName(userName).get();
        if (!questionName.contains(option)) {
            switch (questionName) {
                case "Java Core 1" -> user.setQuestionName("Java Core 1");
                case "Java Core 2" -> user.setQuestionName("Java Core 2");
                case "SQL Question" -> user.setQuestionName("SQL Question");
                case "Spring Question" -> user.setQuestionName("Spring Question");
                case "Hibernate Question" -> user.setQuestionName("Hibernate Question");
            }
            userRepository.save(user);
        }
    }

    public boolean testQuestion(User user) {
        Result javaCore1 = new Result();
        Result javaCore2 = new Result();
        Result sqlQuestion = new Result();
        Result springQuestion = new Result();
        Result hidernatQuestion = new Result();
        for (int i = 0; i < user.getResultList().size(); i++) {
            switch (user.getResultList().get(i).getQuestionName()) {
                case "Java Core 1" -> javaCore1 = user.getResultList().get(i);
                case "Java Core 2" -> javaCore2 = user.getResultList().get(i);
                case "SQL Question" -> sqlQuestion = user.getResultList().get(i);
                case "Spring Question" -> springQuestion = user.getResultList().get(i);
                case "Hibernate Question" -> hidernatQuestion = user.getResultList().get(i);
            }
        }

        if (javaCore1.getResultQuestion() >= 0 &&
                javaCore1.getQuestionName().equals(user.getQuestionName())) {
            return true;
        } else if (javaCore1.getResultQuestion() >= 350 &&
                javaCore2.getQuestionName().equals(user.getQuestionName())) {
            return true;
        } else if (javaCore2.getResultQuestion() >= 350 &&
                sqlQuestion.getQuestionName().equals(user.getQuestionName())) {
            return true;
        } else if (sqlQuestion.getResultQuestion() >= 250 &&
                springQuestion.getQuestionName().equals(user.getQuestionName())) {
            return true;
        } else return springQuestion.getResultQuestion() >= 175 &&
                hidernatQuestion.getQuestionName().equals(user.getQuestionName());
    }

    public void test(String userName, Long chatId, String messageText, ReplyKeyboardMarkup replyKeyboardMarkup) {
        User user = userRepository.findByUserName(userName).get();
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);
        int number = 1;
        Test test;

        if (testQuestion(user)) {
            test = testRepository.findByName(user.getQuestionName()).get();

            if (user.getRandom() >= 1) {
                testExamination(chatId, user, test, messageText);
            }

            if (user.getCount() >= 0 && user.getCount() <= Objects.requireNonNull(test).getQuestionList().size() - 1) {
                if (test.getName().equals(user.getQuestionName())) {
                    Question question = test.getQuestionList().get(user.getCount());
                    number += user.getCount();
                    sendmessage.setText("Вопрос: " + number + ") " + testOption(question, user) + "\n");
                    user.setCount(user.getCount() + 1);
                    userRepository.save(user);
                    buttonRep(chatId, replyKeyboardMarkup);
                }

            } else {
                if (user.getCount() == test.getQuestionList().size()) {
                    stopTest(chatId, user.getUserName(), replyKeyboardMarkup);
                }
            }
        } else {
            sendmessage.setText(Emojis.EARTH_ASIA + " У вас мало очков для перехода на следующий этап теста \n пересдайте заново!");
        }

        try {
            execute(sendmessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void buttonTest(Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);
        sendmessage.setText(Emojis.EARTH_ASIA + " Выбирайте");
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList1 = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardRow keyboardRow4 = new KeyboardRow();
        KeyboardRow keyboardRow5 = new KeyboardRow();

        keyboardRow1.add("Java Core 1");
        keyboardRow2.add("Java Core 2");
        keyboardRow3.add("SQL Question");
        keyboardRow4.add("Spring Question");
        keyboardRow5.add("Hibernate Question");

        keyboardRowList1.add(keyboardRow1);
        keyboardRowList1.add(keyboardRow2);
        keyboardRowList1.add(keyboardRow3);
        keyboardRowList1.add(keyboardRow4);
        keyboardRowList1.add(keyboardRow5);

        replyKeyboardMarkup.setKeyboard(keyboardRowList1);

        sendmessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(sendmessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
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
        KeyboardRow keyboardRow1 = new KeyboardRow();

        keyboardRow.add("A");
        keyboardRow.add("B");
        keyboardRow.add("C");
        keyboardRow.add("D");
        keyboardRow1.add("/stop");
        keyboardRowList.add(keyboardRow);
        keyboardRowList.add(keyboardRow1);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public String testOption(Question question, User user) {
        Random random = new Random();
        int random_one = random.nextInt(1, 5);
        user.setRandom(random_one);
        userRepository.save(user);
        return switch (random_one) {
            case 1 -> question.getQuestionTest() + "\n\n" +
                    "A:  " + question.getCorrectAnswer() + "\n\n" +
                    "B:  " + question.getIncorrectAnswerOne() + "\n\n" +
                    "C:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                    "D:  " + question.getIncorrectAnswerThree();
            case 2 -> question.getQuestionTest() + "\n\n" +
                    "A:  " + question.getIncorrectAnswerOne() + "\n\n" +
                    "B:  " + question.getCorrectAnswer() + "\n\n" +
                    "C:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                    "D:  " + question.getIncorrectAnswerThree();
            case 3 -> question.getQuestionTest() + "\n\n" +
                    "A:  " + question.getIncorrectAnswerOne() + "\n\n" +
                    "B:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                    "C:  " + question.getCorrectAnswer() + "\n\n" +
                    "D:  " + question.getIncorrectAnswerThree();
            case 4 -> question.getQuestionTest() + "\n\n" +
                    "A:  " + question.getIncorrectAnswerOne() + "\n\n" +
                    "B:  " + question.getIncorrectAnswerTwo() + "\n\n" +
                    "C:  " + question.getIncorrectAnswerThree() + "\n\n" +
                    "D:  " + question.getCorrectAnswer();
            default -> "";
        };
    }


    public void testExamination(Long chatId, User user, Test test, String text) {
        Question question = test.getQuestionList().get(user.getCount() - 1);
        Result result = new Result();
        for (int i = 0; i < user.getResultList().size(); i++) {
            if (user.getResultList().get(i).getQuestionName().equals(user.getQuestionName())) {
                result = user.getResultList().get(i);
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        if (user.getRandom() == 1 && text.equals("A")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            result.setResultQuestion(result.getResultQuestion() + 10);
            resultRepository.save(result);
        } else if (user.getRandom() == 2 && text.equals("B")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            result.setResultQuestion(result.getResultQuestion() + 10);
            resultRepository.save(result);
        } else if (user.getRandom() == 3 && text.equals("C")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            result.setResultQuestion(result.getResultQuestion() + 10);
            resultRepository.save(result);
        } else if (user.getRandom() == 4 && text.equals("D")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            result.setResultQuestion(result.getResultQuestion() + 10);
            resultRepository.save(result);
        } else {
            sendMessage.setText(WRONG + "\n Правильный ответ ->  " + question.getCorrectAnswer());
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

    public void userRegister(Long chatId) {
        sendTextToUser(chatId, Emojis.COMPUTER + " Напишите вашу электронную почту");
    }

    public void saveUser(Long chatId, Update update) {
        sendTextToUser(chatId, userService.registerUser(update.getMessage().getChat().getUserName()));
    }

    public void startCommandReceived(Long chatId, String name) {
        String answer = Emojis.COMPUTER + name + ", добро пожаловать, мы рады видеть вас на странице Peaksoft Java Test, \n проверьте свои знание и нажмите " +
                ">> /register << ";
        log.info("Replied t user " + name);
        sendTextToUser(chatId, answer);
    }

    public void photo(Long chatId ) throws MalformedURLException {
        URL url = new URL("https://yt3.googleusercontent.com/ZUIzEd1QgUNZV9wEkj6neqXVRxE7qf7s2py--veKssv4HVRrG8Zs89rJqnd22D8MKp0WWFtvcQ=s900-c-k-c0x00ffffff-no-rj");
        InputFile photo = new InputFile(String.valueOf(url));
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(photo);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void deleteUser(Long chatId, String userName) {
        User user = userRepository.findByUserName(userName).get();
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);
        userRepository.delete(user);
        sendTextToUser(chatId, "Успешно удолено!");
    }

    public void registrationConfirm(Integer pin, String username, Long chatId) {
        User user = userRepository.findByUserName(username).get();
        String outText;
        if (Objects.equals(user.getPin(), pin) && user.getPinExpiration().isAfter(LocalDateTime.now())) {
            user.setEmailActive(true);
            user.setPin(0);
            outText = "email успешно зарегистрирован!" + "\n" +
                    "Если вы готовы проверить свои знания и пройти тест , нажмите  ниже\n" +
                    ">> /test <<";
        } else {
            outText = Emojis.EARTH_ASIA + " Pin is not correct or pin expired!\n Пройдите регистрацию заново!";
        }
        userRepository.save(user);
        sendTextToUser(chatId, outText);
    }

    private void sendTextToUser(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .parseMode(ParseMode.MARKDOWN)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
