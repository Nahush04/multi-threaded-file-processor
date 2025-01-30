package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class MoveService {

    private static final Logger logger = LoggerFactory.getLogger(MoveService.class);

    // Moves file to success folder
    public void moveToSuccessFolder(File file, String successFolder) {
        moveFile(file, successFolder, "success");
    }

    // Moves file to error folder with reason
    public void moveToErrorFolder(File file, String errorFolder, String reason) {
        moveFile(file, errorFolder, "error (" + reason + ")");
    }

    // Generic method to move file
    private void moveFile(File file, String destinationFolder, String type) {
        try {
            Path targetPath = Path.of(destinationFolder, file.getName());
            Files.createDirectories(Path.of(destinationFolder)); // Ensure destination folder exists
            Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING); // Move file
            logger.info("File moved to {} folder: {}", type, targetPath);
        } catch (IOException e) {
            logger.error("Failed to move file: {}", file.getName(), e);
        }
    }
}
