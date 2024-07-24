package bot.telegram.services;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class BotService extends TelegramLongPollingBot {
    private static final BotAdminService adminService = BotAdminService.getInstance();
    private static final BotUserService userService = BotUserService.getInstance();
    private static final Long adminId = userService.adminId;
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            System.out.println(update.getCallbackQuery().getMessage().getChatId()+"->"+update.getCallbackQuery().getFrom().getFirstName() +" -> "+ update.getCallbackQuery().getData());
            if(update.getCallbackQuery().getMessage().getChatId().equals(adminId))
                adminService.callbackHandler(update);
            else
                userService.callbackHandler(update);
        } else if (update.hasMessage()) {
            System.out.println(update.getMessage().getChatId()+"->"+update.getMessage().getChat().getFirstName()+"-> "+ update.getMessage().getText());
            if(update.getMessage().getChatId().equals(adminId))
                adminService.messageHandler(update);
            else
                userService.messageHandler(update);
        }
    }


    @Override
    public String getBotUsername() {
        return "pdp_restaurant_bot";
    }

    @Override
    public String getBotToken() {

        return "7267041813:AAGb5_7OmST5u6_KUKc_MZ8lfQMklshtQS4";
    }


    public void executeMessages(SendMessage... messages) {
        for (SendMessage message : messages) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private BotService() {
        System.out.println("Bot is successfully working...");
    }

    private static BotService botService;

    public static BotService getInstance() {
        if (botService == null) {
            botService = new BotService();
        }
        return botService;
    }

    @SneakyThrows
    public void executeMessages(ForwardMessage forwardMessage) {
        execute(forwardMessage);
    }

    @SneakyThrows
    public void executeMessages(SendPhoto sendPhoto) {
        execute(sendPhoto);
    }

    @SneakyThrows
    public void executeMessages(DeleteMessage deleteMessage) {
        execute(deleteMessage);
    }

    @SneakyThrows
    public Message executeMessages(SendMessage deleteMessage) {
        return execute(deleteMessage);
    }

    @SneakyThrows
    public void executeMessages(EditMessageText editMessageText) {
        execute(editMessageText);
    }
}
