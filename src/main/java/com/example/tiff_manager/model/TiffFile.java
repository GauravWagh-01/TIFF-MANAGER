package com.example.tiff_manager.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Alternative TiffFile entity WITHOUT Lombok
 * Use this if Lombok causes compilation issues
 */
@Entity
@Table(name = "tiff_files")
public class TiffFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String filePath;

    @Column
    private Long fileSize;

    @Column
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime importedDate;

    @Column(length = 500)
    private String tags;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column
    private String compression;

    // Constructors
    public TiffFile() {
    }

    public TiffFile(String fileName, String filePath, Long fileSize, LocalDateTime createdDate) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.createdDate = createdDate;
        this.importedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getImportedDate() {
        return importedDate;
    }

    public void setImportedDate(LocalDateTime importedDate) {
        this.importedDate = importedDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    @Override
    public String toString() {
        return String.format("%s (%s KB) - %s",
                fileName,
                fileSize != null ? fileSize / 1024 : 0,
                createdDate != null ? createdDate.toString() : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TiffFile)) return false;
        TiffFile tiffFile = (TiffFile) o;
        return id != null && id.equals(tiffFile.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}