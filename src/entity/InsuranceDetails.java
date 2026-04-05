// InsuranceDetails.java
package entity;

public class InsuranceDetails {
    private String providerName;
    private String policyNumber;

    public InsuranceDetails(String providerName, String policyNumber) {
        this.providerName = providerName;
        this.policyNumber = policyNumber;
    }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
}
