package bot.telegram.entity;


import bot.telegram.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
    private Long id;
    private String name;
    private Double balance;
    private State state;
    private Food curruntFood;
    private Integer messageId;
}
