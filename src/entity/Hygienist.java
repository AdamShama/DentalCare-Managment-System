// Hygienist.java
package entity;

import java.util.List;

public class Hygienist extends Staff {
    public Hygienist(String id, String name, String contact, List<Appointment> schedule) {
        super(id, name, contact, schedule);
    }
    // no extra fields
}
