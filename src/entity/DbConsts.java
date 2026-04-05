package entity;

import java.io.File;
import java.net.URLDecoder;

public final class DbConsts {
    private DbConsts() {
        throw new AssertionError("no instances");
    }

    private static final String DB_FILEPATH = getDBPath();
    public static final String CONN_STR =
            "jdbc:ucanaccess://" + DB_FILEPATH + ";COLUMNORDER=DISPLAY";

    public static final String SQL_IMPORT_INVENTORY_XML =
            "{ call qryImportInventoryXML(?) }";

    public static final String SQL_EXPORT_INVENTORY_XML =
            "{ call qryExportInventoryXML(?) }";

    public static final String TABLE_PATIENTS = "Patients";
    public static final String TABLE_STAFF = "Staff";
    public static final String TABLE_ROLES = "Roles";
    public static final String TABLE_APPOINTMENTS = "Appointments";
    public static final String TABLE_APPOINTMENT_REMINDERS = "AppointmentReminders";
    public static final String TABLE_TREATMENT_PLANS = "TreatmentPlans";
    public static final String TABLE_TREATMENT_PLAN_STAFF = "TreatmentPlanStaff";
    public static final String TABLE_INSURANCE_DETAILS = "InsuranceDetails";
    public static final String TABLE_MEDICAL_HISTORY = "MedicalHistory";
    public static final String TABLE_DENTAL_HISTORY = "DentalHistory";
    public static final String TABLE_TREATMENTS = "Treatments";
    public static final String TABLE_TREATMENT_REPORTS = "TreatmentReports";
    public static final String TABLE_INVOICES = "Invoices";
    public static final String TABLE_INVENTORY = "InventoryItems";
    public static final String TABLE_SUPPLIERS = "Suppliers";

    public static final String COL_PATIENT_ID = "PatientID";
    public static final String COL_PATIENT_FULLNAME = "FullName";
    public static final String COL_PATIENT_PHONE = "PhoneNumber";
    public static final String COL_PATIENT_EMAIL = "Email";
    public static final String COL_PATIENT_AGE = "Age";
    public static final String COL_PATIENT_INSURANCE = "InsuranceID";
    public static final String COL_PATIENT_DENTIST = "DentistID";

    public static final String COL_STAFF_ROLE_ID = "RoleID";

    public static final String COL_ROLE_ID = "RoleID";
    public static final String COL_ROLE_NAME = "RoleName";

    public static final String COL_APPT_ID = "AppointmentID";
    public static final String COL_APPT_PATIENT_ID = "PatientID";
    public static final String COL_APPT_STAFF_ID = "StaffID";
    public static final String COL_APPT_DATETIME = "AppointmentDate";
    public static final String COL_APPT_REASON = "VisitReason";
    public static final String COL_APPT_STATUS = "Status";
    public static final String COL_APPT_STERILIZED = "Sterilized";
    public static final String COL_APPT_PAID = "Paid";
    public static final String COL_APPT_REFUNDED = "Refunded";

    public static final String COL_REMINDER_ID = "ReminderID";
    public static final String COL_REMINDER_APPT_ID = "AppointmentID";
    public static final String COL_REMINDER_METHOD = "ReminderMethod";
    public static final String COL_REMINDER_DATE = "ReminderDate";

    public static final String COL_PLAN_ID = "PlanID";
    public static final String COL_PLAN_PATIENT_ID = "PatientID";
    public static final String COL_PLAN_START_DATE = "StartDate";
    public static final String COL_PLAN_END_DATE = "EndDate";
    public static final String COL_PLAN_STATUS = "PlanStatus";

    public static final String COL_TPS_ID = "treatmentPlanStaffID";
    public static final String COL_TPS_STAFF_ID = "StaffID";
    public static final String COL_TPS_ASSIGNED_DATE = "AssignedDate";
    public static final String COL_TPS_PATIENT_ID = "PatientID";
    public static final String COL_TPS_TREATMENT_ID = "TreatmentID";
    public static final String COL_TPS_PLAN_ID = "PlanID";

    public static final String COL_TREAT_ID = "TreatmentID";
    public static final String COL_TREAT_COST = "Cost";
    public static final String COL_TREAT_STAFF_ID = "AssignedStaffID";
    public static final String COL_TREAT_TYPE = "TreatmentType";
    public static final String COL_TREAT_PHASE = "Phase";
    public static final String COL_TREAT_PLAN_ID = "PlanID";
    public static final String COL_TREAT_APPT_ID = "AppointmentID";

    public static final String COL_REPORT_ID = "ReportID";
    public static final String COL_REPORT_TREAT_ID = "TreatmentID";
    public static final String COL_REPORT_PATIENT_ID = "PatientID";
    public static final String COL_REPORT_DATE = "ReportDate";
    public static final String COL_REPORT_NOTES = "Notes";

    public static final String COL_INV_ITEM_ID = "ItemID";
    public static final String COL_INV_NAME = "ItemName";
    public static final String COL_INV_DESC = "Description";
    public static final String COL_INV_CATEGORY = "CategoryID";
    public static final String COL_INV_QTY = "QuantityInStock";
    public static final String COL_INV_SUPPLIER = "SupplierID";
    public static final String COL_INV_EXPIRY = "ExpirationDate";
    public static final String COL_INV_THRESHOLD = "ReorderThreshold";

