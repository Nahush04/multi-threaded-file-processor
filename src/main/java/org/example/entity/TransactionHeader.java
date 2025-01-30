package org.example.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TransactionHeader {
    private int id;
    private Date date;
    private double amount;
}
