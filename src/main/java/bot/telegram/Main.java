package bot.telegram;

import bot.telegram.services.BotService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi api =new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(BotService.getInstance());
    }
}