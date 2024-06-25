package bot.telegram.entity;

import bot.telegram.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.text.StrBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private final String id = UUID.randomUUID().toString();
    private User user;
    private List<Food> foods = new ArrayList<>();
    private Status status = Status.NEW;
    private Double price;
}
