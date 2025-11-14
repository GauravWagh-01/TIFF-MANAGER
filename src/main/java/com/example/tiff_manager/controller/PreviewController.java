package com.example.tiff_manager.controller;
import com.example.tiff_manager.model.TiffFile;
import com.example.tiff_manager.service.TiffService;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class PreviewController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Label infoLabel;

    @Autowired
    private TiffService tiffService;

    @FXML
    public void initialize() {
        // Configure ImageView
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }

    public void loadImage(TiffFile tiffFile) {
        try {
            File file = new File(tiffFile.getFilePath());

            if (!file.exists()) {
                infoLabel.setText("File not found: " + tiffFile.getFilePath());
                return;
            }

            infoLabel.setText("Loading...");

            new Thread(() -> {
                try {
                    BufferedImage bufferedImage = tiffService.readTiff(file);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    Platform.runLater(() -> {
                        imageView.setImage(fxImage);
                        String info = String.format("%s - %d x %d pixels",
                                tiffFile.getFileName(),
                                tiffFile.getWidth(),
                                tiffFile.getHeight());
                        infoLabel.setText(info);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        infoLabel.setText("Error loading image: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            infoLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        imageView.getScene().getWindow().hide();
    }
}
