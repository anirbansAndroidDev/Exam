package ica.Utility;

import ica.ProfileInfo.StatusMessage;

public class FtpUpgradeInfo {

	private int isUpgradeAvailable = 0;
	private String ftpLoction;
	private StatusMessage upgradeStatus;

	public StatusMessage getUpgradeStatus() {
		return upgradeStatus;
	}

	public void setUpgradeStatus(StatusMessage upgradeStatus) {
		this.upgradeStatus = upgradeStatus;
	}

	public int getIsUpgradeAvailable() {
		return isUpgradeAvailable;
	}

	public void setIsUpgradeAvailable(int isUpgradeAvailable) {
		this.isUpgradeAvailable = isUpgradeAvailable;
	}

	public String getFtpLoction() {
		return ftpLoction;
	}

	public void setFtpLoction(String ftpLoction) {
		this.ftpLoction = ftpLoction;
	}

}
