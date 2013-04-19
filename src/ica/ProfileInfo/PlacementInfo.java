package ica.ProfileInfo;

import android.graphics.Bitmap;

public class PlacementInfo {

	int ID;
	String studentCode;
	String studentPhotoUrl;
	
	public String getStudentPhotoUrl() {
		return studentPhotoUrl;
	}

	public void setStudentPhotoUrl(String studentPhotoUrl) {
		this.studentPhotoUrl = studentPhotoUrl;
	}

	String placedStudentName;
	String centerCode;
	String employerName;
	double Salary;
	int year;
	int month;
	int day;
	String contactPersonName;

	Bitmap photobmp=null;
	
	
	public Bitmap getPhotobmp() {
		return photobmp;
	}

	public void setPhotobmp(Bitmap photobmp) {
		this.photobmp = photobmp;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getStudentCode() {
		return studentCode;
	}

	public void setStudentCode(String studentCode) {
		this.studentCode = studentCode;
	}

	public String getPlacedStudentName() {
		return placedStudentName;
	}

	public void setPlacedStudentName(String placedStudentName) {
		this.placedStudentName = placedStudentName;
	}

	public String getCenterCode() {
		return centerCode;
	}

	public void setCenterCode(String centerCode) {
		this.centerCode = centerCode;
	}

	public String getEmployerName() {
		return employerName;
	}

	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	public double getSalary() {
		return Salary;
	}

	public void setSalary(double salary) {
		Salary = salary;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getContactPersonName() {
		return contactPersonName;
	}

	public void setContactPersonName(String contactPersonName) {
		this.contactPersonName = contactPersonName;
	}

}