    public static final String COL_INV_ID = "InvoiceID";
    public static final String COL_INV_PLAN_ID = "PlanID";
    public static final String COL_INV_AMOUNT = "Amount";
    public static final String COL_INV_PAID = "Paid";
    public static final String COL_INV_DATE = "IssuedDate";

    public static final String COL_SUPPLIER_ID = "SupplierID";
    public static final String COL_SUPPLIER_NAME = "SupplierName";
    public static final String COL_SUPPLIER_CONTACT = "ContactDetails";

    public static final String SQL_SEL_PATIENTS = "SELECT * FROM " + TABLE_PATIENTS;
    public static final String SQL_SEL_STAFF = "SELECT * FROM " + TABLE_STAFF;
    public static final String SQL_SEL_ROLES = "SELECT * FROM " + TABLE_ROLES;
    public static final String SQL_SEL_APPOINTMENTS = "SELECT * FROM " + TABLE_APPOINTMENTS;
    public static final String SQL_SEL_APPT_REMINDERS = "SELECT * FROM " + TABLE_APPOINTMENT_REMINDERS;
    public static final String SQL_SEL_TREATMENT_PLANS = "SELECT * FROM " + TABLE_TREATMENT_PLANS;
    public static final String SQL_SEL_PLAN_STAFF = "SELECT * FROM " + TABLE_TREATMENT_PLAN_STAFF;
    public static final String SQL_SEL_INSURANCE = "SELECT * FROM " + TABLE_INSURANCE_DETAILS;
    public static final String SQL_SEL_MEDICAL_HISTORY = "SELECT * FROM " + TABLE_MEDICAL_HISTORY;
    public static final String SQL_SEL_DENTAL_HISTORY = "SELECT * FROM " + TABLE_DENTAL_HISTORY;
    public static final String SQL_SEL_TREATMENTS = "SELECT * FROM " + TABLE_TREATMENTS;
    public static final String SQL_SEL_TREATMENT_REPORTS = "SELECT * FROM " + TABLE_TREATMENT_REPORTS;
    public static final String SQL_SEL_INVOICES = "SELECT * FROM " + TABLE_INVOICES;

    public static final String SQL_SEL_INVENTORY = "SELECT "
            + COL_INV_ITEM_ID + ", "
            + COL_INV_NAME + ", "
            + COL_INV_DESC + ", "
            + COL_INV_CATEGORY + ", "
            + COL_INV_QTY + ", "
            + COL_INV_SUPPLIER + ", "
            + COL_INV_EXPIRY + ", "
            + COL_INV_THRESHOLD
            + " FROM " + TABLE_INVENTORY;

    public static final String COL_STAFF_ID = "StaffID";
    public static final String COL_STAFF_FULLNAME = "FullName";
    public static final String COL_STAFF_ROLE = "Role";
    public static final String COL_STAFF_CONTACT = "ContactNumber";
    public static final String COL_STAFF_EMAIL = "Email";
    public static final String COL_STAFF_QUALIFICATION = "Qualification";
    public static final String COL_STAFF_SPECIALTY = "Specialty";
    public static final String COL_STAFF_IS_MANAGER = "IsManager";
    public static final String COL_STAFF_SPECIALIZATION_ID = "SpecializationID";

    public static final String TABLE_INSURANCE = "InsuranceDetails";
    public static final String COL_INSURANCE_ID = "InsuranceID";
    public static final String COL_APPT_DATE = "AppointmentDate";

    private static String getDBPath() {
        try {
            File currentDir = new File(System.getProperty("user.dir"));
            File classPathBase = getClassPathBase();

            File[] candidates = new File[] {
                    new File(currentDir, "database/DataBase99.accdb"),
                    new File(currentDir, "DataBase99.accdb"),
                    new File(classPathBase, "database/DataBase99.accdb"),
                    new File(classPathBase, "DataBase99.accdb")
            };

            System.out.println("currentDir = " + currentDir.getAbsolutePath());
            System.out.println("classPathBase = " + classPathBase.getAbsolutePath());

            for (File dbFile : candidates) {
                System.out.println("Trying DB path: " + dbFile.getAbsolutePath());
                System.out.println("DB exists: " + dbFile.exists());
                if (dbFile.exists()) {
                    return dbFile.getAbsolutePath();
                }
            }

            throw new RuntimeException("Database file not found.");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to resolve database path", ex);
        }
    }

    private static File getClassPathBase() {
        try {
            String classPath = System.getProperty("java.class.path");
            if (classPath == null || classPath.isEmpty()) {
                return new File(System.getProperty("user.dir"));
            }

            String firstEntry = classPath.split(File.pathSeparator)[0];
            String decoded = URLDecoder.decode(firstEntry, "UTF-8");
            File cpFile = new File(decoded);

            if (cpFile.isFile()) {
                return cpFile.getParentFile();
            }

            if (cpFile.isDirectory()) {
                return cpFile;
            }

            return new File(System.getProperty("user.dir"));
        } catch (Exception e) {
            return new File(System.getProperty("user.dir"));
        }
    }
}