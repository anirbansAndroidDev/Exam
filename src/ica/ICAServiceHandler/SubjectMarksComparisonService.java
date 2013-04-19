package ica.ICAServiceHandler;

import java.util.ArrayList;
import java.util.List;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.SubjectInfo;
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

public class SubjectMarksComparisonService extends Activity {

	private SQLiteDatabase db;

	Context CurContext;

	public SubjectMarksComparisonService(Context context) {
		CurContext = context;
	}

	public TaskStatusMsg FetchMarks(String EmailID) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ExamSubject);
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
							.getString(R.string.SUBJECT_MARKS_COMPARISON_METHOD_NAME));

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
					.getString(R.string.SUBJECT_MARKS_COMPARISON_SOAP_ACTION),
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

				int SubjectCount = rootBlock.getPropertyCount();

				if (SubjectCount > 0) {

					cleanDB();
					for (int cnt = 0; cnt < SubjectCount; cnt++) {

						SoapObject Subject = (SoapObject) rootBlock
								.getProperty(cnt);

						if (Subject != null && Subject.getAttributeCount() > 0) {

							int SubjID = Integer.parseInt(Subject
									.getAttributeAsString("SubjectId"));

							String StrSubjName = Subject
									.getAttributeAsString("SubjectName");

							String StrStatus = Subject
									.getAttributeAsString("Status");

							double SubjMarks = Double.parseDouble(Subject
									.getAttributeAsString("Mark"));

							double MaxMarks = Double.parseDouble(Subject
									.getAttributeAsString("HMark"));

							SubjectInfo newSubject = new SubjectInfo();

							newSubject.setId(SubjID);

							newSubject.setName(StrSubjName);

							newSubject.setMarks(SubjMarks);
							newSubject.setHiMarks(MaxMarks);
							newSubject.setCompletionStatus(StrStatus);

							try {
								SaveSubjects(newSubject);
								info.setStatus(0);
								info.setMessage("Exam Subject information sync successful.");

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
		}

		return info;
	}

	private long SaveSubjects(SubjectInfo subject) {

		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FLD_SUBJECT_EXAM_ID, subject.getId());

			values.put(DatabaseHelper.FLD_SUBJECT_EXAM_NAME, subject.getName());

			values.put(DatabaseHelper.FLD_SUBJECT_EXAM_STATUS,
					subject.getCompletionStatus());

			values.put(DatabaseHelper.FLD_SUBJECT_EXAM_MARKS,
					subject.getMarks());

			values.put(DatabaseHelper.FLD_SUBJECT_EXAM_HIMARKS,
					subject.getHiMarks());

			lreturn = db.insertWithOnConflict(
					DatabaseHelper.TBL_SUBJECT_EXAM_DETAILS, null, values,
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
			// /Delete Subject Table
			db.execSQL(DatabaseHelper.DROP_TBL_SUBJECT_EXAM_INFO);
			db.execSQL(DatabaseHelper.CREATE_TBL_SUBJECT_EXAM_DTLS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<SubjectInfo> getAllSubject() {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		List<SubjectInfo> SubjectList = new ArrayList<SubjectInfo>();

		Cursor cursorSubjectDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_SUBJECT_EXAM_DETAILS, null);
		// + " where "+ DatabaseHelper.FLD_SUBJECT_EXAM_MODULE_ID + "='" +
		// ModuleID + "'", null);

		try {
			startManagingCursor(cursorSubjectDtl);

			if (cursorSubjectDtl != null) {

				int userCount = cursorSubjectDtl.getCount();

				if (userCount > 0 && cursorSubjectDtl.moveToFirst()) {

					do {

						int columnidx = cursorSubjectDtl
								.getColumnIndex(DatabaseHelper.FLD_SUBJECT_EXAM_ID);

						int curSubjectID = cursorSubjectDtl.getInt(columnidx);
						
						columnidx = cursorSubjectDtl
								.getColumnIndex(DatabaseHelper.FLD_SUBJECT_EXAM_NAME);

						String SubjectName = cursorSubjectDtl
								.getString(columnidx);

						columnidx = cursorSubjectDtl
								.getColumnIndex(DatabaseHelper.FLD_SUBJECT_EXAM_STATUS);

						String SubjectStatus = cursorSubjectDtl
								.getString(columnidx);

						columnidx = cursorSubjectDtl
								.getColumnIndex(DatabaseHelper.FLD_SUBJECT_EXAM_MARKS);

						Float SubjectMarks = cursorSubjectDtl
								.getFloat(columnidx);

						columnidx = cursorSubjectDtl
								.getColumnIndex(DatabaseHelper.FLD_SUBJECT_EXAM_HIMARKS);

						Float SubjectHiMarks = cursorSubjectDtl
								.getFloat(columnidx);

						SubjectInfo newSubject = new SubjectInfo();
						newSubject.setId(curSubjectID);
						newSubject.setName(SubjectName);
						newSubject.setMarks(SubjectMarks);
						newSubject.setHiMarks(SubjectHiMarks);
						newSubject.setCompletionStatus(SubjectStatus);

						SubjectList.add(newSubject);
					} while (cursorSubjectDtl.moveToNext());

				}

				cursorSubjectDtl.close();

			}

		} catch (SQLiteException sqle) {
			if (cursorSubjectDtl != null) {
				try {
					cursorSubjectDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorSubjectDtl != null) {
				try {
					cursorSubjectDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorSubjectDtl != null) {
				try {
					cursorSubjectDtl.close();

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

		return SubjectList;

	}
}
