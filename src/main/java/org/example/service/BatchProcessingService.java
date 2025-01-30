package org.example.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class BatchProcessingService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Thread pool for parallel execution
    private final ValidationService validationService;
    private final ListenerService listenerService;
    private final MoveService moveService;

    public BatchProcessingService(ValidationService validationService, ListenerService listenerService, MoveService moveService) {
        this.validationService = validationService;
        this.listenerService = listenerService;
        this.moveService = moveService;
    }

    public void processBatchFile(File file, Map<Integer, List<String[]>> batchBlocks, String successFolder, String errorFolder) {
        List<Future<Boolean>> futures = new ArrayList<>();
        boolean hasErrors = false;

        // Submit each BH block as a separate task
        for (Map.Entry<Integer, List<String[]>> entry : batchBlocks.entrySet()) {
            int batchKey = entry.getKey();
            List<String[]> block = entry.getValue();

            Future<Boolean> future = executorService.submit(() -> processSingleBatch(batchKey, block, file, errorFolder));
            futures.add(future);
        }

        // Wait for all threads to finish and check results
        for (Future<Boolean> future : futures) {
            try {
                if (!future.get()) {
                    hasErrors = true;
                }
            } catch (Exception e) {
                hasErrors = true;
                System.err.println("Error in parallel BH block processing: " + e.getMessage());
            }
        }

        // Move file based on block success/failure
        if (hasErrors) {
            moveService.moveToErrorFolder(file, errorFolder, "error in some BH blocks");
        } else {
            moveService.moveToSuccessFolder(file, successFolder);
        }
    }

    private boolean processSingleBatch(int batchKey, List<String[]> block, File file, String errorFolder) {
        try {
            // Validate the batch block before inserting
            if (!validationService.validateCsvRows(block)) {
                System.err.println("Validation failed for block " + batchKey + " in file: " + file.getName());
                return false;
            }

            // Insert the validated block into the database
            listenerService.saveAllRows(block);
            return true;
        } catch (Exception e) {
            System.err.println("Error processing BH block " + batchKey + " in file: " + file.getName() + ": " + e.getMessage());
            return false;
        }
    }
}
