package ru.malm.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.malm.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
