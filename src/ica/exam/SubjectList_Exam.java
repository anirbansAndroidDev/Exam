package ica.exam;

import static ica.exam.IndexActivity.ExamStatusCode;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ica.ICAConstants.CourseMatIntent;
import ica.ICAConstants.DownloadOptions;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppPreferenceStatus;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;

public class SubjectList_Exam extends Activity {
	private Cursor cursorSubject;
	private Cursor cursorChapter;
	private SQLiteDatabase db;
	private int idx;
	private AlertDialog.Builder adChapter;
	private AlertDialog.Builder adStartExam;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private long ExamTimeElapsed;
	private String[] aChapterID = null;
	private CharSequence[] aChapterName = null;

	Context CurContext;
	private String ActionType;

	int examCount = 0;

	StudentDetails StudentInfo = null;
	ExamSyncService mExamSyncService;
	ProgressDialog pgExam;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subject_list);

		db = (new DatabaseHelper(this)).getWritableDatabase();
		CurContext = this;
		if (cursorChapter != null) {
			cursorChapter.close();
		}

		CourseMatIntent type = AppPreferenceStatus.getStudyDownload(CurContext);

		switch (type) {
		case DownlaodMock:
			break;
		case MockExam:
			ActionType = "'Mock Exam'";
			break;
		case PracticeExam:
			ActionType = "'Practice Exam'";
			break;
		case StudyMaterials:
			ActionType = "'Study Material'";
			break;

		}

		StudentDetails.initInstance(CurContext);

		StudentInfo = StudentDetails.getInstance();

		String sEmail = StudentInfo.getStudentID();

		if (sEmail != null) {

			setTitle("Subject List-" + ActionType + "- [" + sEmail + "]");

		}

		mExamSyncService = new ExamSyncService(CurContext);

		adChapter = new AlertDialog.Builder(CurContext);
		adStartExam = new AlertDialog.Builder(CurContext);

		pgExam = new ProgressDialog(CurContext);
		pgExam.setMessage("Please wait while downloading...");
		pgExam.setIndeterminate(true);
		pgExam.setCancelable(false);
		pgExam.setCanceledOnTouchOutside(false);

		try {
			cursorSubject = db.query(DatabaseHelper.TBL_SUBJECT, new String[] {
					DatabaseHelper.FLD_ROWID, DatabaseHelper.FLD_ID_SUBJECT,
					DatabaseHelper.FLD_NAME_SUBJECT }, null, null, null, null,
					null);

			ListAdapter ListAdaptersubject = new SubjectAdapter(this,
					cursorSubject);
			ListView ListViewSubject = (ListView) findViewById(R.id.lvSubject);
			ListViewSubject.setAdapter(ListAdaptersubject);

			ListViewSubject
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							int iCount = 0;

							CourseMatIntent type = AppPreferenceStatus
									.getStudyDownload(CurContext);

							switch (type) {
							case DownlaodMock:
								break;
							case MockExam:
								ActionType = "'Mock Exam'";
								break;
							case PracticeExam:
								ActionType = "'Practice Exam'";
								break;
							case StudyMaterials:
								ActionType = "'Study Material'";
								break;

							}
							cursorSubject.moveToPosition(position);
							SelectedSubjectID = cursorSubject.getString(cursorSubject
									.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT));

							// if (type.equals(CourseMatIntent.PracticeExam)) {

							// ////UPLOAD /DOWNLOAD EXAM
							// PracticeExamDownloader();

							// } else if (type.equals(CourseMatIntent.MockExam))
							// {

							try {

								try {

									cursorChapter = db
											.rawQuery(
													"SELECT "
															+ DatabaseHelper.FLD_ROWID
															+ ", "
															+ DatabaseHelper.FLD_ID_CHAPTER
															+ ","
															+ DatabaseHelper.FLD_NAME_CHAPTER
															+ " FROM "
															+ DatabaseHelper.TBL_CHAPTER
															+ " WHERE "
															// +
															// DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER
															// + " = 'F'"
															// + " AND "
															+ DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER
															+ " = 'T'"
															+ " AND "
															+ DatabaseHelper.FLD_ID_SUBJECT
															+ " = ?",
													new String[] { SelectedSubjectID });

									iCount = cursorChapter.getCount();
									int colID = cursorChapter
											.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER);
									int colChapterName = cursorChapter
											.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER);

									startManagingCursor(cursorChapter);

									aChapterID = new String[iCount];
									aChapterName = new CharSequence[iCount];

									if (cursorChapter != null) {
										if (cursorChapter.moveToFirst()) {
											idx = 0;
											do {
												aChapterID[idx] = cursorChapter
														.getString(colID);
												aChapterName[idx] = cursorChapter
														.getString(colChapterName);

												idx++;
											} while (cursorChapter.moveToNext());
										}
									}

									cursorChapter.close();
								} catch (SQLiteException sqle) {
									Toast.makeText(getApplicationContext(),
											sqle.getMessage(),
											Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(),
											e.getMessage(), Toast.LENGTH_LONG)
											.show();
								}

								Cursor cursorCompletedChapter = db
										.rawQuery(
												"SELECT "
														+ DatabaseHelper.FLD_ROWID
														+ ", "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ ","
														+ DatabaseHelper.FLD_NAME_CHAPTER
														+ " FROM "
														+ DatabaseHelper.TBL_CHAPTER
														+ " WHERE "
														+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER
														+ " = 'F'"
														+ " AND "
														+ DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER
														+ " = 'T'"
														+ " AND "
														+ DatabaseHelper.FLD_ID_SUBJECT
														+ " = ?",
												new String[] { SelectedSubjectID });

								examCount = cursorCompletedChapter.getCount();

								if (examCount > 0) {
									adChapter.setTitle("Chapter(s)");
									adChapter.setIcon(R.drawable.folder_yellow);
									adChapter
											.setItems(
													aChapterName,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int item) {
															SelectedChapterID = GetChapterID(
																	SelectedSubjectID,
																	aChapterName[item]
																			.toString());

															adStartExam
																	.setIcon(R.drawable.bt_question);
															adStartExam
																	.setTitle("Exam status");
															adStartExam
																	.setPositiveButton(
																			"Yes",
																			null)
																	.create();
															adStartExam
																	.setNegativeButton(
																			"No",
																			null)
																	.create();
															adStartExam
																	.setMessage(
																			"Are you ready to take the "
																					+ ActionType
																					+ "? Yes will start the exam and No will cancel.")
																	.setPositiveButton(
																			"No",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int id) {
																					dialog.cancel();
																				}
																			})

																	.setNegativeButton(
																			"Yes",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int id) {
																					fillExam(
																							SelectedSubjectID,
																							SelectedChapterID);
																				}
																			});

															AlertDialog altStartExam = adStartExam
																	.create();
															altStartExam.show();
														}
													});
									AlertDialog altChapter = adChapter.create();
									altChapter.show();

								} else {
									adStartExam.setIcon(R.drawable.warning);
									adStartExam.setTitle("Exam status");
									adStartExam.setPositiveButton("Ok", null)
											.create();
									adStartExam
											.setMessage("There is no uncompleted "
													+ ActionType
													+ " available for this subject. Please download the exam for this subject and then try again.");
									adStartExam.setCancelable(false);
									adStartExam
											.setPositiveButton(
													"Ok",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int id) {
															dialog.cancel();
														}
													});
									AlertDialog altStartExam = adStartExam
											.create();
									altStartExam.show();
								}

								// /Exam Activity

							} catch (SQLiteException sqle) {
								Toast.makeText(getApplicationContext(),
										sqle.getMessage(), Toast.LENGTH_LONG)
										.show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(), Toast.LENGTH_LONG)
										.show();
							}

							// }

						}
					});
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		try {

			cursorSubject.close();
			cursorChapter.close();
			db.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void onBackPressed() {
		//goHome();
		//super.onBackPressed();
		Intent i = new Intent(this, MainMenuInTabView.class);
		startActivity(i);
		finish();
	}

	private void goHome() {
		try {
			// Intent intent = new Intent(this, ExamActivity.class);
			// startActivity(intent);
			finish();
			return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
		}

		return;
	}

	private String GetChapterID(String SubjectID, String ChapterName) {
		String ChapterID = null;

		try {
			cursorChapter = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ ", " + DatabaseHelper.FLD_ID_CHAPTER + ","
					+ DatabaseHelper.FLD_NAME_CHAPTER + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = 'F'"
					+ " AND " + DatabaseHelper.FLD_ID_SUBJECT + " = ?",
					new String[] { SubjectID });

			int colID = cursorChapter
					.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER);
			int colChapter = cursorChapter
					.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER);

			startManagingCursor(cursorChapter);

			if (cursorChapter != null) {
				if (cursorChapter.moveToFirst()) {
					idx = 0;
					do {
						if (ChapterName.equals(cursorChapter
								.getString(colChapter))) {
							ChapterID = cursorChapter.getString(colID);
						}
						idx++;
					} while (cursorChapter.moveToNext());
				}
			}

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return ChapterID;
	}

	private void fillExam(String SubjectID, String ChapterID) {
		Intent intent = new Intent(this, QuestionList_Exam.class);
		intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SubjectID);
		intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, ChapterID);
		intent.putExtra(DatabaseHelper.FLD_EXAM_TIME,
				GetExamTime(SubjectID, ChapterID));
		intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

		startActivity(intent);
		finish();
		return;
	}

	private long GetExamTime(String subjectid, String chapterid) {
		long examtime = 0;

		try {
			Cursor cursorExam;

			cursorExam = db.rawQuery("SELECT " + DatabaseHelper.FLD_ID_EXAM
					+ "," + DatabaseHelper.FLD_EXAM_TIME + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?",
					new String[] { chapterid });

			int colID = cursorExam.getColumnIndex(DatabaseHelper.FLD_EXAM_TIME);
			startManagingCursor(cursorExam);

			if (cursorExam.moveToFirst()) {
				examtime = cursorExam.getLong(colID);
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		examtime = examtime * 60;
		return examtime;
	}

	private class SubjectAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public SubjectAdapter(Context context, Cursor cursor) {
			super(context, cursor, true);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			try {
				TextView t;
				TextView t1;
				t = (TextView) view.findViewById(R.id.tvSubject);
				t.setText(cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.FLD_NAME_SUBJECT)));

				cursorChapter = db.rawQuery("SELECT "
						+ DatabaseHelper.FLD_ID_CHAPTER + " FROM "
						+ DatabaseHelper.TBL_CHAPTER + " WHERE "
						+ DatabaseHelper.FLD_ID_SUBJECT + " = ?" + " AND "
						+ DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER + " = 'T'"
						+ " AND " + DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER
						+ " = 'F'", new String[] { cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT)) });

				t1 = (TextView) view.findViewById(R.id.tvChapterCount);
				int noOfChapter = cursorChapter.getCount();

				t1.setText("No. of available exams (" + noOfChapter + ")");
				String str  = t1.getText().toString();
				int noOfChap = Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(")")));
				//Toast.makeText(getApplicationContext(),str.substring(str.indexOf("(") + 1, str.indexOf(")")) ,Toast.LENGTH_SHORT).show();
				
				if(noOfChap > 0)
				{
					t1.setTextColor(Color.parseColor("#04B431"));
				}
				else if(noOfChap == 0)
				{
					t1.setTextColor((Color.parseColor("#41627E")));
				}
				
			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.subject_row, parent,
					false);
			return view;
		}
	}

	Builder adLevel;
	String SelectedLevelID;

	boolean isMock = false;

	// /UPLOAD / DOWNLOAD EXAM

	public class AsyncQuestionDownloader extends
			AsyncTask<String, TaskStatusMsg, Integer> {

		@Override
		protected Integer doInBackground(String... params) {

			TaskStatusMsg infoUpload = mExamSyncService.AnswerUpload(
					StudentInfo, (SubjectList_Exam) CurContext);

			publishProgress(infoUpload);

			int dwldSatus = DownloadExam(SelectedSubjectID, SelectedChapterID,
					SelectedLevelID);

			return dwldSatus;
		}

		@Override
		protected void onProgressUpdate(TaskStatusMsg... values) {

			super.onProgressUpdate(values);

			Toast.makeText(CurContext, values[0].getMessage(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onPostExecute(Integer result) {

			super.onPostExecute(result);

			mProgressHandler.sendEmptyMessage(result);
		}

	}

	private int DownloadExam(String SubjectID, String ChapterID, String LevelID) {

		int StatusMsg = -3;

		String ExamID = null;
		String ExamName = null;
		HttpTransportSE androidHttpTransport = null;
		SoapSerializationEnvelope envelope = null;
		int ExamTime = 0;

		SoapObject request = null;
		SoapObject soapResult = null;

		try {
			request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.EXAM_METHOD_NAME));

			PropertyInfo inf_subjectid = new PropertyInfo();
			inf_subjectid.setName("requestQuestionSubjectId");
			inf_subjectid.setValue(StudentInfo.getStudentStatusCode());
			request.addProperty(inf_subjectid);

			PropertyInfo inf_chapterid = new PropertyInfo();
			inf_chapterid.setName("requestQuestionChapterId");
			inf_chapterid.setValue(ChapterID);
			request.addProperty(inf_chapterid);

			PropertyInfo inf_levelid = new PropertyInfo();
			inf_levelid.setName("requestQuestionSetId");

			PropertyInfo inf_isMock = new PropertyInfo();
			inf_isMock.setName("requestExamType");
			if (isMock) {
				inf_isMock.setValue("M");
				inf_levelid.setValue(LevelID);

			} else {
				inf_isMock.setValue("E");
				inf_levelid.setValue("1");

			}

			request.addProperty(inf_levelid);
			request.addProperty(inf_isMock);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {
			StatusMsg = -1;

		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.EXAM_SOAP_ACTION), envelope);
		} catch (Exception e) {
			StatusMsg = -2;

		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (ClassCastException e) {

			StatusMsg = -5;

		} catch (Exception e) {

			StatusMsg = -2;

		}

		if (soapResult != null) {
			try {
				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				if (rootBlock.getPropertyCount() > 0) {
					ExamID = rootBlock.getAttribute(1).toString();
					ExamName = rootBlock.getAttribute(2).toString();
					ExamTime = Integer.parseInt(rootBlock.getAttribute(3)
							.toString());

					db.execSQL("DELETE FROM " + DatabaseHelper.TBL_EXAM
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = "
							+ ChapterID);
					db.execSQL("DELETE FROM "
							+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE + " WHERE "
							+ DatabaseHelper.FLD_ID_CHAPTER + " = " + ChapterID);
					db.execSQL("DELETE FROM "
							+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE + " WHERE "
							+ DatabaseHelper.FLD_ID_CHAPTER + " = " + ChapterID);
					db.execSQL("DELETE FROM "
							+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = "
							+ ChapterID);

					for (int iQuestion = 0; iQuestion < rootBlock
							.getPropertyCount(); iQuestion++) {
						String QuestionID = null;
						String QuestionType = null;
						int QuestionMarks = 0;
						String QuestionBody = null;

						SoapObject questionBlock = (SoapObject) rootBlock
								.getProperty(iQuestion);

						QuestionID = questionBlock.getAttribute(0).toString();
						QuestionType = questionBlock.getAttribute(1).toString()
								.toUpperCase().trim();
						QuestionMarks = Integer.parseInt(questionBlock
								.getAttribute(2).toString());
						QuestionBody = questionBlock.getAttribute(3).toString();

						if (QuestionType.equals("MAM")) {
							createExam(ChapterID, ExamID, ExamName, ExamTime,
									QuestionID, "MCQ", QuestionMarks,
									QuestionBody);
							parseMCQ(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("MCQ")) {
							createExam(ChapterID, ExamID, ExamName, ExamTime,
									QuestionID, "SCQ", QuestionMarks,
									QuestionBody);
							parseSCQ(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("FIB")) {
							createExam(ChapterID, ExamID, ExamName, ExamTime,
									QuestionID, "FIB", QuestionMarks,
									QuestionBody);
							parseFIB(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("LDG")) {
							createExam(ChapterID, ExamID, ExamName, ExamTime,
									QuestionID, "M&M", QuestionMarks,
									QuestionBody);
							parseMM(questionBlock, ChapterID, ExamID,
									QuestionID);
						}

						updateChapter(ChapterID, "T");
					}

					StatusMsg = 0;
				} else {
					StatusMsg = -6;
				}

			} catch (Exception e) {
				StatusMsg = -3;

			}
		} else {
			StatusMsg = -5;
		}

		return StatusMsg;
	}

	private void parseMM(SoapObject soapObj, String chapterID, String examID,
			String questionID) {
		try {
			SoapObject QOptionBlock = (SoapObject) soapObj.getProperty("Q"
					+ questionID);

			if (QOptionBlock != null) {

				for (int iQAidx = 0; iQAidx < QOptionBlock.getPropertyCount(); iQAidx++) {
					String QAttribute1 = null;

					SoapObject questionAtt1Block = (SoapObject) QOptionBlock
							.getProperty(iQAidx);
					QAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();

					if (QAttribute1 != null) {
						createQuestionAttribute(chapterID, examID, questionID,
								QAttribute1, "Dr.", "0");
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		try {
			SoapObject AOptionBlock = (SoapObject) soapObj.getProperty("A"
					+ questionID);

			if (AOptionBlock != null) {

				for (int iAAidx = 0; iAAidx < AOptionBlock.getPropertyCount(); iAAidx++) {
					String AAttribute1 = null;
					String AAttribute2 = null;
					String AAttribute3 = null;

					SoapObject questionAtt1Block = (SoapObject) AOptionBlock
							.getProperty(iAAidx);
					AAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();
					AAttribute2 = questionAtt1Block.getAttribute("attribute2")
							.toString();
					AAttribute3 = questionAtt1Block.getAttribute("attribute3")
							.toString();

					if (AAttribute1 != null) {
						createAnswerAttribute(chapterID, examID, questionID,
								AAttribute1, AAttribute2, AAttribute3);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		return;
	}

	private void parseMCQ(SoapObject soapObj, String chapterID, String examID,
			String questionID) {
		try {
			SoapObject QOptionBlock = (SoapObject) soapObj.getProperty("Q"
					+ questionID);

			if (QOptionBlock != null) {

				for (int iQAidx = 0; iQAidx < QOptionBlock.getPropertyCount(); iQAidx++) {
					String QAttribute1 = null;
					String QAttribute2 = null;

					SoapObject questionAtt1Block = (SoapObject) QOptionBlock
							.getProperty(iQAidx);
					QAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();
					QAttribute2 = questionAtt1Block.getAttribute("attribute2")
							.toString();

					if (QAttribute1 != null || QAttribute2 != null) {
						createQuestionAttribute(chapterID, examID, questionID,
								QAttribute1, QAttribute2, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		try {
			SoapObject AOptionBlock = (SoapObject) soapObj.getProperty("A"
					+ questionID);

			if (AOptionBlock != null) {
				for (int iAAidx = 0; iAAidx < AOptionBlock.getPropertyCount(); iAAidx++) {
					String AAttribute1 = null;
					String AAttribute2 = null;

					SoapObject answerAtt1Block = (SoapObject) AOptionBlock
							.getProperty(iAAidx);
					AAttribute1 = answerAtt1Block.getAttribute("attribute1")
							.toString();
					AAttribute2 = answerAtt1Block.getAttribute("attribute2")
							.toString();

					if (AAttribute1 != null || AAttribute2 != null) {
						createAnswerAttribute(chapterID, examID, questionID,
								AAttribute1, AAttribute2, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}
		return;
	}

	private void parseFIB(SoapObject soapObj, String chapterID, String examID,
			String questionID) {
		try {
			SoapObject QOptionBlock = (SoapObject) soapObj.getProperty("Q"
					+ questionID);

			if (QOptionBlock != null) {

				for (int iQAidx = 0; iQAidx < QOptionBlock.getPropertyCount(); iQAidx++) {
					String QAttribute1 = null;
					SoapObject questionAtt1Block = (SoapObject) QOptionBlock
							.getProperty(iQAidx);
					QAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();

					if (QAttribute1 != null) {
						createQuestionAttribute(chapterID, examID, questionID,
								QAttribute1, null, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		try {
			SoapObject AOptionBlock = (SoapObject) soapObj.getProperty("A"
					+ questionID);

			if (AOptionBlock != null) {
				for (int iAAidx = 0; iAAidx < AOptionBlock.getPropertyCount(); iAAidx++) {
					String AAttribute1 = null;
					SoapObject questionAtt1Block = (SoapObject) AOptionBlock
							.getProperty(iAAidx);
					AAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();

					if (AAttribute1 != null) {
						createAnswerAttribute(chapterID, examID, questionID,
								AAttribute1, null, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		return;
	}

	private void parseSCQ(SoapObject soapObj, String chapterID, String examID,
			String questionID) {
		try {
			SoapObject QOptionBlock = (SoapObject) soapObj.getProperty("Q"
					+ questionID);

			if (QOptionBlock != null) {

				for (int iQAidx = 0; iQAidx < QOptionBlock.getPropertyCount(); iQAidx++) {
					String QAttribute1 = null;

					SoapObject questionAtt1Block = (SoapObject) QOptionBlock
							.getProperty(iQAidx);
					QAttribute1 = questionAtt1Block.getAttribute("attribute1")
							.toString();

					if (QAttribute1 != null) {
						createQuestionAttribute(chapterID, examID, questionID,
								QAttribute1, null, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		try {
			SoapObject AOptionBlock = (SoapObject) soapObj.getProperty("A"
					+ questionID);

			if (AOptionBlock != null) {
				for (int iAAidx = 0; iAAidx < AOptionBlock.getPropertyCount(); iAAidx++) {
					String AAttribute1 = null;

					SoapObject answerAtt1Block = (SoapObject) AOptionBlock
							.getProperty(iAAidx);
					AAttribute1 = answerAtt1Block.getAttribute("attribute1")
							.toString();

					if (AAttribute1 != null) {
						createAnswerAttribute(chapterID, examID, questionID,
								AAttribute1, null, null);
					}
				}
			}
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return;
		}

		return;
	}

	DownloadOptions downloadOptionSelected = DownloadOptions.MockExam;

	private long createExam(String id_chapter, String id_exam,
			String name_exam, int exam_time, String id_question,
			String question_type, int marks, String body) {
		long ret = 0;

		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_ID_CHAPTER, id_chapter);
			values.put(DatabaseHelper.FLD_ID_EXAM, id_exam);
			values.put(DatabaseHelper.FLD_EXAM_NAME, name_exam);
			values.put(DatabaseHelper.FLD_EXAM_TIME, exam_time);
			values.put(DatabaseHelper.FLD_ID_QUESTION, id_question);
			values.put(DatabaseHelper.FLD_QUESTION_TYPE, question_type);
			values.put(DatabaseHelper.FLD_QUESTION_MARKS, marks);
			values.put(DatabaseHelper.FLD_QUESTION_BODY, body);
			values.put(DatabaseHelper.FLD_QUESTION_ANSWERED, "F");
			values.put(DatabaseHelper.FLD_ANSWER_CORRECT, "F");

			ret = db.insert(DatabaseHelper.TBL_EXAM, null, values);
		} catch (SQLiteException sqle) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		}

		return ret;
	}

	private long createQuestionAttribute(String id_chapter, String id_exam,
			String id_question, String attribute_1, String attribute_2,
			String attribute_3) {
		long ret = 0;

		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_ID_CHAPTER, id_chapter);
			values.put(DatabaseHelper.FLD_ID_EXAM, id_exam);
			values.put(DatabaseHelper.FLD_ID_QUESTION, id_question);
			values.put(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1, attribute_1);
			values.put(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_2, attribute_2);
			values.put(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_3, attribute_3);

			ret = db.insert(DatabaseHelper.TBL_QUESTION_ATTRIBUTE, null, values);
		} catch (SQLiteException sqle) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		}
		return ret;
	}

	private long createAnswerAttribute(String id_chapter, String id_exam,
			String id_question, String attribute_1, String attribute_2,
			String attribute_3) {
		long ret = 0;

		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_ID_CHAPTER, id_chapter);
			values.put(DatabaseHelper.FLD_ID_EXAM, id_exam);
			values.put(DatabaseHelper.FLD_ID_QUESTION, id_question);
			values.put(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1, attribute_1);
			values.put(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_2, attribute_2);
			values.put(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_3, attribute_3);

			ret = db.insert(DatabaseHelper.TBL_ANSWER_ATTRIBUTE, null, values);
		} catch (SQLiteException sqle) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		}

		return ret;
	}

	private long updateChapter(String id_chapter, String downloaded) {
		long ret = 0;

		try {
			Cursor cursorUser = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_CHAPTER + " where "
					+ DatabaseHelper.FLD_ID_CHAPTER + " ='" + id_chapter + "'",
					null);
			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				ret = cursorUser.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * // ///////////////////////////////
		 * 
		 * try { String[] args = { new Integer(id_chapter).toString() }; String
		 * query = "UPDATE " + DatabaseHelper.TBL_CHAPTER + " SET " +
		 * DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER + "='" + downloaded + "'"
		 * + " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + "='" + id_chapter +
		 * "'";
		 * 
		 * Cursor cu = db.rawQuery(query, args);
		 * 
		 * if (cu != null) { int userCount = cu.getCount(); } cu.moveToFirst();
		 * cu.close(); } catch (Exception e1) { // TODO Auto-generated catch
		 * block e1.printStackTrace(); }
		 * 
		 * // /////////////////////////////////////
		 */
		try {
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.FLD_EXAM_DOWNLOADED_CHAPTER, downloaded);
			values.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER, "F");

			ret = db.update(DatabaseHelper.TBL_CHAPTER, values,
					DatabaseHelper.FLD_ID_CHAPTER + " = ?",
					new String[] { id_chapter });
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			mProgressHandler.sendEmptyMessage(-3);
			return ret;
		}

		return ret;
	}

	Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);

			AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
					(SubjectList_Exam) CurContext);

			switch (msg.what) {
			case 0:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.information);
				dlgMsgbuilder.setTitle("Download status");

				String SuccessMessage = "";

				switch (downloadOptionSelected) {
				case MockExam:
					SuccessMessage = "Exam has been successfully downloaded to your device. To start exam select 'Mock Test' from Mock home.";
					break;
				case PracticeExam:
					SuccessMessage = "Practice Exam has been successfully downloaded to your device. To start exam select 'Class Exercise' from Mock home.";
					break;
				case StudyMaterials:
					SuccessMessage = "Study Material has been successfully downloaded to your device. To start reading select 'Study Material' from Mock home.";
					break;
				}

				dlgMsgbuilder
						.setMessage(SuccessMessage
								+ "Press 'Ok' to proceed with the exam.'Cancel' to skip exam.'");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();

							}
						});

				dlgMsgbuilder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {

									dialog.dismiss();
									InvokeExamIntent();
									// Exam Call
									// startActivity(MainIntent);
									// finish();
								} catch (SQLiteException sqle) {
									Toast.makeText(getApplicationContext(),
											"1 : " + sqle.getMessage(),
											Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(),
											"2 : " + e.getMessage(),
											Toast.LENGTH_SHORT).show();
								}
							}
						});

				AlertDialog altEndDownload = dlgMsgbuilder.create();
				altEndDownload.show();

				break;
			case -1:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.error);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Application error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			case -2:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.error);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Connection error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;

			case -3:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.warning);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Application error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			case -5:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.warning);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder
						.setMessage("Data Exception! Invalid Data.Contact Admin.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			case -6:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.information);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder
						.setMessage("No questions available under this level.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			}
		}
	};

	public void InvokeExamIntent() {

		adStartExam.setIcon(R.drawable.bt_question);
		adStartExam.setTitle("Exam status");
		adStartExam.setPositiveButton("Yes", null).create();
		adStartExam.setNegativeButton("No", null).create();
		adStartExam
				.setMessage(
						"Are you ready to take the exam? 'Yes' will start the exam and 'No' will cancel.")
				.setPositiveButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})

				.setNegativeButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								fillExam(SelectedSubjectID, SelectedChapterID);
							}
						});

		AlertDialog altStartExam = adStartExam.create();
		altStartExam.show();

		// /Exam Activity

	}

}
