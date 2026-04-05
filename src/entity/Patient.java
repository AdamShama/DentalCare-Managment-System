package entity;

public class Patient {
    private String patientId;
    private String fullName;
    private int age;
    private String phoneNumber;
    private String email;
    private int insuranceID;   // matches DbConsts.COL_PATIENT_INSURANCE
    private String insuranceName;
    private int dentistID;     // matches DbConsts.COL_PATIENT_DENTIST

    // No-arg ctor needed if you map via setters
    public Patient() { }

    // Full arg ctor (optional, in case you map via constructor)
    public Patient(String patientId, String fullName, int age,
                   String phoneNumber, String email,
                   int insuranceID ,int dentistID) {
        this.patientId   = patientId;
        this.fullName    = fullName;
        this.age         = age;
        this.phoneNumber = phoneNumber;
        this.email       = email;
        this.insuranceID = insuranceID;
        this.dentistID   = dentistID;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public String getPatientId()    { return patientId;    }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName()     { return fullName;     }
    public void setFullName(String fullName)   { this.fullName = fullName; }

    public int getAge()             { return age;          }
    public void setAge(int age)     { this.age = age;      }

    public String getPhoneNumber()  { return phoneNumber;  }
    public void setPhoneNumber(String phone)   { this.phoneNumber = phone; }

    public String getEmail()        { return email;        }
    public void setEmail(String email)         { this.email = email; }

    /** Fix #1: expose the insuranceID column */
    public int getInsuranceID()     { return insuranceID;  }
    public void setInsuranceID(int id)           { this.insuranceID = id; }

    public int getDentistID()       { return dentistID;    }
    public void setDentistID(int id)             { this.dentistID = id;   }

	public String getInsuranceName() {return insuranceName;}
	public void setInsuranceName(String insuranceName)   {this.insuranceName = insuranceName;}
    

	@Override
	public String toString() {
	    // for example: show “123 – Jane Doe”
	    return getPatientId() + " – " + getFullName();
	}

    
}
