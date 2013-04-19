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

public class Question_SCQ_Details_Result extends Activity{
	private SQLiteDatabase db;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedExamID;
	private String SelectedQuestionID;
	private String PreviousQuestionID;
	private String NextQuestionID;
	private StudentDetails studentDetails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.question_scq_details_result);

			try{
				db = (new DatabaseHelper(this)).getWritableDatabase();
	
				StudentDetails.initInstance(this);

				studentDetails = StudentDetails.getInstance();
				
				
			    String sEmail = studentDetails.getStudentID();
			    if (sEmail != null){
			    	setTitle("Result - [" + sEmail + "]");
			    }
				
				SelectedSubjectID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_SUBJECT);
				SelectedChapterID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_CHAPTER);
				SelectedExamID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_EXAM);
				SelectedQuestionID = getIntent().getExtras().getString(DatabaseHelper.FLD_ID_QUESTION);

				ImageButton buttonPrevious = (ImageButton)this.findViewById(R.id.btPrevious);
				 buttonPrevious.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
			        	goPrevious();
				    }
				 });
				        
				 ImageButton buttonQuestionList = (ImageButton)this.findViewById(R.id.btQuestionList);
				 buttonQuestionList.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
				    	goQuestion();
				    }
				 });

				 ImageButton buttonNext = (ImageButton)this.findViewById(R.id.btNext);
				 buttonNext.setOnClickListener(new OnClickListener() {
				    public void onClick(View v) {
				    	goNext();
				    }
				 });
				
				fillData(SelectedChapterID, SelectedExamID, SelectedQuestionID);
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
		Intent intent = new Intent(this, SubjectList_Result.class);
		startActivity(intent);
		finish();
		return;
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_result_each, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.itPrevious:
        	goPrevious();
            return true;

        case R.id.itQuestion:
        	goQuestion();
            return true;

        case R.id.itResult:
        	goResult();
            break;
            
        case R.id.itNext:
        	goNext();
            break;
    }

        return true;
}*/

	private void fillData(String chapterid, String examid, String questionid){
		String sQMarks = null;
		String sQBody = null;

		try{
			Cursor cursorExam = db.rawQuery("SELECT " + DatabaseHelper.FLD_EXAM_NAME + ", " + DatabaseHelper.FLD_ID_QUESTION + ", " + DatabaseHelper.FLD_QUESTION_MARKS + "," 
							+ DatabaseHelper.FLD_QUESTION_BODY  + ", " + DatabaseHelper.FLD_QUESTION_ANSWERED
							+ " FROM " + DatabaseHelper.TBL_EXAM 
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
							+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?"
							+ " ORDER BY " + DatabaseHelper.FLD_ID_QUESTION,
							new String[]{ chapterid, examid });
			
			//int iCount = cursorExam.getCount();
			int colQID = cursorExam.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQMarks = cursorExam.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS);
			int colQBody = cursorExam.getColumnIndex(DatabaseHelper.FLD_QUESTION_BODY);
			//int colQAnswered = cursorExam.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED);
			//int idx = 0;
				
			startManagingCursor(cursorExam);

			if (cursorExam != null) {
				if (cursorExam.moveToFirst()) {
					
					TextView t = (TextView) findViewById(R.id.tvQuestionType);
					t.setText("Single choice");
					
                    do {
                    	//idx++;
                    	if (cursorExam.getString(colQID).equals(SelectedQuestionID)){
                    		sQMarks = cursorExam.getString(colQMarks);
                    		sQBody = cursorExam.getString(colQBody);
                    		//Answered = cursorExam.getString(colQAnswered);
                    		break;
                    	}
                    } while (cursorExam.moveToNext());
				}
			}
			
			cursorExam.close();
			
			TextView txtQuestionMarks = (TextView) findViewById(R.id.tvQuestionMarks);
	        txtQuestionMarks.setText("(Point - " + sQMarks + ")");

			TextView txtQuestionID = (TextView) findViewById(R.id.tvQuestionID);
	        txtQuestionID.setText("id. " + SelectedQuestionID);

			TextView txtQuestionBody = (TextView) findViewById(R.id.tvQuestionBody);
			txtQuestionBody.setText(sQBody);
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		}catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		try{
			Cursor cursorAnswerAttribute = db.rawQuery("SELECT " + DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1   
					+ " FROM " + DatabaseHelper.TBL_ANSWER_ATTRIBUTE 
					+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_QUESTION + " = ?", 
					new String[]{ chapterid, examid, questionid });
		
			startManagingCursor(cursorAnswerAttribute);
			
	        if (cursorAnswerAttribute != null) {
	        	if (cursorAnswerAttribute.moveToFirst()) {
	        		TextView txtAnswer = (TextView) findViewById(R.id.tvAnswer);
	    			txtAnswer.setText(cursorAnswerAttribute.getString(cursorAnswerAttribute.getColumnIndex(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1)));
	            }
	        }
	            
	        cursorAnswerAttribute.close();
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return;
	}
		
	private void goPrevious() {
		boolean bFound = false;
		boolean bPrevious = false;
		String QID = null;
	 	String QType = null;
	   	Cursor cursorQuestion;
	    	
		try{
			PreviousQuestionID = null;

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ID_QUESTION + ", " + DatabaseHelper.FLD_QUESTION_TYPE + ", " 
					+ DatabaseHelper.FLD_QUESTION_ANSWERED 
					+ " FROM " + DatabaseHelper.TBL_EXAM 
					+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?"
					+ " ORDER BY " + DatabaseHelper.FLD_ID_QUESTION, 
					new String[]{ SelectedChapterID, SelectedExamID });
			
			int colQID = cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQType = cursorQuestion.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE);
			
			startManagingCursor(cursorQuestion);

            if (cursorQuestion != null) {
            	do{
               		if (cursorQuestion.moveToLast()) {
               			do {
               				QID = cursorQuestion.getString(colQID);
               				QType = cursorQuestion.getString(colQType);
               				
           					if (bFound){
               					PreviousQuestionID =  QID;
               					bPrevious = true;
              					break;
           					}
               					                    	
               				if (SelectedQuestionID.equals(QID)){
               					bFound = true;
               				}
               				
                		} while (cursorQuestion.moveToPrevious());
            		}
            	} while (!bPrevious);
            }

            cursorQuestion.close();
            
            if (PreviousQuestionID != null){
			    if (QType.equals("SCQ")) {
			    	Intent intent = new Intent(this, Question_SCQ_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, PreviousQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("MCQ")){
			    	Intent intent = new Intent(this, Question_MCQ_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, PreviousQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("FIB")){
			    	Intent intent = new Intent(this, Question_FIB_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, PreviousQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("M&M")){
			    	Intent intent = new Intent(this, Question_MM_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, PreviousQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
            }
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return;
	}

	private void goQuestion() {
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
	
	/*private void goResult() {
		try{
			Intent intent = new Intent(this, Score_Card_Result.class);
        	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
        	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
        	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
        	startActivity(intent);
        	finish();
        	return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return;
	}*/
		
	private void goNext() {
		boolean bFound = false;
		boolean bNext = false;
		String QID = null;
	 	String QType = null;
	   	Cursor cursorQuestion;
	   	
	   	try{
       		NextQuestionID = null;

       		cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ID_QUESTION + ", " + DatabaseHelper.FLD_QUESTION_TYPE + ", " 
       				+ DatabaseHelper.FLD_QUESTION_ANSWERED 
					+ " FROM " + DatabaseHelper.TBL_EXAM 
					+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER + " = ?"
					+ " AND " + DatabaseHelper.FLD_ID_EXAM + " = ?"
					+ " ORDER BY " + DatabaseHelper.FLD_ID_QUESTION, 
					new String[]{ SelectedChapterID, SelectedExamID });
     		
			int colQID = cursorQuestion.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQType = cursorQuestion.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE);
	            
			startManagingCursor(cursorQuestion);
			
            if (cursorQuestion != null) {
            	do{
            		if (cursorQuestion.moveToFirst()) {
	                    do {
	                    	QID = cursorQuestion.getString(colQID);
	                    	QType = cursorQuestion.getString(colQType);
	                    	
           					if (bFound){
           						NextQuestionID =  QID;
           						bNext = true;
               					break;
           					}
	                    	
		                    if (SelectedQuestionID.equals(QID)){
		                    	bFound = true;
		                    }
	                    } while (cursorQuestion.moveToNext());
            		}
            	} while (!bNext);
            }

            cursorQuestion.close();
            
            if (NextQuestionID != null){
			    if (QType.equals("SCQ")) {
			    	Intent intent = new Intent(this, Question_SCQ_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, NextQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("MCQ")){
			    	Intent intent = new Intent(this, Question_MCQ_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, NextQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("FIB")){
			    	Intent intent = new Intent(this, Question_FIB_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, NextQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
			    else if(QType.equals("M&M")){
			    	Intent intent = new Intent(this, Question_MM_Details_Result.class);
			    	intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
			    	intent.putExtra(DatabaseHelper.FLD_ID_QUESTION, NextQuestionID);
			    	
					startActivity(intent);
					finish();
					return;
			    }
            }
            
		}catch(SQLiteException sqle){
			Toast.makeText(getApplicationContext(), sqle.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		return;
    }
	
}
