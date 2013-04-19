package ica.exam;

import ica.ProfileInfo.StudentDetails;
import android.app.ListActivity;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class QuestionList_Result extends ListActivity{
	private Cursor cursorQuestion; 
	private SQLiteDatabase db;
	private String SelectedSubjectId;
	private String SelectedChapterId;
	private String SelectedExamId;
	private String QuestionType;
	private Intent intentQSCQ;
	private Intent intentQMCQ;
	private Intent intentQFIB;
	private Intent intentQMMM;
	private StudentDetails studentDetails;
	    
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			try{
			    setContentView(R.layout.question_list_result);
				SelectedSubjectId = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_SUBJECT);
				SelectedChapterId = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_CHAPTER);
				
				db = (new DatabaseHelper(this)).getWritableDatabase();
	
				StudentDetails.initInstance(this);

				studentDetails = StudentDetails.getInstance();
				
				
			    String sEmail = studentDetails.getStudentID();
			    if (sEmail != null){
			    	setTitle("Result - [" + sEmail + "]");
			    }

			    ImageButton buttonHome = (ImageButton)this.findViewById(R.id.btHome);
			    buttonHome.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
				    	goHome();
				    }
				});
			    
			    ImageButton buttonDone = (ImageButton)this.findViewById(R.id.btReview);
				buttonDone.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
				    	goResult();
				    }
				});

			    
				intentQSCQ = new Intent(this, Question_SCQ_Details_Result.class);
				intentQMCQ = new Intent(this, Question_MCQ_Details_Result.class);
				intentQFIB = new Intent(this, Question_FIB_Details_Result.class);
				intentQMMM = new Intent(this, Question_MM_Details_Result.class);
				
			}catch(SQLiteException sqle){
				Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			try{
				cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID + "," + DatabaseHelper.FLD_ID_EXAM + "," + DatabaseHelper.FLD_EXAM_NAME + "," + DatabaseHelper.FLD_ID_QUESTION
						+ ", " + DatabaseHelper.FLD_QUESTION_TYPE + ", " + DatabaseHelper.FLD_QUESTION_MARKS + ", " + DatabaseHelper.FLD_QUESTION_BODY 
						+ ", " + DatabaseHelper.FLD_QUESTION_ANSWERED + ", " + DatabaseHelper.FLD_ANSWER_CORRECT
						+ " FROM " + DatabaseHelper.TBL_EXAM 
						+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
						+ " ORDER BY " + DatabaseHelper.FLD_ID_QUESTION, 
				new String[]{ SelectedChapterId });
				
				startManagingCursor(cursorQuestion);
				
				if (cursorQuestion != null) {
					if (cursorQuestion.moveToFirst()) {
						SelectedExamId = cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_EXAM));
						
						ListAdapter ListAdapterquestion = new QuestionAdapter(this,cursorQuestion);
						ListView ListViewQuestion = (ListView) findViewById(android.R.id.list);
						ListViewQuestion.setAdapter(ListAdapterquestion);
				
						ListViewQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		              		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try{
									cursorQuestion.moveToPosition(position);
									QuestionType = cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE));
									
									if (QuestionType.equals("SCQ")) {
										intentQSCQ.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectId);
										intentQSCQ.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterId);
										intentQSCQ.putExtra(DatabaseHelper.FLD_ID_EXAM, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
										intentQSCQ.putExtra(DatabaseHelper.FLD_ID_QUESTION, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
										startActivity(intentQSCQ);
										finish();
										return;
										
									}
									else if(QuestionType.equals("MCQ")){
										intentQMCQ.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectId);
										intentQMCQ.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterId);
										intentQMCQ.putExtra(DatabaseHelper.FLD_ID_EXAM, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
										intentQMCQ.putExtra(DatabaseHelper.FLD_ID_QUESTION, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
										startActivity(intentQMCQ);
										finish();
										return;
									}
									else if(QuestionType.equals("FIB")){
										intentQFIB.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectId);
										intentQFIB.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterId);
										intentQFIB.putExtra(DatabaseHelper.FLD_ID_EXAM, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
										intentQFIB.putExtra(DatabaseHelper.FLD_ID_QUESTION, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
										startActivity(intentQFIB);
										finish();
										return;
									}
									else if(QuestionType.equals("M&M")){
										intentQMMM.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectId);
										intentQMMM.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterId);
										intentQMMM.putExtra(DatabaseHelper.FLD_ID_EXAM, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
										intentQMMM.putExtra(DatabaseHelper.FLD_ID_QUESTION, cursorQuestion.getString(cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
										startActivity(intentQMMM);
										finish();
										return;
									}
								}catch(SQLiteException sqle){
									Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
		db.close();
	}

	@Override
	public void onBackPressed() {
    	Intent intent = new Intent(this, SubjectList_Result.class);
		startActivity(intent);
		finish();
		return;
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_result, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.itHome:
        	goHome();
            return true;

        case R.id.itResult:
        	goResult();
            return true;
    }

        return true;
}*/
	
	private void goHome() {
		try{
//				finish();
//				Intent intent = new Intent(this, SubjectList_Result.class);
//				startActivity(intent);
			
			finish();
			return;
				
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		
		return;
	}
	
	private void goResult() {
		try{
			Intent intent = new Intent(this, Score_Card_Result.class);
			intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectId);
    	    intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterId);
    	    intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamId);
    	    startActivity(intent);
    	    finish();
    	    return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return;
	}

	private class QuestionAdapter extends CursorAdapter{
	    private final LayoutInflater mInflater;

	    public QuestionAdapter(Context context, Cursor cursor) {
	      super(context, cursor, true);
	      mInflater = LayoutInflater.from(context);
	    }

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	    	try{
			      TextView t;
			      t = (TextView) view.findViewById(R.id.tvquestionbody);
			      t.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_BODY)));
			      
			      t = (TextView) view.findViewById(R.id.tvQuestionType);
			      if ((cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE))).equals("FIB")){
			    	  t.setText("Fill the blank");
			      }else if ((cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE))).equals("SCQ")){
			    	  t.setText("Single choice");
			      }else if ((cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE))).equals("MCQ")){
			    	  t.setText("Mix & match");
			      }else if ((cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE))).equals("M&M")){
			    	  t.setText("A/C ledger");
			      }
			      
			      t = (TextView) view.findViewById(R.id.tvQuestionMarks);
			      t.setText(" (Point - " + cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS)) + ")");
			      
			      ImageView i = (ImageView) view.findViewById(R.id.imgStatus);
			      
			      if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED)).equals("T")){
			    	  if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.FLD_ANSWER_CORRECT)).equals("T")){
			    		  i.setImageResource(R.drawable.ico_correct_answer);
			    	  }else{
			    		  i.setImageResource(R.drawable.ico_rong_answer);
			    	  }
			      }else{
			    	  i.setImageResource(R.drawable.bt_question);
			      }
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
	    }
	    
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	      final View view = mInflater.inflate(R.layout.question_row, parent, false);
	      return view;
	    }
	  }
		
	
}
