// MedicalHistory.java
package entity;

import java.util.List;

public class MedicalHistory {
    private List<String> allergies;
    private List<String> preExistingConditions;

    public MedicalHistory(List<String> allergies, List<String> preExistingConditions) {
        this.allergies = allergies;
        this.preExistingConditions = preExistingConditions;
    }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getPreExistingConditions() { return preExistingConditions; }
    public void setPreExistingConditions(List<String> preExistingConditions) { this.preExistingConditions = preExistingConditions; }
}
