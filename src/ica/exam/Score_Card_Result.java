package ica.exam;


import ica.ProfileInfo.StudentDetails;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Score_Card_Result extends Activity{
	private SQLiteDatabase db;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedExamID;
	private StudentDetails studentDetails;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			try{
				setContentView(R.layout.score_card_result);
			
				SelectedSubjectID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_SUBJECT);
				SelectedChapterID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_CHAPTER);
				SelectedExamID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_EXAM);
				
				db = (new DatabaseHelper(this)).getWritableDatabase();

				StudentDetails.initInstance(this);

				studentDetails = StudentDetails.getInstance();
				
				
				String sEmail = studentDetails.getStudentID();
			    if (sEmail != null){
			    	setTitle("Result - [" + sEmail + "]");
			    }

				fillData(SelectedSubjectID,SelectedChapterID, SelectedExamID);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			try{
				 ImageButton buttonDone = (ImageButton)this.findViewById(R.id.btReview);
				 buttonDone.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
				    	goReview();
				    }
				 });
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			
			return;
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
		goHome();
		return;
	}

	private void goHome() {
		try{			
				finish();
				return;
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		
		return;
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.score_card, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.itHome:
        	goHome();
            return true;

        case R.id.itReview:
        	goReview();
            return true;
    }

        return true;
	}*/
	
	private void fillData(String subjectid, String chapterid, String examid){
		int iMarks = 0;
		int iQuestionCount = 0;
		int iAttemptedCount = 0;
		int iCorrectCount = 0;
		int iRongCount = 0;
		int iScoreCount = 0;
		
		try{
			TextView tvSubject = (TextView) findViewById(R.id.tvSubjectValue);
			tvSubject.setText(GetSubject(subjectid));

			TextView tvChapter = (TextView) findViewById(R.id.tvChapterValue);
			tvChapter.setText(GetChapter(subjectid, chapterid));
		
			Cursor cursorExam = db.rawQuery("SELECT " + DatabaseHelper.FLD_EXAM_NAME + "," + DatabaseHelper.FLD_QUESTION_MARKS    
					+ " FROM " + DatabaseHelper.TBL_EXAM 
					+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?", 
			new String[]{ chapterid, examid });
				
			iQuestionCount = cursorExam.getCount();
			int colMarks = cursorExam.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS);

			startManagingCursor(cursorExam);
				
            if (cursorExam != null) {
            		if (cursorExam.moveToFirst()) {
	                    do {
	                    	iMarks = iMarks + Integer.parseInt(cursorExam.getString(colMarks));
	                    	
	                    } while (cursorExam.moveToNext());
            		}
            }
			
            cursorExam.close();

            TextView tMarks = (TextView) findViewById(R.id.tvTotalMarksValue);
    		tMarks.setText(iMarks + "");

    		TextView tTotalQuestion = (TextView) findViewById(R.id.tvTotalQuestionValue);
    		tTotalQuestion.setText(iQuestionCount+"");
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
        

		try{
			Cursor cursorAnswer = db.rawQuery("SELECT " + DatabaseHelper.FLD_ANSWER_CORRECT + "," + DatabaseHelper.FLD_QUESTION_ANSWERED + "," + DatabaseHelper.FLD_QUESTION_MARKS   
					+ " FROM " + DatabaseHelper.TBL_EXAM 
					+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?", 
			new String[]{ chapterid, examid });
				
			int colCorrect = cursorAnswer.getColumnIndex(DatabaseHelper.FLD_ANSWER_CORRECT);
			int colAnswered = cursorAnswer.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED);
			int colMarks = cursorAnswer.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS);
			
			startManagingCursor(cursorAnswer);
				
            if (cursorAnswer != null) {
            		if (cursorAnswer.moveToFirst()) {
	                    do {
	                    	if (cursorAnswer.getString(colAnswered).equals("T")){
	                    		iAttemptedCount++;

		                    	if (cursorAnswer.getString(colCorrect).equals("T")){
		                    		iCorrectCount++;
		                    		iScoreCount = iScoreCount + Integer.parseInt(cursorAnswer.getString(colMarks));
		                    	}else{
		                    		iRongCount++; 		
		                    	}

	                    	}
	                    } while (cursorAnswer.moveToNext());
            		}
            }
			
            cursorAnswer.close();

            TextView tScore = (TextView) findViewById(R.id.tvScoreValue);
            tScore.setText(iScoreCount+"");

            TextView tAttemped = (TextView) findViewById(R.id.tvAttemptedValue);
            tAttemped.setText(iAttemptedCount+"");

            TextView tCorrect = (TextView) findViewById(R.id.tvCorrectValue);
            tCorrect.setText(iCorrectCount+"");

            TextView tRong = (TextView) findViewById(R.id.tvWrongValue);
            tRong.setText(iRongCount+"");
            
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		return;
	}

	/*private void goHome() {
		try{
				Intent intent = new Intent(this, ExamActivity.class);
				startActivity(intent);
				finish();
				return;
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		
		return;
	}*/

	private void goReview() {
		try{
	    	Intent intent = new Intent(this, QuestionList_Result.class);
	    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
	    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			startActivity(intent);
			finish();
			return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	   	return;
	}
	
	private String GetSubject(String subjectid) {
		String subject = null;
		
		try{
			Cursor cursorSubject = db.rawQuery("SELECT " + DatabaseHelper.FLD_NAME_SUBJECT 
					+ " FROM " + DatabaseHelper.TBL_SUBJECT 
					+ " WHERE " + DatabaseHelper.FLD_ID_SUBJECT + " = ?", 
			new String[]{ subjectid });
				
			int colName = cursorSubject.getColumnIndex(DatabaseHelper.FLD_NAME_SUBJECT);
			startManagingCursor(cursorSubject);
			
			if (cursorSubject != null){
				if (cursorSubject.moveToFirst()) {
					subject = cursorSubject.getString(colName);
				}
			}
			
			cursorSubject.close();
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return subject;
	}

	private String GetChapter(String subjectid, String chapterid) {
		String chapter = null;
		
		try{
			Cursor cursorChapter = db.rawQuery("SELECT " + DatabaseHelper.FLD_NAME_CHAPTER 
					+ " FROM " + DatabaseHelper.TBL_CHAPTER 
					+ " WHERE " + DatabaseHelper.FLD_ID_SUBJECT + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_CHAPTER + " = ?",
			new String[]{ subjectid, chapterid });
				
			int colName = cursorChapter.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER);
			startManagingCursor(cursorChapter);
			
			if (cursorChapter != null){
				if (cursorChapter.moveToFirst()) {
					chapter = cursorChapter.getString(colName);
				}
			}
			cursorChapter.close();
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return chapter;
	}

}
