package org.example.entity;

import lombok.Data;

@Data
public class TransactionItem {
    private int id;
    private String description;
    private double amount;
}
