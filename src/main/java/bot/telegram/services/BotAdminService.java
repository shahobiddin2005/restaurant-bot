package bot.telegram.services;

import bot.telegram.database.Db;
import bot.telegram.entity.Food;
import bot.telegram.entity.Order;
import bot.telegram.entity.User;
import bot.telegram.enums.State;
import bot.telegram.enums.Status;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.*;

import static bot.telegram.enums.State.*;
import static bot.telegram.enums.Status.*;
import static bot.telegram.util.Utils.*;

public class BotAdminService {

    private SendMessage sendMessage = new SendMessage();
    Db db = Db.getInstance();

    BotService botService = BotService.getInstance();
    private final ReplyMarkupService replyService = new ReplyMarkupService();
    private final InlineMarkupService inlineService = new InlineMarkupService();
    private State state = MAIN;
    private String menu = null;
    private Food food = new Food();

    public void callbackHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String text = update.getCallbackQuery().getData();
        if (text.equals("yes")) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(chatId);
            editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            try {
                if (db.getMenus().get(menu).remove(food))
                    editMessageText.setText("Food deleted successfully ✅");
                else
                    editMessageText.setText("Food already deleted ❗\uFE0F");
                state = MAIN;
                botService.executeMessages(editMessageText);
                sendMessage.setChatId(chatId);
                sendMessage.setText("Main menu");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                botService.executeMessages(sendMessage);
            } catch (Exception e) {
                editMessageText.setText("Noto'g'ri buyruq ❗\uFE0F");
                botService.executeMessages(editMessageText);
                state = MAIN;
                sendMessage.setChatId(chatId);
                sendMessage.setText("Main menu");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                botService.executeMessages(sendMessage);
            }
        }

        if (text.equals("no")) {
            sendMessage.setChatId(chatId);
            sendMessage.setText("Main menu");
            state = MAIN;
            sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
            botService.executeMessages(sendMessage);
        }

        if (text.length() > 15) {
            if (text.split(" ")[1].equals("confirm")) {
                Optional<Order> optionalOrder = db.getOrderById(text.split(" ")[2]);
                if (optionalOrder.isPresent()) {
                    Order order = optionalOrder.get();
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setChatId(chatId);
                    editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    if (Objects.equals(order.getStatus(), WAITING)) {
                        order.setStatus(CONFIRMED);
                        editMessageText.setText("Order confirmed ✅");
                        sendMessage.setChatId(order.getUser().getId());
                        sendMessage.setText("Admin has confirmed your order ✅\nYou can see the history");
                        botService.executeMessages(sendMessage);
                    } else if (Objects.equals(order.getStatus(), CONFIRMED)) {
                        editMessageText.setText("Order already CONFIRMED ❗\uFE0F \nYou can not CONFIRM this order ❌");
                    } else if (Objects.equals(order.getStatus(), ADMIN_CANCELED)) {
                        editMessageText.setText("Order already CANCELED ❗\uFE0F \nYou can not CONFIRM this order ❌");
                    }
                    botService.executeMessages(editMessageText);
                }
            } else if (text.split(" ")[1].equals("cancel")) {
                Optional<Order> optionalOrder = db.getOrderById(text.split(" ")[2]);
                if (optionalOrder.isPresent()) {
                    Order order = optionalOrder.get();
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setChatId(chatId);
                    editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    if (Objects.equals(order.getStatus(), WAITING)) {
                        order.setStatus(ADMIN_CANCELED);
                        editMessageText.setText("Order canceled ❌");
                        sendMessage.setChatId(order.getUser().getId());
                        sendMessage.setText("Admin has canceled your order ❌\nYou can see the history");
                        botService.executeMessages(sendMessage);
                    } else if (Objects.equals(order.getStatus(), CONFIRMED)) {
                        editMessageText.setText("Order already CONFIRMED ❗\uFE0F \nYou can not CANCEL this order ❌");
                    } else if (Objects.equals(order.getStatus(), ADMIN_CANCELED)) {
                        editMessageText.setText("Order already CANCELED ❗\uFE0F \nYou can not CANCEL this order ❌");
                    }
                    botService.executeMessages(editMessageText);
                }
            }
        }
    }

    public void messageHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if (Objects.equals(state, FOOD_EDIT_MENU)) {
            if (db.getMenus().containsKey(text)) {
                menu = text;
                state = EDIT_FOOD_NAME;
                sendMessage.setChatId(chatId);
                sendMessage.setText("Chose food:");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(text)));
                botService.executeMessages(sendMessage);
                return;
            }
        }

        if (Objects.equals(state, EDIT_FOOD_NAME) && !text.equals(BACK)) {
            Optional<Food> optionalFood = db.getFood(text);
            sendMessage.setChatId(chatId);
            if (optionalFood.isPresent()) {
                state = EDITION_TYPE;
                food = optionalFood.get();
                sendMessage.setText("Food: " + text + "\n\nChose edition type: ");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(editionType));
            } else {
                sendMessage.setText("Food is not found ❗\uFE0F");
                sendMessage.setReplyMarkup(null);
            }
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, EDITION_TYPE)&& !text.equals(BACK)) {
            sendMessage.setChatId(chatId);
            if (text.equals(EDIT_NAME)) {
                sendMessage.setText("Enter new food name:");
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true, true));
                sendMessage.setChatId(chatId);
                state = EDITION_FOOD_NAME;
            } else if (text.equals(EDIT_PRICE)) {
                sendMessage.setText("Enter new food price:");
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true, true));
                sendMessage.setChatId(chatId);
                state = EDITION_FOOD_PRICE;
            }
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, EDITION_FOOD_NAME)) {
            sendMessage.setChatId(chatId);
            state = EDITION_TYPE;
            sendMessage.setReplyMarkup(replyService.keyboardMaker(editionType));
            if (db.getFood(text).isPresent()) {
                sendMessage.setText("Bunday maxsulot mavjud ❗\uFE0F");
                botService.executeMessages(sendMessage);
                return;
            }
            food.setName(text);
            sendMessage.setText("Food name edited successfully ✅\n\nChose edition type: ");
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, EDITION_FOOD_PRICE)) {
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(replyService.keyboardMaker(editionType));
            state = EDITION_TYPE;
            try {
                food.setPrice(Math.abs(Double.parseDouble(text)));
                sendMessage.setText("Food price edited successfully ✅\n\nChose edition type: ");
                botService.executeMessages(sendMessage);
            } catch (Exception e) {
                sendMessage.setText("Food price can not edited ❌\nEnter text for price ❗\uFE0F \n\nChose edition type: ");
                botService.executeMessages(sendMessage);
            }
            return;
        }

        if (Objects.equals(state, FOOD_DELETE_MENU) && !text.equals(BACK)) {
            if (db.getMenus().containsKey(text)) {
                state = DELETE_FOOD_NAME;
                menu = text;
                sendMessage.setChatId(chatId);
                sendMessage.setText("Chose food:");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(text)));
                botService.executeMessages(sendMessage);
                return;
            }
        }

        if (Objects.equals(state, DELETE_FOOD_NAME) && !text.equals(BACK)) {
            Optional<Food> optionalFood = db.getFood(text);
            sendMessage.setChatId(chatId);
            botService.executeMessages();
            if (optionalFood.isPresent()) {
                food = optionalFood.get();
                sendMessage.setText("Food: " + text + "\n\nConfirm deletion?");
                sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("Yes ✅", "yes"), new InlineString("No ❌", "no")}}));
            } else {
                sendMessage.setText("Food is not found ❗\uFE0F");
                sendMessage.setReplyMarkup(null);
            }
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, IS_MENU_NAME)) {
            state = MAIN;
            if (db.getMenus().containsKey(text.toUpperCase())) {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Bunday menu mavjud ❗\uFE0F");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                botService.executeMessages(sendMessage);
                return;
            }
            db.getMenus().put(text.toUpperCase(), new ArrayList<>());
            sendMessage.setText("Menu qo'shildi ✅");
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, IS_FOOD_NAME)) {
            if (db.getFood(text).isPresent()) {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Bunday maxsulot mavjud ❗\uFE0F");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                state = MAIN;
                botService.executeMessages(sendMessage);
                return;
            }
            food.setName(text);
            sendMessage.setText("Enter food price:");
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true, true));
            sendMessage.setChatId(chatId);
            state = IS_FOOD_PRICE;
            botService.executeMessages(sendMessage);
            return;
        }

        if (Objects.equals(state, IS_FOOD_PRICE)) {
            try {
                food.setPrice(Double.valueOf(text));
                sendMessage.setText("Food added successfully ✅");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                sendMessage.setChatId(chatId);
                db.getMenus().get(food.getType()).add(food);
                state = MAIN;
                botService.executeMessages(sendMessage);
                return;
            } catch (Exception e) {
                sendMessage.setText("Narx uchun matn kiritdingiz ❗\uFE0F");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                sendMessage.setChatId(chatId);
                state = MAIN;
                botService.executeMessages(sendMessage);
                return;
            }
        }

        switch (text) {
            case "/start" -> {
                sendMessage.setText("Botimizga xush kelibsiz!");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case CREAT_MENU -> {
                sendMessage.setText("Enter menu name:");
                state = IS_MENU_NAME;
                ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
                replyKeyboardRemove.setRemoveKeyboard(true);
                replyKeyboardRemove.setSelective(true);
                sendMessage.setReplyMarkup(replyKeyboardRemove);
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case ADD_FOOD -> {
                sendMessage.setText("Chose food type:");
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                state = IS_FOOD_TYPE;
                food = new Food();
                botService.executeMessages(sendMessage);
            }
            case EDIT_FOOD -> {
                state = FOOD_EDIT_MENU;
                sendMessage.setText("Menuni tanlang:");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case DELETE_FOOD -> {
                state = FOOD_DELETE_MENU;
                sendMessage.setText("Menuni tanlang:");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case SHOW_HISTORY -> {
                showHistory(update);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                sendMessage.setText("Main menu");
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case BACK -> {
                if (Objects.equals(state, IS_FOOD_TYPE)) {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Main menu");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(state, FOOD_EDIT_MENU) || Objects.equals(state, FOOD_DELETE_MENU)) {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Main menu");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(state, EDIT_FOOD_NAME)) {
                    state = FOOD_EDIT_MENU;
                    sendMessage.setText("Menuni tanlang:");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                    sendMessage.setChatId(chatId);
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(state, DELETE_FOOD_NAME)) {
                    state = FOOD_DELETE_MENU;
                    sendMessage.setText("Menuni tanlang:");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                    sendMessage.setChatId(chatId);
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(state, EDITION_TYPE)) {
                    if (db.getMenus().containsKey(menu)) {
                        state = EDIT_FOOD_NAME;
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Chose food:");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(menu)));
                        menu = null;
                        botService.executeMessages(sendMessage);
                    }
                }
            }
            default -> {
                if (Objects.equals(state, IS_FOOD_TYPE)) {
                    if (db.getMenus().containsKey(text)) {
                        food.setType(text);
                        sendMessage.setText("Enter food name:");
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true, true));
                        sendMessage.setChatId(chatId);
                        state = IS_FOOD_NAME;
                        botService.executeMessages(sendMessage);
                        return;
                    } else {
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Bunday menu mavjud emas ❗\uFE0F");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                        state = MAIN;
                        botService.executeMessages(sendMessage);
                        return;
                    }
                }

                sendMessage.setChatId(chatId);
                sendMessage.setText("Noto'g'ri buyruq ❗\uFE0F");
                state = MAIN;
                sendMessage.setReplyMarkup(replyService.keyboardMaker(adminMenu));
                botService.executeMessages(sendMessage);
            }
        }
    }

    private void showHistory(Update update) {
        boolean isEmpty = true;
        Map<User, List<Order>> userOrder = db.getUserOrder();
        sendMessage.setChatId(update.getMessage().getChatId());
        for (User user : userOrder.keySet()) {
            for (Order order : userOrder.get(user)) {
                isEmpty = false;
                if (Objects.equals(order.getStatus(), WAITING)) {
                    sendMessage.setText(historyOrder(order, true));
                    sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("Confirm ✅", user.getId() + " confirm " + order.getId()), new InlineString("Cancel ❌", user.getId() + " cancel " + order.getId())}}));
                } else {
                    sendMessage.setText(historyOrder(order, true));
                    sendMessage.setReplyMarkup(null);
                }
                botService.executeMessages(sendMessage);
            }
        }
        if (isEmpty) {
            sendMessage.setText("History is empty \uD83D\uDEAB");
            botService.executeMessages(sendMessage);
        }
    }

    private static BotAdminService botAdminService;

    public static BotAdminService getInstance() {
        if (botAdminService == null)
            botAdminService = new BotAdminService();
        return botAdminService;
    }
}
