package ru.malm.service.impl;

import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import ru.malm.dao.AppDocumentDAO;
import ru.malm.dao.AppPhotoDAO;
import ru.malm.entity.AppDocument;
import ru.malm.entity.AppPhoto;
import ru.malm.entity.BinaryContent;
import ru.malm.service.FileService;
import ru.malm.utils.CryptoTool;

import java.io.File;
import java.io.IOException;

@Log4j
@Service // аннотация создаст из класса спринговый бин
public class FileServiceImpl implements FileService {

    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, AppPhotoDAO appPhotoDAO, CryptoTool cryptoTool) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String hash) {
        // Дешифрование хеш-строки
        //var id = Long.parseLong(docId);
        var id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return appDocumentDAO.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String hash) {
        // Дешифрование хеш-строки
        //var id = Long.parseLong(photoId);
        var id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return appPhotoDAO.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO доабвить генерацию имени временного файла (т.к., возможно, оно может быть не уникальным)
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit(); // При завершении работы приложения файл будет удален из постоянной памяти компьютера (метод регистрирует файл в очереди на удаление)
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
