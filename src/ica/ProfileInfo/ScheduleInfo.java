package ica.ProfileInfo;

public class ScheduleInfo {
	
	int ID=-999;
	public int getScheduleID() {
		return ID;
	}

	public void setScheduleID(int iD) {
		ID = iD;
	}

	String UserName;
	int Year;
	int Month;
	int DayOfMonth;
	String NotificationType;
	String CompletionStatus="F";
	String IsSynced;
	
	
	public String getIsSynced() {
		return IsSynced;
	}

	public void setIsSynced(String isSynced) {
		IsSynced = isSynced;
	}


	String Message;

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public int getYear() {
		return Year;
	}

	public void setYear(int year) {
		Year = year;
	}

	public int getMonth() {
		return Month;
	}

	public void setMonth(int month) {
		Month = month;
	}

	public int getDayOfMonth() {
		return DayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		DayOfMonth = dayOfMonth;
	}

	public String getNotificationType() {
		return NotificationType;
	}

	public void setNotificationType(String notificationType) {
		NotificationType = notificationType;
	}

	public String getCompletionStatus() {
		return CompletionStatus;
	}

	public void setCompletionStatus(String completionStatus) {
		CompletionStatus = completionStatus;
	}


	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

}
