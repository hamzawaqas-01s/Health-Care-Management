package com.healthcare.model;

public class Doctor extends User {
    private String specialization;
    private String availability;
    private int totalPatients;

    public Doctor(int id, String name, String email, String phone, String specialization, String availability) {
        super(id, name, email, phone);
        this.specialization = specialization;
        this.availability = availability;
        this.totalPatients = 0;
    }

    public String getSpecialization() { return specialization; }
    public String getAvailability() { return availability; }
    public int getTotalPatients() { return totalPatients; }
    public void incrementPatients() { totalPatients++; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override
    public String getRole() { return "Doctor"; }
}
