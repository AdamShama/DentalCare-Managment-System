// TreatmentPlan.java
package entity;

import java.time.LocalDate;
import java.util.List;

public class TreatmentPlan {
    public enum PlanStatus { ACTIVE, COMPLETED, CANCELLED }

    private String id;
    private Patient patient;
    private LocalDate startDate;
    private LocalDate estimatedCompletion;
    private PlanStatus status;
    private List<TreatmentPhase> phases;
    private Invoice invoice;

    public TreatmentPlan(String id, Patient patient, LocalDate startDate,
                         LocalDate estimatedCompletion) {
        this.id = id;
        this.patient = patient;
        this.startDate = startDate;
        this.estimatedCompletion = estimatedCompletion;
        this.status = PlanStatus.ACTIVE;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEstimatedCompletion() { return estimatedCompletion; }
    public void setEstimatedCompletion(LocalDate estimatedCompletion) { this.estimatedCompletion = estimatedCompletion; }

    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }

    public List<TreatmentPhase> getPhases() { return phases; }
    public void setPhases(List<TreatmentPhase> phases) { this.phases = phases; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}
