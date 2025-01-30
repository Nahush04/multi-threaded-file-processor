package org.example.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BatchHeader {
    private int id;
    private Date date;
    private String location;
}
