package ica.ProfileInfo;

public class SubjectInfo {

	private String Name;
	private String CompletionStatus;

	private int Id;
	private double Marks;
	private double HiMarks;

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getCompletionStatus() {
		return CompletionStatus;
	}

	public void setCompletionStatus(String completionStatus) {
		CompletionStatus = completionStatus;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public double getMarks() {
		return Marks;
	}

	public void setMarks(double marks) {
		Marks = marks;
	}

	public double getHiMarks() {
		return HiMarks;
	}

	public void setHiMarks(double hiMarks) {
		HiMarks = hiMarks;
	}

}
