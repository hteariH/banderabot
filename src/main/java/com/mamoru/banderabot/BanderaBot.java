package com.mamoru.banderabot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

/**
 * This example bot is an echo bot that just repeats the messages sent to him
 */
@Component
class BanderaBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(BanderaBot.class);

    final TranslatorService translatorService;
    final OpenAiHelper openAiHelper;

    private final String token;
    private final String username;

    BanderaBot(@Value("${bot.token}") String token,
               @Value("${bot.username}") String username,
               TranslatorService translatorService,
               OpenAiHelper openAiHelper) {
        this.token = token;
        this.username = username;
        this.translatorService = translatorService;
        this.openAiHelper = openAiHelper;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            Message message = update.getMessage();

            if (isAskToTranslate(message)) {
                translateMessage(message);
            } else if (isSentToBot(message)) {
                if (isDonate(message.getText())) {
                    donateCredentials(message);
                }else {
                    answerByAI(message);
                }
            }


        }
    }

    private void donateCredentials(Message message) {
       String text = "Я буду вдячний за будь-які донати на рахунок власника боту Бандери. Ви можете перерахувати гроші на цей рахунок:\n" +
               "\n" +
               "Account holder: Oleksii Berkunskyi\n" +
               "BIC: TRWIBEB1XXX\n" +
               "IBAN: BE60 9675 3248 2270\n" +
               "Wise's address: Avenue Louise 54, Room S52\n" +
               "Brussels\n" +
               "1050\n" +
               "Belgium\n";
        SendMessage build = SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .replyToMessageId(message.getMessageId())
                .text(text)
                .build();
        try {
            execute(build);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDonate(String text) {

        return text.trim().startsWith("/donate") || text.toLowerCase().contains("задонатити тобі") || text.toLowerCase().contains("як тобі допомогти")
                || text.toLowerCase().contains("куди слати гроші") || text.toLowerCase().contains("твої реквізити") || text.toLowerCase().contains("засылать донаты") || (text.toLowerCase().contains("донатить"));
    }

    private void answerByAI(Message message) {
        for (int i = 0; i < 3; i++) {
            try {
                String text = stripName(message.getText());
                logger.info("text:" + text);
//                String translate = translatorService.translate(text);
//                logger.info("translation:" + translate);
                String completion = openAiHelper.getCompletion(text);
//            String completion = "";
                SendMessage build = SendMessage.builder()
                        .chatId(String.valueOf(message.getChatId()))
                        .replyToMessageId(message.getMessageId())
                        .text(completion)
                        .build();
                execute(build);
                return;
            } catch (Exception e) {
                logger.error("Failed to send message \"{}\" to {} due to error: {}", message.getText(), message.getChatId(), e.getMessage());

                logger.info("Retrying number {} :", i);

            }
        }
    }

    private String stripName(String text) {
        if (text.toLowerCase().startsWith("бандера")) {
            String[] s = text.split(" ", 2);
            return s[1];
        }
        return text;
    }

    private void translateMessage(Message message) {
        try {
            String text = message.getText().substring(18).trim();
            String post = translatorService.translate(text);
            SendMessage build = SendMessage.builder()
                    .chatId(String.valueOf(message.getChatId()))
                    .replyToMessageId(message.getMessageId())
                    .text(post)
                    .build();
            execute(build);
            logger.info("Sent message \"{}\" to {}", post, message.getChatId());
        } catch (TelegramApiException e) {
            logger.error("Failed to send message \"{}\" to {} due to error: {}", message.getText(), message.getChatId(), e.getMessage());
        }
    }

    private boolean isAskToTranslate(Message message) {
        if (message.getText() != null) {
            return message.getText().toLowerCase().contains("бандера, переклади");
        }
        return false;
    }

    private boolean isSentToBot(Message message) {
        logger.info("message:" + message);
        if (message.getText().startsWith("/donate")&& message.isUserMessage()){
            return true;
        }
        if (message.getText().startsWith("/donate@BanderaFatherBot")){
            return true;
        }
        if (message.getText().toLowerCase().startsWith("бандера") || message.getText().trim().contains("@BanderaFatherBot")) {
            return true;
        } else if (message.getReplyToMessage() != null && message.getReplyToMessage().getFrom().getIsBot()) {

            return message.getReplyToMessage().getFrom().getUserName().toLowerCase().contains("banderafatherbot");
        }
        return false;
    }

    @PostConstruct
    public void start() {
        logger.info("username: {}, token: {}", username, token);
    }

}


