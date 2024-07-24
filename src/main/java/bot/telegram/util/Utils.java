package bot.telegram.util;

import bot.telegram.entity.Food;
import bot.telegram.entity.Order;
import org.apache.commons.lang3.text.StrBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static bot.telegram.enums.Status.*;

public interface Utils {
    String CREAT_MENU = "Creat menu \uD83D\uDCDD";
    String ADD_FOOD = "Add food ➕";
    String EDIT_FOOD = "Edit food ✏\uFE0F";
    String DELETE_FOOD = "Delete food \uD83D\uDDD1";
    String SHOW_HISTORY = "Show history \uD83D\uDDC2";
    String[][] adminMenu = {{CREAT_MENU, ADD_FOOD}, {EDIT_FOOD, DELETE_FOOD}, {SHOW_HISTORY}};

    String MENU = "Menu \uD83D\uDD16";
    String BASKET = "Savat \uD83D\uDED2";
    String HISTORY = "History \uD83D\uDDD2";
    String[][] userMenu = {{MENU}, {BASKET, HISTORY}};

    String BACK = "\uD83D\uDD19 Orqaga";
    String REMOVE_FROM_BASKET = "Edit from basket ✏\uFE0F";

    String EDIT_NAME = "Edit name";
    String EDIT_PRICE = "Edit price";
    String[][] editionType = {{EDIT_NAME, EDIT_PRICE}, {BACK}};

    static String foodCaption(Food food) {
        StrBuilder foodCaption = new StrBuilder();
        foodCaption.append("Menu: ")
                .append(food.getType())
                .append("\nFood: ")
                .append(food.getName())
                .append("\nPrice: ")
                .append(food.getPrice())
                .append("\n\nCount: ")
                .append(food.getCount());
        return foodCaption.toString();
    }

    static String orderCaption(Order order) {
        StrBuilder foodCaption = new StrBuilder();
        double price = 0;
        if (order.getUser().getId().equals(6870548934L)){
            foodCaption.append("Customer: ")
                    .append(order.getUser().getName())
                    .append('\n');
        }
        for (Food food : order.getFoods()) {
            foodCaption.append("Food: ")
                    .append(food.getName())
                    .append("\nCount: ")
                    .append(food.getCount())
                    .append("\n\n");
            price += food.getPrice() * food.getCount();
        }
        foodCaption.append("Total price: ")
                .append(price);
        if (order.getChangedTime() != null){
            foodCaption.append(order.getChangedTime().format(DateTimeFormatter.ofPattern("'\nDate:' dd.MM.yyyy  'Time:' kk:mm")));
        }
        return foodCaption.toString();
    }

    static String historyOrder(Order order, boolean isAdmin) {
        StrBuilder foodCaption = new StrBuilder();
        double price = 0;
        if (isAdmin)
            foodCaption.append("Customer: ")
                    .append(order.getUser().getName())
                    .append('\n');
        for (Food food : order.getFoods()) {
            foodCaption.append("Food: ")
                    .append(food.getName())
                    .append(" (")
                    .append(food.getCount())
                    .append(")\n");
            price += food.getPrice() * food.getCount();
        }
        foodCaption.append("Total price: ")
                .append(price);

        if (Objects.equals(order.getStatus(), WAITING)) {
            foodCaption.append('\n')
                    .append("Status: WAITING ⏳");
        } else if (Objects.equals(order.getStatus(), CONFIRMED)) {
            foodCaption.append('\n')
                    .append("Status: CONFIRMED ✅");
        } else {
            foodCaption.append('\n')
                    .append(Objects.equals(order.getStatus(), ADMIN_CANCELED) ? "Status: ADMIN_CANCELED ❌" : "Status: USER_CANCELED ❌");
        }
        if (order.getChangedTime() != null){
            foodCaption.append(order.getChangedTime().format(DateTimeFormatter.ofPattern("'\nDate:' dd.MM.yyyy  'Time:' kk:mm")));
        }
        return foodCaption.toString();
    }
}
