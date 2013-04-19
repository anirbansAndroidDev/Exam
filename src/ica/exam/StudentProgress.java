package ica.exam;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ica.ICAConstants.CourseMatIntent;
import ica.ICAConstants.DownloadOptions;
import ica.ICAConstants.ExamStatusTypes;
import ica.ICAConstants.UploadTask;
import ica.ICAServiceHandler.ChapterMarksComparisonService;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ICAServiceHandler.ModuleMarksComparisonService;
import ica.ICAServiceHandler.SubjectMarksComparisonService;
import ica.ProfileInfo.ChapterInfo;
import ica.ProfileInfo.ModuleInfo;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.SubjectInfo;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppPreferenceStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

@SuppressWarnings("deprecation")
public class StudentProgress extends TabActivity {

	private ProgressDialog pgLogin;
	private Activity actvity;

	StudentDetails StudentInfo = null;
	public Context CurContext;

	String sEmail;
	LinearLayout llModule;
	LinearLayout llSubject;
	Context IndexContext;
	SubjectInfo selectedSubjectedInfo;

	ChapterMarksComparisonService chapterMarksComparisonService;
	ModuleMarksComparisonService moduleMarksComparisonService;
	SubjectMarksComparisonService subjectMarksComparisonService;

	TableLayout tblVuModule;
	TableLayout tblVuSubject;
	private ExpandableListView expList;

	ExamSyncService mExamSyncService;

