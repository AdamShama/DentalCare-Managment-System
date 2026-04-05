package entity;

public class Treatment {
    private int    TreatmentId;
    private String Name;
    private String Description;
    private double Cost;

    /** For loading existing treatments from the DB */
    public Treatment(int id, String name, String description, double cost) {
        this.TreatmentId          = id;
        this.Description        = name;
        this.Name = description;
        this.Cost        = cost;
    }

    /** For creating brand-new treatments (ID auto-assigned) */
    public Treatment(String name, String description, double cost) {
        this(-1, name, description, cost);
    }

	public int getTreatmentId() {
		return TreatmentId;
	}

	public void setTreatmentId(int treatmentId) {
		TreatmentId = treatmentId;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public double getCost() {
		return Cost;
	}

	public void setCost(double cost) {
		Cost = cost;
	}

    
}
