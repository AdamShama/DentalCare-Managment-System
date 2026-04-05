// src/entity/Supplier.java
package entity;

public class Supplier {
    private int supplierID;
    private String supplierName;
    private String contactDetails;

    // No-arg constructor for frameworks / controls
    public Supplier() {}

    // Constructor for reading from DB
    public Supplier(int supplierID, String supplierName, String contactDetails) {
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.contactDetails = contactDetails;
    }

    // Constructor for creating new suppliers (ID will be auto-generated)
    public Supplier(String supplierName, String contactDetails) {
        this(0, supplierName, contactDetails);
    }

    public int getSupplierID() {
        return supplierID;
    }
    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    public String getContactDetails() {
        return contactDetails;
    }
    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }
}
