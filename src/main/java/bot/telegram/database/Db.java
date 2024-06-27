package bot.telegram.database;

import bot.telegram.entity.Food;
import bot.telegram.entity.Order;
import bot.telegram.entity.User;
import com.github.javafaker.Faker;
import lombok.Getter;

import java.util.*;

import static bot.telegram.enums.Status.NEW;

public class Db {

    private Set<User> users = new HashSet<>();
    @Getter
    private Map<User, List<Order>> userOrder = new HashMap<>();
    @Getter
    private Map<User, Order> userBasket = new HashMap<>();
    @Getter
    private Map<String, List<Food>> menus = new HashMap<>();

    public Db() {
        adds();
    }

    public Optional<User> getUserById(Long id) {
        for (User user : users) {
            if (user.getId().equals(id))
                return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean addUser(User user) {
        userBasket.put(user, null);
        userOrder.put(user, new ArrayList<>());
        return users.add(user);
    }

    public Optional<Food> getFood(String foodName) {
        for (String menu : menus.keySet()) {
            for (Food food : menus.get(menu)) {
                if (food.getName().equals(foodName)) return Optional.of(food);
            }
        }
        return Optional.empty();
    }

    public Optional<Food> getFoodFromBasket(String foodName, User user) {
        for (Food food : userBasket.get(user).getFoods()) {
            if (food.getName().equals(foodName)) return Optional.of(food);
        }
        return Optional.empty();
    }

    public void foodCheckingAddBasket(Food food, User user) {
        if (db.isExist(food.getName(), user)) {
            getFoodFromBasket(food.getName(), user).get().setCount(getFoodFromBasket(food.getName(), user).get().getCount() + food.getCount());
        } else {
            if (db.getUserBasket().get(user) == null)
                db.getUserBasket().put(user, new Order(user, new ArrayList<>(), NEW, 0., null));
            db.getUserBasket().get(user).getFoods().add(user.getCurruntFood());
        }
    }

    public boolean isExist(String foodName, User user) {
        if (userBasket.get(user) == null)return false;
        for (Food food : userBasket.get(user).getFoods()) {
            if (food.getName().equals(foodName)) return true;
        }
        return false;
    }

    public Optional<Order> getOrderById(String id) {
        for (User user : userOrder.keySet()) {
            for (Order order : userOrder.get(user)) {
                if (order.getId().equals(id)) return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    private static Db db;

    public static Db getInstance() {
        if (db == null) db = new Db();
        return db;
    }

    private void adds() {
        Faker faker = new Faker();
        com.github.javafaker.Food food = faker.food();
        menus.put("TEST_MENU", new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            Food food1 = new Food("TEST_MENU", food.ingredient(), 15000., 1);
            menus.get("TEST_MENU").add(food1);
        }
    }
}
