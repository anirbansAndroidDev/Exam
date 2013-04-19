package ica.ICAServiceHandler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import ica.ICAConstants.ActionStatus;
import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.ExamResultStudent;
import ica.ProfileInfo.QuestionDetails;
import ica.ProfileInfo.StatusMessage;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.DownloaderService;
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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

public class ExamSyncService extends Activity {

	private SQLiteDatabase db;

	String sUserID = null;
	String sSubjectID = null;
	String sChapterID = null;
	String sExamID = null;
	String sQuestionID = null;
	String sAnsCorrect = null;
	String sMarks = null;
	String sAllQid = null;
	String sAllMarks = null;

	String sExamOn = null;
	String sExamType = null;

	Context CurContext;

	public ExamSyncService(Context context) {
		CurContext = context;

	}

	public TaskStatusMsg AnswerUpload(StudentDetails studentDetails,Activity actvity) {
		//Toast.makeText(CurContext, "Inside AnswerUpload",Toast.LENGTH_LONG).show();
		
		TaskStatusMsg info = new TaskStatusMsg();
		db = (new DatabaseHelper(CurContext)).getWritableDatabase();
		StudentDetails.initInstance(CurContext);
		studentDetails = StudentDetails.getInstance();

		String sUserID = null;
		try 
		{
			sUserID = studentDetails.getStudentID();
		} 
		catch (Exception e) 
		{
			info.setStatus(-10);
			info.setMessage("Application error!" + e.toString());
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);

			return info;
		}

		Cursor cursorChapter = null;
		try {

			cursorChapter = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ID_SUBJECT + ", "
					+ DatabaseHelper.FLD_ID_CHAPTER + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = ?",
					new String[] { "T" });

			startManagingCursor(cursorChapter);

			if (cursorChapter != null && cursorChapter.getCount() > 0) {
				try {
					int colSubjectID = cursorChapter
							.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT);
					int colChapterID = cursorChapter
							.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER);

