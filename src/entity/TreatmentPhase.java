// TreatmentPhase.java
package entity;

import java.math.BigDecimal;

public class TreatmentPhase {
    private String id;
    private String description;
    private BigDecimal cost;
    private Dentist assignedDentist;
    private Appointment appointment;

    public TreatmentPhase(String id, String description, BigDecimal cost,
                          Dentist assignedDentist, Appointment appointment) {
        this.id = id;
        this.description = description;
        this.cost = cost;
        this.assignedDentist = assignedDentist;
        this.appointment = appointment;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public Dentist getAssignedDentist() { return assignedDentist; }
    public void setAssignedDentist(Dentist assignedDentist) { this.assignedDentist = assignedDentist; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
}
