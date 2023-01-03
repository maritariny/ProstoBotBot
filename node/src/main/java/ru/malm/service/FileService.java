package ru.malm.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.malm.entity.AppDocument;
import ru.malm.entity.AppPhoto;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