					if (cursorChapter.moveToFirst()) {
						do {

							sSubjectID = cursorChapter.getString(colSubjectID);
							sChapterID = cursorChapter.getString(colChapterID);
							sExamID = null;
							sQuestionID = null;
							sAnsCorrect = null;
							sMarks = null;
							sAllQid = null;
							sAllMarks = null;
							if (sSubjectID != null && sChapterID != null) {

								Cursor cursorExam = db.rawQuery("SELECT "
										+ DatabaseHelper.FLD_ID_EXAM + " FROM "
										+ DatabaseHelper.TBL_EXAM + " WHERE "
										+ DatabaseHelper.FLD_ID_CHAPTER
										+ " = ?", new String[] { sChapterID });

								int colExamID = cursorExam
										.getColumnIndex(DatabaseHelper.FLD_ID_EXAM);

								startManagingCursor(cursorExam);

								if (cursorExam != null
										&& cursorExam.getCount() > 0) {
									if (cursorExam.moveToFirst()) {
										do {
											sExamID = cursorExam
													.getString(colExamID);

											Cursor cursorAnswer = db
													.rawQuery(
															"SELECT "
																	+ DatabaseHelper.FLD_ID_QUESTION
																	+ ","
																	+ DatabaseHelper.FLD_ANSWER_CORRECT
																	+ ","
																	+ DatabaseHelper.FLD_QUESTION_MARKS
																	+ " FROM "
																	+ DatabaseHelper.TBL_EXAM
																	+ " WHERE "
																	+ DatabaseHelper.FLD_ID_CHAPTER
																	+ " = ?"
																	+ " AND "
																	+ DatabaseHelper.FLD_ID_EXAM
																	+ " = ?",
															new String[] {
																	sChapterID,
																	sExamID });

											startManagingCursor(cursorAnswer);

											if (cursorAnswer != null
													&& cursorAnswer.getCount() > 0) {
												if (cursorAnswer.moveToFirst()) {
													do {
														sQuestionID = cursorAnswer
																.getString(cursorAnswer
																		.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION));
														sAnsCorrect = cursorAnswer
																.getString(cursorAnswer
																		.getColumnIndex(DatabaseHelper.FLD_ANSWER_CORRECT));

														if (sAnsCorrect
																.equals("T")) {
															sMarks = cursorAnswer
																	.getString(cursorAnswer
																			.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS));
														} else {
															sMarks = "0";
														}

														if (sAllQid != null) {
															sAllQid = sQuestionID
																	+ "|"
																	+ sAllQid;
															sAllMarks = sMarks
																	+ "|"
																	+ sAllMarks;
														} else {
															sAllQid = sQuestionID;
															sAllMarks = sMarks;
														}

													} while (cursorAnswer
															.moveToNext());
												}
											}

											cursorAnswer.close();

											// /// Fetch Exam Type
											Cursor cursorExamType = db
													.rawQuery(
															"SELECT "
																	+ DatabaseHelper.FLD_EXAM_ON
																	+ ","
																	+ DatabaseHelper.FLD_EXAM_TYPE
																	+ " FROM "
																	+ DatabaseHelper.TBL_EXAM_UPLOAD_INFO
																	+ " WHERE "
																	+ DatabaseHelper.FLD_ID_EXAM
																	+ " = ?",
															new String[] { sExamID });

											try {
												startManagingCursor(cursorExamType);
												if (cursorExamType != null
														&& cursorExamType
																.getCount() > 0) {
													cursorExamType
															.moveToFirst();

													sExamOn = cursorExamType
															.getString(cursorExamType
																	.getColumnIndex(DatabaseHelper.FLD_EXAM_ON));
													sExamType = cursorExamType
															.getString(cursorExamType
																	.getColumnIndex(DatabaseHelper.FLD_EXAM_TYPE));

												}
											} catch (Exception e1) {

												e1.printStackTrace();
											}

											cursorExamType.close();

											// /// Fetch Exam Type

											SoapObject request = null;
											SoapObject soapResult = null;

											if (sUserID != null
													&& sSubjectID != null
													&& sChapterID != null
													&& sExamID != null
													&& sAllQid != null
													&& sAllMarks != null
													&& sExamOn != null
													&& sExamType != null) {

												try {
													request = new SoapObject(
															CurContext
																	.getString(R.string.WEBSERVICE_NAMESPACE),
															CurContext
																	.getString(R.string.EXAM_UPLOAD_METHOD_NAME));

													PropertyInfo inf_email = new PropertyInfo();
													inf_email
															.setName("emailid");
													inf_email.setValue(sUserID);
													request.addProperty(inf_email);

													PropertyInfo inf_subject = new PropertyInfo();
													inf_subject
															.setName("subjectid");
													inf_subject
															.setValue(sSubjectID);
													request.addProperty(inf_subject);

													PropertyInfo inf_chapter = new PropertyInfo();
													inf_chapter
															.setName("chapterid");
													inf_chapter
															.setValue(sChapterID);
													request.addProperty(inf_chapter);

													PropertyInfo inf_exam = new PropertyInfo();
													inf_exam.setName("examid");
													inf_exam.setValue(sExamID);
													request.addProperty(inf_exam);

													PropertyInfo inf_question = new PropertyInfo();
													inf_question
															.setName("questionid");
													inf_question
															.setValue(sAllQid);
													request.addProperty(inf_question);

													PropertyInfo inf_marks = new PropertyInfo();
													inf_marks.setName("marks");
													inf_marks
															.setValue(sAllMarks);
													request.addProperty(inf_marks);

													PropertyInfo inf_exam_on = new PropertyInfo();
													inf_exam_on
															.setName("ExamOn");
													inf_exam_on
															.setValue(sExamOn);
													request.addProperty(inf_exam_on);

													PropertyInfo inf_exam_type = new PropertyInfo();
													inf_exam_type
															.setName("ExamType");
													inf_exam_type
															.setValue(sExamType);
													request.addProperty(inf_exam_type);

													SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
															SoapEnvelope.VER11);
													envelope.dotNet = true;
													envelope.setOutputSoapObject(request);

													HttpTransportSE androidHttpTransport = new HttpTransportSE(
															CurContext
																	.getString(R.string.SOAP_URL));
													androidHttpTransport
															.call(CurContext
																	.getString(R.string.EXAM_SOAP_UPLOAD_ACTION),
																	envelope);
													soapResult = (SoapObject) envelope.bodyIn;

												} catch (Exception e) {

													info.setStatus(-12);
													info.setMessage("Connection error! Please check the connection and try it again.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);

													return info;
												}

												if (soapResult == null) {

													info.setStatus(-13);
													info.setMessage("Upload error! Parsing Error.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);

												} else {
													info.setStatus(1);
													info.setMessage("Exam result has been successfully published to the http://icaerp.com to keep track your performance.");
													info.setTitle("Upload Status");
													info.setTaskDone(UploadTask.Upload);
												}

												sAllQid = null;
												sAllMarks = null;
												sExamOn = null;
												sExamType = null;

												// {{ Delete Existing data

												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);
												db.execSQL("DELETE FROM "
														+ DatabaseHelper.TBL_EXAM
														+ " WHERE "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = "
														+ sChapterID
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = " + sExamID);

												// }}

												break;

											} else {
												info.setStatus(-13);
												info.setMessage("No data available for upload!");
												info.setTitle("Upload Status");
												info.setTaskDone(UploadTask.Upload);

											}

										} while (cursorExam.moveToNext());
									}
								} else {
									info.setStatus(-13);
									info.setMessage("No data available for upload!");
									info.setTitle("Upload Status");
									info.setTaskDone(UploadTask.Upload);
								}

								cursorExam.close();

								ContentValues values = new ContentValues();
								values.put(
										DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER,
										"F");
								values.put(
										DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
										"F");

								db.update(DatabaseHelper.TBL_CHAPTER, values,
										DatabaseHelper.FLD_ID_SUBJECT
												+ " = ? AND "
												+ DatabaseHelper.FLD_ID_CHAPTER
												+ " = ?", new String[] {
												sSubjectID, sChapterID });
							} else {

								info.setStatus(-13);
								info.setMessage("No data available for upload!");
								info.setTitle("Upload Status");
								info.setTaskDone(UploadTask.Upload);
							}

						} while (cursorChapter.moveToNext());
					}

					cursorChapter.close();

				} catch (SQLiteException sqle) {
					info.setStatus(-11);
					info.setMessage("Application data error!" + sqle.toString());
					info.setTitle("Upload Status");
					info.setTaskDone(UploadTask.Upload);

				} catch (Exception e) {
					info.setStatus(-10);
					info.setMessage("Application error!" + e.toString());
					info.setTitle("Upload Status");
					info.setTaskDone(UploadTask.Upload);

				}
			} else {
				info.setStatus(-13);
				info.setMessage("No data available for upload!");
				info.setTitle("Upload Status");
				info.setTaskDone(UploadTask.Upload);
			}
		} catch (Exception e) {
			info.setStatus(-13);
			info.setMessage("No data available for upload!");
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);
		}

		return info;
	}

	public TaskStatusMsg AnswerUploadPractice(StudentDetails studentDetails,
			Activity actvity) {

		TaskStatusMsg info = new TaskStatusMsg();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		String sUserID = null;
		try {
			sUserID = studentDetails.getStudentID();
		} catch (Exception e) {

			info.setStatus(-10);
			info.setMessage("Application error!" + e.toString());
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);

			return info;
		}

		sSubjectID = "";
		sChapterID = "1";
		sExamID = null;
		sQuestionID = null;
		sAnsCorrect = null;
		sMarks = null;
		sAllQid = null;
		sAllMarks = null;
		if (sSubjectID != null && sChapterID != null) {

			Cursor cursorExam = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ID_EXAM + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?",
					new String[] { sChapterID });

			int colExamID = cursorExam
					.getColumnIndex(DatabaseHelper.FLD_ID_EXAM);

			startManagingCursor(cursorExam);

			if (cursorExam != null && cursorExam.getCount() > 0) {
				if (cursorExam.moveToFirst()) {
					do {
						sExamID = cursorExam.getString(colExamID);

						Cursor cursorAnswer = db.rawQuery(
								"SELECT " + DatabaseHelper.FLD_ID_QUESTION
										+ ","
										+ DatabaseHelper.FLD_ANSWER_CORRECT
										+ ","
										+ DatabaseHelper.FLD_QUESTION_MARKS
										+ " FROM " + DatabaseHelper.TBL_EXAM
										+ " WHERE "
										+ DatabaseHelper.FLD_ID_CHAPTER
										+ " = ?" + " AND "
										+ DatabaseHelper.FLD_ID_EXAM + " = ?",
								new String[] { sChapterID, sExamID });

						startManagingCursor(cursorAnswer);

						if (cursorAnswer != null && cursorAnswer.getCount() > 0) {
							if (cursorAnswer.moveToFirst()) {
								do {
									sQuestionID = cursorAnswer
											.getString(cursorAnswer
													.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION));
									sAnsCorrect = cursorAnswer
											.getString(cursorAnswer
													.getColumnIndex(DatabaseHelper.FLD_ANSWER_CORRECT));

									if (sAnsCorrect.equals("T")) {
										sMarks = cursorAnswer
												.getString(cursorAnswer
														.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS));
									} else {
										sMarks = "0";
									}

									if (sAllQid != null) {
										sAllQid = sQuestionID + "|" + sAllQid;
										sAllMarks = sMarks + "|" + sAllMarks;
									} else {
										sAllQid = sQuestionID;
										sAllMarks = sMarks;
									}

								} while (cursorAnswer.moveToNext());
							}
						}

						cursorAnswer.close();

						// /// Fetch Exam Type
						Cursor cursorExamType = db.rawQuery("SELECT "
								+ DatabaseHelper.FLD_EXAM_ON + ","
								+ DatabaseHelper.FLD_EXAM_TYPE + " FROM "
								+ DatabaseHelper.TBL_EXAM_UPLOAD_INFO
								+ " WHERE " + DatabaseHelper.FLD_ID_EXAM
								+ " = ?", new String[] { sExamID });

						try {
							startManagingCursor(cursorExamType);
							if (cursorExamType != null
									&& cursorExamType.getCount() > 0) {
								cursorExamType.moveToFirst();

								sExamOn = cursorExamType
										.getString(cursorExamType
												.getColumnIndex(DatabaseHelper.FLD_EXAM_ON));
								sExamType = cursorExamType
										.getString(cursorExamType
												.getColumnIndex(DatabaseHelper.FLD_EXAM_TYPE));

							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						cursorExamType.close();

						// /// Fetch Exam Type

						SoapObject request = null;
						SoapObject soapResult = null;

						if (sUserID != null && sSubjectID != null
								&& sChapterID != null && sExamID != null
								&& sAllQid != null && sAllMarks != null
								&& sExamOn != null && sExamType != null) {

							try {
								request = new SoapObject(
										CurContext
												.getString(R.string.WEBSERVICE_NAMESPACE),
										CurContext
												.getString(R.string.EXAM_UPLOAD_METHOD_NAME));

								PropertyInfo inf_email = new PropertyInfo();
								inf_email.setName("emailid");
								inf_email.setValue(sUserID);
								request.addProperty(inf_email);

								PropertyInfo inf_subject = new PropertyInfo();
								inf_subject.setName("subjectid");
								inf_subject.setValue(sSubjectID);
								request.addProperty(inf_subject);

								PropertyInfo inf_chapter = new PropertyInfo();
								inf_chapter.setName("chapterid");
								inf_chapter.setValue(sChapterID);
								request.addProperty(inf_chapter);

								PropertyInfo inf_exam = new PropertyInfo();
								inf_exam.setName("examid");
								inf_exam.setValue(sExamID);
								request.addProperty(inf_exam);

								PropertyInfo inf_question = new PropertyInfo();
								inf_question.setName("questionid");
								inf_question.setValue(sAllQid);
								request.addProperty(inf_question);

								PropertyInfo inf_marks = new PropertyInfo();
								inf_marks.setName("marks");
								inf_marks.setValue(sAllMarks);
								request.addProperty(inf_marks);

								PropertyInfo inf_exam_on = new PropertyInfo();
								inf_exam_on.setName("ExamOn");
								inf_exam_on.setValue(sExamOn);
								request.addProperty(inf_exam_on);

								PropertyInfo inf_exam_type = new PropertyInfo();
								inf_exam_type.setName("ExamType");
								inf_exam_type.setValue(sExamType);
								request.addProperty(inf_exam_type);

								SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
										SoapEnvelope.VER11);
								envelope.dotNet = true;
								envelope.setOutputSoapObject(request);

								HttpTransportSE androidHttpTransport = new HttpTransportSE(
										CurContext.getString(R.string.SOAP_URL));
								androidHttpTransport
										.call(CurContext
												.getString(R.string.EXAM_SOAP_UPLOAD_ACTION),
												envelope);
								soapResult = (SoapObject) envelope.bodyIn;

							} catch (Exception e) {

								info.setStatus(-12);
								info.setMessage("Connection error! Please check the connection and try it again.");
								info.setTitle("Upload Status");
								info.setTaskDone(UploadTask.Upload);

								return info;
							}

							if (soapResult == null) {

								info.setStatus(-13);
								info.setMessage("Upload error! Parsing Error.");
								info.setTitle("Upload Status");
								info.setTaskDone(UploadTask.Upload);

							} else {
								info.setStatus(1);
								info.setMessage("Practice Exam result has been successfully published to the http://icaerp.com to keep track your performance.");
								info.setTitle("Upload Status");
								info.setTaskDone(UploadTask.Upload);
							}

							sAllQid = null;
							sAllMarks = null;
							sExamOn = null;
							sExamType = null;

							// {{ Delete Existing data

							db.execSQL("DELETE FROM "
									+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
									+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
									+ " = " + sChapterID + " AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = "
									+ sExamID);
							db.execSQL("DELETE FROM "
									+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
									+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
									+ " = " + sChapterID + " AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = "
									+ sExamID);
							db.execSQL("DELETE FROM "
									+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE
									+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
									+ " = " + sChapterID + " AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = "
									+ sExamID);
							db.execSQL("DELETE FROM " + DatabaseHelper.TBL_EXAM
									+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
									+ " = " + sChapterID + " AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = "
									+ sExamID);

							break;

						} else {
							info.setStatus(-13);
							info.setMessage("No data available for upload!");
							info.setTitle("Upload Status");
							info.setTaskDone(UploadTask.Upload);

						}

					} while (cursorExam.moveToNext());
				}
			} else {
				info.setStatus(-13);
				info.setMessage("No data available for upload!");
				info.setTitle("Upload Status");
				info.setTaskDone(UploadTask.Upload);
			}

			cursorExam.close();

			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER, "F");
			values.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER, "F");

			db.update(DatabaseHelper.TBL_CHAPTER, values,
					DatabaseHelper.FLD_ID_CHAPTER + " = ?",
					new String[] { sChapterID });
		} else {

			info.setStatus(-13);
			info.setMessage("No data available for upload!");
			info.setTitle("Upload Status");
			info.setTaskDone(UploadTask.Upload);
		}

		return info;
	}

	public StatusMessage DownloadPracticeExamResult() {
		StatusMessage info = new StatusMessage();
		info.setActionStatus(ActionStatus.Unsuccessful);
		info.setIconValue(R.drawable.information);
		info.setMessage("");
		info.setTitle("");

		return info;
	}

	public ArrayList<QuestionDetails> getAllCheckedQuestion(
			StudentDetails studentDetails) {

		ArrayList<QuestionDetails> lstQuestionInfo = new ArrayList<QuestionDetails>();

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		try {
			Cursor cursorQuestionDtls = db.rawQuery(
					"select * from " + DatabaseHelper.TBL_QUESTION_DETAILS
							+ " where " + DatabaseHelper.FLD_ID_USER + "='"
							+ studentDetails.getStudentID() + "'", null);

			if (cursorQuestionDtls != null && cursorQuestionDtls.getCount() > 0) {

				startManagingCursor(cursorQuestionDtls);

				cursorQuestionDtls.moveToFirst();

				do {

					int columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ID);

					String qid = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_TEXT);

					String qtext = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_CHECKED);

					String qchkd = cursorQuestionDtls.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE);

					String qritpercent = cursorQuestionDtls
							.getString(columnIndex);

					columnIndex = cursorQuestionDtls
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE);

					String qrngpercent = cursorQuestionDtls
							.getString(columnIndex);

					QuestionDetails questionDetails = new QuestionDetails();
					questionDetails.setID(qid);
					questionDetails.setText(qtext);
					questionDetails.setRightPercentage(Integer
							.parseInt(qritpercent));
					questionDetails.setWrongPercentage(Integer
							.parseInt(qrngpercent));

					lstQuestionInfo.add(questionDetails);

				} while (cursorQuestionDtls.moveToNext());
			} else {
				lstQuestionInfo = null;
			}

			cursorQuestionDtls.close();
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return lstQuestionInfo;

	}

	private void CleanPracticeResult() {

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_QUESTION_DETAILS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.DROP_TBL_QUESTION_STUDENT_DETAILS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_QUESTION_DETAILS);

			db.delete(DatabaseHelper.TBL_QUESTION_DETAILS, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			db.execSQL(DatabaseHelper.CREATE_TBL_QUESTION_STUDENT_DETAILS);

			db.delete(DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS, null, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public StatusMessage SyncServerToDBCheckedQuestion(
			StudentDetails studentDetails) {

		StatusMessage info = new StatusMessage();
		info.setActionStatus(ActionStatus.Unsuccessful);
		info.setIconValue(R.drawable.information);
		info.setMessage("");
		info.setTitle("Practice Exam Result");

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {

			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext
							.getString(R.string.PRACTICE_EXAM_RESULT_METHOD_NAME));

			PropertyInfo inf_facultycode = new PropertyInfo();
			inf_facultycode.setName("StudentId");
			inf_facultycode.setValue(studentDetails.getStudentStatusCode());
			request.addProperty(inf_facultycode);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setMessage("Wifi authentication failure:Please authenticate and try again.");
			info.setActionStatus(ActionStatus.NoInternetConnection);

			return info;
		}

		try {
			androidHttpTransport.call(CurContext
					.getString(R.string.PRACTICE_EXAM_RESULT_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setMessage("Wifi authentication failure:Please authenticate and try again.");
			info.setActionStatus(ActionStatus.NoInternetConnection);

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setMessage("Data Error:Contact admin");
			info.setActionStatus(ActionStatus.ParseError);

			return info;
		}

		try {
			if (soapResult != null) {

				db = (new DatabaseHelper(CurContext)).getWritableDatabase();

				CleanPracticeResult();

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				if (rootBlock != null && rootBlock.getPropertyCount() > 0) {

					rootBlock = (SoapObject) rootBlock.getProperty(0);

					int QCount = rootBlock.getPropertyCount();

					for (int cnt = 0; cnt < QCount; cnt++) {

						SoapObject dtlsBlock = (SoapObject) rootBlock
								.getProperty(cnt);

						if (dtlsBlock != null
								&& dtlsBlock.getAttributeCount() > 0) {

							soapCheckedQuestionParser(dtlsBlock, studentDetails);

						}
					}

					info.setActionStatus(ActionStatus.Successfull);
					info.setMessage("Batch information downloaded successfully.");

				} else {
					info.setMessage("No Result Available");
					info.setActionStatus(ActionStatus.Unsuccessful);

				}
			}

			db.close();
		} catch (Exception e) {
			info.setMessage("Data Exception:" + e.toString());
			info.setActionStatus(ActionStatus.Exception);

		}

		finally {

			try {
				db.close();
			} catch (Exception e2) {
			}

		}

		return info;
	}

	private void soapCheckedQuestionParser(SoapObject dtlsBlock,
			StudentDetails studentDetails) {

		if (dtlsBlock != null && dtlsBlock.getAttributeCount() > 0) {

			String qid = dtlsBlock.getAttributeAsString("QId");

			String correctPerc = (String) dtlsBlock
					.getAttributeSafelyAsString("CorrectPerc");

			String qText = (String) dtlsBlock
					.getAttributeSafelyAsString("Question");

			String wrongPerc = (String) dtlsBlock
					.getAttributeSafelyAsString("InCorrectPerc");

			QuestionDetails question = new QuestionDetails();
			question.setID(qid);
			question.setText(qText);
			question.setRightPercentage(ParseSafeInt(correctPerc));
			question.setWrongPercentage(ParseSafeInt(wrongPerc));

			if (updsertCheckedQuestions(true, studentDetails, question) > 0) {

				int studentCount = dtlsBlock.getPropertyCount();

				if (studentCount > 0) {

					for (int i = 0; i < studentCount; i++) {

						SoapObject studentObj = (SoapObject) dtlsBlock
								.getProperty(i);

						if (studentObj != null) {
							soapQuestionStudentDetails(studentObj);
						}
					}
				}
			}
		}

	}

	private int ParseSafeInt(String strInt) {
		int Value = 0;

		try {
			Value = Integer.parseInt(strInt);
		} catch (NumberFormatException e) {

			e.printStackTrace();
		}

		return Value;
	}

	private long updsertCheckedQuestions(boolean isChecked,
			StudentDetails studentDetails, QuestionDetails question) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		long rows = 0;

		try {
			db.execSQL("DELETE FROM " + DatabaseHelper.TBL_QUESTION_DETAILS
					+ " WHERE " + DatabaseHelper.FLD_QUESTION_ID + " = '"
					+ question.getID() + "' AND "
					+ DatabaseHelper.FLD_QUESTION_TEXT + " = '"
					+ question.getText() + "' ");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (studentDetails != null) {
			// /Insert Question Exam Percentage Details

			try {

				if (question != null) {

					ContentValues values = new ContentValues();

					values.put(DatabaseHelper.FLD_QUESTION_ID, question.getID());

					values.put(DatabaseHelper.FLD_ID_USER,
							studentDetails.getStudentID());

					values.put(DatabaseHelper.FLD_QUESTION_TEXT,
							question.getText());

					values.put(DatabaseHelper.FLD_QUESTION_CHECKED, "F");

					values.put(DatabaseHelper.FLD_QUESTION_RIGHT_PERCENTAGE,
							question.getRightPercentage());

					values.put(DatabaseHelper.FLD_QUESTION_WRONG_PERCENTAGE,
							question.getWrongPercentage());

					rows = db.insertWithOnConflict(
							DatabaseHelper.TBL_QUESTION_DETAILS, null, values,
							SQLiteDatabase.CONFLICT_IGNORE);

				}

			} catch (SQLiteException sqle) {

				sqle.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return rows;
	}

	public void soapQuestionStudentDetails(SoapObject soapStudent) {

		if (soapStudent != null && soapStudent.getAttributeCount() > 0) {

			String qid = soapStudent.getAttributeAsString("QId");

			String ansStatus = soapStudent.getAttributeAsString("Answer");

			String studentCode = soapStudent
					.getAttributeAsString("StudentCode");

			String studentnm = soapStudent.getAttributeAsString("StudentName");

			String studentphotoURL = soapStudent
					.getAttributeAsString("StudentPhoto");

			ExamResultStudent student = new ExamResultStudent();
			student.setQuestionID(qid);
			student.setAnsStatus(ExamResultStudent.StudentAnsStatus
					.valueOf(ansStatus));
			student.setStudentCode(studentCode);
			student.setName(studentnm);
			student.setImgPath(studentphotoURL);

			// Download Image

			student.setStudentImage(DownloaderService.downloadBitmap(student
					.getImgPath()));

			try {
				saveQuestionStudent(student);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public long saveQuestionStudent(ExamResultStudent student) {

		long lreturn = 0;

		try {

			if (student != null) {

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_QUESTION_ID,
						student.getQuestionID());

				values.put(DatabaseHelper.FLD_QUESTION_ATTEMPT_STATUS, student
						.getAnsStatus().toString());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_CODE,
						student.getStudentCode());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_NAME,
						student.getName());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO,
						student.getImgPath());

				values.put(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO_BLOB,
						getByteArray(student.getStudentImage()));

				lreturn = db.insertOrThrow(
						DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS, null,
						values);

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;

	}

	private byte[] getByteArray(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

		byte[] b = baos.toByteArray();
		return b;
	}

	public ArrayList<ExamResultStudent> getAllStudentAnswered(
			String QuestionID, Boolean isWrong) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Cursor cursorPlacementItem = null;

		ArrayList<ExamResultStudent> lstStudentDtls = new ArrayList<ExamResultStudent>();

		try {

			cursorPlacementItem = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_QUESTION_STUDENT_DETAILS + " where "
					+ DatabaseHelper.FLD_QUESTION_ID + "='" + QuestionID + "'",
					null);
			startManagingCursor(cursorPlacementItem);

			cursorPlacementItem.moveToFirst();

			if (cursorPlacementItem != null
					&& cursorPlacementItem.getCount() > 0) {
				do {

					int columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ID);

					String Qid = cursorPlacementItem.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTEMPT_STATUS);

					String attemptStatus = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_CODE);

					String studentCode = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_NAME);

					String studentnm = cursorPlacementItem
							.getString(columnIndex);

					columnIndex = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO);

					String studentphoto = cursorPlacementItem
							.getString(columnIndex);

					int idx = cursorPlacementItem
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_STUDENT_PHOTO_BLOB);
					byte[] blob = cursorPlacementItem.getBlob(idx);
					Bitmap bmp = null;
					if (blob != null) {
						bmp = BitmapFactory.decodeByteArray(blob, 0,
								blob.length);
					}

					Bitmap studntphoto = bmp;

					ExamResultStudent student = new ExamResultStudent();

					student.setQuestionID(Qid);
					student.setAnsStatus(ExamResultStudent.StudentAnsStatus
							.valueOf(ExamResultStudent.StudentAnsStatus.class,
									attemptStatus));

					student.setStudentCode(studentCode);
					student.setName(studentnm);
					student.setStudentImage(studntphoto);

					if (isWrong) {
						// Wrong Percentage

						if (student.getAnsStatus().equals(
								ExamResultStudent.StudentAnsStatus.Wrong)
								|| student
										.getAnsStatus()
										.equals(ExamResultStudent.StudentAnsStatus.Unattempted)) {
							lstStudentDtls.add(student);
						}

					} else {
						// Right Percentage

						if (student.getAnsStatus().equals(
								ExamResultStudent.StudentAnsStatus.Right)) {
							lstStudentDtls.add(student);
						}
					}

				} while (cursorPlacementItem.moveToNext());
			} else {
				lstStudentDtls = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			db.close();
			cursorPlacementItem.close();
		} catch (Exception e) {
		}

		finally {

			try {
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return lstStudentDtls;

	}

}
