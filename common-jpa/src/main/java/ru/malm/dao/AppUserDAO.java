package ru.malm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.malm.entity.AppUser;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id); // Реализацию метода берет на себя Spring
}
