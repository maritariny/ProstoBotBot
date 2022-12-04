package ru.malm.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.malm.dao.AppUserDAO;
import ru.malm.dao.RawDataDAO;
import ru.malm.entity.AppUser;
import ru.malm.entity.RawData;
import ru.malm.service.MainService;
import ru.malm.service.ProducerService;

import static ru.malm.entity.enums.UserState.BASIC_STATE;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        // Разбор входящего update на составные части, доставание телеграмовского юзера
        var textMessage = update.getMessage();
        var telegramUser = textMessage.getFrom();
        var appUser = findOrSaveAppUser(telegramUser);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(User telegramUser) {
        // persistent = Объект есть в БД, имеет заполненный первичный ключ и связан с Hibernate
        // transient = Объекта еще нет в БД, необходимо его сохранить
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());

        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    // TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser); // Записывает в базу, заполняет ключ, привязывает объект к сессии Hibernate
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update) // Вызывается сеттер
                .build();
        rawDataDAO.save(rawData);
    }
}
