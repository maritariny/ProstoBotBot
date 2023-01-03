package ru.malm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.malm.entity.AppPhoto;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long> {
}
