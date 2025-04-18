package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "benhan_table")
public class BenhAn {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String diagnosis;
    public String medicalHistory;
    public String labResults;
    public String allergies;
    public String currentMedications;
    public String diseaseStage;
}
