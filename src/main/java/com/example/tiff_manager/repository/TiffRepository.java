package com.example.tiff_manager.repository;

import com.example.tiff_manager.model.TiffFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiffRepository extends JpaRepository<TiffFile, Long> {

    Optional<TiffFile> findByFilePath(String filePath);

    List<TiffFile> findAllByOrderByImportedDateDesc();

    boolean existsByFilePath(String filePath);

    List<TiffFile> findByFileNameContainingIgnoreCase(String fileName);
}