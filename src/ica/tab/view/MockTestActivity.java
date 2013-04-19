package ica.tab.view;

import static ica.exam.IndexActivity.ExamStatusCode;
import ica.ICAConstants.CourseMatIntent;
import ica.ICAServiceHandler.ChapterMarksComparisonService;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ICAServiceHandler.ModuleMarksComparisonService;
import ica.ICAServiceHandler.ScheduleDataHandler;
import ica.ICAServiceHandler.SubjectMarksComparisonService;
import ica.ProfileInfo.ScheduleInfo;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppInfo;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.CalendarDateUtiliy;
import ica.exam.DatabaseHelper;
import ica.exam.ExamActivity;
import ica.exam.ExamResultActivity;
import ica.exam.Exam_Download;
import ica.exam.QuestionList_Exam;
import ica.exam.R;
import ica.exam.SubjectList_Exam;
import ica.exam.ExamActivity.AsyncFetchScheduleDates;
import ica.exam.ExamActivity.AsyncQuestionDownloader;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.examples.android.calendar.CalendarAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewSwitcher.ViewFactory;

public class MockTestActivity extends Activity implements ViewFactory {

	Context IndexContext = this;
	public static final int DownloadMockExamStatusCode = 2111;
	public static final int DownloadStudyMatStatusCode = 2112;
	public static final int DownloadPracticeExamStatusCode = 2113;
	public static final int MockExamStatusCode = 2114;

	private SQLiteDatabase db;
	private ProgressDialog pgLogin;
	private ProgressDialog pgUpload;
	LinearLayout llDotHome;

	StudentDetails StudentInfo = null;
	public Context CurContext;

	String sEmail;

	LinearLayout llModule;
	LinearLayout llChapter;
	LinearLayout llSubject;

	CalendarView CalendarWidet;

	ChapterMarksComparisonService chapterMarksComparisonService;
	ModuleMarksComparisonService moduleMarksComparisonService;
	SubjectMarksComparisonService subjectMarksComparisonService;
	ScheduleDataHandler mScheduleSyncService;

	ExamSyncService mExamSyncService;
	// //CALENDAR
	public Calendar month;
	public CalendarAdapter calendarAdapter;
	public ArrayList<String> calendarItems; // container to store some random
											// calendar

	// //CALENDAR

	ArrayList<ScheduleInfo> lstScheduleList;

