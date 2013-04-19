package ica.ICAServiceHandler;

import java.util.ArrayList;
import java.util.List;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.ModuleInfo;
import ica.ProfileInfo.TaskStatusMsg;
import ica.exam.DatabaseHelper;
import ica.exam.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ModuleMarksComparisonService extends Activity {

	private SQLiteDatabase db;

	Context CurContext;

	public ModuleMarksComparisonService(Context context) {
		CurContext = context;
	}

	public TaskStatusMsg FetchMarks(String EmailID) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ExamModule);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);
		
		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();
			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext
							.getString(R.string.MODULE_MARKS_COMPARISON_METHOD_NAME));

			PropertyInfo inf_email = new PropertyInfo();
			inf_email.setName("EmailID");
			inf_email.setValue(EmailID);
			request.addProperty(inf_email);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			androidHttpTransport.call(CurContext
					.getString(R.string.MODULE_MARKS_COMPARISON_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				int ModuleCount = rootBlock.getPropertyCount();

				if (ModuleCount > 0) {

					cleanDB();
					for (int cnt = 0; cnt < ModuleCount; cnt++) {

						SoapObject Module = (SoapObject) rootBlock
								.getProperty(cnt);

						if (Module != null && Module.getAttributeCount() > 0) {

							int ModuleID = Integer.parseInt(Module
									.getAttributeAsString("ModuleId"));

							String ModuleName = Module
									.getAttributeAsString("ModuleName");

							String StrStatus = Module
									.getAttributeAsString("Status");

							double ModuleMarks = Double.parseDouble(Module
									.getAttributeAsString("Mark"));

							double MaxMarks = Double.parseDouble(Module
									.getAttributeAsString("HMark"));

							ModuleInfo newModule = new ModuleInfo();

							newModule.setId(ModuleID);

							newModule.setEmailID(EmailID);

							newModule.setName(ModuleName);

							newModule.setCompletionStatus(StrStatus);

							newModule.setMarks(ModuleMarks);

							newModule.setHiMarks(MaxMarks);

							try {
								SaveModule(newModule);

								info.setStatus(0);
								info.setMessage("Exam Module information sync successful.");

							} catch (Exception e) {

								info.setStatus(-3);
								info.setMessage("Data Exception" + e.toString());

								e.printStackTrace();
							}

						}

					}
				}
			}

			db.close();
		} catch (Exception e) {
			info.setStatus(-3);
			info.setMessage("Data Exception" + e.toString());
			// TODO: handle exception
		}

		return info;
	}

	private long SaveModule(ModuleInfo module) {

		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FLD_MODULE_EXAM_ID, module.getId());

			values.put(DatabaseHelper.FLD_MODULE_EXAM_UserID,
					module.getEmailID());

			values.put(DatabaseHelper.FLD_MODULE_EXAM_NAME, module.getName());

			values.put(DatabaseHelper.FLD_MODULE_EXAM_STATUS,
					module.getCompletionStatus());

			values.put(DatabaseHelper.FLD_MODULE_EXAM_MARKS, module.getMarks());

			values.put(DatabaseHelper.FLD_MODULE_EXAM_HIMARKS,
					module.getHiMarks());

			lreturn = db.insertWithOnConflict(
					DatabaseHelper.TBL_MODULE_EXAM_DETAILS, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	private void cleanDB() {

		try {
			// /Delete Module Table
			db.execSQL(DatabaseHelper.DROP_TBL_MODULE_EXAM_INFO);
			db.execSQL(DatabaseHelper.CREATE_TBL_MODULE_EXAM_DTLS);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public List<ModuleInfo> getAllModules(String UserId) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		List<ModuleInfo> ModuleList = new ArrayList<ModuleInfo>();

		Cursor cursorModuleDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_MODULE_EXAM_DETAILS + " where "
				+ DatabaseHelper.FLD_MODULE_EXAM_UserID + "='" + UserId + "'",
				null);

		try {
			startManagingCursor(cursorModuleDtl);

			if (cursorModuleDtl != null) {

				int userCount = cursorModuleDtl.getCount();

				if (userCount > 0 && cursorModuleDtl.moveToFirst()) {

					do {

						int columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_MODULE_EXAM_ID);

						int curModuleID = cursorModuleDtl.getInt(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_MODULE_EXAM_NAME);

						String MouduleName = cursorModuleDtl
								.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_MODULE_EXAM_STATUS);

						String ModuleStatus = cursorModuleDtl
								.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_MODULE_EXAM_MARKS);

						Float ModuleMarks = cursorModuleDtl.getFloat(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_MODULE_EXAM_HIMARKS);

						Float ModuleHiMarks = cursorModuleDtl
								.getFloat(columnidx);

						ModuleInfo newModule = new ModuleInfo();

						newModule.setId(curModuleID);
						newModule.setEmailID(UserId);

						newModule.setName(MouduleName);
						newModule.setMarks(ModuleMarks);
						newModule.setHiMarks(ModuleHiMarks);
						newModule.setCompletionStatus(ModuleStatus);

						ModuleList.add(newModule);
					} while (cursorModuleDtl.moveToNext());
				}
				cursorModuleDtl.close();
			}

		} catch (SQLiteException sqle) {
			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

		return ModuleList;

	}

}
