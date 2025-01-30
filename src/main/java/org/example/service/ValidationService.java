package org.example.service;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValidationService {

    private final Logger logger;

    public ValidationService(Logger logger) {
        this.logger = logger;
    }

    public boolean validateCsvRows(List<String[]> rows) {
        /* Create a hashmap to record the batch number as key and number of transaction items as value */
        Map<Integer, Integer> batchToItemCountMap = new HashMap<>();
        Integer currentBatchNumber = null;

        for (String[] row : rows) {
            String key = row[0];

            if (key == null) {
                throw new NullPointerException("Null key encountered in first column for the row: " + Arrays.toString(row));
            }

            switch (key){
                case "BH":
                    // Start tracking a new Batch header block
                    currentBatchNumber = Integer.parseInt(row[1]);
                    // Add the current Batch header id to the hashmap as a key
                    batchToItemCountMap.put(currentBatchNumber, 0);
                    break;

                case "TH":
                    break;

                case "TI":
                    if (currentBatchNumber != null)
                        // Count Transaction items for the current BH block and add it as the value to the current key (BH id)
                        batchToItemCountMap.put(currentBatchNumber, batchToItemCountMap.get(currentBatchNumber) + 1);
                    break;

                case "BI":
                    // Now after reaching the Batch summary row...
                    int batchNo = Integer.parseInt(row[1]); // First column corresponds to the batch number
                    int itemCount = Integer.parseInt(row[2]); // Second column corresponds to the number of transaction items

                    // If the current batch number is not the same as BI row's batch number, log error and fail validation
                    if (!batchToItemCountMap.containsKey(batchNo)) {
                        logger.error("Batch Summary row references an unknown Batch Header number: {}", batchNo);
                        return false;
                    }

                    // If the item count for current batch number (stored in the Hashmap) is not the same as BI row's item count, log error and fail validation
                    if (batchToItemCountMap.get(batchNo) != itemCount) {
                        logger.error("Mismatch in Transaction Item count for Batch Header {}: expected {}, found {}", batchNo, itemCount, batchToItemCountMap.get(batchNo));
                        return false;
                    }

                    // Reset current BH tracking after validation
                    currentBatchNumber = null;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown key in first column: " + key);
            }
        }

        logger.info("CSV validation successful!");
        return true;
    }
}
