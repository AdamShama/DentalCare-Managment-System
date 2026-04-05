// src/entity/Staff.java
package entity;

import java.util.List;

/**
 * Represents one row from the Staff table (and also serves
 * as the base for Dentist/Hygienist/Secretary domain objects).
 */
public class Staff {
    // ─── DB fields ────────────────────────────────────────────────────────────
    private final int    staffID;
    private final String fullName;
    private final int    roleID;
    private final String contactNumber;
    private final String email;
    private final String qualification;
    private final int    specializationID;

    // ─── Domain field (used by Dentist/Hygienist/Secretary) ─────────────────
    private final List<Appointment> schedule;

    /**
     * 7‑arg constructor for CRUD in StaffControl:
     * new Staff(int, String, int, String, String, String, int)
     */
    public Staff(
      int staffID,
      String fullName,
      int roleID,
      String contactNumber,
      String email,
      String qualification,
      int specializationID
    ) {
        this.staffID         = staffID;
        this.fullName        = fullName;
        this.roleID          = roleID;
        this.contactNumber   = contactNumber;
        this.email           = email;
        this.qualification   = qualification;
        this.specializationID= specializationID;
        this.schedule        = null;  // no schedule in generic CRUD usage
    }

    /**
     * 4‑arg constructor for domain objects (Dentist/Hygienist/Secretary):
     * super(String id, String name, String contact, List<Appointment> schedule)
     */
    public Staff(
      String id,
      String name,
      String contact,
      List<Appointment> schedule
    ) {
        // we parse the String id into our int staffID
        this.staffID         = Integer.parseInt(id);
        this.fullName        = name;
        this.roleID          = -1;    // unknown here
        this.contactNumber   = contact;
        this.email           = null;
        this.qualification   = null;
        this.specializationID= -1;
        this.schedule        = schedule;
    }

    // ─── Getters for DB columns ───────────────────────────────────────────────
    public int    getStaffID()        { return staffID; }
    public String getFullName()       { return fullName; }
    public int    getRoleID()         { return roleID; }
    public String getContactNumber()  { return contactNumber; }
    public String getEmail()          { return email; }
    public String getQualification()  { return qualification; }
    public int    getSpecializationID(){ return specializationID; }

    // ─── Getter for domain schedule ──────────────────────────────────────────
    public List<Appointment> getSchedule() { return schedule; }
}
