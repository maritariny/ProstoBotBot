package ru.malm.service;

import org.springframework.core.io.FileSystemResource;
import ru.malm.entity.AppDocument;
import ru.malm.entity.AppPhoto;
import ru.malm.entity.BinaryContent;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);

    // преобразование массива байт в объект FileSystemResource, который необходим для передачи контента в теле http-ответа
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
