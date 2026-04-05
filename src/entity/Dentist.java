// Dentist.java
package entity;

import java.util.List;

public class Dentist extends Staff {
    private String specialization;
    private boolean isManager;

    public Dentist(String id, String name, String contact,
                   String specialization, boolean isManager,
                   List<Appointment> schedule) {
        super(id, name, contact, schedule);
        this.specialization = specialization;
        this.isManager = isManager;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isManager() { return isManager; }
    public void setManager(boolean manager) { isManager = manager; }
}
