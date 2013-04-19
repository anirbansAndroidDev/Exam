package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ICAConstants.DownloadOptions;
import ica.ICAServiceHandler.ExamSyncService;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.DownloaderService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Question_Download extends ActivityGroup {

	private Cursor cursorChapter;
	private SQLiteDatabase db;
	private AlertDialog.Builder adLevel;
	private AlertDialog.Builder adContextMenu;

	private AlertDialog.Builder adStartExam;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedLevelID;
	private ProgressDialog pgExam;

	private String WSURL;
	private String WSNameSpace;
	private String WSMethod;
	private String SPOAction;

	private Activity actvity;
	ListView ListViewSubject;
	private Context CurContext;
	private ArrayList<View> history;
	StudentDetails StudentInfo = null;
	public static int checkClass = 0;
	
	ExamSyncService mExamSyncService;

	DownloadOptions downloadOptionSelected = DownloadOptions.MockExam;
	boolean isMock = false;
	AlertDialog.Builder dlgMsgbuilder ;
	private void InitiateSoapResource() {

		SelectedSubjectID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_SUBJECT);
		WSURL = CurContext.getString(R.string.SOAP_URL);
		WSNameSpace = CurContext.getString(R.string.WEBSERVICE_NAMESPACE);
		WSMethod = CurContext.getString(R.string.LEVEL_METHOD_NAME);
		SPOAction = CurContext.getString(R.string.LEVEL_SOAP_ACTION);

	}

	int IntentFrom;
	private CourseMatIntent intentType;
	CourseMatIntent type;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chapter_list_download);
		this.history = new ArrayList<View>();

		actvity = Question_Download.this;
		CurContext = Question_Download.this;

		mExamSyncService = new ExamSyncService(CurContext);
		try {
			InitiateSoapResource();

			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			type = AppPreferenceStatus
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

			StudentDetails.initInstance(CurContext);

			StudentInfo = StudentDetails.getInstance();

			String sEmail = StudentInfo.getStudentID();
			if (sEmail != null) {

				setTitle("Chapter List-" + ActionType + "- [" + sEmail + "]");
			}

			IntentFrom = getIntent().getIntExtra("ExamIntent",CourseMatIntent.DownlaodMock.getNumber());
			intentType = CourseMatIntent.fromInteger(IntentFrom);

			//------------------------------------
			
			adLevel = new Builder(this.getParent().getParent());
			adStartExam = new Builder(this.getParent().getParent());
			adContextMenu = new Builder(this.getParent().getParent());
			dlgMsgbuilder = new Builder(this.getParent().getParent());
			
			
//			if(checkClass == 2)
//			{
//				Toast.makeText(getApplicationContext(), "CheckClass 2",Toast.LENGTH_SHORT).show();
//				
//				adLevel = new Builder(this.getParent().getParent().getParent().getParent());
//				adStartExam = new Builder(this.getParent().getParent().getParent().getParent());
//				adContextMenu = new Builder(this.getParent().getParent().getParent().getParent());
//				dlgMsgbuilder = new Builder(this.getParent().getParent().getParent().getParent());
//			}
//			else
//			{
//
//			}
			//------------------------------------

			pgExam = new ProgressDialog(this.getParent().getParent());
			pgExam.setMessage("Please wait while downloading...");
			pgExam.setIndeterminate(true);
			pgExam.setCancelable(false);
			pgExam.setCanceledOnTouchOutside(false);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		// ////////////////SYNC CHAPTER
		// LISTS///////////////////////////////////////////////////////////////

		try {

			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			cursorChapter = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ ", " + DatabaseHelper.FLD_ID_CHAPTER + ","
					+ DatabaseHelper.FLD_NAME_CHAPTER + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					// + DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = 'F'"
					// + " AND "
					+ DatabaseHelper.FLD_ID_SUBJECT + " = ?",
					new String[] { SelectedSubjectID });

			startManagingCursor(cursorChapter);

			ListAdapter ListAdaptersubject = new ChapterAdapter(CurContext,cursorChapter);
			ListViewSubject = (ListView) findViewById(R.id.lvChapter);
			ListViewSubject.setAdapter(ListAdaptersubject);

			ListViewSubject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

					chapterPosition = position;
					//openOptionsMenu();
					
					openDialog();
				}
			});

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		finally {

		}

	}
	//==================================================================================================================================
	public void openDialog()
	{
	AlertDialog.Builder alertDialog = new AlertDialog.Builder(Question_Download.this.getParent().getParent());
	 
    alertDialog.setTitle("Make your choice...");
    alertDialog.setMessage("What do you want to download?");
    alertDialog.setIcon(R.drawable.save_file_floppy);

    alertDialog.setPositiveButton("Mock Exam", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) 
        {
        	downloadOptionSelected = DownloadOptions.MockExam;
			isMock = true;
			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.MockExam);
			MockExamActivity(chapterPosition);
        }
    });

    alertDialog.setNeutralButton("Study Mats.", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) 
        {
        	downloadOptionSelected = DownloadOptions.StudyMaterials;
			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.StudyMaterials);
			StudyMatActivity(chapterPosition);
        }
    });
    
    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) 
        {
        	
        }
    });
    alertDialog.show();
	}
