// TreatmentReport.java
package entity;

import java.time.LocalDateTime;

public class TreatmentReport {
    private int reportId;
    private int treatmentId;
    private int patientId;
    private LocalDateTime reportDate;
    private String notes;

    public TreatmentReport(int reportId,
                           int treatmentId,
                           int patientId,
                           LocalDateTime reportDate,
                           String notes) {
        this.reportId    = reportId;
        this.treatmentId = treatmentId;
        this.patientId   = patientId;
        this.reportDate  = reportDate;
        this.notes       = notes;
    }

    public int getReportId() {
        return reportId;
    }
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getTreatmentId() {
        return treatmentId;
    }
    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public int getPatientId() {
        return patientId;
    }
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }
    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
