package ru.malm.service.impl;

import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.malm.dao.AppDocumentDAO;
import ru.malm.dao.AppPhotoDAO;
import ru.malm.dao.BinaryContentDAO;
import ru.malm.entity.AppDocument;
import ru.malm.entity.AppPhoto;
import ru.malm.entity.BinaryContent;
import ru.malm.exceptions.UploadFileException;
import ru.malm.service.FileService;
import ru.malm.service.enums.LinkType;
import ru.malm.utils.CryptoTool;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${link.address}")
    private String linkAddress;
    private final AppDocumentDAO appDocumentDAO; // Внедрение бинов
    private final AppPhotoDAO appPhotoDAO; // Внедрение бина для сохранения объекта фото в базу
    private final BinaryContentDAO binaryContentDAO;
    private final CryptoTool cryptoTool; // Внедрение бина для шифрования номера файла в ссылке

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, BinaryContentDAO binaryContentDAO, CryptoTool cryptoTool) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
        this.binaryContentDAO = binaryContentDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        Document telegramDoc = telegramMessage.getDocument();
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId); // http get запрос к серверу телеграма
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc); // сохранение в БД, вернется persistentObject
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response); // кастомное исключение
        }
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        //TODO пока что обрабатывается только одно фото в сообщении
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? photoSizeCount - 1: 0;
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId); // http get запрос к серверу телеграма
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDAO.save(transientAppPhoto); // сохранение в БД, вернется persistentObject
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response); // кастомное исключение
        }
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath); // снова запрос к серверу телеграма
        BinaryContent transientBinaryContent = BinaryContent.builder() // объект, который пока не привязан к базе данных
                .fileAsArrayOfBytes(fileInByte)
                .build();
        return binaryContentDAO.save(transientBinaryContent); // сохранение в БД;
    }

    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate(); // спринговый инструмент, сделать исходящий запрос
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null; // стандартный механизм java для скачивания
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        // TODO подумать над оптимизацией
        try (InputStream is = urlObj.openStream()) { // открытие стрима для скачивания контента
            return is.readAllBytes(); // скачивание одним большим куском без разбиения на чанки
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    @Override
    public String generateLink(Long docId, LinkType linkType) {
        // получение хэша и вставка в ссылку вместо номера id документа/фото
        var hash = cryptoTool.hashOf(docId);
        return "http://" + linkAddress + "/" + linkType + "?id=" + hash;
    }

}
