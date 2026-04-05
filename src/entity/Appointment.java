package entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Appointment {
    private final int       appointmentID;
    private final int       patientID;
    private final String    FULLNAME;
    private final int       staffID;
    private final LocalDate date;
    private final LocalTime time;
    private final boolean   sterilized;
    private final String    visitReason;
    private final String    status;
    private final boolean   paid;
    private final boolean refunded;
    private final TreatmentType treatmentType;
    private final List<String>  procedures;
    public enum TreatmentType { ROUTINE, URGENT; }



    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_SUSPENDED = "Suspended";

    /** 10‑arg constructor used by mapRow(...) */
    public Appointment(int appointmentID,int patientID,String patientFULLNAME,int staffID,LocalDate date,LocalTime time,
boolean sterilized,String visitReason, String status, boolean paid, boolean refunded , TreatmentType treatmentType , List<String> procedures)
    {
        this.appointmentID = appointmentID;
        this.patientID     = patientID;
        this.FULLNAME   = patientFULLNAME;
        this.staffID       = staffID;
        this.date          = date;
        this.time          = time;
        this.sterilized    = sterilized;
        this.visitReason   = visitReason;
        this.status        = status;
        this.paid          = paid;
        this.refunded      = refunded;
        this.treatmentType = treatmentType;
        this.procedures    = procedures;
    }

    // — getters —

    public int        getAppointmentID() { return appointmentID; }
    public int        getPatientID()     { return patientID; }
    public String getFULLNAME() {return FULLNAME;}
	public int        getStaffID()       { return staffID; }
    public LocalDate  getDate()          { return date; }
    public LocalTime  getTime()          { return time; }
    public boolean    isSterilized()     { return sterilized; }
    public String     getVisitReason()   { return visitReason; }
    public String     getStatus()        { return status; }
    public boolean    isPaid()           { return paid; }
    public boolean    isRefunded()       {return refunded; }
	public TreatmentType getTreatmentType()   {return treatmentType;}
	public List<String> getProcedures()     {return procedures;}
    
}
