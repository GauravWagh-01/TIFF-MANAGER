package com.example.tiff_manager.controller;

import com.example.tiff_manager.model.TiffFile;
import com.example.tiff_manager.service.TiffService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Component
public class MainController {

    @FXML
    private ListView<TiffFile> fileListView;

    @FXML
    private ImageView previewImageView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button openButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button previewButton;

    @FXML
    private TextArea metadataTextArea;

    @Autowired
    private TiffService tiffService;

    @Autowired
    private ApplicationContext applicationContext;

    private ObservableList<TiffFile> tiffFiles;

    @FXML
    public void initialize() {
        tiffFiles = FXCollections.observableArrayList();
        fileListView.setItems(tiffFiles);

        // Configure ListView cell factory for custom display
        fileListView.setCellFactory(param -> new ListCell<TiffFile>() {
            @Override
            protected void updateItem(TiffFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Enable/disable buttons based on selection
        fileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean hasSelection = newValue != null;
                    deleteButton.setDisable(!hasSelection);
                    previewButton.setDisable(!hasSelection);
                }
        );

        // Load existing files
        loadTiffFiles();
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open TIFF File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TIFF Files", "*.tif", "*.tiff"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) openButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            importFile(selectedFile);
        }
    }

    @FXML
    private void handleDeleteFile() {
        TiffFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete File");
            alert.setHeaderText("Delete TIFF file record?");
            alert.setContentText("This will remove the record from database. The actual file will not be deleted.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                tiffService.deleteTiffFile(selected.getId());
                tiffFiles.remove(selected);
                statusLabel.setText("File record deleted: " + selected.getFileName());
                previewImageView.setImage(null);
                metadataTextArea.clear();
            }
        }
    }

    @FXML
    private void handlePreviewFile() {
        TiffFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadPreview(selected);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleConvertToPng() {
        TiffFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a TIFF file first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as PNG");
        fileChooser.setInitialFileName(selected.getFileName().replace(".tif", ".png").replace(".tiff", ".png"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        Stage stage = (Stage) openButton.getScene().getWindow();
        File outputFile = fileChooser.showSaveDialog(stage);

        if (outputFile != null) {
            try {
                File inputFile = new File(selected.getFilePath());
                tiffService.convertToPng(inputFile, outputFile);
                statusLabel.setText("Converted to PNG: " + outputFile.getName());
                showAlert("Success", "File converted successfully to PNG.");
            } catch (Exception e) {
                showAlert("Error", "Failed to convert file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleOpenInNewWindow() {
        TiffFile selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a TIFF file first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/preview-view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            PreviewController previewController = loader.getController();
            previewController.loadImage(selected);

            Stage previewStage = new Stage();
            previewStage.setTitle("Preview: " + selected.getFileName());
            previewStage.setScene(new Scene(root));
            previewStage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open preview window: " + e.getMessage());
        }
    }

    private void importFile(File file) {
        try {
            statusLabel.setText("Importing file...");

            // Import in background thread
            new Thread(() -> {
                try {
                    TiffFile tiffFile = tiffService.importTiffFile(file);

                    Platform.runLater(() -> {
                        if (!tiffFiles.contains(tiffFile)) {
                            tiffFiles.add(0, tiffFile);
                        }
                        fileListView.getSelectionModel().select(tiffFile);
                        loadPreview(tiffFile);
                        statusLabel.setText("Imported: " + tiffFile.getFileName());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        showAlert("Import Error", "Failed to import file: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            showAlert("Error", "Failed to import file: " + e.getMessage());
        }
    }

    private void loadPreview(TiffFile tiffFile) {
        try {
            File file = new File(tiffFile.getFilePath());
            if (!file.exists()) {
                showAlert("File Not Found", "The TIFF file no longer exists at: " + tiffFile.getFilePath());
                return;
            }

            statusLabel.setText("Loading preview...");

            new Thread(() -> {
                try {
                    BufferedImage bufferedImage = tiffService.readTiff(file);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    Platform.runLater(() -> {
                        previewImageView.setImage(fxImage);
                        previewImageView.setPreserveRatio(true);
                        statusLabel.setText("Preview loaded: " + tiffFile.getFileName());

                        // Display metadata
                        displayMetadata(tiffFile);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error loading preview: " + e.getMessage());
                        showAlert("Preview Error", "Failed to load preview: " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void displayMetadata(TiffFile tiffFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("File Name: ").append(tiffFile.getFileName()).append("\n");
        sb.append("File Path: ").append(tiffFile.getFilePath()).append("\n");
        sb.append("File Size: ").append(tiffFile.getFileSize() / 1024).append(" KB\n");
        sb.append("Dimensions: ").append(tiffFile.getWidth()).append(" x ").append(tiffFile.getHeight()).append("\n");
        sb.append("Created: ").append(tiffFile.getCreatedDate()).append("\n");
        sb.append("Imported: ").append(tiffFile.getImportedDate()).append("\n");
        sb.append("Tags: ").append(tiffFile.getTags()).append("\n");

        metadataTextArea.setText(sb.toString());
    }

    private void loadTiffFiles() {
        try {
            List<TiffFile> files = tiffService.getAllTiffFiles();
            tiffFiles.setAll(files);
            statusLabel.setText("Loaded " + files.size() + " file(s)");
        } catch (Exception e) {
            statusLabel.setText("Error loading files: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
