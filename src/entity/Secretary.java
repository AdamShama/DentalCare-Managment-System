// Secretary.java
package entity;

import java.util.List;

public class Secretary extends Staff {
    public Secretary(String id, String name, String contact, List<Appointment> schedule) {
        super(id, name, contact, schedule);
    }
    // no extra fields
}
