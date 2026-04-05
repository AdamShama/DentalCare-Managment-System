// DentalHistory.java
package entity;

import java.util.List;

public class DentalHistory {
    private List<String> pastTreatments;
    private List<String> xrayReferences;

    public DentalHistory(List<String> pastTreatments, List<String> xrayReferences) {
        this.pastTreatments = pastTreatments;
        this.xrayReferences = xrayReferences;
    }

    public List<String> getPastTreatments() { return pastTreatments; }
    public void setPastTreatments(List<String> pastTreatments) { this.pastTreatments = pastTreatments; }

    public List<String> getXrayReferences() { return xrayReferences; }
    public void setXrayReferences(List<String> xrayReferences) { this.xrayReferences = xrayReferences; }
}
