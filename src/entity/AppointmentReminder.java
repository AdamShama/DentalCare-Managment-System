// AppointmentReminder.java
package entity;

import java.time.LocalDateTime;

public class AppointmentReminder {
    private String id;
    private Appointment appointment;
    private String method;
    private LocalDateTime remindAt;

    public AppointmentReminder(String id, Appointment appointment,
                               String method, LocalDateTime remindAt) {
        this.id = id;
        this.appointment = appointment;
        this.method = method;
        this.remindAt = remindAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
}
