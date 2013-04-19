package ica.ProfileInfo;

import ica.ICAConstants.ActionStatus;
import ica.exam.R;

public class StatusMessage {

	private String Message;
	private String Title;
	private ActionStatus actionStatus = ActionStatus.None;
	private int iconValue = R.drawable.information;

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public ActionStatus getActionStatus() {
		return actionStatus;
	}

	public void setActionStatus(ActionStatus actionStatus) {
		this.actionStatus = actionStatus;
	}

	public int getIconValue() {
		return iconValue;
	}

	public void setIconValue(int iconValue) {
		this.iconValue = iconValue;
	}
}
