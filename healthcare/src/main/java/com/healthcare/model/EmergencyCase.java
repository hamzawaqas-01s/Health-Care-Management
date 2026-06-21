package com.healthcare.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a hospital emergency case.
 *
 * <p>OOP concepts demonstrated:
 * <ul>
 *   <li><b>Encapsulation</b> – all fields are private; accessed only through
 *       getters/setters.</li>
 *   <li><b>Inheritance</b> – not applicable here directly, but EmergencyCase
 *       <em>composes</em> {@link Patient} and {@link Doctor}, and it is
 *       referenced by {@link Appointment} (via Status.EMERGENCY), showing
 *       real-world association/composition.</li>
 *   <li><b>Polymorphism</b> – {@code getSeverityLabel()} behaves differently
 *       for each {@link Severity} value (enum-based polymorphism).</li>
 * </ul>
 *
 * <p>Required by PBL Deliverable 1, item 4:
 * "Identify classes: Patient, Doctor, Appointment, <b>emergencyCase</b>"
 */
public class EmergencyCase {

    // ─────────────────────────────────────────────────────────────
    //  Severity levels – drives prioritisation (F4)
    // ─────────────────────────────────────────────────────────────
    public enum Severity {
        CRITICAL(1, "🔴 Critical"),
        HIGH    (2, "🟠 High"),
        MODERATE(3, "🟡 Moderate"),
        LOW     (4, "🟢 Low");

        private final int priority;   // lower = more urgent
        private final String label;

        Severity(int priority, String label) {
            this.priority = priority;
            this.label    = label;
        }

        public int    getPriority() { return priority; }
        public String getLabel()    { return label; }
    }

    // ─────────────────────────────────────────────────────────────
    //  Status of the emergency case
    // ─────────────────────────────────────────────────────────────
    public enum CaseStatus {
        ACTIVE,
        STABILISED,
        RESOLVED,
        REFERRED
    }

    // ─────────────────────────────────────────────────────────────
    //  Private fields  (Encapsulation)
    // ─────────────────────────────────────────────────────────────
    private final int           caseId;
    private final Patient       patient;
    private       Doctor        assignedDoctor;
    private final LocalDateTime arrivalTime;
    private       Severity      severity;
    private       CaseStatus    caseStatus;
    private       String        chiefComplaint;   // reason patient arrived
    private       String        treatmentNotes;
    private       LocalDateTime resolvedTime;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm");

    // ─────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────
    public EmergencyCase(int caseId, Patient patient, Doctor assignedDoctor,
                         Severity severity, String chiefComplaint) {
        this.caseId         = caseId;
        this.patient        = patient;
        this.assignedDoctor = assignedDoctor;
        this.severity       = severity;
        this.chiefComplaint = chiefComplaint;
        this.arrivalTime    = LocalDateTime.now();
        this.caseStatus     = CaseStatus.ACTIVE;
        this.treatmentNotes = "";

        // Mark the patient as an emergency in Patient record too
        if (patient != null) {
            patient.setEmergency(true);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Business logic
    // ─────────────────────────────────────────────────────────────

    /**
     * Resolves the emergency case and records the resolution time.
     * Demonstrates a domain-specific behaviour method (not just a setter).
     */
    public void resolve(String finalNotes) {
        this.caseStatus    = CaseStatus.RESOLVED;
        this.resolvedTime  = LocalDateTime.now();
        this.treatmentNotes = finalNotes;
        if (patient != null) patient.setEmergency(false);
    }

    /** Stabilise without full resolution (e.g. transferred to ward). */
    public void stabilise(String notes) {
        this.caseStatus    = CaseStatus.STABILISED;
        this.treatmentNotes = notes;
    }

    /** Returns a human-readable severity label (enum-based polymorphism). */
    public String getSeverityLabel() {
        return severity.getLabel();
    }

    /** Returns how long the case has been open (in minutes). */
    public long getElapsedMinutes() {
        LocalDateTime end = (resolvedTime != null) ? resolvedTime : LocalDateTime.now();
        return java.time.Duration.between(arrivalTime, end).toMinutes();
    }

    public String getFormattedArrivalTime() {
        return arrivalTime.format(FMT);
    }

    // ─────────────────────────────────────────────────────────────
    //  Getters / Setters  (Encapsulation)
    // ─────────────────────────────────────────────────────────────
    public int           getCaseId()          { return caseId; }
    public Patient       getPatient()         { return patient; }
    public Doctor        getAssignedDoctor()  { return assignedDoctor; }
    public LocalDateTime getArrivalTime()     { return arrivalTime; }
    public Severity      getSeverity()        { return severity; }
    public CaseStatus    getCaseStatus()      { return caseStatus; }
    public String        getChiefComplaint()  { return chiefComplaint; }
    public String        getTreatmentNotes()  { return treatmentNotes; }
    public LocalDateTime getResolvedTime()    { return resolvedTime; }

    public void setAssignedDoctor(Doctor doctor)    { this.assignedDoctor = doctor; }
    public void setSeverity(Severity severity)       { this.severity       = severity; }
    public void setCaseStatus(CaseStatus status)     { this.caseStatus     = status; }
    public void setChiefComplaint(String complaint)  { this.chiefComplaint = complaint; }
    public void setTreatmentNotes(String notes)      { this.treatmentNotes = notes; }

    @Override
    public String toString() {
        return String.format("EmergencyCase[id=%d, patient=%s, severity=%s, status=%s]",
            caseId,
            patient  != null ? patient.getName()  : "N/A",
            severity.name(),
            caseStatus.name());
    }
}
