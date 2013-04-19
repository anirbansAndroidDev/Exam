package ica.ICAServiceHandler;

import java.util.ArrayList;
import java.util.List;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.ChapterInfo;
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

public class ChapterMarksComparisonService extends Activity {

	private SQLiteDatabase db;

	Context CurContext;

	public ChapterMarksComparisonService(Context context) {
		CurContext = context;
	}

	public TaskStatusMsg FetchMarks(String EmailID) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ExamChapter);
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
							.getString(R.string.CHAPTER_MARKS_COMPARISON_METHOD_NAME));

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
					.getString(R.string.CHAPTER_MARKS_COMPARISON_SOAP_ACTION),
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

				int ChapterCount = rootBlock.getPropertyCount();

				if (ChapterCount > 0) {

					cleanDB();
					for (int cnt = 0; cnt < ChapterCount; cnt++) {

						SoapObject Chapter = (SoapObject) rootBlock
								.getProperty(cnt);

						if (Chapter != null && Chapter.getAttributeCount() > 0) {
							int SubjID = Integer.parseInt(Chapter
									.getAttributeAsString("SubjectId"));
							int ChapID = Integer.parseInt(Chapter
									.getAttributeAsString("ChapterId"));

							String StrSubjName = Chapter
									.getAttributeAsString("SubjectName");
							String StrChapterName = Chapter
									.getAttributeAsString("ChapterName");
							String StrStatus = Chapter
									.getAttributeAsString("Status");

							double SubjMarks = Double.parseDouble(Chapter
									.getAttributeAsString("Mark"));
							double MaxMarks = Double.parseDouble(Chapter
									.getAttributeAsString("HMark"));

							ChapterInfo newChapter = new ChapterInfo();
							newChapter.setSubjectID(SubjID);
							newChapter.setSubjectName(StrSubjName);
							newChapter.setId(ChapID);
							newChapter.setName(StrChapterName);
							newChapter.setMarks(SubjMarks);
							newChapter.setHiMarks(MaxMarks);
							newChapter.setCompletionStatus(StrStatus);

							try {
								SaveChapters(newChapter);

								info.setStatus(0);
								info.setMessage("Exam Chapter information sync successful.");

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

	private long SaveChapters(ChapterInfo Chapter) {

		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_SUBJECT_ID,
					Chapter.getSubjectID());
			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_ID, Chapter.getId());

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_NAME, Chapter.getName());

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_SUBJECT_NAME,
					Chapter.getSubjectName());

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_STATUS,
					Chapter.getCompletionStatus());

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_MARKS,
					Chapter.getMarks());

			values.put(DatabaseHelper.FLD_CHAPTER_EXAM_HIMARKS,
					Chapter.getHiMarks());

			lreturn = db.insertWithOnConflict(
					DatabaseHelper.TBL_CHAPTER_EXAM_DETAILS, null, values,
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
			// /Delete Chapter Table
			db.execSQL(DatabaseHelper.DROP_TBL_CHAPTER_EXAM_INFO);
			db.execSQL(DatabaseHelper.CREATE_TBL_CHAPTER_EXAM_DTLS);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public List<ChapterInfo> getAllChapter() {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		List<ChapterInfo> ChapterList = new ArrayList<ChapterInfo>();

		Cursor cursorChapterDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_CHAPTER_EXAM_DETAILS, null);
		// + " where "+ DatabaseHelper.FLD_CHAPTER_EXAM_SUBJECT_ID + "='" +
		// SubjectID
		// + "'", null);

		try {
			startManagingCursor(cursorChapterDtl);

			if (cursorChapterDtl != null) {

				int userCount = cursorChapterDtl.getCount();

				if (userCount > 0 && cursorChapterDtl.moveToFirst()) {
					do {
						int columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_SUBJECT_ID);

						int curChapterSubjectID = cursorChapterDtl
								.getInt(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_ID);

						int curChapterID = cursorChapterDtl.getInt(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_NAME);

						String ChapterName = cursorChapterDtl
								.getString(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_SUBJECT_NAME);

						String ChapterSubjectName = cursorChapterDtl
								.getString(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_STATUS);

						String ChapterStatus = cursorChapterDtl
								.getString(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_MARKS);

						Float ChapterMarks = cursorChapterDtl
								.getFloat(columnidx);

						columnidx = cursorChapterDtl
								.getColumnIndex(DatabaseHelper.FLD_CHAPTER_EXAM_HIMARKS);

						Float ChapterHiMarks = cursorChapterDtl
								.getFloat(columnidx);

						ChapterInfo newChapter = new ChapterInfo();
						newChapter.setSubjectID(curChapterSubjectID);
						newChapter.setSubjectName(ChapterSubjectName);
						newChapter.setId(curChapterID);
						newChapter.setName(ChapterName);
						newChapter.setMarks(ChapterMarks);
						newChapter.setHiMarks(ChapterHiMarks);
						newChapter.setCompletionStatus(ChapterStatus);

						ChapterList.add(newChapter);
					} while (cursorChapterDtl.moveToNext());
					
					

				}
				
				cursorChapterDtl.close();

			}

		} catch (SQLiteException sqle) {
			if (cursorChapterDtl != null) {
				try {
					cursorChapterDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorChapterDtl != null) {
				try {
					cursorChapterDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorChapterDtl != null) {
				try {
					cursorChapterDtl.close();

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

		return ChapterList;

	}
}
