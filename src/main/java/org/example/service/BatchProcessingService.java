package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingService.class);

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
            int batchNo = entry.getKey();
            List<String[]> batch = entry.getValue();

            Future<Boolean> future = executorService.submit(() -> processSingleBatch(batchNo, batch, file, errorFolder));
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
                logger.error("Error in parallel BH block processing: {}", e.getMessage());
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
                logger.error("Validation failed for block {} in file: {}", batchKey, file.getName());
                return false;
            }

            // Insert the validated block into the database
            listenerService.saveAllRows(block);
            return true;
        } catch (Exception e) {
            logger.error("Error processing BH block {} in file: {}: {}", batchKey, file.getName(), e.getMessage());
            return false;
        }
    }
}
