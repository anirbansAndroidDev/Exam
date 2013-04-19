package ica.ProfileInfo;

public class QuestionDetails {

	private String ID;
	private String Text;
	private boolean IsChecked;
	private int RightPercentage;
	private int WrongPercentage;
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getText() {
		return Text;
	}

	public void setText(String text) {
		Text = text;
	}

	public boolean isChecked() {
		return IsChecked;
	}

	public void setIsChecked(boolean isChecked) {
		IsChecked = isChecked;
	}

	public int getRightPercentage() {
		return RightPercentage;
	}

	public void setRightPercentage(int rightPercentage) {
		RightPercentage = rightPercentage;
	}

	public int getWrongPercentage() {
		return WrongPercentage;
	}

	public void setWrongPercentage(int wrongPercentage) {
		WrongPercentage = wrongPercentage;
	}

}
