package bot.telegram.services;

import bot.telegram.entity.Food;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static bot.telegram.util.Utils.BACK;


public class ReplyMarkupService {
    public ReplyKeyboardMarkup keyboardMaker(String[][] buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (String[] button : buttons) {
            KeyboardRow row = new KeyboardRow();
            for (String s : button) {
                KeyboardButton keyboardButton = new KeyboardButton(s);
                row.add(keyboardButton);
            }
            keyboardRows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup keyboardMaker(Set<String> buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        List<String> list = buttons.stream().toList();
        for (int i = 0; i < list.size(); i++) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton keyboardButton = new KeyboardButton(list.get(i++));
            row.add(keyboardButton);
            if (i<list.size()){
                KeyboardButton keyboardButton1 = new KeyboardButton(list.get(i));
                row.add(keyboardButton1);
            }
            keyboardRows.add(row);
        }
        KeyboardRow row = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(BACK);
        row.add(keyboardButton);
        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup keyboardMaker(List<Food> buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton keyboardButton = new KeyboardButton(buttons.get(i++).getName());
            row.add(keyboardButton);
            if (i<buttons.size()){
                KeyboardButton keyboardButton1 = new KeyboardButton(buttons.get(i).getName());
                row.add(keyboardButton1);
            }
            keyboardRows.add(row);
        }
        KeyboardRow row = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(BACK);
        row.add(keyboardButton);
        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
