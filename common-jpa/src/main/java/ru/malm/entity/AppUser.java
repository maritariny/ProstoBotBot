package ru.malm.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.malm.entity.enums.UserState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity // @Entity - указывает, что класс будет сущностью, которая связана с таблице БД
@Table(name = "app_user") // @Table - позволяет задать имя таблицы
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Задается стратегия генерации первичных ключей. IDENTITY = Позволяем БД самой генерировать значения для первичных ключей
    private Long id;
    private Long telegramUserId; // id юзера из телеграма
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private Boolean isActive;
    @Enumerated(EnumType.STRING)
    private UserState state;
}
