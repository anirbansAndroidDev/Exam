package ica.ICAServiceHandler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import ica.ICAConstants.ActionStatus;
import ica.ProfileInfo.PlacementInfo;
import ica.ProfileInfo.StatusMessage;
import ica.Utility.DownloaderService;
import ica.Utility.PlacementPreference;
import ica.exam.DatabaseHelper;
import ica.exam.R;

import com.placement.PlacementSelectorActivity.MonthItems;
import com.placement.PlacementSelectorActivity.PlacementType;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PlacementDownloaderService extends Activity {

	Context CurContext;
	private SQLiteDatabase db;
	PlacementType placemenType;

	public PlacementDownloaderService(Context context, PlacementType type) {
		CurContext = context;
		placemenType = type;
	}

	public StatusMessage SyncServerToDB(String strPlacementType) {

		StatusMessage info = new StatusMessage();
		info.setActionStatus(ActionStatus.Unsuccessful);
		info.setIconValue(R.drawable.information);

		info.setMessage("Service Error!");

		String SyncPlacementType = "";
		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();
			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext
							.getString(R.string.PLACEMENT_DOWNLOAD_METHOD_NAME));

			PropertyInfo inf_studentcode = new PropertyInfo();
			inf_studentcode.setName("Type");
			inf_studentcode.setValue(strPlacementType);
			request.addProperty(inf_studentcode);

			switch (placemenType) {
			case ALLIndia:
				SyncPlacementType = "All India";
				break;
			case ForDays:
				SyncPlacementType = "Days";
				break;
			case ForMonth:
				SyncPlacementType = "Month";
				break;

			}

			info.setTitle("Sync Status: " + SyncPlacementType + " Data");

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setActionStatus(ActionStatus.NoInternetConnection);
			info.setMessage("Wifi authentication failure:Please authenticate and try again.");

			return info;
		}

		try {
			androidHttpTransport.call(CurContext
					.getString(R.string.PLACEMENT_DOWNLOAD_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setMessage("Wifi authentication failure:Please authenticate and try again.");
			info.setActionStatus(ActionStatus.WiFiAuthError);

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setMessage("Wifi authentication failure:Please authenticate and try again.");
			info.setActionStatus(ActionStatus.WiFiAuthError);

			return info;
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				SoapObject placementBlock = (SoapObject) rootBlock
						.getProperty(0);

				if (placementBlock != null
						&& placementBlock.getPropertyCount() > 0) {

					if (placementBlock.getAttributeCount() > 0) {

						String placementType = placementBlock
								.getAttributeAsString("Type");

						String placementItemCount = placementBlock
								.getAttributeAsString("NoOfStudents");

						String placementDays = placementBlock
								.getAttributeAsString("Days");

						String placementMonth = placementBlock
								.getAttributeAsString("Month");

						String placementYear = placementBlock
								.getAttributeAsString("Year");

						if ("A".equals(placementType)) {
							PlacementPreference.setPlacementCountAllIndia(
									CurContext,
									Integer.parseInt(placementItemCount));
						} else if ("D".equals(placementType)) {

							PlacementPreference.setPlacementCountDay(
									CurContext,
									Integer.parseInt(placementItemCount));

							PlacementPreference
									.setPlacementDaysCount(CurContext,
											Integer.parseInt(placementDays));

						} else if ("M".equals(placementType)) {

							PlacementPreference.setPlacementCountMonth(
									CurContext,
									Integer.parseInt(placementItemCount));

							int mnthCnt = Integer.parseInt(placementMonth);

							MonthItems mnth = MonthItems.values()[mnthCnt - 1];

							PlacementPreference.setPlacementMonth(CurContext,
									mnth);

							PlacementPreference.setPlacementMonthYr(CurContext,
									Integer.parseInt(placementYear));
						}

					}

					int placementItemCnt = placementBlock.getPropertyCount();

					cleanDB();
					for (int cnt = 0; cnt < placementItemCnt; cnt++) {

						SoapObject placementSoap = (SoapObject) placementBlock
								.getProperty(cnt);

						if (placementSoap != null
								&& placementSoap.getAttributeCount() > 0) {

							int pid = Integer.parseInt(placementSoap
									.getAttributeAsString("PId"));

							String studentcode = placementSoap
									.getAttributeAsString("StudentCode");

							int yr = Integer.parseInt(placementSoap
									.getAttributeAsString("Year"));

							int mth = Integer.parseInt(placementSoap
									.getAttributeAsString("Month"));

							int dom = Integer.parseInt(placementSoap
									.getAttributeAsString("Day"));

							String studentnm = placementSoap
									.getAttributeAsString("StudentName");

							String centerCode = placementSoap
									.getAttributeAsString("CenterCode");

							String employer = placementSoap
									.getAttributeAsString("Employer");

							String Salary = placementSoap
									.getAttributeAsString("Salary");

							String contactPerson = placementSoap
									.getAttributeAsString("ContactPerson");

							String photoUrl = placementSoap
									.getAttributeAsString("StudentPhoto");

							PlacementInfo newPlacementInfo = new PlacementInfo();

							newPlacementInfo.setID(pid);
							newPlacementInfo.setStudentCode(studentcode);
							newPlacementInfo.setPlacedStudentName(studentnm);
							newPlacementInfo.setYear(yr);
							newPlacementInfo.setMonth(mth);
							newPlacementInfo.setDay(dom);
							newPlacementInfo.setCenterCode(centerCode);
							newPlacementInfo.setEmployerName(employer);
							newPlacementInfo.setSalary(Double
									.parseDouble(Salary));
							newPlacementInfo
									.setContactPersonName(contactPerson);
							newPlacementInfo.setStudentPhotoUrl(photoUrl);

							try {
								SavePlacement(newPlacementInfo);

								info.setActionStatus(ActionStatus.Successfull);
								info.setMessage("Information downloaded successfully.");

							} catch (Exception e) {

								info.setMessage("Data Error:Parsing Error.Contact admin.");
								info.setActionStatus(ActionStatus.Unsuccessful);

								e.printStackTrace();
							}

						} else {
							info.setMessage("Data Error:No placement data available.");
							info.setActionStatus(ActionStatus.Unsuccessful);

						}

					}
				} else {
					info.setMessage("Data Error:No placement data available.");
					info.setActionStatus(ActionStatus.Unsuccessful);

				}
			}

			db.close();
		} catch (Exception e) {
			info.setMessage("Data Exception:" + e.toString());
			info.setActionStatus(ActionStatus.Exception);
			e.printStackTrace();
		}

		return info;
	}

	private long SavePlacement(PlacementInfo placementInfo) {

		long lreturn = 0;
		// Check If already Exists
		try {

			String durationField = "";

			switch (placemenType) {
			case ALLIndia:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_ALL_INDIA;
				break;
			case ForDays:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_DAY;
				break;
			case ForMonth:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_MONTH;
				break;

			}

			Cursor cursorPlacementItem = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_PLACEMENT_DTLS + " where "
					+ DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE + "='"
					+ placementInfo.getStudentCode() + "'", null);

			startManagingCursor(cursorPlacementItem);

			cursorPlacementItem.moveToFirst();

			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			ContentValues values = null;
			if (placementInfo != null) {

				values = new ContentValues();

				values.put(durationField, "Y");

				values.put(DatabaseHelper.FLD_PLACEMENT_ID,
						placementInfo.getID());

				values.put(DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE,
						placementInfo.getStudentCode());

				values.put(DatabaseHelper.FLD_PLACEMENT_DATE_YEAR,
						placementInfo.getYear());

				values.put(DatabaseHelper.FLD_PLACEMENT_DATE_MONTH,
						placementInfo.getMonth());

				values.put(DatabaseHelper.FLD_PLACEMENT_DATE_DAY_OF_MONTH,
						placementInfo.getDay());

				values.put(DatabaseHelper.FLD_PLACEMENT_CENTER_CODE,
						placementInfo.getCenterCode());

				values.put(DatabaseHelper.FLD_PLACEMENT_EMPLOYER_NAME,
						placementInfo.getEmployerName());

				values.put(DatabaseHelper.FLD_PLACEMENT_STUDENT_NAME,
						placementInfo.getPlacedStudentName());

				values.put(DatabaseHelper.FLD_PLACEMENT_SALARY,
						placementInfo.getSalary());

				values.put(DatabaseHelper.FLD_PLACEMENT_CONTACT_PERSON,
						placementInfo.getContactPersonName());

				values.put(DatabaseHelper.FLD_PLACEMENT_PHOTO_LOC,
						placementInfo.getStudentPhotoUrl());

			}

			if ((placementInfo != null && values != null)) {
				if (cursorPlacementItem != null
						&& cursorPlacementItem.getCount() > 0) {

					lreturn = db.update(DatabaseHelper.TBL_PLACEMENT_DTLS,
							values, DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE
									+ " = ?",
							new String[] { placementInfo.getStudentCode() });

				} else {

					lreturn = db.insertOrThrow(
							DatabaseHelper.TBL_PLACEMENT_DTLS, null, values);

				}

				downloadBitmap(placementInfo);
			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	private void downloadBitmap(PlacementInfo placementInfo) {

		if (placementInfo != null) {
			// IF BLOB AVAILABLE SKIP DOWNLOAD
			// placementInfo.setPhotobmp(GetBitmapFomBlob(placementInfo));

			// ELSE DOWNLOAD AND SAVE TO BLOB
			// if (placementInfo.getPhotobmp() == null) {

			if (placementInfo.getStudentPhotoUrl() != null
					&& !placementInfo.getStudentPhotoUrl().equals("")) {
				placementInfo.setPhotobmp(DownloaderService
						.downloadBitmap(placementInfo.getStudentPhotoUrl()));

				// Save BLOB to DB

				SaveBitmapToBlob(placementInfo);
			}
			// }
		}
	}

	public boolean SaveBitmapToBlob(PlacementInfo placementInfo) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		boolean isSuccess = false;

		Cursor cursorPlacementItem = null;

		try {
			if (placementInfo.getPhotobmp() != null) {

				cursorPlacementItem = db.rawQuery("select * from "
						+ DatabaseHelper.TBL_PLACEMENT_DTLS + " where "
						+ DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE + "='"
						+ placementInfo.getStudentCode() + "'", null);

				startManagingCursor(cursorPlacementItem);

				cursorPlacementItem.moveToFirst();
				if (cursorPlacementItem != null
						&& cursorPlacementItem.getCount() > 0) {

					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.FLD_PLACEMENT_IMAGE,
							getByteArray(placementInfo.getPhotobmp()));

					long rows = db.update(DatabaseHelper.TBL_PLACEMENT_DTLS,
							values, DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE
									+ " = ? ",
							new String[] { placementInfo.getStudentCode() });

					if (rows > 0) {
						isSuccess = true;
					} else {
						isSuccess = false;
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public Bitmap GetBitmapFomBlob(PlacementInfo placementInfo) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Cursor cursorPlacementItem = null;

		try {
			if (placementInfo != null) {
				cursorPlacementItem = db.rawQuery("select * from "
						+ DatabaseHelper.TBL_PLACEMENT_DTLS + " where "
						+ DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE + "='"
						+ placementInfo.getStudentCode() + "'", null);

				startManagingCursor(cursorPlacementItem);

				cursorPlacementItem.moveToFirst();
				if (cursorPlacementItem != null) {

					int idx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_IMAGE);
					byte[] blob = cursorPlacementItem.getBlob(idx);
					Bitmap bmp = null;
					if (blob != null) {
						bmp = BitmapFactory.decodeByteArray(blob, 0,
								blob.length);

					}

					return bmp;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private byte[] getByteArray(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

		byte[] b = baos.toByteArray();
		return b;
	}

	@SuppressWarnings("deprecation")
	public ArrayList<PlacementInfo> getAllPlacementInfo() {
		ArrayList<PlacementInfo> listPlacementInfo = new ArrayList<PlacementInfo>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		int iCount = 0;
		int placementCount = 0;

		Cursor cursorPlacementItem = null;

		try {

			String durationField = "";

			switch (placemenType) {
			case ALLIndia:
				placementCount = PlacementPreference
						.getPlacementCountAllIndia(CurContext);
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_ALL_INDIA;
				break;
			case ForDays:
				placementCount = PlacementPreference
						.getPlacementCountDay(CurContext);
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_DAY;
				break;
			case ForMonth:
				placementCount = PlacementPreference
						.getPlacementCountMonth(CurContext);
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_MONTH;
				break;

			}

			cursorPlacementItem = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_PLACEMENT_DTLS + " where "
					+ durationField + "='" + "Y" + "' ORDER BY "
					+ DatabaseHelper.FLD_PLACEMENT_SALARY + " DESC", null);

			startManagingCursor(cursorPlacementItem);

			cursorPlacementItem.moveToFirst();
			if (cursorPlacementItem != null
					&& cursorPlacementItem.getCount() > 0) {

				do {

					int columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_ID);

					int plcmntID = cursorPlacementItem.getInt(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_STUDENT_CODE);

					String plcmntStudentCode = cursorPlacementItem
							.getString(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_DATE_YEAR);

					int yr = cursorPlacementItem.getInt(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_DATE_MONTH);

					int mth = cursorPlacementItem.getInt(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_DATE_DAY_OF_MONTH);

					int dom = cursorPlacementItem.getInt(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_CENTER_CODE);

					String centerCode = cursorPlacementItem
							.getString(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_EMPLOYER_NAME);

					String EmployerNm = cursorPlacementItem
							.getString(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_STUDENT_NAME);

					String placedstudentnm = cursorPlacementItem
							.getString(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_SALARY);

					double salary = cursorPlacementItem.getDouble(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_CONTACT_PERSON);

					String contactperson = cursorPlacementItem
							.getString(columnidx);

					columnidx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_PLACEMENT_PHOTO_LOC);

					String photoLoc = cursorPlacementItem.getString(columnidx);

					PlacementInfo newPlacementInfo = new PlacementInfo();

					newPlacementInfo.setID(plcmntID);
					newPlacementInfo.setStudentCode(plcmntStudentCode);
					newPlacementInfo.setPlacedStudentName(placedstudentnm);
					newPlacementInfo.setYear(yr);
					newPlacementInfo.setMonth(mth);
					newPlacementInfo.setDay(dom);
					newPlacementInfo.setCenterCode(centerCode);
					newPlacementInfo.setEmployerName(EmployerNm);
					newPlacementInfo.setSalary(salary);
					newPlacementInfo.setContactPersonName(contactperson);
					newPlacementInfo.setStudentPhotoUrl(photoLoc);

					// /Filter With Time

					switch (placemenType) {
					case ALLIndia:

						// listPlacementInfo.add(newPlacementInfo);
						// iCount = iCount + 1;
						break;
					case ForDays:
						// int daycount = PlacementPreference
						// .getPlacementDaysCount(CurContext);
						//
						// Calendar dayLimitCal = Calendar.getInstance();
						//
						// dayLimitCal.add(Calendar.DATE, -daycount);
						//
						// // if (listPlacementInfo.size() <= daycount)
						//
						// Calendar placmntCal = newPlacementInfo
						// .placementCalender();
						//
						// int compare = placmntCal.compareTo(dayLimitCal);
						//
						// compare =
						// placmntCal.compareTo(Calendar.getInstance());
						//
						// if (placmntCal.compareTo(dayLimitCal) >= 0
						// && placmntCal.compareTo(Calendar.getInstance()) <= 0)
						// {
						// listPlacementInfo.add(newPlacementInfo);
						// iCount = iCount + 1;
						// }

						break;
					case ForMonth:

						// int monthyr = pl;
						//
						// if (newPlacementInfo.getYear() == monthyr
						// && placementMonth.getIndex() == newPlacementInfo
						// .getMonth()) {
						// listPlacementInfo.add(newPlacementInfo);
						// iCount = iCount + 1;
						// }
						break;

					}
					iCount = iCount + 1;
					listPlacementInfo.add(newPlacementInfo);

				} while (cursorPlacementItem.moveToNext()
						&& iCount <= placementCount);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				if (cursorPlacementItem != null) {
					cursorPlacementItem.close();

				}
				db.close();
			} catch (Exception e) {

				e.printStackTrace();

			}
		}

		return listPlacementInfo;
	}

	private void cleanDB() {

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_PLACEMENT_DTLS);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			ContentValues values = new ContentValues();

			String durationField = "";

			switch (placemenType) {
			case ALLIndia:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_ALL_INDIA;
				break;
			case ForDays:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_DAY;
				break;
			case ForMonth:
				durationField = DatabaseHelper.FLD_PLACEMENT_DURATION_TYPE_MONTH;
				break;

			}
			values.put(durationField, "N");

			if (values != null) {

				int lreturn = db.update(DatabaseHelper.TBL_PLACEMENT_DTLS,
						values, durationField + " = ?", new String[] { "Y" });

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void emptyDB(Context curContext) {

		SQLiteDatabase dbSrc = (new DatabaseHelper(curContext))
				.getWritableDatabase();

		try {
			dbSrc.execSQL(DatabaseHelper.DROP_TBL_PLACEMENT_DTLS);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			dbSrc.execSQL(DatabaseHelper.CREATE_TBL_PLACEMENT_DTLS);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