	String strDate = "";
	private String ActionType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mock_test);
	
		CurContext = this.getParent();

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

		mScheduleSyncService = new ScheduleDataHandler(CurContext);
		mExamSyncService = new ExamSyncService(CurContext);

		db = (new DatabaseHelper(this)).getWritableDatabase();

		String currentDateTimeString = DateFormat.getDateInstance().format(
				new Date());

		StudentDetails.initInstance(CurContext);

		StudentInfo = StudentDetails.getInstance();

		CalendarWidet = (CalendarView) findViewById(R.id.calendarSchedule);

		CalendarWidet.setDate(System.currentTimeMillis(), true, true);
		TextView txtCalender = (TextView) findViewById(R.id.txtCalender);

		txtCalender.setText(currentDateTimeString);

		HorizontalScrollView hrView = (HorizontalScrollView) findViewById(R.id.horizontalexammenu);
		hrView.setHorizontalScrollBarEnabled(false);

		txtCalender.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// set up dialog
				// builder.setView(viewDialog);
				// alertDialog = builder.create();

				// dialog.setContentView(viewDialog);
				// dialog.setTitle("Student Schedule");
				// dialog.setCancelable(true);
				// there are a lot of settings, for dialog, check them all out!

				// set up text
				// TextView text = (TextView)
				// dialog.findViewById(R.id.TextView01);
				// text.setText(R.string.lots_of_text);
				//
				// //set up image view
				// ImageView img = (ImageView)
				// dialog.findViewById(R.id.ImageView01);
				// img.setImageResource(R.drawable.nista_logo);
				//
				// //set up button
				// Button button = (Button) dialog.findViewById(R.id.Button01);
				// button.setOnClickListener(new OnClickListener() {
				// @Override
				// public void onClick(View v) {
				// finish();
				// }
				// });
				// now that the dialog is set up, it's time to show it
				// alertDialog.show();

				// CalendarWidet.setDate(System.currentTimeMillis(), true,
				// true);

				// Intent calendarIntent=new Intent(CurContext,
				// StudentScheduleActivity.class);

				// startActivityForResult(calendarIntent, ExamStatusCode);

				// AlertDialog.Builder builder;
				// AlertDialog alertDialog;
				//
				//
				// LayoutInflater inflater = (LayoutInflater)
				// CurContext.getSystemService(LAYOUT_INFLATER_SERVICE);
				// View layout = inflater.inflate(R.layout.studentcalender,
				// (ViewGroup) findViewById(R.id.llcalendarparent));
				//
				//
				// builder = new AlertDialog.Builder(CurContext);
				// builder.setView(layout);
				// alertDialog = builder.create();

				// alertDialog.show();

			}
		});

		final ImageButton buttonDownload = (ImageButton) findViewById(R.id.btDownload);
		buttonDownload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ShowDownload();
			}
		});

		// //CALENDAR
		month = Calendar.getInstance();
		onNewIntent(getIntent());

		calendarItems = new ArrayList<String>();
		calendarAdapter = new CalendarAdapter(this, month);
		calendarAdapter.setToday(Calendar.getInstance());
		GridView gridview = (GridView) findViewById(R.id.calendarGridview);
		gridview.setAdapter(calendarAdapter);

		new AsyncFetchScheduleDates().execute(month);

		TextView title = (TextView) findViewById(R.id.calendarTitle);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

		TextView previous = (TextView) findViewById(R.id.calendarPrevious);
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (month.get(Calendar.MONTH) == month
						.getActualMinimum(Calendar.MONTH)) {
					month.set((month.get(Calendar.YEAR) - 1),
							month.getActualMaximum(Calendar.MONTH), 1);
				} else {
					month.set(Calendar.MONTH, month.get(Calendar.MONTH) - 1);
				}
				refreshCalendar();
			}
		});

		TextView next = (TextView) findViewById(R.id.calendarNext);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (month.get(Calendar.MONTH) == month
						.getActualMaximum(Calendar.MONTH)) {
					month.set((month.get(Calendar.YEAR) + 1),
							month.getActualMinimum(Calendar.MONTH), 1);
				} else {
					month.set(Calendar.MONTH, month.get(Calendar.MONTH) + 1);
				}
				refreshCalendar();

			}
		});

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView date = (TextView) v.findViewById(R.id.calendarDate);

				if (date instanceof TextView && !date.getText().equals("")) {

					String day = date.getText().toString();
					if (day.length() == 1) {
						day = "0" + day;
					}
					// return chosen date as string format

					strDate = "";
					strDate = day
							+ "/"
							+ android.text.format.DateFormat.format("MM/yyyy",
									month);

					Calendar curEventCal = CalendarDateUtiliy
							.stringToCalendar(strDate);
					lstScheduleList = null;
					lstScheduleList = fetchDateData(curEventCal);

					if (lstScheduleList != null && lstScheduleList.size() > 0) {
						final CharSequence[] aLevelName = new CharSequence[lstScheduleList
								.size()];
						int i = 0;
						for (ScheduleInfo schdl : lstScheduleList) {

							aLevelName[i++] = Integer.toString(schdl
									.getDayOfMonth())
									+ "/"
									+ Integer.toString(schdl.getMonth())
									+ "/"
									+ Integer.toString(schdl.getYear())
									+ " : "
									+ schdl.getMessage();

						}
						AlertDialog.Builder adLevel = new Builder(CurContext);

						adLevel.setTitle("Event(s)");
						adLevel.setIcon(R.drawable.information);
						adLevel.setItems(aLevelName,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {

										if (lstScheduleList != null
												&& lstScheduleList.size() > 0) {
											if (lstScheduleList.get(item) != null)
												ShowAddEditDlg(false,
														lstScheduleList
																.get(item),
														null);
										}
									}
								});
						adLevel.setPositiveButton("(+) Add New",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										ShowAddEditDlg(true, null, strDate);
									}
								});
						AlertDialog altLevel = adLevel.create();
						altLevel.show();

					} else {
						ShowAddEditDlg(true, null, strDate);
					}

				}

			}
		});

		// //CALENDAR

		final ImageButton buttonExam = (ImageButton) findViewById(R.id.btExam);
		buttonExam.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				AppPreferenceStatus.setLastExamStatus(CurContext, true);
				AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.MockExam);
				ShowExam();
			}
		});
		buttonExam.performClick();
		
		final ImageButton buttonResult = (ImageButton) findViewById(R.id.btResult);
		buttonResult.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ShowResult();

			}
		});

		ImageButton btnPDFChapters = (ImageButton) findViewById(R.id.btnStudyMaterials);

		btnPDFChapters.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AppPreferenceStatus.setStudyDownload(CurContext,
						CourseMatIntent.StudyMaterials);
				showStudyMat();
			}
		});

		ImageButton btnPractice = (ImageButton) findViewById(R.id.btnPracticeExam);

		btnPractice.setOnClickListener(new OnClickListener() {
			//
			public void onClick(View v) {

				AppPreferenceStatus.setLastExamStatus(CurContext, false);
				AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.PracticeExam);
				showPracticeExam();
			}
		});
		
	}

	public ArrayList<ScheduleInfo> fetchDateData(Calendar dateData) {
		ArrayList<ScheduleInfo> lstSchedule = new ArrayList<ScheduleInfo>();
		lstSchedule = mScheduleSyncService.getAllSchedulesByMonthofYear(
				StudentInfo, dateData);

		return lstSchedule;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

	}

	public void ShowAddEditDlg(boolean isAdd, ScheduleInfo schedule,
			String strDate) {

		AlertDialog.Builder adEditSchedule;
		final View vwLedger;

		LayoutInflater factory = LayoutInflater.from(CurContext);
		vwLedger = factory.inflate(R.layout.scheduleedit, null);
		adEditSchedule = new AlertDialog.Builder(CurContext);

		if (isAdd) {
			TextView txtDate = (TextView) vwLedger.findViewById(R.id.txtDate);
			txtDate.setText(strDate);
			vwLedger.setTag(null);

		} else {

			EditText txtMsg = (EditText) vwLedger
					.findViewById(R.id.editMessage);

			txtMsg.setText(schedule.getMessage());

			TextView txtDate = (TextView) vwLedger.findViewById(R.id.txtDate);
			txtDate.setText(schedule.getDayOfMonth() + "/"
					+ schedule.getMonth() + "/" + schedule.getYear());

			vwLedger.setTag(schedule);
		}

		adEditSchedule.setIcon(R.drawable.dlg_ledger);
		adEditSchedule.setTitle("Schedule entry");
		adEditSchedule
				.setMessage("To enter a new scehdule fill up the following fields and press 'Ok' .Press 'Cancel' to exit.");
		adEditSchedule.setView(vwLedger);
		adEditSchedule.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				})

		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText txtMsg = (EditText) vwLedger
						.findViewById(R.id.editMessage);
				TextView txtDate = (TextView) vwLedger
						.findViewById(R.id.txtDate);

				String EventMsg = txtMsg.getText().toString();
				String EventDate = txtDate.getText().toString();

				Calendar curEventCal = CalendarDateUtiliy
						.stringToCalendar(EventDate);

				ScheduleInfo newSchedule = new ScheduleInfo();

				StudentDetails.initInstance(CurContext);

				StudentInfo = StudentDetails.getInstance();

				String sUserID = StudentInfo.getStudentID();

				if (sUserID != null && !"".equals(sUserID)) {

					newSchedule.setUserName(sUserID);

					if (vwLedger.getTag() == null) {
						newSchedule.setNotificationType("S");
						newSchedule.setYear(curEventCal.get(Calendar.YEAR));
						newSchedule.setMonth(curEventCal.get(Calendar.MONTH));
						newSchedule.setDayOfMonth(curEventCal
								.get(Calendar.DAY_OF_MONTH));
						newSchedule.setIsSynced("N");
						newSchedule.setMessage(EventMsg);

						vwLedger.setTag(null);
					} else {
						newSchedule = (ScheduleInfo) vwLedger.getTag();
						newSchedule.setNotificationType("S");
						newSchedule.setIsSynced("N");
						newSchedule.setMessage(EventMsg);

					}

				} else {
					newSchedule = null;
				}

				if (newSchedule != null) {
					if (mScheduleSyncService.SaveUpdateSchedule(newSchedule) > 0) {
						Toast.makeText(
								CurContext,
								"Event creation successful.Please sync to update server.",
								Toast.LENGTH_LONG).show();

					} else {
						Toast.makeText(
								CurContext,
								"Event creation not successful.Please try again.",
								Toast.LENGTH_LONG).show();

					}
				} else {
					Toast.makeText(
							CurContext,
							"User data out of sync.Please Login again and perform sync.",
							Toast.LENGTH_LONG).show();
				}
				refreshCalendar();
			}
		});

		adEditSchedule.create();
		adEditSchedule.show();
		// ///

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		try {
			if (db != null) {
				db.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	@Override
//	public void onBackPressed() {
//
//		Intent intent = new Intent(CurContext, IndexActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
//	}

	private void ShowDownload() {
		try {
			Intent intent = new Intent(CurContext, Exam_Download.class);

			int IntentType = CourseMatIntent.DownlaodMock.getNumber();
			intent.putExtra("ExamIntent", IntentType);
			startActivityForResult(intent, ExamStatusCode);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void ShowExam() {
		try 
		{
			Intent intent = new Intent(CurContext, SubjectList_Exam.class);
			int IntentType = CourseMatIntent.MockExam.getNumber();
			intent.putExtra("ExamIntent", IntentType);
			startActivityForResult(intent, ExamStatusCode);

		} 
		catch (Exception e) 
		{
			Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
		}
	}

	private void showStudyMat() {
		try {
			Intent intent = new Intent(CurContext, Exam_Download.class);

			int IntentType = CourseMatIntent.StudyMaterials.getNumber();
			intent.putExtra("ExamIntent", IntentType);
			startActivityForResult(intent, ExamStatusCode);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void showPracticeExam() {
		// ShowExam();

		PracticeExamDownloader();
	}

	String SelectedSubjectID;
	String SelectedChapterID;
	String SelectedLevelID;
	private long ExamTimeElapsed;
	// ///Practice Exam Download

	private AlertDialog.Builder adStartExam;
	ProgressDialog pgExam;

	private void PracticeExamDownloader() {

		pgExam = new ProgressDialog(CurContext);
		pgExam.setMessage("Please wait while downloading...");
		pgExam.setIndeterminate(true);
		pgExam.setCancelable(false);
		pgExam.setCanceledOnTouchOutside(false);

		AppPreferenceStatus.setStudyDownload(CurContext,
				CourseMatIntent.PracticeExam);

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

		adStartExam = null;
		adStartExam = new Builder(CurContext);
		adStartExam.setIcon(R.drawable.bt_question);
		adStartExam.setTitle("Download status");
		adStartExam
				.setMessage(
						"Do you want to download "
								+ type
								+ " from Server? 'Yes' will download the "
								+ type + " and 'No' will cancel download.")
				.setPositiveButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.setNegativeButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								pgExam.show();

								new AsyncQuestionDownloader().execute("");
							}
						});
		AlertDialog altStartExam = adStartExam.create();
		altStartExam.show();
	}

	public class AsyncQuestionDownloader extends
			AsyncTask<String, TaskStatusMsg, Integer> {

		@Override
		protected Integer doInBackground(String... params) {

			TaskStatusMsg infoUpload = mExamSyncService.AnswerUpload(
					StudentInfo, (ExamActivity) CurContext);

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

			mProgressHandlerQuestion.sendEmptyMessage(result);
		}

	}

	boolean isMock = false;

	private int DownloadExam(String SubjectID, String ChapterID, String LevelID) {

		int StatusMsg = -3;

		String ExamID = null;
		String ExamName = null;
		HttpTransportSE androidHttpTransport = null;
		SoapSerializationEnvelope envelope = null;
		int ExamTime = 0;

		SoapObject request = null;
		SoapObject soapResult = null;

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();
		try {
			request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.EXAM_METHOD_NAME));

			// <StudentCode>string</StudentCode>
			// <requestQuestionSubjectId>string</requestQuestionSubjectId>
			// <requestQuestionChapterId>string</requestQuestionChapterId>
			// <requestQuestionSetId>string</requestQuestionSetId>
			// <requestExamType>string</requestExamType>
			//
			// X9981218
			//
			// E

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

					SelectedSubjectID = rootBlock.getAttribute(5).toString();

					SelectedChapterID = rootBlock.getAttribute(0).toString();

					ChapterID = SelectedChapterID;

					if (ChapterID != null && !"".equals(ChapterID)) {
						db.execSQL("DELETE FROM " + DatabaseHelper.TBL_EXAM
								+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
								+ " = " + ChapterID);
						db.execSQL("DELETE FROM "
								+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE
								+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
								+ " = " + ChapterID);
						db.execSQL("DELETE FROM "
								+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
								+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
								+ " = " + ChapterID);
						db.execSQL("DELETE FROM "
								+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
								+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
								+ " = " + ChapterID);
					}

					db.execSQL("DELETE FROM " + DatabaseHelper.TBL_CHAPTER
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = "
							+ SelectedChapterID);

					createChapter(SelectedSubjectID, SelectedChapterID, "",
							"F", "F");

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

						long retStatus = 0;
						if (QuestionType.equals("MAM")) {
							retStatus = createExam(ChapterID, ExamID, ExamName,
									ExamTime, QuestionID, "MCQ", QuestionMarks,
									QuestionBody);
							parseMCQ(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("MCQ")) {
							retStatus = createExam(ChapterID, ExamID, ExamName,
									ExamTime, QuestionID, "SCQ", QuestionMarks,
									QuestionBody);
							parseSCQ(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("FIB")) {
							retStatus = createExam(ChapterID, ExamID, ExamName,
									ExamTime, QuestionID, "FIB", QuestionMarks,
									QuestionBody);
							parseFIB(questionBlock, ChapterID, ExamID,
									QuestionID);
						} else if (QuestionType.equals("LDG")) {
							retStatus = createExam(ChapterID, ExamID, ExamName,
									ExamTime, QuestionID, "M&M", QuestionMarks,
									QuestionBody);
							parseMM(questionBlock, ChapterID, ExamID,
									QuestionID);
						}

						retStatus = updateChapter(ChapterID, "T");

						retStatus += 0;
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
			Cursor cursorValidChapter = db.rawQuery("select * from "
					+ DatabaseHelper.TBL_CHAPTER + " where "
					+ DatabaseHelper.FLD_ID_CHAPTER + " ='" + id_chapter + "'",
					null);
			startManagingCursor(cursorValidChapter);

			if (cursorValidChapter != null) {
				ret = cursorValidChapter.getCount();
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

	Handler mProgressHandlerQuestion = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);

			AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(CurContext);

			switch (msg.what) {
			case 0:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				InvokeExamIntent();

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
								if ((SelectedSubjectID != null && !""
										.equals(SelectedSubjectID))
										&& (SelectedChapterID != null && !""
												.equals(SelectedChapterID))) {
									fillExam(SelectedSubjectID,
											SelectedChapterID);
								} else {
									Toast.makeText(
											CurContext,
											"Incorrect Data: application requires syncing",
											Toast.LENGTH_SHORT).show();
								}
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

	public void refreshCalendar() {
		TextView title = (TextView) findViewById(R.id.calendarTitle);

		calendarAdapter.setToday(Calendar.getInstance());
		calendarAdapter.refreshDays();
		calendarAdapter.notifyDataSetChanged();
		
		new AsyncFetchScheduleDates().execute(month);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
	}

	public class AsyncFetchScheduleDates extends
			AsyncTask<Calendar, Void, Void> {
		@Override
		protected void onPreExecute() {

			calendarAdapter.setToday(Calendar.getInstance());

			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Calendar... params) {

			// Fetch from DB
			if (params != null && params[0] != null) {
				FetchMonthInfo(params[0]);

			} else {
				calendarItems.clear();
			}
			

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (calendarItems != null && calendarItems.size() > 0) {
				calendarAdapter.setItems(calendarItems);

			}
			calendarAdapter.notifyDataSetChanged();
		}

		private boolean FetchMonthInfo(Calendar MonthInfo) {

			boolean isSuccess = false;

			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			calendarItems.clear();
			try {

				int month = MonthInfo.get(Calendar.MONTH);
				int yr = MonthInfo.get(Calendar.YEAR);
				Cursor cursorMonthlySchedule = db.rawQuery("select * from "
						+ DatabaseHelper.TBL_STUDENT_SCHEDULE + " where "
						+ DatabaseHelper.FLD_SCHEDULE_DATE_YEAR + "='" + yr
						+ "' AND " + DatabaseHelper.FLD_SCHEDULE_DATE_MONTH
						+ "='" + month + "'", null);

				if (cursorMonthlySchedule != null
						&& cursorMonthlySchedule.getCount() > 0) {
					isSuccess = true;

					int colnum = cursorMonthlySchedule
							.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH);

					startManagingCursor(cursorMonthlySchedule);

					cursorMonthlySchedule.moveToFirst();
					do {

						String curDate = cursorMonthlySchedule
								.getString(colnum);

						if (curDate != null && !"".equals(curDate)) {

							if (curDate.length() == 1) {
								curDate = "0" + curDate;
							}
							calendarItems.add(curDate);
						}
					} while (cursorMonthlySchedule.moveToNext());
				} else {
					isSuccess = false;
				}

				cursorMonthlySchedule.close();
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

			return isSuccess;
		}
	}

	private void ShowResult() {
		try {
			Intent intent = new Intent(CurContext, ExamResultActivity.class);
			startActivityForResult(intent, 13);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
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
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return lreturn;
	}

	private void showStudentPhoto(String studentimage) {
		File imgFile = new File(studentimage);
		if (imgFile.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
					.getAbsolutePath());
			ImageView image = (ImageView) findViewById(R.id.ivStudent);
			image.setImageBitmap(myBitmap);
		}

	}

	Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			try {
				AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
						CurContext);

				switch (msg.what) {
				case 0:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					if (StudentInfo != null) {

						setTitle("ICA Student Connect (Ver: "
								+ AppInfo.versionInfo(CurContext)
										.getVersionName() + ")-" + ActionType
								+ "- [" + StudentInfo.getStudentID() + "]");
						showStudentPhoto(Environment
								.getExternalStorageDirectory()
								+ File.separator
								+ R.string.STUDENT_IMAGE);

						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder.setMessage(ActionType
								+ " test downloaded successfully.");
						dlgMsgbuilder.setPositiveButton("Ok", null).create();
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.show();

					} else {
						// setTitle("Option");
						setTitle("ICA Student Connect (Ver: "
								+ AppInfo.versionInfo(CurContext)
										.getVersionName() + ")-" + ActionType
								+ "- [" + StudentInfo.getStudentID() + "]");

						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder.setMessage(ActionType
								+ " test download not successful.");
						dlgMsgbuilder.setPositiveButton("Ok", null).create();
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.show();
					}

					break;
				case -1:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Connectivity status");
					dlgMsgbuilder
							.setMessage("Connection error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();
					break;
				case -2:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Login status");
					dlgMsgbuilder
							.setMessage("Student details not available.Please login to download exams.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -3:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -4:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -5:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -6:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case -7:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}
					break;
				case 1:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.information);
					dlgMsgbuilder.setTitle("Upload status");
					dlgMsgbuilder
							.setMessage("Exam result has been successfully published to the http://icaerp.com to keep track your performance.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();
					break;
				case -10:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application status");
					dlgMsgbuilder.setMessage("Application error! Try again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -11:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application status");
					dlgMsgbuilder
							.setMessage("Application data error! Contact administrator.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -12:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Connectivity status");
					dlgMsgbuilder
							.setMessage("Connection error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -13:
					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Upload status");
					dlgMsgbuilder
							.setMessage("Upload error! Please check the connection and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	public View makeView() {
		ImageView imageView = new ImageView(this);
		imageView.setBackgroundColor(0xFF000000);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		return imageView;

	}
	@Override
	public void onBackPressed() {
		//super.onBackPressed();

		//finish();
		//Toast.makeText( getApplicationContext(),"Back pressed",Toast.LENGTH_SHORT).show();
		
		//==================================================================================================================
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getParent()); 
		  
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
