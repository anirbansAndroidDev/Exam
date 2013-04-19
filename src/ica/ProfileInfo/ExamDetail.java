package ica.ProfileInfo;

public class ExamDetail {

	private String subjectName;
	private int SubjectID;
	private double subjectMarks;
	
	private MonthInfo examMonthInfo;
	
	
	public MonthInfo getExamMonthInfo() {
		return examMonthInfo;
	}
	
	public void setExamMonthInfo(MonthInfo examMonthInfo) {
		this.examMonthInfo = examMonthInfo;
	}
	
	public int getSubjectID() {
		return SubjectID;
	}
	
	public void setSubjectID(int subjectID) {
		SubjectID = subjectID;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	public double getSubjectMarks() {
		return subjectMarks;
	}
	public void setSubjectMarks(double subjectMarks) {
		this.subjectMarks = subjectMarks;
	}
	@Override
	public String toString() {
		return "ExamDetail [subjectName=" + subjectName + ", subjectMarks="
				+ subjectMarks + "]";
	}	
}
