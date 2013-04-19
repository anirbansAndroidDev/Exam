package ica.Utility;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppInfo {

	public static ApplicationVersions versionInfo(Context context) {

		ApplicationVersions appVersion = null;
		try {

			String packageName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).packageName;

			String versionName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;

			int versionNumber = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;

			appVersion = new ApplicationVersions();

			appVersion.setPackageName(packageName);
			appVersion.setVersionName(versionName);
			appVersion.setVersionNumber(versionNumber);

		} catch (NameNotFoundException e) {

			e.printStackTrace();
		} catch (Exception e) {

		}

		return appVersion;
	}

}
