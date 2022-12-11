package ru.malm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.malm.entity.BinaryContent;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {

}
