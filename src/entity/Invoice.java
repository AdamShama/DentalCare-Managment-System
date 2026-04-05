// Invoice.java
package entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Invoice {
    private String id;
    private TreatmentPlan plan;
    private BigDecimal amount;
    private boolean paid;
    private LocalDate issuedDate;

    public Invoice(String id, TreatmentPlan plan, BigDecimal amount, LocalDate issuedDate) {
        this.id = id;
        this.plan = plan;
        this.amount = amount;
        this.issuedDate = issuedDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TreatmentPlan getPlan() { return plan; }
    public void setPlan(TreatmentPlan plan) { this.plan = plan; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
}
