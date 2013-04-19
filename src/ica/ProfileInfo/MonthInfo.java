package ica.ProfileInfo;

public class MonthInfo {

	String MonthName;
	int MonthID;
	int MonthOfYear;

	public String getMonthName() {
		return MonthName;
	}

	public void setMonthName(String monthName) {
		MonthName = monthName;
	}

	public int getMonthID() {
		return MonthID;
	}

	public void setMonthID(int monthID) {
		MonthID = monthID;
	}

	public int getMonthOfYear() {
		return MonthOfYear;
	}

	public void setMonthOfYear(int monthOfYear) {
		MonthOfYear = monthOfYear;
	}

	@Override
	public String toString() {
		return "MonthInfo [MonthName=" + MonthName + ", MonthID=" + MonthID
				+ ", MonthOfYear=" + MonthOfYear + "]";
	}
}
