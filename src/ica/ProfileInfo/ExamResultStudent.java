package ica.ProfileInfo;

import android.graphics.Bitmap;

public class ExamResultStudent {
	String StudentCode;

	public String getStudentCode() {
		return StudentCode;
	}

	public void setStudentCode(String studentCode) {
		StudentCode = studentCode;
	}

	String Name;
	String ImgPath;
	Bitmap StudentImage;

	StudentAnsStatus AnsStatus;

	public enum StudentAnsStatus {

		Unattempted, Right, Wrong
	}

	

	String QuestionID;
	
	public String getQuestionID() {
		return QuestionID;
	}

	public void setQuestionID(String questionID) {
		QuestionID = questionID;
	}

	public StudentAnsStatus getAnsStatus() {
		return AnsStatus;
	}

	public void setAnsStatus(StudentAnsStatus ansStatus) {
		AnsStatus = ansStatus;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getImgPath() {
		return ImgPath;
	}

	public void setImgPath(String imgPath) {
		ImgPath = imgPath;
	}

	public Bitmap getStudentImage() {
		return StudentImage;
	}

	public void setStudentImage(Bitmap studentImage) {
		StudentImage = studentImage;
	}
}