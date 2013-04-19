package ica.exam;


import java.util.ArrayList;

import ica.ICAConstants.CourseMatIntent;
import ica.PracticeMaterials.StudyMaterialSubjectBrowserActivity;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppPreferenceStatus;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.AlertDialog.Builder;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class Exam_Download extends ActivityGroup {
	
	public static Exam_Download group;
	private Cursor cursorSubject;
	private SQLiteDatabase db;
	private String SelectedSubjectID;
	private ArrayList<View> history;

	StudentDetails studentDetails = null;

	Context CurContext;
	
	CourseMatIntent intentType;

	int IntentFrom;
	private String ActionType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subject_list_download);
		CurContext = this;
		this.history = new ArrayList<View>();
		
		//-----------------------------------------------
//		Intent intent = getIntent();
//		String extras = intent.getStringExtra("Class");
//		try {
//			if(extras != null)
//			{
//				if(extras.equalsIgnoreCase("StudyMaterialSubjectBrowserActivity"))
//				{
//					Toast.makeText(getApplicationContext(), "StudyMaterialSubjectBrowserActivity",Toast.LENGTH_SHORT).show();
//					StudyMaterialSubjectBrowserActivity.checkClass = 1;
//				}
//				
//				else if(extras.equalsIgnoreCase("Question_Download"))
//				{
//					Toast.makeText(getApplicationContext(), "Question_Download",Toast.LENGTH_SHORT).show();
//					Question_Download.checkClass = 2;
//				}
//			}
//			else
//			{
//				Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_SHORT).show();
//			}
//			
//		} 
//		catch (Throwable e1) 
//		{
////			Toast.makeText(getApplicationContext(), ""+e1, Toast.LENGTH_SHORT).show();
//		}
		//------------------------------------------------


		try {
			db = (new DatabaseHelper(this)).getWritableDatabase();

			StudentDetails.initInstance(CurContext);

			studentDetails = StudentDetails.getInstance();

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

			String sEmail = studentDetails.getStudentID();
			if (sEmail != null) {

				setTitle("Subject List-" + ActionType + "- [" + sEmail + "]");

			}

			cursorSubject = db.query(DatabaseHelper.TBL_SUBJECT, new String[] {
					DatabaseHelper.FLD_ROWID, DatabaseHelper.FLD_ID_SUBJECT,
					DatabaseHelper.FLD_NAME_SUBJECT }, null, null, null, null,
					null);

			IntentFrom = getIntent().getIntExtra("ExamIntent",
					CourseMatIntent.DownlaodMock.getNumber());
			intentType = CourseMatIntent.fromInteger(IntentFrom);

			startManagingCursor(cursorSubject);

			ListAdapter ListAdaptersubject = new SubjectAdapter(this,
					cursorSubject);
			ListView ListViewSubject = (ListView) findViewById(R.id.lvSubject);
			ListViewSubject.setAdapter(ListAdaptersubject);

			ListViewSubject
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {

							try {
								cursorSubject.moveToPosition(position);
								SelectedSubjectID = cursorSubject.getString(cursorSubject
										.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT));

								switch (intentType) {
								case DownlaodMock:
									Intent intentDownlaodMock = new Intent();
									intentDownlaodMock.setClass(CurContext,Question_Download.class);
									intentDownlaodMock.putExtra(DatabaseHelper.FLD_ID_SUBJECT,SelectedSubjectID);
									intentDownlaodMock.putExtra("ExamIntent", intentType);
									
									//startActivity(intent);
									View DownlaodMockview = getLocalActivityManager()
											 .startActivity("intentDownlaodMock", intentDownlaodMock
											 .addFlags(Intent.EXTRA_DOCK_STATE_DESK))
						                     .getDecorView();
							        
									replaceView(DownlaodMockview);
									//finish();

									break;
								case MockExam:
									Intent intentMockExam = new Intent();
									intentMockExam.setClass(CurContext,
											Question_Download.class);
									intentMockExam.putExtra(
											DatabaseHelper.FLD_ID_SUBJECT,
											SelectedSubjectID);
									intentMockExam.putExtra("ExamIntent", intentType);
									//startActivity(intentMockExam);
									View intentMockExamview = getLocalActivityManager()
											 .startActivity("intentMockExam", intentMockExam
											 .addFlags(Intent.EXTRA_DOCK_STATE_DESK))
						                     .getDecorView();
							        
									replaceView(intentMockExamview);
									//finish();

									break;
								case PracticeExam:
									Intent intentPracticeExam = new Intent();
									intentPracticeExam.setClass(CurContext,
											Question_Download.class);
									intentPracticeExam.putExtra(
											DatabaseHelper.FLD_ID_SUBJECT,
											SelectedSubjectID);
									intentPracticeExam.putExtra("ExamIntent", intentType);
									
									//startActivity(intentPracticeExam);
									View intentPracticeExamview = getLocalActivityManager()
											 .startActivity("intentPracticeExam", intentPracticeExam
											 .addFlags(Intent.EXTRA_DOCK_STATE_DESK))
						                     .getDecorView();
							        
									replaceView(intentPracticeExamview);
									//finish();

									break;
								case StudyMaterials:
									Intent intentStudyMaterials = new Intent();
									intentStudyMaterials.setClass(CurContext,StudyMaterialSubjectBrowserActivity.class);
									intentStudyMaterials.putExtra(DatabaseHelper.FLD_ID_SUBJECT,SelectedSubjectID);
									//startActivity(intentStudyMaterials);
									
									View intentStudyMaterialsview = getLocalActivityManager()
											 .startActivity("intentStudyMaterials", intentStudyMaterials
											 .addFlags(Intent.EXTRA_DOCK_STATE_DESK))
						                     .getDecorView();
							        
									replaceView(intentStudyMaterialsview);

									//finish();

									break;
								}

							} catch (SQLiteException sqle) {
								Toast.makeText(getApplicationContext(),
										sqle.getMessage(), Toast.LENGTH_SHORT)
										.show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(), Toast.LENGTH_SHORT)
										.show();
							}
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

	public void replaceView(View v) {
		history.add(v);
		setContentView(v);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();

		try {
			if (db != null)
				db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	@Override
//	public void onBackPressed() {
//		finish();
//	}

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
				t = (TextView) view.findViewById(R.id.tvSubject);
				t.setText(cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.FLD_NAME_SUBJECT)));

				Cursor cursorChapter = db
						.rawQuery(
								"SELECT " + DatabaseHelper.FLD_ID_CHAPTER
										+ " FROM " + DatabaseHelper.TBL_CHAPTER
										+ " WHERE "
										+ DatabaseHelper.FLD_ID_SUBJECT
										+ " = ?",
								new String[] { cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT)) });

				startManagingCursor(cursorChapter);

				if (cursorChapter != null) {
					t = (TextView) view.findViewById(R.id.tvChapterCount);
					t.setText("No. of chapter (" + cursorChapter.getCount()
							+ ")");
				} else {
					t = (TextView) view.findViewById(R.id.tvChapterCount);
					t.setText("No. of chapter (0)");
				}

				cursorChapter.close();
			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.subject_row, parent,
					false);
			return view;
		}
	}
	public void back() {
		if(history.size() > 0) {
			history.remove(history.size()-1);
			setContentView(history.get(history.size()-1));
		}else {
			finish();
		}
	}

