package org.example.service;

import org.example.entity.BatchHeader;
import org.example.entity.TransactionHeader;
import org.example.entity.TransactionItem;
import org.example.exception.InvalidDataFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Service
public class ListenerService {
    private static final Logger logger = LoggerFactory.getLogger(ListenerService.class);

    private final JdbcTemplate jdbcTemplate;

    public ListenerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void saveAllRows(List<String[]> rows) throws InvalidDataFormatException, DuplicateKeyException {
        try {
            for (String[] row : rows) {
                saveRowToTable(row[0], row); // Call row insertion
            }
        } catch (InvalidDataFormatException | DuplicateKeyException e) {
            throw e; // Rethrow so transaction rolls back
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during batch insert", e);
        }
    }

    private void saveRowToTable(String key, String[] row) throws DuplicateKeyException, ParseException {
        try {
            switch (key) {
                case "BH":
                    BatchHeader table1 = new BatchHeader();
                    table1.setId(Integer.parseInt(row[1]));
                    table1.setDate(new SimpleDateFormat("MM/dd/yyyy").parse(row[2]));
                    table1.setLocation(row[3]);
                    insertIntoTable1(table1);
                    break;

                case "TH":
                    TransactionHeader table2 = new TransactionHeader();
                    table2.setId(Integer.parseInt(row[1]));
                    table2.setDate(new SimpleDateFormat("MM/dd/yyyy").parse(row[2]));
                    table2.setAmount(Double.parseDouble(row[3]));
                    insertIntoTable2(table2);
                    break;

                case "TI":
                    TransactionItem table3 = new TransactionItem();
                    table3.setId(Integer.parseInt(row[1]));
                    table3.setDescription(row[2]);
                    table3.setAmount(Double.parseDouble(row[3]));
                    insertIntoTable3(table3);
                    break;

                case "BI": // Skip BI rows which is only used for validation
                    break;

                default:
                    logger.warn("Unknown key: {}", key);
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Error saving row to table because of duplicate key", e);
            throw new DuplicateKeyException("Duplicate key encountered: " + row[1], e);
        } catch (NumberFormatException e) {
            logger.error("Number format error in row: {}. Expected numerical value but got invalid data.", row, e);
            throw new InvalidDataFormatException("Number format error in row: " + Arrays.toString(row), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid data format in row: {}. Unexpected value encountered.", row, e);
            throw new InvalidDataFormatException("Invalid data format in row: " + Arrays.toString(row), e);
        }
    }

    // REMOVED @Transactional from individual insert functions (handled at batch level)
    public void insertIntoTable1(BatchHeader table1) {
        jdbcTemplate.update("INSERT INTO batchHeader (BatchNo, BatchDate, Locat) VALUES (?, ?, ?)",
                table1.getId(), new java.sql.Date(table1.getDate().getTime()), table1.getLocation());
    }

    public void insertIntoTable2(TransactionHeader table2) {
        jdbcTemplate.update("INSERT INTO txnHeader (TranRefNo, Date, TxnAmount) VALUES (?, ?, ?)",
                table2.getId(), new java.sql.Date(table2.getDate().getTime()), table2.getAmount());
    }

    public void insertIntoTable3(TransactionItem table3) {
        jdbcTemplate.update("INSERT INTO txnItem (ItemNo, Description, Amount) VALUES (?, ?, ?)",
                table3.getId(), table3.getDescription(), table3.getAmount());
    }
}
