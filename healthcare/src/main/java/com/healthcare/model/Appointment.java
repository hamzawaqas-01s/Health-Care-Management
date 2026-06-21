package com.healthcare.model;

import java.time.LocalDate;

public class Appointment {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED, EMERGENCY }

    private int id;
    private Patient patient;
    private Doctor doctor;
    private LocalDate date;
    private String timeSlot;
    private Status status;
    private String notes;

    public Appointment(int id, Patient patient, Doctor doctor, LocalDate date, String timeSlot) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = Status.SCHEDULED;
    }

    public int getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getDate() { return date; }
    public String getTimeSlot() { return timeSlot; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }
    public void setStatus(Status status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}