//   public void onBackPressed() {
//    	Exam_Download.group.back();
//    	finish();
//        return;
//    }
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();

		//finish();
		//Toast.makeText( getApplicationContext(),""+this.getClass().getSimpleName(),Toast.LENGTH_SHORT).show();
		
		//==================================================================================================================
		
		LocalActivityManager localManager = getLocalActivityManager();
	    Activity currentActivity = null;
	    if (localManager!=null)
	    {
	       currentActivity = localManager.getCurrentActivity();
	    }

	    String activityName = null;
	    if (currentActivity!=null)
	    {
	        activityName = currentActivity.toString();
	    }

	    if(activityName!=null && activityName.contains("Question_Download"))
	    {
	        getCurrentActivity().onBackPressed();
//	    	Toast.makeText( getApplicationContext(),"From Question_Download",Toast.LENGTH_LONG).show();
	    }
	    
	    else if(activityName!=null && activityName.contains("StudyMaterialSubjectBrowserActivity"))
	    {
	        getCurrentActivity().onBackPressed();
//	    	Toast.makeText( getApplicationContext(),"From StudyMaterialSubjectBrowserActivity",Toast.LENGTH_LONG).show();
	    }
	    else
	    {

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getParent()); 
			  
	        alertDialog.setTitle("Confirm Exit ..."); 
	        alertDialog.setMessage("Are you sure to exit ?"); 
	        alertDialog.setIcon(R.drawable.tick); 
	  
	        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() { 
	            public void onClick(DialogInterface dialog,int which) {
	            	AppPreferenceStatus.setLoggedOutStatus(CurContext, true);
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
	    }
		//==================================================================================================================
	}


}