	ProgressDialog pgExam;

	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.studentprogress);
		IndexContext = this;
		CurContext = this;
		Resources res = getResources();
		TabHost tabHost = getTabHost();

		TabHost.TabSpec spec;
		spec = tabHost.newTabSpec("ModuleTab")
				.setIndicator("Module", res.getDrawable(R.drawable.tab_state))
				.setContent(R.id.tab1_layout_Module);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("SubjectTab")
				.setIndicator("Subject", res.getDrawable(R.drawable.tab_state))
				.setContent(R.id.tab2_layout_Subject);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("ChapterTab")
				.setIndicator("Chapter", res.getDrawable(R.drawable.tab_state))
				.setContent(R.id.tab3_layout_Chapter);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);

		// ///Set Up Progress

		actvity = this;
		CurContext = this;

		mExamSyncService = new ExamSyncService(CurContext);

		StudentDetails.initInstance(CurContext);

		StudentInfo = StudentDetails.getInstance();

		setTitle("Student Progress- [" + StudentInfo.getStudentID() + "]");

		moduleMarksComparisonService = new ModuleMarksComparisonService(
				CurContext);
		subjectMarksComparisonService = new SubjectMarksComparisonService(
				CurContext);
		chapterMarksComparisonService = new ChapterMarksComparisonService(
				CurContext);

		llModule = (LinearLayout) findViewById(R.id.llModuleParent);
		llModule.setAnimation(AnimationUtils.makeInAnimation(CurContext, true));

		llSubject = (LinearLayout) findViewById(R.id.llSubjectParent);
		llSubject
				.setAnimation(AnimationUtils.makeInAnimation(CurContext, true));

		tblVuModule = (TableLayout) findViewById(R.id.tblModule);
		tblVuSubject = (TableLayout) findViewById(R.id.tblSubject);

		sEmail = StudentInfo.getStudentID();

		if (sEmail != null) {

			new AsyncDownloader().execute("");
		}

		expList = (ExpandableListView) findViewById(R.id.ExpandableListView01);
		expList.setAdapter(new ExpAdapter(this));

		pgExam = new ProgressDialog(CurContext);
		pgExam.setMessage("Please wait while downloading...");
		pgExam.setIndeterminate(true);
		pgExam.setCancelable(false);
		pgExam.setCanceledOnTouchOutside(false);

		expList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				Log.e("onGroupExpand", "OK");
				if (SubjectList != null && SubjectList.size() > 0
						&& SubjectList.get(groupPosition) != null) {
					selectedSubjectedInfo = SubjectList.get(groupPosition);
				} else {
					selectedSubjectedInfo = null;
				}

			}
		});

		expList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.e("onGroupCollapse", "OK");

				selectedSubjectedInfo = null;
			}
		});

		expList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				ChapterInfo curChapter = (ChapterInfo) v.getTag();

				if (curChapter != null) {
					// Upload Last Exam And Download Latest Mock Exam

					AppPreferenceStatus.setStudyDownload(CurContext,
							CourseMatIntent.MockExam);
					MockExamActivity(curChapter);
				}
				return false;
			}
		});
	}

	private String WSURL;
	private String WSNameSpace;
	private String WSMethod;
	private String SPOAction;

	private void InitiateSoapResource() {

		WSURL = CurContext.getString(R.string.SOAP_URL);
		WSNameSpace = CurContext.getString(R.string.WEBSERVICE_NAMESPACE);
		WSMethod = CurContext.getString(R.string.LEVEL_METHOD_NAME);
		SPOAction = CurContext.getString(R.string.LEVEL_SOAP_ACTION);

	}

	Builder adLevel;
	Builder adStartExam;

	String SelectedLevelID;
	String SelectedSubjectID;
	String SelectedChapterID;

	boolean isMock = true;
	private String ActionType;

	private void MockExamActivity(ChapterInfo chapter) {

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

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		TaskStatusMsg info = new TaskStatusMsg();

		info.setTaskDone(UploadTask.Upload);
		info.setTitle("Sync Exam");

		adLevel = new Builder(CurContext);
		adStartExam = new Builder(CurContext);

		InitiateSoapResource();

		SelectedSubjectID = Integer.toString(chapter.getSubjectID());
		SelectedChapterID = Integer.toString(chapter.getId());

		try {

			HttpTransportSE androidHttpTransport = null;
			SoapSerializationEnvelope envelope = null;

			SoapObject request = null;
			SoapObject soapResult = null;

			try {
				request = new SoapObject(WSNameSpace, WSMethod);

				PropertyInfo inf_userId = new PropertyInfo();
				inf_userId.setName("stringemailid");
				StudentInfo = StudentDetails.getInstance();

				inf_userId.setValue(StudentInfo.getStudentID());
				request.addProperty(inf_userId);

				PropertyInfo inf_subjectid = new PropertyInfo();
				inf_subjectid.setName("stringSubjectId");
				inf_subjectid.setValue(SelectedSubjectID);
				request.addProperty(inf_subjectid);

				PropertyInfo inf_chapterid = new PropertyInfo();
				inf_chapterid.setName("stringChapterid");
				inf_chapterid.setValue(SelectedChapterID);
				request.addProperty(inf_chapterid);

				envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				envelope.setOutputSoapObject(request);

				androidHttpTransport = new HttpTransportSE(WSURL);
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-1);
				return;
			}

			try {
				androidHttpTransport.call(SPOAction, envelope);
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-2);
				return;
			}

			try {
				soapResult = (SoapObject) envelope.bodyIn;
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-5);
				return;
			}
			if (soapResult != null) {
				try {
					SoapObject soapBlock = (SoapObject) soapResult
							.getProperty(0);
					SoapObject rootBlock = (SoapObject) soapBlock
							.getProperty(0);

					if (rootBlock.getPropertyCount() > 0) {
						final String[] aLevelID = new String[rootBlock
								.getPropertyCount()];
						final CharSequence[] aLevelName = new CharSequence[rootBlock
								.getPropertyCount()];

						for (int iLevel = 0; iLevel < rootBlock
								.getPropertyCount(); iLevel++) {
							SoapObject levelBlock = (SoapObject) rootBlock
									.getProperty(iLevel);

							aLevelID[iLevel] = levelBlock.getAttribute(0)
									.toString();
							aLevelName[iLevel] = levelBlock.getAttribute(3)
									.toString();
						}

						adLevel.setTitle("Level(s)");
						adLevel.setIcon(R.drawable.folder_yellow);
						adLevel.setItems(aLevelName,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {

										try {
											SelectedLevelID = aLevelID[item]
													.toString();
										} catch (Exception e) {
											return;
										}

										adStartExam
												.setIcon(R.drawable.bt_question);
										adStartExam.setTitle("Download status");
										adStartExam.setPositiveButton("Yes",
												null).create();
										adStartExam
												.setMessage(
														"Do you want to download "
																+ ActionType
																+ " from Server?  'Yes' will download the exam and 'No' will cancel download.")
												.setNegativeButton(
														"No",
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																dialog.cancel();
															}
														})
												.setPositiveButton(
														"Yes",
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																dialog.dismiss();
																pgExam.show();

																new AsyncQuestionDownloader()
																		.execute("");

															}
														});
										AlertDialog altStartExam = adStartExam
												.create();
										altStartExam.show();

									}
								});
						AlertDialog altLevel = adLevel.create();
						altLevel.show();

					} else {
						adStartExam.setIcon(R.drawable.information);
						adStartExam.setTitle("Download status");
						adStartExam.setPositiveButton("Ok", null).create();
						adStartExam
								.setMessage(
										"There is no "
												+ ActionType
												+ " available to download under this chapter.")
								.setCancelable(false)
								.setPositiveButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.dismiss();
												return;
											}
										});
						AlertDialog altStartExam = adStartExam.create();
						altStartExam.show();
					}

				} catch (Exception e) {
					mProgressHandler.sendEmptyMessage(-3);
					return;
				}
			}
			return;

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	// /UPLOAD / DOWNLOAD EXAM

	public class AsyncQuestionDownloader extends
			AsyncTask<String, TaskStatusMsg, Integer> {

		@Override
		protected Integer doInBackground(String... params) {

			TaskStatusMsg infoUpload = mExamSyncService.AnswerUpload(
					StudentInfo, actvity);

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

			PropertyInfo inf_studentcode = new PropertyInfo();
			inf_studentcode.setName("StudentCode");
			inf_studentcode.setValue(StudentInfo.getStudentStatusCode());
			request.addProperty(inf_studentcode);

			PropertyInfo inf_subjectid = new PropertyInfo();
			inf_subjectid.setName("requestQuestionSubjectId");
			inf_subjectid.setValue(SubjectID);
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

			AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(actvity);

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
				dlgMsgbuilder.setNegativeButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {

									dialog.dismiss();
									InvokeExamIntent();
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

				dlgMsgbuilder.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();

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

	public class ExpAdapter extends BaseExpandableListAdapter {

		private Context myContext;

		public ExpAdapter(Context context) {
			myContext = context;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		private ArrayList<ChapterInfo> getAllChapter(String SubjectName) {

			ArrayList<ChapterInfo> lstChapter = new ArrayList<ChapterInfo>();

			for (ChapterInfo chapter : ChapterList) {
				if (chapter.getSubjectName().equals(SubjectName)) {
					lstChapter.add(chapter);
				}
			}

			return lstChapter;
		}

		ArrayList<ChapterInfo> lstChapter = null;

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.expandable_child_row,
						null);
			}

			// Fetch Data

			if (SubjectList != null && SubjectList.size() > 0
					&& SubjectList.get(groupPosition) != null) {
				selectedSubjectedInfo = SubjectList.get(groupPosition);
			} else {
				selectedSubjectedInfo = null;
			}

			if (selectedSubjectedInfo != null
					&& lstChapter != null
					&& (lstChapter.size() > 0 && lstChapter.size() > childPosition + 1)
					&& lstChapter.get(childPosition) != null) {

				ChapterInfo chapterInfo = lstChapter.get(childPosition);
				convertView.setTag(chapterInfo);
				SyncChapterViewToData(chapterInfo, childPosition, convertView);

			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {

			// Size of Child Array

			if (SubjectList != null && SubjectList.size() > 0
					&& SubjectList.get(groupPosition) != null) {
				selectedSubjectedInfo = SubjectList.get(groupPosition);
			} else {
				selectedSubjectedInfo = null;
			}

			if (selectedSubjectedInfo != null) {
				lstChapter = null;

				lstChapter = getAllChapter(selectedSubjectedInfo.getName());
			}

			if (lstChapter == null)
				return 0;
			else
				return lstChapter.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return SubjectList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.expandable_group_row,
						null);
			}

			TextView tvGroupName = (TextView) convertView
					.findViewById(R.id.tvGroupName);
			SubjectInfo subjInfo = SubjectList.get(groupPosition);
			tvGroupName.setText(subjInfo.getName());

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	public class AsyncDownloader extends AsyncTask<String, String, Boolean> {

		private void moduleHeader() {
			LayoutInflater factory = LayoutInflater.from(CurContext);

			View VuProgressRowHDR = factory.inflate(
					R.layout.examprogressrowhdr, null);

			TableRow llProgressRowHdr = (TableRow) VuProgressRowHDR
					.findViewById(R.id.trExamProgressHdr);

			TextView txtSubNameHdr = (TextView) VuProgressRowHDR
					.findViewById(R.id.txtParentIDHdr);
			txtSubNameHdr.setVisibility(View.VISIBLE);
			txtSubNameHdr.setText("");

			tblVuModule.addView(llProgressRowHdr);
		}

		private void subjectHeader() {
			LayoutInflater factory = LayoutInflater.from(CurContext);

			View VuProgressRowHDR = factory.inflate(
					R.layout.examprogressrowhdr, null);
			TableRow llProgressSubjectRowHdr = (TableRow) VuProgressRowHDR
					.findViewById(R.id.trExamProgressHdr);

			TextView txtSubNameHdr = (TextView) VuProgressRowHDR
					.findViewById(R.id.txtParentIDHdr);
			txtSubNameHdr.setVisibility(View.VISIBLE);
			txtSubNameHdr.setText("");

			tblVuSubject.addView(llProgressSubjectRowHdr);

		}

		private void chapterHeader() {
			LayoutInflater factory = LayoutInflater.from(CurContext);

			View VuProgressRowHDR = factory.inflate(
					R.layout.examprogressrowhdr, null);

			TextView txtSubNameHdr = (TextView) VuProgressRowHDR
					.findViewById(R.id.txtParentIDHdr);
			txtSubNameHdr.setVisibility(View.VISIBLE);
			txtSubNameHdr.setText("Subject");

			// tblVuChapter.addView(llProgressChapterRowHdr);
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			moduleHeader();
			subjectHeader();
			chapterHeader();

			pgLogin = new ProgressDialog(CurContext);
			pgLogin.setMessage("Please wait while view is synced...");
			pgLogin.setIndeterminate(true);

			pgLogin.setCancelable(false);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

		}

		@Override
		protected void onProgressUpdate(String... values) {

			super.onProgressUpdate(values);

			ShowMessage(values[0]);
		}

		private void ShowMessage(String Message) {
			Toast.makeText(CurContext, Message, Toast.LENGTH_LONG).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			String sEmail = StudentDetails.getInstance().getStudentID();

			if (sEmail != null) {

				if (StudentInfo.getStudentID() != null) {

					ModuleList = moduleMarksComparisonService
							.getAllModules(StudentInfo.getStudentID());
					SubjectList = subjectMarksComparisonService.getAllSubject();

					ChapterList = chapterMarksComparisonService.getAllChapter();

					if ((ModuleList != null && ModuleList.size() > 0)
							&& (SubjectList != null && ModuleList.size() > 0)
							&& (ChapterList != null && ChapterList.size() > 0)) {
						publishProgress("View Synced...");
					} else {
						publishProgress("No Data available.Please try after sync.");
					}

				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);

			if (ModuleList != null && ModuleList.size() > 0) {
				int cnt = 1;
				for (ModuleInfo moduleInfo : ModuleList) {
					SyncModuleViewToData(moduleInfo, cnt);
					cnt++;
				}

			}

			if (SubjectList != null && SubjectList.size() > 0) {
				int cnt = 1;
				for (SubjectInfo subjectInfo : SubjectList) {
					SyncSubjectViewToData(subjectInfo, cnt);
					cnt++;
				}

			}

			expList.setAdapter(new ExpAdapter(CurContext));

			try {
				if (pgLogin != null) {
					if (pgLogin.isShowing()) {
						pgLogin.cancel();
						pgLogin.dismiss();
					}
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	List<ModuleInfo> ModuleList = new ArrayList<ModuleInfo>();
	List<ChapterInfo> ChapterList = new ArrayList<ChapterInfo>();
	List<SubjectInfo> SubjectList = new ArrayList<SubjectInfo>();

	public void SyncModuleViewToData(ModuleInfo moduleInfo, int idx) {

		LayoutInflater factory = LayoutInflater.from(CurContext);

		View VuProgressRow = factory.inflate(R.layout.exampogressrow, null);
		TableRow llProgressRow = (TableRow) VuProgressRow
				.findViewById(R.id.trExamProgress);

		TextView txtProgSeqnc = (TextView) VuProgressRow
				.findViewById(R.id.txtSequence);

		txtProgSeqnc.setText(Integer.toString(idx));

		TextView txtProgName = (TextView) VuProgressRow
				.findViewById(R.id.txtPrgrssName);

		txtProgName.setText(moduleInfo.getName());

		TextView txtProgSubjectName = (TextView) VuProgressRow
				.findViewById(R.id.txtParentID);

		txtProgSubjectName.setText("");

		txtProgSubjectName.setVisibility(View.GONE);

		TextView txtHiddenID = (TextView) VuProgressRow
				.findViewById(R.id.txtHdnRowID);

		txtHiddenID.setText(Integer.toString(moduleInfo.getId()));

		TextView txtMarks = (TextView) VuProgressRow
				.findViewById(R.id.txtMarks);

		txtMarks.setText(Long.toString(Math.round(moduleInfo.getMarks())));

		TextView txtHimarks = (TextView) VuProgressRow
				.findViewById(R.id.txtHighestObtained);

		txtHimarks.setText(Long.toString(Math.round(moduleInfo.getHiMarks())));

		String Status = moduleInfo.getCompletionStatus();

		ExamStatusUISync(Status, VuProgressRow);

		llProgressRow.setPadding(0, 0, 0, 10);

		tblVuModule.addView(llProgressRow);
		tblVuModule.requestLayout();

	}

	private void ExamStatusUISync(String Status, View VuProgressRow) {
		ImageView imgBeg = (ImageView) VuProgressRow
				.findViewById(R.id.imgBeginner);
		ImageView imgIntemed = (ImageView) VuProgressRow
				.findViewById(R.id.imgIntermed);
		ImageView imgExpert = (ImageView) VuProgressRow
				.findViewById(R.id.imgExpert);

		switch (ExamStatusTypes.valueOf(Status)) {
		case Unattempted:
			imgBeg.setBackgroundResource(R.drawable.glossy_yello);
			imgIntemed.setBackgroundResource(R.drawable.glossy_yello);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);

			break;
		case Beginner:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_yello);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);

			break;
		case Intermediate:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_green);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);

			break;
		case Advanced:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_green);
			imgExpert.setBackgroundResource(R.drawable.glossy_green);

			break;
		}
	}

	public void SyncSubjectViewToData(SubjectInfo subjectInfo, int idx) {

		LayoutInflater factory = LayoutInflater.from(CurContext);

		View VuProgressRow = factory.inflate(R.layout.exampogressrow, null);
		TableRow llProgressRow = (TableRow) VuProgressRow
				.findViewById(R.id.trExamProgress);

		TextView txtProgSeqnc = (TextView) VuProgressRow
				.findViewById(R.id.txtSequence);

		txtProgSeqnc.setText(Integer.toString(idx));

		TextView txtProgSubjectName = (TextView) VuProgressRow
				.findViewById(R.id.txtParentID);

		txtProgSubjectName.setText("");

		txtProgSubjectName.setVisibility(View.GONE);

		TextView txtProgName = (TextView) VuProgressRow
				.findViewById(R.id.txtPrgrssName);

		txtProgName.setText(subjectInfo.getName());

		TextView txtHiddenID = (TextView) VuProgressRow
				.findViewById(R.id.txtHdnRowID);

		txtHiddenID.setText(Integer.toString(subjectInfo.getId()));

		TextView txtMarks = (TextView) VuProgressRow
				.findViewById(R.id.txtMarks);

		txtMarks.setText(Long.toString(Math.round(subjectInfo.getMarks())));

		TextView txtHimarks = (TextView) VuProgressRow
				.findViewById(R.id.txtHighestObtained);

		txtHimarks.setText(Long.toString(Math.round(subjectInfo.getHiMarks())));

		String Status = subjectInfo.getCompletionStatus();

		ExamStatusUISync(Status, VuProgressRow);

		llProgressRow.setPadding(0, 0, 0, 10);

		tblVuSubject.addView(llProgressRow);
		tblVuSubject.requestLayout();

	}

	public void SyncChapterViewToData(ChapterInfo chapterInfo, int idx,
			View VuProgressRow) {

		TextView txtProgSeqnc = (TextView) VuProgressRow
				.findViewById(R.id.txtExpSequence);

		txtProgSeqnc.setText(Integer.toString(idx));

		TextView txtProgSubjectName = (TextView) VuProgressRow
				.findViewById(R.id.txtExpParentID);

		txtProgSubjectName.setText(chapterInfo.getSubjectName());

		txtProgSubjectName.setVisibility(View.VISIBLE);

		TextView txtProgName = (TextView) VuProgressRow
				.findViewById(R.id.txtExpPrgrssName);

		txtProgName.setText(chapterInfo.getName());

		TextView txtHiddenID = (TextView) VuProgressRow
				.findViewById(R.id.txtExpHdnRowID);

		txtHiddenID.setText(Integer.toString(chapterInfo.getId()));

		TextView txtMarks = (TextView) VuProgressRow
				.findViewById(R.id.txtExpMarks);

		txtMarks.setText(Long.toString(Math.round(chapterInfo.getMarks())));

		TextView txtHimarks = (TextView) VuProgressRow
				.findViewById(R.id.txtExpHighestObtained);

		txtHimarks.setText(Long.toString(Math.round(chapterInfo.getHiMarks())));

		String Status = chapterInfo.getCompletionStatus();

		ImageView imgBeg = (ImageView) VuProgressRow
				.findViewById(R.id.imgExpBeginner);
		ImageView imgIntemed = (ImageView) VuProgressRow
				.findViewById(R.id.imgExpIntermed);
		ImageView imgExpert = (ImageView) VuProgressRow
				.findViewById(R.id.imgExpExpert);

		switch (ExamStatusTypes.valueOf(Status)) {
		case Unattempted:
			imgBeg.setBackgroundResource(R.drawable.glossy_yello);
			imgIntemed.setBackgroundResource(R.drawable.glossy_yello);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);
			break;
		case Beginner:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_yello);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);
			break;
		case Intermediate:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_green);
			imgExpert.setBackgroundResource(R.drawable.glossy_yello);
			break;
		case Advanced:
			imgBeg.setBackgroundResource(R.drawable.glossy_green);
			imgIntemed.setBackgroundResource(R.drawable.glossy_green);
			imgExpert.setBackgroundResource(R.drawable.glossy_green);
			break;
		}

	}

	int tabHeight = 40;

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

	private long ExamTimeElapsed;

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
	@Override
	public void onBackPressed() {
		//super.onBackPressed();

		//finish();
		//Toast.makeText( getApplicationContext(),"Back pressed",Toast.LENGTH_SHORT).show();
		
		//==================================================================================================================
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this); 
		  
        alertDialog.setTitle("Confirm Exit ..."); 
        alertDialog.setMessage("Are you sure to exit ?"); 
        alertDialog.setIcon(R.drawable.tick); 
  
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog,int which) {
            	AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
        		finish();
        		//System.runFinalizersOnExit(true);
        		//System.exit(0);
        		Intent intent = new Intent(Intent.ACTION_MAIN);
        		intent.addCategory(Intent.CATEGORY_HOME);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
            } 
        }); 
  
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int which) { 
            dialog.cancel(); 
            } 
        }); 
  
        alertDialog.show();
		//==================================================================================================================
	}

}
