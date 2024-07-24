package bot.telegram.services;

import bot.telegram.database.Db;
import bot.telegram.entity.Food;
import bot.telegram.entity.Order;
import bot.telegram.entity.User;
import bot.telegram.enums.Status;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static bot.telegram.enums.State.*;
import static bot.telegram.enums.Status.*;
import static bot.telegram.util.Utils.*;

public class BotUserService {

    private SendMessage sendMessage = new SendMessage();
    Db db = Db.getInstance();

    BotService botService = BotService.getInstance();
    private final ReplyMarkupService replyService = new ReplyMarkupService();
    private final InlineMarkupService inlineService = new InlineMarkupService();
    public final Long adminId = 6870548934L;

    public void callbackHandler(Update update) {
        User currentUser;
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String text = update.getCallbackQuery().getData();
        if (db.getUserById(chatId).isEmpty()) {
            User user = new User();
            user.setId(chatId);
            user.setName(update.getMessage().getChat().getFirstName());
            user.setBalance(2000000.);
            if (db.addUser(user)) {
                System.out.println("New user successfully added!");
            }
            currentUser = user;
        } else {
            currentUser = db.getUserById(chatId).get();
        }

        switch (text) {
            case "plus" -> {
                Food curruntFood = currentUser.getCurruntFood();
                curruntFood.setCount(curruntFood.getCount() + 1);
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setText(foodCaption(curruntFood));
                editMessageText.setMessageId(currentUser.getMessageId());
                editMessageText.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("-", "minus"), new InlineString("\uD83D\uDC49\uD83C\uDFFC\uD83E\uDDFA", "toBasket"), new InlineString("+", "plus")}, {new InlineString("Remove ❌", "cancel")}}));
                editMessageText.setChatId(chatId);
                botService.executeMessages(editMessageText);
            }
            case "minus" -> {
                Food curruntFood = currentUser.getCurruntFood();
                if (curruntFood.getCount() >= 1) {
                    curruntFood.setCount(curruntFood.getCount() - 1);
                    EditMessageText editMessageText = new EditMessageText();
                    editMessageText.setText(foodCaption(curruntFood));
                    editMessageText.setMessageId(currentUser.getMessageId());
                    if (curruntFood.getCount() == 0)
                        editMessageText.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("+", "plus")}, {new InlineString("Remove ❌", "cancel")}}));
                    else
                        editMessageText.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("-", "minus"), new InlineString("\uD83D\uDC49\uD83C\uDFFC\uD83E\uDDFA", "toBasket"), new InlineString("+", "plus")}, {new InlineString("Remove ❌", "cancel")}}));
                    editMessageText.setChatId(chatId);
                    botService.executeMessages(editMessageText);
                }
            }
            case "toBasket" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(currentUser.getMessageId());
                deleteMessage.setChatId(chatId);
                botService.executeMessages(deleteMessage);
                sendMessage.setChatId(chatId);
                if (Objects.equals(currentUser.getState(), REMOVE_BASKET_FOOD)) {
                    sendMessage.setText("Maxsulot yangilandi ✅\nChose editional food:");
                    currentUser.setState(REMOVE_BASKET);
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getUserBasket().get(currentUser).getFoods()));
                } else {
                    sendMessage.setText("Maxsulot savatga qo'shildi ✅\nXaridni davom ettirishingiz mumkin!");
                    currentUser.setState(ADD_ORDER);
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(currentUser.getCurrentMenu())));
                    db.foodCheckingAddBasket(currentUser.getCurruntFood(), currentUser);
                }
                botService.executeMessages(sendMessage);
            }
            case "cancel" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(currentUser.getMessageId());
                deleteMessage.setChatId(chatId);
                botService.executeMessages(deleteMessage);
                sendMessage.setChatId(chatId);
                if (Objects.equals(currentUser.getState(), REMOVE_BASKET_FOOD)) {
                    db.getUserBasket().get(currentUser).getFoods().remove(currentUser.getCurruntFood());
                    sendMessage.setText("Maxsulot savatdan chiqarildi ✅\nChose editional food:");
                    currentUser.setState(REMOVE_BASKET);
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getUserBasket().get(currentUser).getFoods()));
                } else {
                    if (db.getMenus().containsKey(currentUser.getCurrentMenu())) {
                        sendMessage.setText("Ovqatni tanlang \uD83D\uDCCC");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(currentUser.getCurrentMenu())));
                        currentUser.setState(ADD_ORDER);
                    }
                }
                botService.executeMessages(sendMessage);
            }
            case "confirmOrder" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(currentUser.getMessageId());
                deleteMessage.setChatId(chatId);
                botService.executeMessages(deleteMessage);
                sendMessage.setText("Order sended to admin ✅");
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                botService.executeMessages(sendMessage);
                sendMessage.setText(orderCaption(db.getUserBasket().get(currentUser)));
                sendMessage.setChatId(adminId);
                sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("Confirm ✅", currentUser.getId() + " confirm " + db.getUserBasket().get(currentUser).getId()), new InlineString("Cancel ❌", currentUser.getId() + " cancel " + db.getUserBasket().get(currentUser).getId())}}));
                db.getUserBasket().get(currentUser).setChangedTime(LocalDateTime.now());
                db.getUserBasket().get(currentUser).setStatus(WAITING);
                db.getUserOrder().get(currentUser).add(db.getUserBasket().get(currentUser));
                db.getUserBasket().put(currentUser, null);
                botService.executeMessages(sendMessage);
            }
            case "cancelOrder" -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMessageId(currentUser.getMessageId());
                deleteMessage.setChatId(chatId);
                botService.executeMessages(deleteMessage);
                db.getUserBasket().get(currentUser).setStatus(USER_CANCELED);
                db.getUserOrder().get(currentUser).add(db.getUserBasket().get(currentUser));
                db.getUserBasket().put(currentUser, null);
                sendMessage.setText("Order canceled ✅");
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                botService.executeMessages(sendMessage);
            }
        }
    }

    public void messageHandler(Update update) {
        User currentUser;
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        if (db.getUserById(chatId).isEmpty()) {
            User user = new User();
            user.setId(chatId);
            user.setName(update.getMessage().getChat().getFirstName());
            user.setBalance(2000000.);
            if (db.addUser(user)) {
                System.out.println("New user successfully added!");
            }
            currentUser = user;
            text = "/start";
        } else {
            currentUser = db.getUserById(chatId).get();
        }

        if (Objects.equals(currentUser.getState(), FOOD_MENU)) {
            if (db.getMenus().containsKey(text)) {
                currentUser.setCurrentMenu(text);
                sendMessage.setText("Ovqatni tanlang \uD83D\uDCCC");
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(text)));
                currentUser.setState(ADD_ORDER);
                botService.executeMessages(sendMessage);
                return;
            }
        }

        if (Objects.equals(currentUser.getState(), ADD_ORDER)) {
            Optional<Food> optionalFood = db.getFood(text);
            if (optionalFood.isPresent()) {
                Food food = optionalFood.get().clone();
                sendMessage.setText(foodCaption(food));
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("-", "minus"), new InlineString("\uD83D\uDC49\uD83C\uDFFC\uD83E\uDDFA", "toBasket"), new InlineString("+", "plus")}, {new InlineString("Remove ❌", "cancel")}}));
                currentUser.setState(SET_FOOD);
                currentUser.setCurruntFood(food);
                currentUser.setMessageId(botService.executeMessages(sendMessage).getMessageId());
                return;
            }
        }

        if (Objects.equals(currentUser.getState(), REMOVE_BASKET)) {
            Optional<Food> optionalFood = db.getFoodFromBasket(text, currentUser);
            sendMessage.setChatId(chatId);
            if (optionalFood.isPresent()) {
                Food food = optionalFood.get();
                sendMessage.setText(foodCaption(food));
                sendMessage.setChatId(chatId);
                sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("-", "minus"), new InlineString("\uD83D\uDC49\uD83C\uDFFC\uD83E\uDDFA", "toBasket"), new InlineString("+", "plus")}, {new InlineString("Remove ❌", "cancel")}}));
                currentUser.setCurruntFood(food);
                currentUser.setState(REMOVE_BASKET_FOOD);
                currentUser.setMessageId(botService.executeMessages(sendMessage).getMessageId());
                return;
            } else if (!text.equals(BACK)) {
                sendMessage.setText("Food is not found ❗\uFE0F \nSelect edition food:");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getUserBasket().get(currentUser).getFoods()));
                botService.executeMessages(sendMessage);
                return;
            }
        }

        switch (text) {
            case "/start" -> {
                sendMessage.setText("Botimizga xush kelibsiz!");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case MENU -> {
                sendMessage.setText(MENU);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                sendMessage.setChatId(chatId);
                currentUser.setState(FOOD_MENU);
                botService.executeMessages(sendMessage);
            }
            case REMOVE_FROM_BASKET -> {
                if (Objects.equals(currentUser.getState(), TO_BASKET)) {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(chatId);
                    deleteMessage.setMessageId(currentUser.getMessageId());
                    botService.executeMessages(deleteMessage);
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Select editional food:");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getUserBasket().get(currentUser).getFoods()));
                    currentUser.setState(REMOVE_BASKET);
                    botService.executeMessages(sendMessage);
                }
            }
            case BASKET -> {
                Order order = db.getUserBasket().get(currentUser);
                sendMessage.setChatId(chatId);
                if (order == null || order.getFoods().isEmpty()) {
                    sendMessage.setText("Basket is empty \uD83D\uDEAB");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                    botService.executeMessages(sendMessage);
                    return;
                }
                sendMessage.setText("Your basket");
                sendMessage.setReplyMarkup(replyService.keyboardMaker(new String[][]{{REMOVE_FROM_BASKET}, {BACK}}));
                botService.executeMessages(sendMessage);
                currentUser.setState(TO_BASKET);
                sendMessage.setText(orderCaption(order));
                sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("Confirm order ✅", "confirmOrder")}, {new InlineString("Cancel order ❌", "cancelOrder")}}));
                currentUser.setMessageId(botService.executeMessages(sendMessage).getMessageId());
            }
            case HISTORY -> {
                showHistory(update);
                currentUser.setState(MAIN);
                sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                sendMessage.setText("Main menu");
                sendMessage.setChatId(chatId);
                botService.executeMessages(sendMessage);
            }
            case BACK -> {
                if (Objects.equals(currentUser.getState(), FOOD_MENU)) {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Main menu");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                    currentUser.setState(MAIN);
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(currentUser.getState(), ADD_ORDER)) {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(MENU);
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().keySet()));
                    currentUser.setState(FOOD_MENU);
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(currentUser.getState(), TO_BASKET)) {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(chatId);
                    deleteMessage.setMessageId(currentUser.getMessageId());
                    botService.executeMessages(deleteMessage);
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Main menu");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                    currentUser.setState(MAIN);
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(currentUser.getState(), REMOVE_BASKET)) {
                    Order order = db.getUserBasket().get(currentUser);
                    sendMessage.setChatId(chatId);
                    currentUser.setState(TO_BASKET);
                    if (order == null || order.getFoods().isEmpty()) {
                        sendMessage.setText("Basket is empty \uD83D\uDEAB");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                        botService.executeMessages(sendMessage);
                        return;
                    }
                    sendMessage.setText("Your basket");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(new String[][]{{REMOVE_FROM_BASKET}, {BACK}}));
                    botService.executeMessages(sendMessage);
                    currentUser.setState(TO_BASKET);
                    sendMessage.setText(orderCaption(order));
                    sendMessage.setReplyMarkup(inlineService.inlineMarkup(new InlineString[][]{{new InlineString("Confirm order ✅", "confirmOrder")}, {new InlineString("Cancel order ❌", "cancelOrder")}}));
                    currentUser.setMessageId(botService.executeMessages(sendMessage).getMessageId());
                } else if (Objects.equals(currentUser.getState(), REMOVE_BASKET_FOOD)) {
                    Order order = db.getUserBasket().get(currentUser);
                    sendMessage.setChatId(chatId);
                    botService.executeMessages(new DeleteMessage(chatId.toString(), currentUser.getMessageId()));
                    if (order == null || order.getFoods().isEmpty()) {
                        currentUser.setState(TO_BASKET);
                        sendMessage.setText("Basket is empty \uD83D\uDEAB");
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
                        botService.executeMessages(sendMessage);
                        return;
                    }
                    currentUser.setState(REMOVE_BASKET);
                    sendMessage.setText("Select editional food:");
                    sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getUserBasket().get(currentUser).getFoods()));
                    botService.executeMessages(sendMessage);
                } else if (Objects.equals(currentUser.getState(), SET_FOOD)) {
                    botService.executeMessages(new DeleteMessage(chatId.toString(), currentUser.getMessageId()));
                    if (db.getMenus().containsKey(currentUser.getCurrentMenu())) {
                        sendMessage.setText("Ovqatni tanlang \uD83D\uDCCC");
                        sendMessage.setChatId(chatId);
                        sendMessage.setReplyMarkup(replyService.keyboardMaker(db.getMenus().get(currentUser.getCurrentMenu())));
                        currentUser.setState(ADD_ORDER);
                        botService.executeMessages(sendMessage);
                    }
                }
            }
            default -> {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Noto'g'ri buyruq ❗\uFE0F");
                sendMessage.setReplyMarkup(null);
                botService.executeMessages(sendMessage);
            }
        }
    }

    private void showHistory(Update update) {
        boolean isEmpty = true;
        Map<User, List<Order>> userOrder = db.getUserOrder();
        sendMessage.setChatId(update.getMessage().getChatId());
        for (Order order : userOrder.get(db.getUserById(update.getMessage().getChatId()).get())) {
            isEmpty = false;
            sendMessage.setText(historyOrder(order, false));
            sendMessage.setReplyMarkup(null);
            botService.executeMessages(sendMessage);
        }
        if (isEmpty) {
            sendMessage.setText("History is empty \uD83D\uDEAB");
            sendMessage.setReplyMarkup(replyService.keyboardMaker(userMenu));
            botService.executeMessages(sendMessage);
        }
    }

    private static BotUserService botUserService;

    public static BotUserService getInstance() {
        if (botUserService == null)
            botUserService = new BotUserService();
        return botUserService;
    }
}
