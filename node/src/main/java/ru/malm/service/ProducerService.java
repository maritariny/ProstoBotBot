package ru.malm.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

// Нужен для отправки ответов с ноды в брокер.
// Текст ответа: текстовое сообщение с казанием номера чата пользователя
public interface ProducerService {
    void producerAnswer(SendMessage sendMessage);
}
