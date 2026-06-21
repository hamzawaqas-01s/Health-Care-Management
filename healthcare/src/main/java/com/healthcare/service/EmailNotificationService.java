package com.healthcare.service;

import com.healthcare.model.Appointment;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Sends HTML email notifications for appointment events.
 *
 * <p>All sends are performed on a background thread so the UI never blocks.
 *
 * <h3>Configuration</h3>
 * Before using this class, call {@link #configure(String, String, String, int, boolean)}
 * once (e.g. from MainApp or a settings screen) with your SMTP credentials.
 * The singleton is then ready for the lifetime of the application.
 *
 * <h3>Supported events</h3>
 * <ul>
 *   <li>{@link #sendBookingConfirmation(Appointment)} – sent when an appointment is booked</li>
 *   <li>{@link #sendCancellationNotice(Appointment)} – sent when an appointment is cancelled</li>
 *   <li>{@link #sendCompletionNotice(Appointment)}   – sent when an appointment is marked done</li>
 * </ul>
 *
 * <p>Each method sends one email to the patient and one to the doctor.
 */
public class EmailNotificationService {

    // ── Singleton ──────────────────────────────────────────────────────────

    private static EmailNotificationService instance;

    public static EmailNotificationService getInstance() {
        if (instance == null) instance = new EmailNotificationService();
        return instance;
    }

    // ── State ──────────────────────────────────────────────────────────────

    private static final Logger LOG = Logger.getLogger(EmailNotificationService.class.getName());

    private String  smtpHost     = "smtp.gmail.com";
    private int     smtpPort     = 587;
    private boolean useTls       = true;
    private String  senderEmail  = "";
    private String  senderPassword = "";
    private boolean configured   = false;

    /** Background executor — all SMTP work runs here, never on the FX thread. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "email-notification");
        t.setDaemon(true);
        return t;
    });

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    // ── Configuration ──────────────────────────────────────────────────────

    /**
     * Configures SMTP credentials.  Must be called once before sending.
     *
     * @param host         SMTP server hostname  (e.g. "smtp.gmail.com")
     * @param senderEmail  From-address / login  (e.g. "hospital@gmail.com")
     * @param password     SMTP password or App Password
     * @param port         SMTP port             (587 for STARTTLS, 465 for SSL)
     * @param useTls       {@code true} for STARTTLS (port 587), {@code false} for SSL (port 465)
     */
    public void configure(String host, String senderEmail, String password,
                          int port, boolean useTls) {
        this.smtpHost       = host;
        this.senderEmail    = senderEmail;
        this.senderPassword = password;
        this.smtpPort       = port;
        this.useTls         = useTls;
        this.configured     = true;
        LOG.info("[Email] Configured: " + host + ":" + port);
    }

    /** @return true if {@link #configure} has been called with non-empty credentials. */
    public boolean isConfigured() { return configured && !senderEmail.isBlank(); }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Sends a booking-confirmation email to patient and doctor (async). */
    public void sendBookingConfirmation(Appointment appt) {
        if (!isConfigured()) { LOG.warning("[Email] Not configured — skipping booking email."); return; }

        String dateStr = appt.getDate().format(DATE_FMT);
        boolean isEmergency = appt.getStatus() == Appointment.Status.EMERGENCY;

        // ── Patient email ──────────────────────────────────────────────────
        String patientSubject = isEmergency
                ? "⚠️ EMERGENCY Appointment Confirmed – " + appt.getDoctor().getName()
                : "✅ Appointment Confirmed – " + appt.getDoctor().getName();

        String patientBody = htmlTemplate(
                isEmergency ? "Emergency Appointment Confirmed" : "Appointment Confirmed",
                isEmergency ? "#dc2626" : "#0ea5e9",
                appt.getPatient().getName(),
                "<p>Your appointment has been successfully booked. Details below:</p>"
                + appointmentTable(appt, dateStr)
                + "<p style='margin-top:16px'>If you need to cancel, please contact us at least 24 hours in advance.</p>"
        );

        sendAsync(appt.getPatient().getEmail(), patientSubject, patientBody);

        // ── Doctor email ───────────────────────────────────────────────────
        String doctorSubject = isEmergency
                ? "⚠️ EMERGENCY: New appointment – " + appt.getPatient().getName()
                : "📋 New Appointment: " + appt.getPatient().getName();

        String doctorBody = htmlTemplate(
                isEmergency ? "Emergency Appointment Scheduled" : "New Appointment Scheduled",
                isEmergency ? "#dc2626" : "#7c3aed",
                appt.getDoctor().getName(),
                "<p>A new appointment has been scheduled for you:</p>"
                + appointmentTable(appt, dateStr)
        );

        sendAsync(appt.getDoctor().getEmail(), doctorSubject, doctorBody);
    }

    /** Sends a cancellation notice to patient and doctor (async). */
    public void sendCancellationNotice(Appointment appt) {
        if (!isConfigured()) { LOG.warning("[Email] Not configured — skipping cancellation email."); return; }

        String dateStr = appt.getDate().format(DATE_FMT);

        String patientBody = htmlTemplate(
                "Appointment Cancelled",
                "#dc2626",
                appt.getPatient().getName(),
                "<p>Your appointment has been <strong>cancelled</strong>. Details:</p>"
                + appointmentTable(appt, dateStr)
                + "<p style='margin-top:16px'>Please contact us to reschedule if needed.</p>"
        );
        sendAsync(appt.getPatient().getEmail(),
                "❌ Appointment Cancelled – " + appt.getDoctor().getName(), patientBody);

        String doctorBody = htmlTemplate(
                "Appointment Cancelled",
                "#dc2626",
                appt.getDoctor().getName(),
                "<p>The following appointment has been <strong>cancelled</strong>:</p>"
                + appointmentTable(appt, dateStr)
        );
        sendAsync(appt.getDoctor().getEmail(),
                "❌ Appointment Cancelled – " + appt.getPatient().getName(), doctorBody);
    }

    /** Sends a completion notice to patient and doctor (async). */
    public void sendCompletionNotice(Appointment appt) {
        if (!isConfigured()) { LOG.warning("[Email] Not configured — skipping completion email."); return; }

        String dateStr = appt.getDate().format(DATE_FMT);

        String patientBody = htmlTemplate(
                "Appointment Completed",
                "#16a34a",
                appt.getPatient().getName(),
                "<p>Your appointment has been marked as <strong>completed</strong>. Thank you for visiting!</p>"
                + appointmentTable(appt, dateStr)
                + (appt.getNotes() != null && !appt.getNotes().isBlank()
                        ? "<p style='margin-top:12px'><strong>Doctor's notes:</strong> " + escapeHtml(appt.getNotes()) + "</p>"
                        : "")
                + "<p style='margin-top:16px'>We hope to see you again. Stay healthy! 💚</p>"
        );
        sendAsync(appt.getPatient().getEmail(),
                "✅ Appointment Completed – " + appt.getDoctor().getName(), patientBody);

        String doctorBody = htmlTemplate(
                "Appointment Completed",
                "#16a34a",
                appt.getDoctor().getName(),
                "<p>The following appointment is now marked <strong>completed</strong>:</p>"
                + appointmentTable(appt, dateStr)
        );
        sendAsync(appt.getDoctor().getEmail(),
                "✅ Appointment Completed – " + appt.getPatient().getName(), doctorBody);
    }

    /** Sends a plain test email to verify SMTP configuration. */
    public void sendTestEmail(String to) {
        String body = htmlTemplate(
                "Email Configuration Test",
                "#0ea5e9",
                "Administrator",
                "<p>This is a test message from <strong>Al-Biruni Healthcare System</strong>.</p>"
                + "<p>If you received this email, your SMTP settings are configured correctly. ✅</p>"
        );
        sendAsync(to, "🧪 Al-Biruni — Email Test", body);
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    /** Dispatches a single HTML email on the background thread. */
    private void sendAsync(String to, String subject, String htmlBody) {
        executor.submit(() -> {
            try {
                Properties props = new Properties();
                if (useTls) {
                    props.put("mail.smtp.starttls.enable", "true");
                } else {
                    props.put("mail.smtp.ssl.enable", "true");
                }
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", String.valueOf(smtpPort));

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(senderEmail, "Al-Biruni Healthcare"));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                msg.setSubject(subject, "UTF-8");
                msg.setContent(htmlBody, "text/html; charset=UTF-8");
                Transport.send(msg);

                LOG.info("[Email] Sent '" + subject + "' → " + to);
            } catch (Exception ex) {
                LOG.warning("[Email] Failed to send to " + to + ": " + ex.getMessage());
            }
        });
    }

    /** Renders the appointment summary as an HTML table. */
    private String appointmentTable(Appointment appt, String dateStr) {
        return "<table style='width:100%;border-collapse:collapse;margin-top:12px;font-size:14px'>"
             + tableRow("Patient",        appt.getPatient().getName())
             + tableRow("Doctor",         appt.getDoctor().getName())
             + tableRow("Specialization", appt.getDoctor().getSpecialization())
             + tableRow("Date",           dateStr)
             + tableRow("Time",           appt.getTimeSlot())
             + tableRow("Status",         appt.getStatus().name())
             + tableRow("Appointment ID", "#" + appt.getId())
             + "</table>";
    }

    private String tableRow(String label, String value) {
        return "<tr>"
             + "<td style='padding:8px 12px;background:#f8fafc;border:1px solid #e2e8f0;"
             +     "font-weight:600;width:40%'>" + escapeHtml(label) + "</td>"
             + "<td style='padding:8px 12px;border:1px solid #e2e8f0'>" + escapeHtml(value) + "</td>"
             + "</tr>";
    }

    /**
     * Wraps content in a branded HTML email shell.
     *
     * @param heading     Card heading text
     * @param accentColor Hex colour for the top bar (e.g. "#0ea5e9")
     * @param recipientName Personalises the greeting
     * @param bodyContent Inner HTML (paragraphs, table, etc.)
     */
    private String htmlTemplate(String heading, String accentColor,
                                 String recipientName, String bodyContent) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body "
             + "style='margin:0;padding:0;background:#f1f5f9;font-family:Arial,sans-serif'>"
             + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f1f5f9;padding:32px 0'>"
             + "<tr><td align='center'>"
             + "<table width='600' cellpadding='0' cellspacing='0' "
             +     "style='background:#ffffff;border-radius:12px;overflow:hidden;"
             +     "box-shadow:0 2px 8px rgba(0,0,0,.08)'>"

             // ── Top accent bar ──────────────────────────────────────────
             + "<tr><td style='background:" + accentColor + ";padding:28px 32px'>"
             + "<h1 style='margin:0;color:#ffffff;font-size:22px;font-weight:700'>🏥 Al-Biruni Healthcare</h1>"
             + "<p  style='margin:4px 0 0;color:rgba(255,255,255,.85);font-size:13px'>"
             +     "Smart Healthcare Management System</p>"
             + "</td></tr>"

             // ── Body ────────────────────────────────────────────────────
             + "<tr><td style='padding:32px'>"
             + "<h2 style='margin:0 0 16px;font-size:18px;color:#1e293b'>" + escapeHtml(heading) + "</h2>"
             + "<p style='margin:0 0 12px;color:#475569'>Dear <strong>" + escapeHtml(recipientName) + "</strong>,</p>"
             + "<div style='color:#334155'>" + bodyContent + "</div>"
             + "</td></tr>"

             // ── Footer ──────────────────────────────────────────────────
             + "<tr><td style='background:#f8fafc;padding:20px 32px;border-top:1px solid #e2e8f0'>"
             + "<p style='margin:0;font-size:12px;color:#94a3b8'>"
             +     "This is an automated message from Al-Biruni Healthcare System. "
             +     "Please do not reply to this email."
             + "</p></td></tr>"

             + "</table></td></tr></table></body></html>";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
