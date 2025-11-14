package com.example.tiff_manager.service;


import com.example.tiff_manager.model.TiffFile;
import com.example.tiff_manager.repository.TiffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional
public class TiffService {

    private final TiffRepository tiffRepository;

    public TiffService(TiffRepository tiffRepository) {
        this.tiffRepository = tiffRepository;
    }

    /**
     * Read TIFF file and return as BufferedImage
     */
    public BufferedImage readTiff(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IOException("No suitable ImageReader found for TIFF file");
            }

            ImageReader reader = readers.next();
            reader.setInput(input);

            BufferedImage image = reader.read(0);
            reader.dispose();

            return image;
        }
    }

    /**
     * Convert TIFF to PNG format
     */
    public void convertToPng(File input, File output) throws IOException {
        BufferedImage image = readTiff(input);

        if (!output.getName().toLowerCase().endsWith(".png")) {
            output = new File(output.getAbsolutePath() + ".png");
        }

        ImageIO.write(image, "PNG", output);
    }

    /**
     * Extract metadata from TIFF file
     */
    public Map<String, String> extractMetadata(File file) throws IOException {
        Map<String, String> metadata = new HashMap<>();

        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IOException("No suitable ImageReader found for TIFF file");
            }

            ImageReader reader = readers.next();
            reader.setInput(input);

            // Image dimensions
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            metadata.put("Width", String.valueOf(width));
            metadata.put("Height", String.valueOf(height));
            metadata.put("Format", reader.getFormatName());

            // Get IIOMetadata
            IIOMetadata iioMetadata = reader.getImageMetadata(0);
            if (iioMetadata != null) {
                String[] formatNames = iioMetadata.getMetadataFormatNames();
                metadata.put("MetadataFormats", String.join(", ", formatNames));
            }

            // File attributes
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            metadata.put("FileSize", String.valueOf(file.length()));
            metadata.put("CreatedTime", attrs.creationTime().toString());
            metadata.put("ModifiedTime", attrs.lastModifiedTime().toString());

            reader.dispose();
        }

        return metadata;
    }

    /**
     * Import TIFF file into database
     */
    public TiffFile importTiffFile(File file) throws IOException {
        String filePath = file.getAbsolutePath();

        // Check if already imported
        if (tiffRepository.existsByFilePath(filePath)) {
            return tiffRepository.findByFilePath(filePath).orElseThrow();
        }

        // Extract metadata
        Map<String, String> metadata = extractMetadata(file);

        // Get file attributes
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        LocalDateTime createdDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(attrs.creationTime().toMillis()),
                ZoneId.systemDefault()
        );

        // Create entity
        TiffFile tiffFile = new TiffFile(
                file.getName(),
                filePath,
                file.length(),
                createdDate
        );

        tiffFile.setWidth(Integer.parseInt(metadata.get("Width")));
        tiffFile.setHeight(Integer.parseInt(metadata.get("Height")));
        tiffFile.setTags("TIFF, " + metadata.get("Format"));

        return tiffRepository.save(tiffFile);
    }

    /**
     * Get all imported TIFF files
     */
    public List<TiffFile> getAllTiffFiles() {
        return tiffRepository.findAllByOrderByImportedDateDesc();
    }

    /**
     * Delete TIFF file record
     */
    public void deleteTiffFile(Long id) {
        tiffRepository.deleteById(id);
    }

    /**
     * Get TIFF file by ID
     */
    public Optional<TiffFile> getTiffFileById(Long id) {
        return tiffRepository.findById(id);
    }
}