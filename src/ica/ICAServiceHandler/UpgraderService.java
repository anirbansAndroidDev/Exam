package ica.ICAServiceHandler;

import ica.ICAConstants.ActionStatus;
import ica.ProfileInfo.StatusMessage;
import ica.Utility.AppInfo;
import ica.Utility.ApplicationVersions;
import ica.Utility.FtpUpgradeInfo;
import ica.exam.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.content.Context;

public class UpgraderService {

	Context CurContext;

	public UpgraderService(Context context) {
		CurContext = context;
	}

	public FtpUpgradeInfo FetchIsUpgradeAvailable() {

		FtpUpgradeInfo upgradeInfo = new FtpUpgradeInfo();

		ApplicationVersions appVersionValue = AppInfo.versionInfo(CurContext);

		StatusMessage info = new StatusMessage();
		info.setActionStatus(ActionStatus.Exception);
		info.setTitle("Upgrader Status");
		info.setIconValue(R.drawable.information);

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.FTP_UPGRADE_METHOD_NAME));

			PropertyInfo inf_version = new PropertyInfo();
			inf_version.setName("versionCode");
			inf_version.setValue(appVersionValue.getVersionNumber());
			request.addProperty(inf_version);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {
			info.setActionStatus(ActionStatus.NoInternetConnection);
			info.setMessage("Connection error! Please check the connection and try it again.");
		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.FTP_UPGRADE_SOAP_ACTION),
					envelope);
		} catch (Exception e) {
			info.setActionStatus(ActionStatus.AuthenticationError);
			info.setMessage("Wifi Authentication failure:Wifi requires authentication.");
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setActionStatus(ActionStatus.DatatError);
			info.setMessage("Data Error:" + e.toString());
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				SoapObject upgradeInfoBlock = (SoapObject) rootBlock
						.getProperty(0);

				String isAvailable = upgradeInfoBlock.getAttributeAsString("available");
				String ftpPath = upgradeInfoBlock.getAttributeAsString("path");

				upgradeInfo.setFtpLoction(ftpPath);
				upgradeInfo.setIsUpgradeAvailable(Integer.parseInt(isAvailable));

				if (upgradeInfo.getIsUpgradeAvailable() == 0) {

					info.setMessage("No upgrade available:Your device is running the latest ICA Exam Apllication.");
					info.setActionStatus(ActionStatus.Successfull);

				} else if (upgradeInfo.getIsUpgradeAvailable() == 1) {

					info.setMessage("Upgrade available.This might take a few minutes...");
					info.setActionStatus(ActionStatus.Successfull);
				} else {
					info.setActionStatus(ActionStatus.DatatError);
					info.setMessage("No data available.");
				}

			} else {
				info.setActionStatus(ActionStatus.DatatError);
				info.setMessage("No data available.");
			}

		} catch (Exception e) {

			info.setMessage("Data Exception" + e.toString());
			info.setActionStatus(ActionStatus.Exception);
		}

		upgradeInfo.setUpgradeStatus(info);

		return upgradeInfo;
	}
}
