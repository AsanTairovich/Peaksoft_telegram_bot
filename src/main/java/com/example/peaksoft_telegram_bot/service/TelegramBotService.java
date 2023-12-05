package com.example.peaksoft_telegram_bot.service;

import com.example.peaksoft_telegram_bot.config.TelegramBotConfig;
import com.example.peaksoft_telegram_bot.entity.Question;
import com.example.peaksoft_telegram_bot.entity.Test;
import com.example.peaksoft_telegram_bot.entity.User;
import com.example.peaksoft_telegram_bot.repository.QuestionRepository;
import com.example.peaksoft_telegram_bot.repository.TestRepository;
import com.example.peaksoft_telegram_bot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig telegramBotConfig;
    @Autowired
    private UserService userService;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private UserRepository userRepository;
    static final String HELP_TEXT = "Этот бот создан для того, чтобы демонстрировать возможности Spring \n\n" +
            "Вы можете выполнять команды из главного меню слева или нажимая command  \n\n" +
            " Нажмите /start чтобы посмотреть входящее сообщение \n\n" +
            " Нажмите /mydata чтобы посмотреть сохраненные данные о вас.\n\n" +
            "Нажмите  /help  чтобы увидеть эту информацию снова\n\n"+
            "Приятного использования! 😊";
    static final String option = "A B C D";
    static final String RIGHT = "ВЕРНО ✅";
    static final String WRONG = "НЕВЕРНО ❌";

    public TelegramBotService(TelegramBotConfig telegramBotConfig, EmailService emailService, QuestionRepository questionRepository) {
        this.telegramBotConfig = telegramBotConfig;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Добро пожаловать"));
        listOfCommands.add(new BotCommand("/register", "Регистрация"));
        listOfCommands.add(new BotCommand("/help", "Помощь"));
        listOfCommands.add(new BotCommand("/test", "Тест"));
        listOfCommands.add(new BotCommand("/delete", "Удалить"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            log.info("Список команд успешно настроен.");
        } catch (TelegramApiException e) {
            log.error("Ошибка найстройки списка команд бота:" + e.getMessage());
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
                case "/delete" -> deleteUser(chatId, update.getMessage().getChat().getFirstName());
            }

            if (messageText.contains("@")) {
                saveUser(chatId, update);
            }

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

            if (messageText.equals("Java Core 1") || messageText.equals("Java Core 2") || messageText.equals("SQL Question") ||
                    messageText.equals("Spring Question") || messageText.equals("Hibernate Question") || option.contains(messageText)) {
                userQuestionName(update.getMessage().getChat().getFirstName(), messageText);
                test(update.getMessage().getChat().getFirstName(), chatId, messageText, replyKeyboardMarkup);
            } else if (messageText.equals("/test")) {
                buttonTest(chatId, replyKeyboardMarkup);
            } else if (messageText.equals("/stop")) {
                stopTest(chatId, update.getMessage().getChat().getFirstName(), replyKeyboardMarkup);
            }
        }
    }

    public void stopTest(Long chatId, String userName, ReplyKeyboardMarkup replyKeyboardMarkup) {
        User user = userRepository.findByUserName(userName).get();
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);

        sendmessage.setText(" \uD83E\uDD73 " + "Ваши баллы-> " + user.getTestResult() + "\uD83C\uDDF0\uD83C\uDDEC");
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
            log.error("Произошла ошибка: " + e.getMessage());
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

    public void test(String userName, Long chatId, String messageText, ReplyKeyboardMarkup replyKeyboardMarkup) {
        User user = userRepository.findByUserName(userName).get();
        int number = 1;
        Test test = testRepository.findByName(user.getQuestionName()).get();
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);

        if (user.getRandom() >= 1) {
            testExamination(chatId, user, test, messageText);
        }

        if (user.getCount() >= 0 && user.getCount() <= test.getQuestionList().size() - 1) {
            Question question = test.getQuestionList().get(user.getCount());
            number += user.getCount();
            sendmessage.setText("Вопрос: " + number + ") " + testOption(question, user) + "\n");
            user.setCount(user.getCount() + 1);
            userRepository.save(user);
            buttonRep(chatId, replyKeyboardMarkup);

        } else if (user.getCount() == test.getQuestionList().size()) {
            sendmessage.setText(" \uD83C\uDFC6 " + "Ваш результат-> " + user.getTestResult() + "\uD83C\uDDF0\uD83C\uDDEC");
            user.setCount(0);
            user.setRandom(0);
            user.setTestResult(0);
            userRepository.save(user);

            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
            replyKeyboardRemove.setRemoveKeyboard(true);
            sendmessage.setReplyMarkup(replyKeyboardRemove);
        }
        try {
            execute(sendmessage);
        } catch (TelegramApiException e) {
            log.error("Произошла ошибка: " + e.getMessage());
        }
    }

    public void buttonTest(Long chatId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);
        sendmessage.setText("Выберите");
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
            log.error("Произошла ошибка: " + e.getMessage());
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
            log.error("Произошла ошибка: " + e.getMessage());
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
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        if (user.getRandom() == 1 && text.equals("A")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setTestResult(user.getTestResult() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 2 && text.equals("B")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setTestResult(user.getTestResult() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 3 && text.equals("C")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setTestResult(user.getTestResult() + 10);
            userRepository.save(user);
        } else if (user.getRandom() == 4 && text.equals("D")) {
            sendMessage.setText(RIGHT + "\n" + question.getCorrectAnswer());
            user.setTestResult(user.getTestResult() + 10);
            userRepository.save(user);
        } else {
            sendMessage.setText(WRONG + "\nПравильный ответ ->  " + question.getCorrectAnswer());
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
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setText("Пожалуйста, введите электронную почту");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void saveUser(Long chatId, Update update) {
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

    public void startCommandReceived(Long chatId, String name) {
        String greeting = "Привет, " + name + "!\n"+
                "\uD83D\uDC4B Бот начал работу!\n" +
                "Этот бот создан для тестирования ваших знаний по Java-программированию.\n" +
                "Нажмите >> /register << для продолжения.";
        log.info("Replied t user " + name);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(greeting);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
    public void deleteUser(Long chatId, String userName){
        User user = userRepository.findByUserName(userName).get();

        SendMessage sendmessage = new SendMessage();
        sendmessage.setChatId(chatId);
        sendmessage.setParseMode(ParseMode.MARKDOWN);
        userRepository.delete(user);

        sendmessage.setText("Пользователь `" + userName + "` успешно удален! ✅");
        try {
            execute(sendmessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());

        }
    }
}