//==================================================================================================================================

	int chapterPosition = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.downloadmennu, menu);
		
//---------------------------------------------------------------------------------------------------------
//		AlertDialog.Builder alertDialog = new AlertDialog.Builder(Question_Download.this.getParent().getParent());
//		 
//        alertDialog.setTitle("Make your choice...");
//        alertDialog.setMessage("What do you want to download?");
//        alertDialog.setIcon(R.drawable.save_file_floppy);
//
//        alertDialog.setPositiveButton("Mock Exam", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) 
//            {
//            	downloadOptionSelected = DownloadOptions.MockExam;
//    			isMock = true;
//    			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.MockExam);
//    			MockExamActivity(chapterPosition);
//            }
//        });
//
//        alertDialog.setNeutralButton("Study Mats.", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) 
//            {
//            	downloadOptionSelected = DownloadOptions.StudyMaterials;
//    			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.StudyMaterials);
//    			StudyMatActivity(chapterPosition);
//            }
//        });
//        
//        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) 
//            {
//            	
//            }
//        });
//
//        alertDialog.show();
       return true;
  //---------------------------------------------------------------------------------------------------------
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) 
		{
		case R.id.menuMockExamDownload:

			downloadOptionSelected = DownloadOptions.MockExam;
			isMock = true;
			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.MockExam);
			MockExamActivity(chapterPosition);
			return true;

		case R.id.menuStudyMatDownload:

			downloadOptionSelected = DownloadOptions.StudyMaterials;
			AppPreferenceStatus.setStudyDownload(CurContext,CourseMatIntent.StudyMaterials);
			StudyMatActivity(chapterPosition);
			return true;
			/*
			 * case R.id.menuPracticeExamDownload: downloadOptionSelected =
			 * DownloadOptions.PracticeExam; isMock = false;
			 * AppPreferenceStatus.setStudyDownload(CurContext,
			 * CourseMatIntent.PracticeExam);
			 * cursorChapter.moveToPosition(chapterPosition);
			 * 
			 * SelectedChapterID = cursorChapter.getString(cursorChapter
			 * .getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER));
			 * 
			 * if (SelectedChapterID != null) {
			 * 
			 * PracticeExamDownloader(); } return true;
			 */
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void StudyMatActivity(int Position) {

		try 
		{
			cursorChapter.moveToPosition(Position);
			SelectedChapterID = cursorChapter.getString(cursorChapter.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER));
			new AsyncChapterPDFDowloadService().execute(new String[] {SelectedSubjectID, SelectedChapterID });
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	// PDF DOWLOAD

	public class AsyncChapterPDFDowloadService extends
	AsyncTask<String, Void, Void> {
		ProgressDialog downloadProgress;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			downloadProgress = new ProgressDialog(getParent().getParent());
			downloadProgress.setIcon(R.drawable.note);
			downloadProgress.setTitle("Downloading...");
			downloadProgress.setIndeterminate(true);
			downloadProgress.setCancelable(false);
			downloadProgress.setCanceledOnTouchOutside(false);

			downloadProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			downloadStudyMaterials(params[0], params[1]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			downloadProgress.dismiss();
			downloadProgress.cancel();

			//adContextMenu = new Builder(CurContext);
			adContextMenu.setTitle("Select a pdf to start download"); //
			adContextMenu.setIcon(R.drawable.download_alert);

			if (mapNamePath != null && mapNamePath.size() > 0) {

				ArrayList<String> list = new ArrayList<String>(mapNamePath.values());
				CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
				
				adContextMenu.setItems(cs,new DialogInterface.OnClickListener() 
				{

					public void onClick(DialogInterface dialog,int which) {
						int cnt = which;
						dialog.cancel();
						dialog.dismiss();
						if (mapNamePath != null && mapNamePath.size() > 0) 
						{
							ArrayList<String> lstPath = new ArrayList<String>(mapNamePath.keySet());

							String pdfTobeDownload = lstPath.get(cnt);
							new AsyncPdfDownloader().execute(pdfTobeDownload);
						}
					}
				});

				AlertDialog altLevel = adContextMenu.create();
				altLevel.show();

				openContextMenu(ListViewSubject);
			} else {
				Toast.makeText(CurContext, "No items available for download.",
						Toast.LENGTH_LONG).show();
			}
		}

		private void downloadStudyMaterials(String SubjectID, String ChapterID) {

			HttpTransportSE androidHttpTransport = null;
			SoapSerializationEnvelope envelope = null;

			SoapObject request = null;
			SoapObject soapResult = null;

			try {
				request = new SoapObject(
						CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
						CurContext.getString(R.string.STUDY_MAT_METHOD_NAME));

				PropertyInfo inf_subjectid = new PropertyInfo();
				inf_subjectid.setName("subjectId");
				inf_subjectid.setValue(SubjectID);
				request.addProperty(inf_subjectid);

				PropertyInfo inf_chapterid = new PropertyInfo();
				inf_chapterid.setName("chapterId");
				inf_chapterid.setValue(ChapterID);
				request.addProperty(inf_chapterid);

				envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				envelope.setOutputSoapObject(request);

				androidHttpTransport = new HttpTransportSE(
						CurContext.getString(R.string.SOAP_URL));
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-1);
				return;
			}

			try {
				androidHttpTransport.call(
						CurContext.getString(R.string.STUDY_MAT_SOAP_ACTION),
						envelope);
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-2);
				return;
			}

			try {
				soapResult = (SoapObject) envelope.bodyIn;
			} catch (Exception e) {
				mProgressHandler.sendEmptyMessage(-2);
				return;
			}

			if (soapResult != null) {
				try {
					SoapObject soapBlock = (SoapObject) soapResult
							.getProperty(0);
					SoapObject rootBlock = (SoapObject) soapBlock
							.getProperty(0);

					if (rootBlock != null && rootBlock.getPropertyCount() > 0) {

						for (int matCount = 0; matCount < rootBlock
								.getPropertyCount(); matCount++) {
							mapNamePath.clear();
							SoapObject pdfBlock = (SoapObject) rootBlock
									.getProperty(matCount);
							if (pdfBlock != null
									&& pdfBlock.getAttributeCount() > 0) {
								String pdfPath = pdfBlock
										.getAttributeAsString("pdfPath");
								if (pdfPath != null && !pdfPath.equals("")) {

									int posLastSlash = pdfPath.lastIndexOf("/");
									String strFilename = pdfPath
											.substring(posLastSlash + 1);
									strFilename = strFilename.replaceAll("\\d",
											"");
									mapNamePath.put(pdfPath, strFilename);

								} else {
									mProgressHandler.sendEmptyMessage(-7);
								}

							}

						}

					}

				} catch (Exception e) {
					mProgressHandler.sendEmptyMessage(-3);
					return;
				}
			}
			return;
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		Menu m_menu = menu;
		if (mapNamePath != null && mapNamePath.size() > 0) {

			ArrayList<String> lstFileNames = new ArrayList<String>(
					mapNamePath.values());

			for (int pdfcnt = 0; pdfcnt < lstFileNames.size(); pdfcnt++) {
				m_menu.add(Menu.NONE, pdfcnt, 0, lstFileNames.get(pdfcnt))
				.setIcon(R.drawable.lst_subject);
			}

		} else {
			Toast.makeText(CurContext, "No Items available for download.",
					Toast.LENGTH_LONG).show();
		}

	}

	Map<String, String> mapNamePath = new HashMap<String, String>();
	private String ActionType;

	public class AsyncPdfDownloader extends AsyncTask<String, Boolean, Void> {

		String pathPhysicalFolder = "/mnt/sdcard/ChapterPDFResource";
		ProgressDialog downloadProgress;

		boolean isSuccess = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			downloadProgress = new ProgressDialog(getParent().getParent());
			downloadProgress.setIcon(R.drawable.note);
			downloadProgress.setTitle("Downloading...");
			downloadProgress.setIndeterminate(true);
			downloadProgress.setCancelable(false);
			downloadProgress.setCanceledOnTouchOutside(false);
			downloadProgress.show();

			File pdfFolderResource = new File(pathPhysicalFolder);

			if (!pdfFolderResource.exists()) 
			{
				pdfFolderResource.mkdir();
			}

		}

		@Override
		protected Void doInBackground(String... params) {

			try {
				if (params != null && params.length > 0) {

					String strVirtualPath = params[0];
					int posLastSlash = strVirtualPath.lastIndexOf("/");
					String strFilename = strVirtualPath.substring(posLastSlash + 1);

					strFilename = strFilename.replaceAll("\\d", "");
					pathPhysicalFolder = pathPhysicalFolder + File.separator + strFilename;
					File pdfFolderResource = new File(pathPhysicalFolder);

					if (!pdfFolderResource.exists()) 
					{
						pdfFolderResource.delete();
					}

					if (DownloaderService.downloadStudyMatPDF(strVirtualPath,pathPhysicalFolder)) 
					{
						isSuccess = true;

						if (DBProcSaveChapterDownload(SelectedSubjectID,SelectedChapterID, pathPhysicalFolder,strFilename) > 0) 
						{
							isSuccess = true;
						} 
						else 
						{
							isSuccess = false;
						}

					}
					else 
					{
						isSuccess = false;
					}

				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (downloadProgress != null && downloadProgress.isShowing()) {
				downloadProgress.dismiss();
				downloadProgress.cancel();
			}

			String strMessage = "";

			if (isSuccess) 
			{
				strMessage = "Download successful.Go to 'Study Materials' to view pdf.";
			} 
			else 
			{
				strMessage = "Download not successful.";
			}
			//Toast.makeText(CurContext, strMessage, Toast.LENGTH_LONG).show();

			AlertDialog alertDialog = new AlertDialog.Builder(Question_Download.this.getParent().getParent()).create(); 
			alertDialog.setTitle("Download status"); 
			alertDialog.setMessage(strMessage); 
			alertDialog.setIcon(R.drawable.tick); 

			alertDialog.setButton("OK", new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog, int which) { 

				} 
			}); 
			alertDialog.show();
		}

		private long DBProcSaveChapterDownload(String SubjectID,
				String ChapterID, String strPathPhysicalFolder, String PDFName) {

			long lreturn = 0;

			try {
				db = (new DatabaseHelper(CurContext)).getWritableDatabase();

				ContentValues values = new ContentValues();

				values.put(DatabaseHelper.FLD_STUDYMAT_SUBJECT_ID,
						Integer.parseInt(SubjectID));
				values.put(DatabaseHelper.FLD_STUDYMAT_CHAPTER_ID,
						Integer.parseInt(ChapterID));
				values.put(DatabaseHelper.FLD_STUDYMAT_PDF_NAME, PDFName);

				values.put(DatabaseHelper.FLD_STUDYMAT_PDF_PATH,
						strPathPhysicalFolder);

				if (db.update(
						DatabaseHelper.TBL_SUBJECT_CHAPTER_STUDY_MATERIAL,
						values, DatabaseHelper.FLD_STUDYMAT_SUBJECT_ID
						+ " = ? AND "
						+ DatabaseHelper.FLD_STUDYMAT_CHAPTER_ID
						+ " = ?", new String[] { SubjectID, ChapterID }) == 0) {

					lreturn = db.insert(
							DatabaseHelper.TBL_SUBJECT_CHAPTER_STUDY_MATERIAL,
							null, values);
				}

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

			return lreturn;
		}

	}

	private void MockExamActivity(int Position) {

		type = AppPreferenceStatus.getStudyDownload(CurContext);

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

		try {
			cursorChapter.moveToPosition(Position);

			SelectedChapterID = cursorChapter.getString(cursorChapter.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER));

			HttpTransportSE androidHttpTransport = null;
			SoapSerializationEnvelope envelope = null;

			SoapObject request = null;
			SoapObject soapResult = null;


			try {
				request = new SoapObject(WSNameSpace, WSMethod);

				PropertyInfo inf_userId = new PropertyInfo();
				inf_userId.setName("stringemailid");
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
				mProgressHandler.sendEmptyMessage(-2);
				return;
			}
			//----------------------------------------------------------------------------------------------------------------------
			if (soapResult != null) {

				try {
					SoapObject soapBlock = (SoapObject) soapResult
							.getProperty(0);
					SoapObject rootBlock = (SoapObject) soapBlock
							.getProperty(0);

					if (rootBlock.getPropertyCount() > 0) 
					{
						final String[] aLevelID = new String[rootBlock
						                                     .getPropertyCount()];
						final CharSequence[] aLevelName = new CharSequence[rootBlock
						                                                   .getPropertyCount()];

						for (int iLevel = 0; iLevel < rootBlock.getPropertyCount(); iLevel++) 
						{
							SoapObject levelBlock = (SoapObject) rootBlock.getProperty(iLevel);

							aLevelID[iLevel] = levelBlock.getAttribute(0).toString();
							aLevelName[iLevel] = levelBlock.getAttribute(3).toString();
						}

						adLevel.setTitle("Level(s) - Click below to start download");
						adLevel.setIcon(R.drawable.folder_yellow);
						adLevel.setItems(aLevelName,new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog,int item) 
							{

								try 
								{
									SelectedLevelID = aLevelID[item].toString();
								} 
								catch (Exception e) 
								{
									return;
								}

								adStartExam.setIcon(R.drawable.bt_question);
								adStartExam.setTitle("Download status");

								adStartExam.setMessage("Do you want to download "
										+ type
										+ " from Server?  'Yes' will download the exam and 'No' will cancel download.")
										.setPositiveButton("Yes",new DialogInterface.OnClickListener() 
										{
											public void onClick(DialogInterface dialog,int id) 
											{
												dialog.dismiss();
												pgExam.show();

												new AsyncQuestionDownloader().execute("");
											}
										})

										.setNegativeButton(
												"No",
												new DialogInterface.OnClickListener() 
												{
													public void onClick(DialogInterface dialog,int id) 
													{
														dialog.cancel();
													}
												});
								AlertDialog altStartExam = adStartExam.create();
								altStartExam.show();
							}
						});
						AlertDialog altLevel = adLevel.create();
						altLevel.show();

					} else {

						type = AppPreferenceStatus.getStudyDownload(CurContext);

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

						adStartExam.setIcon(R.drawable.information);
						adStartExam.setTitle("Download status");
						adStartExam.setMessage(
								"There is no "
										+ ActionType
										+ " available to download under this chapter.")
										.setCancelable(false)
										.setPositiveButton("Ok",
												new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,int id) 
											{
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
			//------------------------------------------------------------------------------------------------------------------	
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
		//
	}

	public class AsyncQuestionDownloader extends AsyncTask<String, TaskStatusMsg, Integer> {

		@Override
		protected Integer doInBackground(String... params) {

			TaskStatusMsg infoUpload = mExamSyncService.AnswerUpload(StudentInfo, actvity);
			publishProgress(infoUpload);

			int dwldSatus = DownloadExam(SelectedSubjectID, SelectedChapterID,SelectedLevelID);

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		db.close();
		return;
	}


	@Override
	public void onBackPressed() {
//		Intent intent = new Intent(CurContext, Exam_Download.class);
//		intent.putExtra("ExamIntent", intentType);
		//startActivity(intent);
		//finish();
		//return;

		//------------------------
//		View DownlaodMockview = getLocalActivityManager()
//				.startActivity("intentDownlaodMock", intent
//						.addFlags(Intent.EXTRA_DOCK_STATE_DESK))
//						.getDecorView();
//
//		replaceView(DownlaodMockview);
		
//-----------------------------------------------------
//		Intent intentStudyMaterial = new Intent(CurContext, Exam_Download.class);
//		intentStudyMaterial.putExtra("Class", "Question_Download");
//		intentStudyMaterial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		int IntentTypeForStudyMat = CourseMatIntent.StudyMaterials.getNumber();
//		intentStudyMaterial.putExtra("ExamIntent", IntentTypeForStudyMat);
//		
//		View DownlaodMockview = getLocalActivityManager()
//				.startActivity("intentDownlaodMock", intentStudyMaterial
//						.addFlags(Intent.EXTRA_DOCK_STATE_DESK))
//						.getDecorView();
//
//		replaceView(DownlaodMockview);
//-----------------------------------------------------

	}

	public void replaceView(View v) {
		history.add(v);
		setContentView(v);
	}

	public void back() 
	{
		if(history.size() > 0) 
		{
			history.remove(history.size()-1);
			setContentView(history.get(history.size()-1));
		}
		else 
		{
			finish();
		}
	}



	private class ChapterAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public ChapterAdapter(Context context, Cursor cursor) {
			super(context, cursor, true);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			try {
				TextView t;
				t = (TextView) view.findViewById(R.id.tvChapter);
				t.setText(cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER)));
			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.chapter_row, parent,
					false);
			return view;
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
			SoapObject AOptionBlock = (SoapObject) soapObj.getProperty("A" + questionID);

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
					SuccessMessage = "Exam has been successfully downloaded to your device. To start exam select 'Mock Test'.";
					break;
				case PracticeExam:
					SuccessMessage = "Practice Exam has been successfully downloaded to your device. To start exam select 'Class Exercise'.";
					break;
				case StudyMaterials:
					SuccessMessage = "Study Material has been successfully downloaded to your device. To start reading select 'Study Material'.";
					break;
				}

				dlgMsgbuilder.setMessage(SuccessMessage);
				dlgMsgbuilder.setCancelable(false);
				
				dlgMsgbuilder.setPositiveButton("Give Exam Now",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) 
					{
						Intent intentStudyMaterial = new Intent(CurContext, ica.tab.view.MockTestActivity.class);
						intentStudyMaterial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						
						View DownlaodMockview = getLocalActivityManager()
								.startActivity("intentDownlaodMock", intentStudyMaterial
										.addFlags(Intent.EXTRA_DOCK_STATE_DESK))
										.getDecorView();

						replaceView(DownlaodMockview);
					}
				});
				
				dlgMsgbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) 
					{
						try {
							Intent intentNew = new Intent(CurContext,Exam_Download.class);
							intentNew.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intentNew.putExtra("ExamIntent", intentType);
							//startActivity(intent);

							//							 View DownlaodMockview = getLocalActivityManager()
							//									 .startActivity("intentDownlaodMock", intentNew
							//									 .addFlags(Intent.EXTRA_DOCK_STATE_DESK))
							//					                 .getDecorView();
							//					        
							//							replaceView(DownlaodMockview);

							//finish();
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
				dlgMsgbuilder.setMessage(""
						+ "Insufficient Data!Contact Admin.");
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
			case -7:
				if (pgExam.isShowing()) {
					pgExam.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.information);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("No study materials avaliable.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			}
		}
	};

	//	@Override
	//	public void onBackPressed() {
	//		//super.onBackPressed();
	//		//Question_Download.this.back(();
	//		//finish();
	//		//Toast.makeText( getApplicationContext(),"Back pressed",Toast.LENGTH_SHORT).show();
	//	}
}
