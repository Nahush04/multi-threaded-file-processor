package org.example.listener;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.service.BatchProcessingService;
import org.example.service.MoveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class FileListener {
    private static final Logger logger = LoggerFactory.getLogger(FileListener.class);
    private static final String incomingFolder = "path/to/folder/incoming";
    String successFolder = "path/to/folder/success";
    String errorFolder = "path/to/folder/error";
    private final BatchProcessingService batchProcessingService;
    private final MoveService moveService;

    public FileListener(BatchProcessingService batchProcessingService, MoveService moveService) {
        this.batchProcessingService = batchProcessingService;
        this.moveService = moveService;
    }

    @Scheduled(fixedRate = 20000) // Check in every 20 seconds
    public void checkFolder() throws IOException, CsvException {
        File folder = new File(incomingFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            logger.error("Invalid folder path provided: {}", incomingFolder);
            return;
        }

        // Move Non-CSV Files to Error Folder
        File[] allFiles = folder.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (!file.getName().endsWith(".csv")) {
                    logger.error("Invalid file format: {}. Only CSV files are allowed.", file.getName());
                    moveService.moveToErrorFolder(file, errorFolder, "invalid file format");
                }
            }
        }

        // Process Only CSV Files
        File[] csvFiles = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            logger.info("No CSV files found.");
            return;
        }

        for (File file : csvFiles) {
            processFile(file);
        }
    }

    public void processFile(File file) {
        List<String[]> rows;
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            rows = reader.readAll();
        } catch (Exception e) {
            moveService.moveToErrorFolder(file, errorFolder, "file read error");
            return;
        }

        // Group data into BH blocks
        Map<Integer, List<String[]>> batchBlocks = new LinkedHashMap<>();
        List<String[]> currentBatch = null;
        Integer currentBatchNo = null;

        for (String[] row : rows) {
            if (row.length < 4) continue;

            if (row[0].equals("BH")) {
                currentBatchNo = Integer.parseInt(row[1]);
                currentBatch = new ArrayList<>();
                batchBlocks.put(currentBatchNo, currentBatch);
            }

            if (currentBatch != null) {
                currentBatch.add(row);
            }

            if (row[0].equals("BI")) {
                currentBatch = null;
            }
        }

        // Delegate batch processing to `BatchProcessingService`
        batchProcessingService.processBatchFile(file, batchBlocks, successFolder, errorFolder);
    }
}
