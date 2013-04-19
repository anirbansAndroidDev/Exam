package ica.ICAServiceHandler;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.TaskStatusMsg;
import ica.exam.DatabaseHelper;
import ica.exam.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ExamDownloaderService {

	Context curContext;
	private SQLiteDatabase db;

	public ExamDownloaderService(Context context) {
		curContext = context;
	}

	
	public TaskStatusMsg ChapterSync(String s_user, String s_password) {
		
		TaskStatusMsg info = new TaskStatusMsg();

		info.setMessage("Service Error!");
		info.setTitle("Sync Status");
		info.setTaskDone(UploadTask.SubjectList);
		info.setStatus(-1);
		
		
		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {
			db = (new DatabaseHelper(curContext)).getWritableDatabase();
			
			SoapObject request = new SoapObject(
					curContext.getString(R.string.WEBSERVICE_NAMESPACE),
					curContext.getString(R.string.LOGIN_METHOD_NAME));

			PropertyInfo inf_email = new PropertyInfo();
			inf_email.setName("stringLoginUser");
			inf_email.setValue(s_user);
			request.addProperty(inf_email);

			PropertyInfo inf_pwd = new PropertyInfo();
			inf_pwd.setName("stringLoginPwd");
			inf_pwd.setValue(s_password);
			request.addProperty(inf_pwd);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					curContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setMessage("Connection error! Please check the connection and try it again.");
			info.setTitle("Sync Status");
			info.setTaskDone(UploadTask.SubjectList);
			info.setStatus(-1);

			return info;
		}

		try {
			androidHttpTransport.call(
					curContext.getString(R.string.LOGIN_SOAP_ACTION), envelope);
		} catch (Exception e) {

			info.setMessage("Connection error! Please check the connection and try it again.");
			info.setTitle("Sync Status");
			info.setTaskDone(UploadTask.SubjectList);
			info.setStatus(-1);

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {

			info.setMessage("Connection error! Please check the connection and try it again.");
			info.setTitle("Sync Status");
			info.setTaskDone(UploadTask.SubjectList);
			info.setStatus(-1);

			return info;
		}

		if (soapResult != null && db!=null) {
			SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
			SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);
			String loginStatus = String.valueOf(rootBlock.getAttribute(0)
					.toString().toUpperCase());

			if (loginStatus.equals("F")) {

				info.setMessage("Invalid user information! Please login again and try it again.");
				info.setTitle("Sync Status");
				info.setTaskDone(UploadTask.SubjectList);
				info.setStatus(-2);

				return info;
			} else if (loginStatus.equals("T")) {
				try {
					
					db.execSQL(DatabaseHelper.TRUNCATE_TBL_SUBJECT);
					db.execSQL(DatabaseHelper.TRUNCATE_TBL_CHAPTER);
					
					for (int iSubject = 0; iSubject < rootBlock
							.getPropertyCount(); iSubject++) {
						SoapObject subjectBlock = (SoapObject) rootBlock
								.getProperty(iSubject);
						createSubject(subjectBlock.getAttribute(0).toString(),
								subjectBlock.getAttribute(1).toString());
						populate_chapter(s_user, subjectBlock.getAttribute(0)
								.toString());
					}

					info.setMessage("Mock test subjects downloaded successfully.");
					info.setTitle("Sync Status");
					info.setTaskDone(UploadTask.SubjectList);
					info.setStatus(0);

				} catch (SQLiteException sqle) {
					info.setMessage("Data Exception:Contact Admin ," + sqle.toString());
					info.setTitle("Sync Status");
					info.setTaskDone(UploadTask.SubjectList);
					info.setStatus(-3);
					return info;
				} catch (Exception e) {
					info.setMessage("Data Exception:Contact Admin ," + e.toString());
					info.setTitle("Sync Status");
					info.setTaskDone(UploadTask.SubjectList);
					info.setStatus(-3);

					return info;
				}
			} 
		} else {
			info.setMessage("Data Exception:Contact Admin ," + "Service has no data available for this user");
			info.setTitle("Sync Status");
			info.setTaskDone(UploadTask.SubjectList);
			info.setStatus(-3);
		}

		return info;
	}

	private void populate_chapter(String s_user, String s_subject_id) {
		SoapObject request = new SoapObject(
				curContext.getString(R.string.WEBSERVICE_NAMESPACE),
				curContext.getString(R.string.CHAPTER_METHOD_NAME));

		PropertyInfo inf_subjectid = new PropertyInfo();
		inf_subjectid.setName("stringSubjectid");
		inf_subjectid.setValue(s_subject_id);
		request.addProperty(inf_subjectid);

		PropertyInfo inf_email = new PropertyInfo();
		inf_email.setName("stringStudent");
		inf_email.setValue(s_user);
		request.addProperty(inf_email);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(
				curContext.getString(R.string.SOAP_URL));

		try {
			androidHttpTransport.call(
					curContext.getString(R.string.CHAPTER_SOAP_ACTION),
					envelope);
		} catch (Exception e) {
			return;
		}

		try {
			SoapObject soapResult = (SoapObject) envelope.bodyIn;

			if (soapResult != null) {
				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);

				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				db.execSQL("DELETE FROM " + DatabaseHelper.TBL_CHAPTER
						+ " WHERE " + DatabaseHelper.FLD_ID_SUBJECT + " = "
						+ s_subject_id);
				for (int iChapter = 0; iChapter < rootBlock.getPropertyCount(); iChapter++) {
					SoapObject chapterBlock = (SoapObject) rootBlock
							.getProperty(iChapter);

					createChapter(s_subject_id, chapterBlock.getAttribute(0)
							.toString(), chapterBlock.getAttribute(1)
							.toString(), "F", "F");
				}
			}
		} catch (Exception e) {
			return;
		}

		return;
	}

	private long createSubject(String id_subject, String name_subject) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_ID_SUBJECT, id_subject);
			values.put(DatabaseHelper.FLD_NAME_SUBJECT, name_subject);

			lreturn = db.insert(DatabaseHelper.TBL_SUBJECT, null, values);
		} catch (SQLiteException sqle) {
			lreturn = -99;
		} catch (Exception e) {
			lreturn = -99;
		}

		return lreturn;
	}

	private long createChapter(String id_subject, String id_chapter,
			String name_chapter, String downloaded, String examed) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_ID_SUBJECT, id_subject);
			values.put(DatabaseHelper.FLD_ID_CHAPTER, id_chapter);
			values.put(DatabaseHelper.FLD_NAME_CHAPTER, name_chapter);
			values.put(DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER, downloaded);
			values.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER, examed);

			lreturn = db.insert(DatabaseHelper.TBL_CHAPTER, null, values);
		} catch (SQLiteException sqle) {
			lreturn = -99;
		} catch (Exception e) {
			lreturn = -99;
		}

		return lreturn;
	}

}
