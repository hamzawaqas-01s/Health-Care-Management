package com.healthcare.model;

import java.util.ArrayList;
import java.util.List;

public class Patient extends User {
    private String bloodGroup;
    private int age;
    private List<String> medicalHistory;
    private boolean emergency;

    public Patient(int id, String name, String email, String phone, String bloodGroup, int age) {
        super(id, name, email, phone);
        this.bloodGroup = bloodGroup;
        this.age = age;
        this.medicalHistory = new ArrayList<>();
        this.emergency = false;
    }

    public String getBloodGroup() { return bloodGroup; }
    public int getAge() { return age; }
    public List<String> getMedicalHistory() { return medicalHistory; }
    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }
    public void addMedicalHistory(String record) { medicalHistory.add(record); }

    @Override
    public String getRole() { return "Patient"; }
}
