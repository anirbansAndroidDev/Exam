package ica.PracticeMaterials;

import static ica.exam.IndexActivity.ExamStatusCode;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.achartengine.util.MathHelper;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.exam.DatabaseHelper;
import ica.exam.ExamActivity;
import ica.exam.Exam_Download;
import ica.exam.R;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class StudyMaterialSubjectBrowserActivity extends ActivityGroup {

	private Cursor cursorChapter;
	private SQLiteDatabase db;
	public static AlertDialog.Builder adPDFList;
	public static AlertDialog.Builder adPDFListNew;

	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedStudyMatPath;
	private ArrayList<View> history;
	ListView ListViewSubject;
	private Context CurContext;
	public static int checkClass = 0;

	StudentDetails studentDetails = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chapter_list_download);
		this.history = new ArrayList<View>();
		CurContext = this;

		SelectedSubjectID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_SUBJECT);

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();

		try {

			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			String sEmail = studentDetails.getStudentID();

			if (sEmail != null) {
				setTitle("Study Material Browser- [" + sEmail + "]");

			}
			
			//-------------------------------------------
//			Toast.makeText(getApplicationContext(), "Insiade StudyMet",Toast.LENGTH_SHORT).show();
			adPDFList = new Builder(this.getParent().getParent());
			adPDFListNew = new Builder(this.getParent().getParent());
//
//			if(checkClass == 1)
//			{
//				Toast.makeText(getApplicationContext(), "CheckClass 1",Toast.LENGTH_SHORT).show();
//				
//				adPDFList = new Builder(this.getParent().getParent().getParent().getParent());
//				adPDFListNew = new Builder(this.getParent().getParent().getParent().getParent());
//			}
//			else
//			{
//
//			}
			//---------------------------------------------

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
		}

		// ////////////////SYNC CHAPTER
		// LISTS///////////////////////////////////////////////////////////////

		try {
			cursorChapter = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ ", " + DatabaseHelper.FLD_ID_CHAPTER + ","
					+ DatabaseHelper.FLD_NAME_CHAPTER + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					// + DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = 'F'"
					// + " AND "
					+ DatabaseHelper.FLD_ID_SUBJECT + " = ?",
					new String[] { SelectedSubjectID });

			startManagingCursor(cursorChapter);

			ListAdapter ListAdaptersubject = new ChapterAdapter(CurContext,
					cursorChapter);
			ListViewSubject = (ListView) findViewById(R.id.lvChapter);
			ListViewSubject.setAdapter(ListAdaptersubject);

			ListViewSubject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

					chapterPosition = position;
					new AsyncFetchDBPDFList().execute(Integer.toString(chapterPosition));

				}
			});

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

	}

	int chapterPosition = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.downloadmennu, menu);

		return true;
	}

	public class AsyncFetchDBPDFList extends AsyncTask<String, Void, Void> {
		Cursor cursorPDFList;
		String[] mString = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			if (params != null
					&& (params[0] != null && !params[0].equals(""))
					&& (SelectedSubjectID != null && !SelectedSubjectID
					.equals(""))) {

				cursorChapter.moveToPosition(Integer.parseInt(params[0]));

				SelectedChapterID = cursorChapter.getString(cursorChapter
						.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER));

				if (SelectedChapterID != null && !SelectedChapterID.equals("")) {
					DBProcChapterPDFList(Integer.parseInt(SelectedSubjectID),
							Integer.parseInt(SelectedChapterID));
				} else {
					mString = null;
				}
			} else {
				mString = null;
			}

			return null;
		}

		private void DBProcChapterPDFList(int SubjectID, int ChapterID) {

			try {
				cursorPDFList = db.rawQuery("select * from "
						+ DatabaseHelper.TBL_SUBJECT_CHAPTER_STUDY_MATERIAL
						+ " where " + DatabaseHelper.FLD_STUDYMAT_SUBJECT_ID
						+ "='" + SubjectID + "' AND "
						+ DatabaseHelper.FLD_STUDYMAT_CHAPTER_ID + "='"
						+ ChapterID + "'", null);

				startManagingCursor(cursorPDFList);

				if (cursorPDFList != null && cursorPDFList.getCount() > 0) {

					ArrayList<String> strings = new ArrayList<String>();

					for (cursorPDFList.moveToFirst(); !cursorPDFList
							.isAfterLast(); cursorPDFList.moveToNext()) {
						String mTitleRaw = cursorPDFList
								.getString(cursorPDFList
										.getColumnIndex(DatabaseHelper.FLD_STUDYMAT_PDF_NAME));
						strings.add(mTitleRaw);
					}

					mString = (String[]) strings.toArray(new String[strings
					                                                .size()]);

				} else {
					mString = null;
				}

			} catch (Exception e) {
				e.printStackTrace();
				mString = null;
				try {
					cursorPDFList.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
			cursorPDFList = null;

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mString != null && mString.length > 0) {

				adPDFList.setTitle("Study Materials(s)");
				adPDFList.setIcon(R.drawable.folder_yellow);
				adPDFList.setItems(mString,new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int item) {
						try {

							if ((SelectedSubjectID != null && !"".equals(SelectedSubjectID))&& (SelectedChapterID != null && !"".equals(SelectedChapterID))) 
							{
								cursorPDFList = db
										.rawQuery(
												"select * from "
														+ DatabaseHelper.TBL_SUBJECT_CHAPTER_STUDY_MATERIAL
														+ " where "
														+ DatabaseHelper.FLD_STUDYMAT_SUBJECT_ID
														+ "='"
														+ SelectedSubjectID
														+ "' AND "
														+ DatabaseHelper.FLD_STUDYMAT_CHAPTER_ID
														+ "='"
														+ SelectedChapterID
														+ "'", null);

								startManagingCursor(cursorPDFList);

								if (cursorPDFList != null
										&& cursorPDFList.getCount() > 0) {

									cursorPDFList.moveToPosition(item);

									SelectedStudyMatPath = cursorPDFList.getString(cursorPDFList
											.getColumnIndex(DatabaseHelper.FLD_STUDYMAT_PDF_PATH));

									if (SelectedStudyMatPath != null
											&& !"".equals(SelectedStudyMatPath)) 
									{
										File file = new File(
												SelectedStudyMatPath);

										if (file.exists()) 
										{
											Uri path = Uri.fromFile(file);
											Intent intent = new Intent(Intent.ACTION_VIEW);
											intent.setDataAndType(path,"application/pdf");
											intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

											try 
											{
												startActivity(intent);
											} 
											catch (ActivityNotFoundException e) 
											{
												Toast.makeText(
														CurContext,
														"No Application Available to View PDF",
														Toast.LENGTH_SHORT)
														.show();
											}
										}
									} 

									else 
									{
										adPDFList.setIcon(R.drawable.information);
										adPDFList.setTitle("Search Status");
										adPDFList.setPositiveButton("Ok", null).create();
										adPDFList.setMessage("There are no study material available with this name.")
										.setCancelable(false)
										.setPositiveButton("Ok",new DialogInterface.OnClickListener() 
										{
											public void onClick(DialogInterface dialog,int id) 
											{
												dialog.dismiss();
												return;
											}
										});
										AlertDialog altStartExam = adPDFList.create();
										
											altStartExam.show();
									}

								}

							}

						} catch (Exception e) {
							return;
						}

					}
				});
				adPDFList.setPositiveButton("Cancel",new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog,int id) 
					{
						dialog.dismiss();
						return;
					}
				});
				adPDFList.setCancelable(false);
				AlertDialog altLevel = adPDFList.create();
				
					altLevel.show();
			} 
			else {
				adPDFListNew.setIcon(R.drawable.information);
				adPDFListNew.setTitle("Search Status");
				adPDFListNew.setPositiveButton("Ok", null).create();
				adPDFListNew.setMessage("There are no study materials available under this chapter.Please download from the 'Download' option.")
				.setCancelable(false)
				.setPositiveButton("Ok",new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog,int id) 
					{
						dialog.dismiss();
						return;
					}
				});
				AlertDialog altStartExam = adPDFListNew.create();
					altStartExam.show();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menuMockExamDownload:

			return true;
		case R.id.menuStudyMatDownload:

			return true;
			/*
			 * case R.id.menuPracticeExamDownload:
			 * 
			 * return true;
			 */
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public void onBackPressed() {

		try 
		{
			db.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		//finish();

//		Intent intent = new Intent(CurContext, ExamActivity.class);

//		int IntentType = CourseMatIntent.StudyMaterials.getNumber();
//		intent.putExtra("ExamIntent", IntentType);

		//startActivity(intent);

//		View intentStudyMaterialsview = getLocalActivityManager()
//				.startActivity("intentStudyMaterials", intent
//						.addFlags(Intent.EXTRA_DOCK_STATE_DESK))
//						.getDecorView();
//
//		replaceView(intentStudyMaterialsview);
		//finish();
		
//-------------------------------------------
//		Intent intentStudyMaterial = new Intent(CurContext, Exam_Download.class);
//		intentStudyMaterial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intentStudyMaterial.putExtra("Class", "StudyMaterialSubjectBrowserActivity");
//		int IntentTypeForStudyMat = CourseMatIntent.StudyMaterials.getNumber();
//		intentStudyMaterial.putExtra("ExamIntent", IntentTypeForStudyMat);
//		
//		View DownlaodMockview = getLocalActivityManager()
//				.startActivity("intentDownlaodMock", intentStudyMaterial
//						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//						.getDecorView();
//
//		replaceView(DownlaodMockview);
//-------------------------------------------
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
}
