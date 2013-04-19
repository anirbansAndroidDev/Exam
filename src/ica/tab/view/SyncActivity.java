package ica.tab.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import ica.ICAConstants.MenuItems;
import ica.ICAConstants.UploadTask;
import ica.ICAServiceHandler.ChapterMarksComparisonService;
import ica.ICAServiceHandler.ExamDownloaderService;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ICAServiceHandler.ModuleMarksComparisonService;
import ica.ICAServiceHandler.ScheduleDataHandler;
import ica.ICAServiceHandler.SubjectMarksComparisonService;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppInfo;
import ica.Utility.AppPreferenceStatus;
import ica.exam.CourseFeeSummary;
import ica.exam.DatabaseHelper;
import ica.exam.ExamActivity;
import ica.exam.ICAMainLogin;
import ica.exam.IndexActivity;
import ica.exam.R;
import ica.exam.StudentProgress;
import ica.exam.SubjectVsMarks;
import ica.exam.Carousel.Carousel;
import ica.exam.IndexActivity.AsyncDownloader;

public class SyncActivity extends ActivityGroup {
	private SQLiteDatabase db;
	private ProgressDialog pgLogin;
	private ProgressDialog pgUpload;
	private Activity actvity;

	Carousel carousel;
	private HorizontalScrollView horizontalMenubar;

	public static final int ExamStatusCode = 111;
	public static final int FinanceStatusCode = 112;
	public static final int ProgressStatusCode = 113;
	public static final int PlacementStatusCode = 114;
	public static final int ExitStatusCode = 999;
	public static final int IndexStatusCode = 001;

	ChapterMarksComparisonService chapterMarksComparisonService;
	ModuleMarksComparisonService moduleMarksComparisonService;
	SubjectMarksComparisonService subjectMarksComparisonService;
	ScheduleDataHandler mScheduleSyncService;
	ExamSyncService mExamSyncService;

	StudentDetails studentDetails = null;
	Context IndexContext;
	SlidingDrawer slidingDrawer1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.sync_view);
		IndexContext = this;
		actvity = this;

		db = (new DatabaseHelper(IndexContext)).getWritableDatabase();
		//new AsyncDownloader().execute("");
		
		
//==============================================================================================================================================
//		carousel = (Carousel) findViewById(R.id.carousel);
//		carousel.setSoundEffectsEnabled(true);
//		carousel.setOnItemClickListener(new OnItemClickListener() {
//
//			public void onItemClick(CarouselAdapter<?> parent, View view,
//					int position, long id) {
//
//				if (!slidingDrawer1.isOpened()) {
//
//					CarouselItems carouselCnt = CarouselItems.values()[position];
//					switch (carouselCnt) {
//					case syncing_icon:
//						new AsyncDownloader().execute("");
//						break;
//					case exams_icon:
//						Intent ExamIntent = new Intent(IndexContext,ExamActivity.class);
//						Calendar mToday = Calendar.getInstance();
//
//						ExamIntent.putExtra("date", mToday.get(Calendar.YEAR)
//								+ "-" + mToday.get(Calendar.MONTH) + "-"
//								+ mToday.get(Calendar.DAY_OF_MONTH));
//						startActivityForResult(ExamIntent, ExamStatusCode);
//
//						break;
//					case icon_placement:
//
//						Intent ResultCoverFlowIntent = new Intent(IndexContext,
//								PlacementSelectorActivity.class);
//						startActivityForResult(ResultCoverFlowIntent,
//								PlacementStatusCode);
//						// setFocusedText("Placement Information",
//						// CarouselPosition);
//						break;
//					case icon_progress:
//
//						// Intent ResultIntent = new Intent(IndexContext,
//						// SubjectVsMarks.class);
//						// startActivityForResult(ResultIntent,
//						// ProgressStatusCode);
//						ShowResult();
//						break;
//					// case cost_icon:
//					// Intent FinanceIntent = new Intent(IndexContext,
//					// CourseFeeSummary.class);
//					// startActivityForResult(FinanceIntent, FinanceStatusCode);
//					// // setFocusedText("Finanacial Information",
//					// // CarouselPosition);
//					// break;
//					}
//				}
//			}
//
//		});
//==============================================================================================================================================

		slidingDrawer1 = (SlidingDrawer) findViewById(R.id.slidingDrawer1);

		slidingDrawer1.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				carousel.setOverlayed(slidingDrawer1.isOpened());
			}
		});

		slidingDrawer1.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				carousel.setOverlayed(slidingDrawer1.isOpened());
			}
		});

		chapterMarksComparisonService = new ChapterMarksComparisonService(
				IndexContext);
		moduleMarksComparisonService = new ModuleMarksComparisonService(
				IndexContext);
		subjectMarksComparisonService = new SubjectMarksComparisonService(
				IndexContext);
		mExamSyncService = new ExamSyncService(IndexContext);

		mScheduleSyncService = new ScheduleDataHandler(IndexContext);

		StudentDetails.initInstance(IndexContext);

		studentDetails = StudentDetails.getInstance();

		String sEmail = studentDetails.getStudentID();

		if (sEmail != null) {
			setTitle("ICA Student Connect (Ver: "
					+ AppInfo.versionInfo(IndexContext).getVersionName()
					+ ")-Home- [" + sEmail + "]");

			if (studentDetails != null) {

				if (AppPreferenceStatus.getLoggedOutStatus(IndexContext)) {
					LoginEntryIntent();
				} else {
					IndexIntent();
				}

			} else {
				LoginEntryIntent();
			}
		} else {
			LoginEntryIntent();
		}

		Button btnLogin = (Button) findViewById(R.id.btnLoginIndexPage);

		btnLogin.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				LoginEntryIntent();

			}
		});

		Button btnExit = (Button) findViewById(R.id.btnExitIndexPage);

		btnExit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
