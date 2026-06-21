package com.healthcare.service;

import com.healthcare.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class DataStore {
    private static DataStore instance;
    private List<Patient>     patients     = new ArrayList<>();
    private List<Doctor>      doctors      = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();
    private List<Admin>       admins       = new ArrayList<>();
    private static final Pattern BLOOD_GROUP_PATTERN = Pattern.compile("^(A|B|AB|O)[+-]$");


    // Registered patient credentials: email → password
    private java.util.Map<String, String> patientCredentials = new java.util.HashMap<>();

    private int patientIdCounter     = 100;
    private int appointmentIdCounter = 1000;
    private List<EmergencyCase> emergencyCases = new ArrayList<>();
    private int emergencyCaseIdCounter = 5000;

    // File path for persisting patients
    private static final String PATIENTS_FILE = "patients.txt";

    private DataStore() {
        seedAdminAndDoctors();
        loadPatientsFromFile();
        loadAppointmentsFromFile();
    }

    public static DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    // ──────────────────────────────────────────────
    //  Seed — only admin and doctors (hospital-determined)
    // ──────────────────────────────────────────────
    private void seedAdminAndDoctors() {
        // Single admin — determined by the hospital
        admins.add(new Admin(1, "Admin User", "admin@hospital.com", "0300-0000001", "ADMIN2024"));

        // Doctors
        doctors.add(new Doctor(1, "Dr. Aisha Raza",  "aisha@hospital.com", "0300-1111111", "Cardiologist", "Mon-Fri"));
        doctors.add(new Doctor(2, "Dr. Bilal Malik",  "bilal@hospital.com", "0300-2222222", "Neurologist",  "Tue-Sat"));
        doctors.add(new Doctor(3, "Dr. Sara Khan",    "sara@hospital.com",  "0300-3333333", "Pediatrician", "Mon-Thu"));
        doctors.add(new Doctor(4, "Dr. Omar Farooq",  "omar@hospital.com",  "0300-4444444", "Orthopedic",   "Wed-Sun"));
    }

    // ──────────────────────────────────────────────
    //  File persistence for patients
    // ──────────────────────────────────────────────

    /**
     * Saves all patients to patients.txt.
     * Format (one patient per line):
     *   id|name|email|phone|bloodGroup|age|password|medicalHistory1;medicalHistory2;...
     */
    public void savePatientsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient p : patients) {
                String password = patientCredentials.getOrDefault(p.getEmail().toLowerCase(), "");
                String history  = String.join(";", p.getMedicalHistory());
                pw.println(p.getId()         + "|" +
                           p.getName()       + "|" +
                           p.getEmail()      + "|" +
                           p.getPhone()      + "|" +
                           p.getBloodGroup() + "|" +
                           p.getAge()        + "|" +
                           password          + "|" +
                           history);
            }
        } catch (IOException e) {
            System.err.println("Could not save patients: " + e.getMessage());
        }
    }

    /**
     * Loads patients from patients.txt on startup.
     * Skips  lines silently.
     */
    private void loadPatientsFromFile() {
        File file = new File(PATIENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 7) continue;
                try {
                    int    id       = Integer.parseInt(parts[0]);
                    String name     = parts[1];
                    String email    = parts[2];
                    String phone    = parts[3];
                    String bg       = parts[4];
                    int    age      = Integer.parseInt(parts[5]);
                    String password = parts[6];
                    String histStr  = parts.length > 7 ? parts[7] : "";

                    Patient p = new Patient(id, name, email, phone, bg, age);
                    if (!histStr.isEmpty()) {
                        for (String h : histStr.split(";")) {
                            if (!h.isBlank()) p.addMedicalHistory(h);
                        }
                    }
                    patients.add(p);
                    patientCredentials.put(email.toLowerCase(), password);

                    // Keep counter ahead of loaded IDs
                    if (id >= patientIdCounter) patientIdCounter = id;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Could not load patients: " + e.getMessage());
        }
    }

    // Add these fields
    private static final String APPOINTMENTS_FILE = "appointments.txt";

    // New: Save Appointments
    public void saveAppointmentsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                pw.println(a.getId() + "|" +
                        a.getPatient().getId() + "|" +
                        a.getDoctor().getId() + "|" +
                        a.getDate() + "|" +
                        a.getTimeSlot() + "|" +
                        a.getStatus() + "|" +
                        (a.getNotes() != null ? a.getNotes() : ""));
            }
        } catch (IOException e) {
            System.err.println("Could not save appointments: " + e.getMessage());
        }
    }

    // New: Load Appointments
    private void loadAppointmentsFromFile() {
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                try {
                    int id = Integer.parseInt(parts[0]);
                    int patientId = Integer.parseInt(parts[1]);
                    int doctorId = Integer.parseInt(parts[2]);
                    LocalDate date = LocalDate.parse(parts[3]);
                    String timeSlot = parts[4];
                    Appointment.Status status = Appointment.Status.valueOf(parts[5]);
                    String notes = parts.length > 6 ? parts[6] : null;

                    Patient patient = patients.stream()
                            .filter(p -> p.getId() == patientId)
                            .findFirst().orElse(null);

                    Doctor doctor = doctors.stream()
                            .filter(d -> d.getId() == doctorId)
                            .findFirst().orElse(null);

                    if (patient != null && doctor != null) {
                        Appointment appt = new Appointment(id, patient, doctor, date, timeSlot);
                        appt.setStatus(status);
                        if (notes != null && !notes.isEmpty()) {
                            appt.setNotes(notes);
                        }
                        appointments.add(appt);

                        if (id >= appointmentIdCounter) {
                            appointmentIdCounter = id;
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Could not load appointments: " + e.getMessage());
        }
    }

// Also call save in status updates if needed (optional enhancement)

    // ──────────────────────────────────────────────
    //  Auth helpers
    // ──────────────────────────────────────────────
    public Admin loginAdmin(String email, String password) {
        return admins.stream()
            .filter(a -> a.getEmail().equalsIgnoreCase(email) && a.getAdminCode().equals(password))
            .findFirst().orElse(null);
    }

    public Patient loginPatient(String email, String password) {
        String stored = patientCredentials.get(email.toLowerCase());
        if (stored != null && stored.equals(password)) {
            return patients.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
        }
        return null;
    }

    /** Registers a new patient, saves to file, returns false on duplicate email. */
    public boolean registerPatient(Patient p, String password) {
        String key = p.getEmail().toLowerCase();

        if (p.getAge() > 120) throw new RuntimeException("Invalid age.");
        validatePhone(p.getPhone());
        validateEmail(p.getEmail());
        validateBloodGroup(p.getBloodGroup());

        if (patientCredentials.containsKey(key)) return false;
        patients.add(p);
        patientCredentials.put(key, password);
        savePatientsToFile();
        return true;
    }

    public static void validatePhone(String phone) {
        final Pattern PHONE_PATTERN = Pattern.compile("^\\d{11}$");
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + phone);
        }
    }

    public static void validateBloodGroup(String bloodGroup) {
        if (bloodGroup == null || !BLOOD_GROUP_PATTERN.matcher(bloodGroup).matches()) {
            throw new IllegalArgumentException("Invalid blood group: " + bloodGroup);
        }
    }

    public static void validateEmail(String email)
    {
        if (email == null || !email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }

    // ──────────────────────────────────────────────
    //  Getters / mutations
    // ─────────────────────────────────────────────
    public List<Patient>     getPatients()     { return patients; }
    public List<Doctor>      getDoctors()      { return doctors; }
    public List<Appointment> getAppointments() { return appointments; }
    public List<Admin>       getAdmins()       { return admins; }
    public List<EmergencyCase> getEmergencyCases() { return emergencyCases; }

    public List<EmergencyCase> getEmergencyCasesSorted() {
        return emergencyCases.stream()
                .sorted(Comparator.comparingInt(ec -> ec.getSeverity().getPriority()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void addEmergencyCase(EmergencyCase e) { emergencyCases.add(e); }
    public int  nextEmergencyCaseId()             { return ++emergencyCaseIdCounter; }

    /** Adds a patient by the admin (from PatientsView) and persists immediately. */
    public void addPatient(Patient p) {
        patients.add(p);
        savePatientsToFile();
    }

    /** Adds a patient with a known password (admin-created) and persists. Returns false on duplicate email. */
    public boolean addPatientWithPassword(Patient p, String password) {
        String key = p.getEmail().toLowerCase();
        if (patientCredentials.containsKey(key)) return false;
        patients.add(p);
        patientCredentials.put(key, password);
        savePatientsToFile();
        return true;
    }

    public void addAppointment(Appointment a) {
        appointments.add(a);
        saveAppointmentsToFile();
    }

    public int  nextPatientId() { return ++patientIdCounter; }
    public int  nextAppointmentId() { return ++appointmentIdCounter; }

    public long countByStatus(Appointment.Status status) {
        return appointments.stream().filter(a -> a.getStatus() == status).count();
    }

    public long countEmergencies() {
        return emergencyCases.stream()
                .filter(ec -> ec.getCaseStatus() == EmergencyCase.CaseStatus.ACTIVE
                        || ec.getCaseStatus() == EmergencyCase.CaseStatus.STABILISED)
                .count();
    }
}