//				AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
//				LoginEntryIntent();

			}
		});

		ImageButton btnSyncAll = (ImageButton) findViewById(R.id.btnSync);
		btnSyncAll.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				new AsyncDownloader().execute("");
			}
		});

		/*ImageButton btnAttendance = (ImageButton) findViewById(R.id.btnAttendance);

		btnAttendance.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showMessage("Attendance");

			}
		});*/

		ImageButton btnMockTest = (ImageButton) findViewById(R.id.btnMockTest);

		btnMockTest.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent ExamIntent = new Intent(IndexContext, ExamActivity.class);
				startActivityForResult(ExamIntent, ExamStatusCode);

			}
		});

		ImageButton btnPlacement = (ImageButton) findViewById(R.id.btnPlacement);

		btnPlacement.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showMessage("Placement");

			}
		});

		ImageButton btnAcademicProgress = (ImageButton) findViewById(R.id.btnAcademicProgress);

		btnAcademicProgress.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent ResultIntent = new Intent(IndexContext,
						SubjectVsMarks.class);
				startActivityForResult(ResultIntent, ProgressStatusCode);

			}
		});

		ImageButton btnFinancial = (ImageButton) findViewById(R.id.btnFinancial);

		btnFinancial.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent FinanceIntent = new Intent(IndexContext,
						CourseFeeSummary.class);
				startActivityForResult(FinanceIntent, FinanceStatusCode);
			}
		});

	}
	//----------------------------------------------
	public void sync(View v) {
		new AsyncDownloader().execute("");
	}
	//----------------------------------------------
	private void ShowResult() {
		try {
			Intent intent = new Intent(IndexContext, StudentProgress.class);
			startActivityForResult(intent, 13);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	int CarouselPosition = 0;

	EditText txtUserName;
	EditText txtPassword;

	public void LoginEntryIntent() {

		isMainLoginEntry = true;

		Intent LoginMain = new Intent(IndexContext, ICAMainLogin.class);
		startActivityForResult(LoginMain, IndexStatusCode);

	}

	public class AsyncDownloader extends
			AsyncTask<String, TaskStatusMsg, Boolean> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pgLogin = new ProgressDialog(IndexContext);
			pgLogin.setMessage("Please wait while sync is in progress...");
			pgLogin.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pgLogin.setMax(7);
			pgLogin.setCancelable(false);
			pgLogin.setCanceledOnTouchOutside(false);

			pgLogin.show();

			StudentDetails.refreshSource();
			StudentDetails.initInstance(IndexContext);
			studentDetails = StudentDetails.getInstance();
			SyncDataSource();

		}

		@Override
		protected void onProgressUpdate(TaskStatusMsg... values) {
			super.onProgressUpdate(values);
			pgLogin.incrementProgressBy(1);
			Showmessage(values[0].getMessage());
		}

		private void Showmessage(String Message) {
			Toast.makeText(IndexContext, Message, Toast.LENGTH_SHORT).show();
		}

		int Status = -999;

		@Override
		protected Boolean doInBackground(String... params) {

			String sEmail = studentDetails.getStudentID();

			TaskStatusMsg loggininfo = new TaskStatusMsg();
			loggininfo.setTaskDone(UploadTask.Invalid);
			loggininfo.setTitle("Sync Status");
			loggininfo.setStatus(-111);
			loggininfo
					.setMessage("User information invalid.Please try again after logging in");

			if (sEmail != null) {

				// Answer Upload

				TaskStatusMsg ansUploadinfo = mExamSyncService.AnswerUpload(
						studentDetails, actvity);

				publishProgress(ansUploadinfo);

				TaskStatusMsg ansPracticeUploadinfo = mExamSyncService
						.AnswerUploadPractice(studentDetails, actvity);

				publishProgress(ansPracticeUploadinfo);

				if (studentDetails.getStudentID() != null) {

					// emptyDB();

					TaskStatusMsg examDownloadinfo = new ExamDownloaderService(
							IndexContext).ChapterSync(
							studentDetails.getStudentID(),
							studentDetails.getStudentPWD());

					publishProgress(examDownloadinfo);

					TaskStatusMsg NotficationUploadInfo = mScheduleSyncService
							.SyncDBToServer(studentDetails);
					publishProgress(NotficationUploadInfo);

					TaskStatusMsg NotficationInfo = mScheduleSyncService
							.SyncServerToDB(studentDetails);

					publishProgress(NotficationInfo);

					TaskStatusMsg examModuleinfo = moduleMarksComparisonService
							.FetchMarks(studentDetails.getStudentID());

					publishProgress(examModuleinfo);

					TaskStatusMsg examSubjectinfo = subjectMarksComparisonService
							.FetchMarks(studentDetails.getStudentID());

					publishProgress(examSubjectinfo);

					TaskStatusMsg examChapterinfo = chapterMarksComparisonService
							.FetchMarks(studentDetails.getStudentID());

					publishProgress(examChapterinfo);

				} else {
					publishProgress(loggininfo);
				}

			} else {
				publishProgress(loggininfo);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);

			if (Status != -999) {
				mProgressHandler.sendEmptyMessage(Status);
			}

			if (pgLogin != null) {
				if (pgLogin.isShowing()) {
					pgLogin.cancel();
					pgLogin.dismiss();
				}
			}
		}
	}

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

	boolean isMainLoginEntry = false;

	public void IndexIntent() {

		isMainLoginEntry = false;

		horizontalMenubar = (HorizontalScrollView) findViewById(R.id.horizontalMenubar);

		horizontalMenubar.setHorizontalScrollBarEnabled(false);

		SyncDataSource();
	}

	public void showMessage(String msg) {
		Toast.makeText(IndexContext, msg, 100).show();
	}

	public void SyncDataSource() {

		horizontalMenubar = (HorizontalScrollView) findViewById(R.id.horizontalMenubar);
		horizontalMenubar.setHorizontalScrollBarEnabled(false);

		StudentDetails.initInstance(IndexContext);

		studentDetails = StudentDetails.getInstance();
		String sEmail = studentDetails.getStudentID();

		if (sEmail != null) {
			setTitle("ICA Student Connect (Ver: "
					+ AppInfo.versionInfo(IndexContext).getVersionName()
					+ ")-Home- [" + sEmail + "]");

			if (studentDetails != null) {
				TextView txtStudentName = (TextView) findViewById(R.id.txtStudentName);
				txtStudentName.setText(studentDetails.getStudentFname() + " "
						+ studentDetails.getStudentLname());

				if (showStudentPhoto(Environment.getExternalStorageDirectory()
						+ File.separator + R.string.STUDENT_IMAGE) == null) {
					downloadImage(studentDetails.getStudentImgPath());
				}
			}

		} else {
			setTitle("ICA Student Connect (Ver: "
					+ AppInfo.versionInfo(IndexContext).getVersionName()
					+ ")-Home");
		}

	}

	public void emptyDB() {
		DatabaseHelper dh = new DatabaseHelper(IndexContext);
		dh.onUpgrade(db, 0, 0);
	}

	private void downloadImage(String imageurl) {
		String studentimage = Environment.getExternalStorageDirectory()
				+ File.separator + R.string.STUDENT_IMAGE;
		File file = new File(studentimage);

		if (file.exists()) {
			file.delete();
		}

		InputStream in = null;
		String urlString = imageurl;
		BufferedInputStream bis = null;

		try {
			URL url = new URL(urlString);
			URLConnection ucon = url.openConnection();
			in = ucon.getInputStream();
			bis = new BufferedInputStream(in);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			bis.close();
			in.close();

			mProgressHandler.sendEmptyMessage(0);
		} catch (MalformedURLException e) {
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// / Toast.LENGTH_LONG).show();
		} catch (IOException ex) {
			// Toast.makeText(getApplicationContext(), ex.getMessage(),
			// Toast.LENGTH_LONG).show();
		}
	}

	private void ExitStub() {

		AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
		finish();
		System.runFinalizersOnExit(true);
		System.exit(0);

	}

	public void discardLoginDlg() {
		if (pgLogin != null) {
			if (pgLogin.isShowing()) {
				pgLogin.dismiss();
				pgLogin.cancel();
			}
		}

		if (pgUpload != null) {
			pgUpload.dismiss();
			pgUpload.cancel();
		}

	}

	Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			try {
				AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(
						actvity);

				discardLoginDlg();

				switch (msg.what) {
				case 0:
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					String sEmail = studentDetails.getStudentID();
					if (sEmail != null) {
						// setTitle("Option - [" + sEmail + "]");
						setTitle("ICA Student Connect (Ver: "
								+ AppInfo.versionInfo(IndexContext)
										.getVersionName() + ")-Home- ["
								+ sEmail + "]");
						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder
								.setMessage("Student has been synced successfull.");
						dlgMsgbuilder.setPositiveButton("Ok", null).create();
						dlgMsgbuilder.setCancelable(false);
						dlgMsgbuilder.show();

					} else {
						// setTitle("Option");
						setTitle("ICA Student Connect (Ver: "
								+ AppInfo.versionInfo(IndexContext)
										.getVersionName() + ")-Home");
						dlgMsgbuilder.setIcon(R.drawable.information);
						dlgMsgbuilder.setTitle("Login status");
						dlgMsgbuilder.setMessage("Data sync failed");
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
					dlgMsgbuilder.setTitle("Sync status");
					dlgMsgbuilder
							.setMessage("Invalid user information! Please login again and try it again.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -3:
					// /Exception handling
					if (pgLogin.isShowing()) {
						pgLogin.dismiss();
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Exception");
					dlgMsgbuilder.setMessage("");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

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
				case -111:
					if (pgUpload.isShowing()) {
						pgUpload.dismiss();
					}
					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("No User Data Available");
					dlgMsgbuilder
							.setMessage("Data Error:No Data Available against the UserID");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				case -999:
					if (pgUpload != null) {
						if (pgUpload.isShowing()) {
							pgUpload.dismiss();
							pgUpload.cancel();
						}
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("Application Error!!");
					dlgMsgbuilder
							.setMessage("Application Error:Please contact system admin.");
					dlgMsgbuilder.setPositiveButton("Ok", null).create();
					dlgMsgbuilder.setCancelable(false);
					dlgMsgbuilder.show();

					break;
				default:

					if (pgUpload != null) {
						if (pgUpload.isShowing()) {
							pgUpload.dismiss();
							pgUpload.cancel();
						}
					}

					dlgMsgbuilder.setIcon(R.drawable.error);
					dlgMsgbuilder.setTitle("No User Data Available");
					dlgMsgbuilder
							.setMessage("Data Error:No Data Available against the UserID");
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

	private Bitmap showStudentPhoto(String studentimage) {

		ImageView image = (ImageView) findViewById(R.id.ivStudentImage);
		Bitmap myBitmap = null;
		if (studentimage != null && !studentimage.trim().equals("")) {
			File imgFile = new File(studentimage);
			if (imgFile.exists()) {
				myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				image.setImageResource(R.drawable.anonymous_old);
				if (myBitmap != null) {
					image.setImageBitmap(null);
					image.setImageDrawable(new BitmapDrawable(myBitmap));

				} else {
					image.setImageBitmap(null);
					image.setImageResource(R.drawable.anonymous_old);
				}
			} else {
				image.setImageBitmap(null);
				image.setImageResource(R.drawable.anonymous_old);
			}
		} else {
			image.setImageBitmap(null);
			image.setImageResource(R.drawable.anonymous_old);
		}

		return myBitmap;

	}

	public List<Map<String, Object>> GetMenuItem() {

		List<Map<String, Object>> resourceNames = new ArrayList<Map<String, Object>>();

		for (MenuItems items : MenuItems.values()) {

			switch (items) {
			case Login:
				Map<String, Object> dataLogin = new HashMap<String, Object>();
				dataLogin.put("line1", "Login");
				dataLogin.put("line2", "");
				dataLogin.put("img", R.drawable.lst_next);

				resourceNames.add(dataLogin);
				break;
			case Attendance:
				// define the map which will hold the information for each row
				Map<String, Object> data1 = new HashMap<String, Object>();
				data1.put("line1", "Attendance");
				data1.put("line2", "");
				data1.put("img", R.drawable.lst_next);

				resourceNames.add(data1);

				break;
			case Mock_Test:
				Map<String, Object> data2 = new HashMap<String, Object>();
				data2.put("line1", "Mock Test");
				data2.put("line2", "");
				data2.put("img", R.drawable.lst_next);

				resourceNames.add(data2);
				break;
			case Placement:

				Map<String, Object> data3 = new HashMap<String, Object>();
				data3.put("line1", "Placement");
				data3.put("line2", "");
				data3.put("img", R.drawable.lst_next);

				resourceNames.add(data3);
				break;
			case Academic_Progress:

				Map<String, Object> data4 = new HashMap<String, Object>();
				data4.put("line1", "Academic Progress");
				data4.put("line2", "");
				data4.put("img", R.drawable.lst_next);

				resourceNames.add(data4);
				break;
			case Financial:
				Map<String, Object> data5 = new HashMap<String, Object>();
				data5.put("line1", "Financial");
				data5.put("line2", "");
				data5.put("img", R.drawable.lst_next);

				resourceNames.add(data5);
				break;
			case Study_Material:
				Map<String, Object> data6 = new HashMap<String, Object>();
				data6.put("line1", "Study Material");
				data6.put("line2", "");
				data6.put("img", R.drawable.lst_next);

				resourceNames.add(data6);
				break;
			case Exit:
				Map<String, Object> data7 = new HashMap<String, Object>();
				data7.put("line1", "Exit");
				data7.put("line2", "");
				data7.put("img", R.drawable.lst_next);

				resourceNames.add(data7);
				break;
			default:
				break;
			}
		}

		return resourceNames;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == ExitStatusCode) {
			ExitStub();
		}

		SyncDataSource();
	}

	@Override
	protected void onPause() {
		super.onPause();
		 
		//finish();
		if (carousel != null) {
			AppPreferenceStatus.setLastCarouselItem(IndexContext,
					carousel.getSelectedItemPosition());
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (carousel != null) {
			carousel.setSelection(
					AppPreferenceStatus.getLastCarouselItem(IndexContext), true);
		}
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
