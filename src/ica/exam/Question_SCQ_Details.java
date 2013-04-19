package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppPreferenceStatus;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Question_SCQ_Details extends Activity {
	private SQLiteDatabase db;

	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedExamID;
	private String SelectedQuestionID;
	private String PreviousQuestionID;
	private String NextQuestionID;

	private long ExamTime;
	private long ExamTimeElapsed;
	private long ExamTimeLeft;
	private long ExamTimeElapsedPrev;

	private boolean isFirst = true;

	private RadioGroup radioGroup;
	private StopWatch timer = new StopWatch();
	private AlertDialog.Builder adExamTermination;

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int REFRESH_RATE = 100;

	Context CurContext;
//	Activity CurActivity;
	StudentDetails studentDetails =null;

private String ActionType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_scq_details);
	
		CurContext = this;
	//	CurActivity=this;
		
		CourseMatIntent type=AppPreferenceStatus.getStudyDownload(CurContext);
		
		switch (type) {
		case DownlaodMock:
			break;
		case MockExam:
			ActionType="'Mock Exam'";
			break;
		case PracticeExam:
			ActionType="'Practice Exam'";
			break;
		case StudyMaterials:
			ActionType="'Study Material'";
			break;
		
		}
		
		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();
				
		SelectedSubjectID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_SUBJECT);
		SelectedChapterID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_CHAPTER);
		SelectedExamID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_EXAM);
		SelectedQuestionID = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_QUESTION);
		ExamTime = getIntent().getExtras()
				.getLong(DatabaseHelper.FLD_EXAM_TIME);
		ExamTimeElapsedPrev = getIntent().getExtras().getLong("EXAM_ELAPSED");
		ExamTimeElapsed = 0;

		try {
			ImageButton buttonPrevious = (ImageButton) this
					.findViewById(R.id.btPrevious);
			buttonPrevious.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					goPrevious();
				}
			});

			ImageButton buttonQuestionList = (ImageButton) this
					.findViewById(R.id.btQuestionList);
			buttonQuestionList.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					goQuestion();
				}
			});

			ImageButton buttonDone = (ImageButton) this
					.findViewById(R.id.btDone);
			buttonDone.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					goDone();
				}
			});

			ImageButton buttonNext = (ImageButton) this
					.findViewById(R.id.btNext);
			buttonNext.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					goNext();
				}
			});
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		db = (new DatabaseHelper(this)).getWritableDatabase();

		try {

			radioGroup = (RadioGroup) findViewById(R.id.rgAttributes);
			OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					RadioButton rbUserAnswerAttribute = (RadioButton) findViewById(checkedId);
					if (isFirst == false) {
						updateAnswer(SelectedChapterID, SelectedExamID,
								SelectedQuestionID, rbUserAnswerAttribute
										.getText().toString());
					}
				}
			};

			radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

			fillData(SelectedChapterID, SelectedExamID, SelectedQuestionID);

			adExamTermination = new AlertDialog.Builder(this);

			if (ExamTimeElapsedPrev < ExamTime) {
				mHandler.sendEmptyMessage(MSG_START_TIMER);
			} else {
				ExamTimeElapsed = ExamTimeElapsedPrev;
				TextView tvTextView = (TextView) findViewById(R.id.txClock);
				tvTextView.setText("Time left : 00:00:00");
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		isFirst = false;
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
		goQuestion();
		return;
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.question_menu,
	 * menu); return true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) {
	 * 
	 * case R.id.itPrevious: goPrevious(); return true;
	 * 
	 * case R.id.itQuestion: goQuestion(); return true;
	 * 
	 * case R.id.itDone: goDone(); break;
	 * 
	 * case R.id.itNext: goNext(); break; }
	 * 
	 * return true; }
	 */

	private void fillData(String chapterid, String examid, String questionid) {

		String sQMarks = null;
		String sQBody = null;
		String UserAnswerAttribute = null;
		String Answered = null;

		try {
			Cursor cursorExam = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_EXAM_NAME + ", "
					+ DatabaseHelper.FLD_ID_QUESTION + ", "
					+ DatabaseHelper.FLD_QUESTION_MARKS + ","
					+ DatabaseHelper.FLD_QUESTION_BODY + ", "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_ID_QUESTION, new String[] { chapterid,
					examid });

			int iCount = cursorExam.getCount();
			int colQID = cursorExam
					.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQMarks = cursorExam
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS);
			int colQBody = cursorExam
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_BODY);
			int colQAnswered = cursorExam
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED);
			int idx = 0;

			startManagingCursor(cursorExam);

			if (cursorExam != null) {
				if (cursorExam.moveToFirst()) {
					String sEmail = studentDetails.getStudentID();
					if (sEmail != null) {
						setTitle(cursorExam.getString(cursorExam
								.getColumnIndex(DatabaseHelper.FLD_EXAM_NAME))
								+ " - [" + sEmail + "]");
					}

					do {
						idx++;
						if (cursorExam.getString(colQID).equals(
								SelectedQuestionID)) {
							sQMarks = cursorExam.getString(colQMarks);
							sQBody = cursorExam.getString(colQBody);
							Answered = cursorExam.getString(colQAnswered);
							break;
						}
					} while (cursorExam.moveToNext());
				}
			}

			cursorExam.close();

			TextView txtQuestionSerial = (TextView) findViewById(R.id.tvQuestionSerial);
			txtQuestionSerial.setText(idx + " of " + iCount);

			TextView txtQuestionMarks = (TextView) findViewById(R.id.tvQuestionMarks);
			txtQuestionMarks.setText("(Point - " + sQMarks + ")");

			TextView txtQuestionID = (TextView) findViewById(R.id.tvQuestionID);
			txtQuestionID.setText("(id. " + SelectedQuestionID + ")");

			TextView txtQuestionBody = (TextView) findViewById(R.id.tvQuestionBody);
			txtQuestionBody.setText(sQBody);
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		if (Answered != null) {
			if (Answered.equals("T")) {
				try {

					Cursor cursorUserAnswerAttribute = db.rawQuery("SELECT "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + ", "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2 + ", "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3
							+ " FROM "
							+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
							+ " = ?" + " AND " + DatabaseHelper.FLD_ID_EXAM
							+ " = ?" + " AND " + DatabaseHelper.FLD_ID_QUESTION
							+ " = ?", new String[] { chapterid, examid,
							questionid });
					startManagingCursor(cursorUserAnswerAttribute);

					int colAAttribute_1 = cursorUserAnswerAttribute
							.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);

					if (cursorUserAnswerAttribute != null) {
						if (cursorUserAnswerAttribute.moveToFirst()) {
							UserAnswerAttribute = cursorUserAnswerAttribute
									.getString(colAAttribute_1);
						}
					}

					cursorUserAnswerAttribute.close();

				} catch (SQLiteException sqle) {
					Toast.makeText(getApplicationContext(), sqle.getMessage(),
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		// Populate question attribute
		try {
			Cursor cursorQuestionAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1 + ", "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_2 + ", "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_3 + " FROM "
					+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					chapterid, examid, questionid });

			startManagingCursor(cursorQuestionAttribute);
			int icntQAttribute = cursorQuestionAttribute.getCount();
			int colQAttribute_1 = cursorQuestionAttribute
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1);

			final RadioButton[] radioOption = new RadioButton[icntQAttribute];
			radioGroup.setOrientation(RadioGroup.VERTICAL);
			
			if (cursorQuestionAttribute != null) {
				if (cursorQuestionAttribute.moveToFirst()) {
					int i = 0;
					do {
						radioOption[i] = new RadioButton(this);
						radioGroup.addView(radioOption[i]);
						radioOption[i].setText(cursorQuestionAttribute
								.getString(colQAttribute_1));

						if (Answered != null) {
							if (Answered.equals("T")) {
								if ((UserAnswerAttribute != null)
										&& (cursorQuestionAttribute
												.getString(colQAttribute_1) != null)) {
									if (UserAnswerAttribute
											.equals(cursorQuestionAttribute
													.getString(colQAttribute_1))) {
										radioOption[i].setChecked(true);
									}
								}
							}
						}

						i++;
					} while (cursorQuestionAttribute.moveToNext());
				}
			}

			cursorQuestionAttribute.close();
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void goPrevious() {
		boolean bFound = false;
		boolean bPrevious = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;
		Cursor cursorQuestion;

		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);

			PreviousQuestionID = null;

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'",
					new String[] { SelectedChapterID, SelectedExamID });

			int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();

//			if (iQCount == 0) {
//				goDone();
//				return;
//			}

			cursorQuestion = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ID_QUESTION + ", "
					+ DatabaseHelper.FLD_QUESTION_TYPE + ", "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_ID_QUESTION, new String[] {
					SelectedChapterID, SelectedExamID });

			int colQID = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQType = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE);
			int colQAnswered = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED);

			startManagingCursor(cursorQuestion);

			if (cursorQuestion != null) {
				do {
					if (cursorQuestion.moveToLast()) {
						do {
							QID = cursorQuestion.getString(colQID);
							QType = cursorQuestion.getString(colQType);
							QAnswered = cursorQuestion.getString(colQAnswered);

							if (ExamTimeElapsedPrev < ExamTime) {
								if ((bFound))// && (QAnswered.equals("F"))) 
									{
									PreviousQuestionID = QID;
									bPrevious = true;
									break;
								}
							} else {
								if (bFound) {
									PreviousQuestionID = QID;
									bPrevious = true;
									break;
								}
							}

							if (SelectedQuestionID.equals(QID)) {
								bFound = true;
							}

						} while (cursorQuestion.moveToPrevious());
					}
				} while (!bPrevious);
			}

			cursorQuestion.close();

			if (PreviousQuestionID != null) {
				if (QType.equals("SCQ")) {
					Intent intent = new Intent(this, Question_SCQ_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							PreviousQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("MCQ")) {
					Intent intent = new Intent(this, Question_MCQ_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							PreviousQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("FIB")) {
					Intent intent = new Intent(this, Question_FIB_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							PreviousQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("M&M")) {
					Intent intent = new Intent(this, Question_MM_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							PreviousQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				}
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void goQuestion() {
		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);

			Intent intent = new Intent(this, QuestionList_Exam.class);
			intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
			intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

			startActivity(intent);
			finish();
			return;
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void UpsertLastExamUploadInfo(int ExamID, int ExamOnId) {

		long lreturn = 0;

		String ExamType = "M";

		if (AppPreferenceStatus.getLastExamStatus(CurContext)) {
			ExamType = "M";
		} else {
			ExamType = "E";
		}

		try {
			db.execSQL(DatabaseHelper.CREATE_TBL_STUDENT_SCHEDULE_TABLE);

			ContentValues ExamUploadInfovalues = new ContentValues();
			ExamUploadInfovalues.put(DatabaseHelper.FLD_ID_EXAM, ExamID);
			ExamUploadInfovalues.put(DatabaseHelper.FLD_EXAM_ON, "C");
			ExamUploadInfovalues.put(DatabaseHelper.FLD_EXAM_ON_ID, ExamOnId);
			ExamUploadInfovalues.put(DatabaseHelper.FLD_EXAM_TYPE, ExamType);

			lreturn = db.insertWithOnConflict(
					DatabaseHelper.TBL_EXAM_UPLOAD_INFO, null,
					ExamUploadInfovalues, SQLiteDatabase.CONFLICT_IGNORE);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Data Exception" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void goDone() {
		
		CourseMatIntent type=AppPreferenceStatus.getStudyDownload(CurContext);
		
		switch (type) {
		case DownlaodMock:
			break;
		case MockExam:
			ActionType="'Mock Exam'";
			break;
		case PracticeExam:
			ActionType="'Practice Exam'";
			break;
		case StudyMaterials:
			ActionType="'Study Material'";
			break;
		
		}
		
		try {
			adExamTermination.setIcon(R.drawable.bt_question);
			adExamTermination.setTitle("Mock status");
			adExamTermination
					.setMessage(
							"This concludes your "+ActionType+". Are you sure? Yes will complete the "+type+" and No will continue the exam.")
					.setCancelable(false)
					.setPositiveButton("No",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							dialog.cancel();
						}
					})
					.setNegativeButton("Yes",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							try {
								
								Cursor cursorExam = db.rawQuery("SELECT "
										+ DatabaseHelper.FLD_ID_EXAM + " FROM "
										+ DatabaseHelper.TBL_EXAM + " WHERE "
										+ DatabaseHelper.FLD_ID_CHAPTER + " = ?",
										new String[] { SelectedChapterID });
								
								ContentValues Attempedvalues = new ContentValues();
								Attempedvalues
										.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
												"T");
							int status=	db.update(
										DatabaseHelper.TBL_CHAPTER,
										Attempedvalues,
										DatabaseHelper.FLD_ID_SUBJECT
												+ " = ? AND "
												+ DatabaseHelper.FLD_ID_CHAPTER
												+ " = ?", new String[] {
												SelectedSubjectID,
												SelectedChapterID });
							
							status=status+0;
								UpsertLastExamUploadInfo(Integer
										.parseInt(SelectedExamID), 0);
							} catch (SQLiteException sqle) {
								Toast.makeText(getApplicationContext(),
										sqle.getMessage(),
										Toast.LENGTH_LONG).show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(),
										Toast.LENGTH_SHORT).show();
							}

							mHandler.sendEmptyMessage(MSG_STOP_TIMER);
							ExamTimeElapsed = ExamTime + 1;

							Intent intent = new Intent();
							intent.setClass(getApplicationContext(),
									Score_Card.class);
							intent.putExtra(
									DatabaseHelper.FLD_ID_SUBJECT,
									SelectedSubjectID);
							intent.putExtra(
									DatabaseHelper.FLD_ID_CHAPTER,
									SelectedChapterID);
							intent.putExtra(DatabaseHelper.FLD_ID_EXAM,
									SelectedExamID);
							startActivity(intent);
							finish();
							return;
						}
					});
			AlertDialog altEndExam = adExamTermination.create();
			altEndExam.show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void goNext() {
		boolean bFound = false;
		boolean bNext = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;
		Cursor cursorQuestion;

		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);

			NextQuestionID = null;

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'",
					new String[] { SelectedChapterID, SelectedExamID });

			int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();

//			if (iQCount == 0) {
//				goDone();
//				return;
//			}

			cursorQuestion = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ID_QUESTION + ", "
					+ DatabaseHelper.FLD_QUESTION_TYPE + ", "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_ID_QUESTION, new String[] {
					SelectedChapterID, SelectedExamID });

			int colQID = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION);
			int colQType = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE);
			int colQAnswered = cursorQuestion
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED);

			startManagingCursor(cursorQuestion);

			if (cursorQuestion != null) {
				do {
					if (cursorQuestion.moveToFirst()) {
						do {
							QID = cursorQuestion.getString(colQID);
							QType = cursorQuestion.getString(colQType);
							QAnswered = cursorQuestion.getString(colQAnswered);

							if (ExamTimeElapsedPrev < ExamTime) {
								if ((bFound) )//&& (QAnswered.equals("F"))) 
									{
									NextQuestionID = QID;
									bNext = true;
									break;
								}
							} else {
								if (bFound) {
									NextQuestionID = QID;
									bNext = true;
									break;
								}
							}

							if (SelectedQuestionID.equals(QID)) {
								bFound = true;
							}
						} while (cursorQuestion.moveToNext());
					}
				} while (!bNext);
			}

			cursorQuestion.close();

			if (NextQuestionID != null) {
				if (QType.equals("SCQ")) {
					Intent intent = new Intent(this, Question_SCQ_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							NextQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("MCQ")) {
					Intent intent = new Intent(this, Question_MCQ_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							NextQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("FIB")) {
					Intent intent = new Intent(this, Question_FIB_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							NextQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				} else if (QType.equals("M&M")) {
					Intent intent = new Intent(this, Question_MM_Details.class);
					intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
							SelectedSubjectID);
					intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
							SelectedChapterID);
					intent.putExtra(DatabaseHelper.FLD_ID_EXAM, SelectedExamID);
					intent.putExtra(DatabaseHelper.FLD_ID_QUESTION,
							NextQuestionID);
					intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
					intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

					startActivity(intent);
					finish();
					return;
				}
			}

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void updateAnswer(String ChapterID, String ExamID,
			String QuestionID, String Attribute1) {
		String AnswerAttribute1 = null;

		try {
			if ((ChapterID != null) && (ExamID != null) && (QuestionID != null)
					&& (Attribute1 != null)) {

				try {
					Cursor cursorAnswerAttribute = db.rawQuery("SELECT "
							+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1 + " FROM "
							+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE + " WHERE "
							+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
							+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
							new String[] { ChapterID, ExamID, QuestionID });

					startManagingCursor(cursorAnswerAttribute);

					int colAAttribute1 = cursorAnswerAttribute
							.getColumnIndex(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1);
					AnswerAttribute1 = null;

					if (cursorAnswerAttribute != null) {
						if (cursorAnswerAttribute.moveToFirst()) {
							AnswerAttribute1 = cursorAnswerAttribute
									.getString(colAAttribute1);
						}
					}

					cursorAnswerAttribute.close();

				} catch (SQLiteException sqle) {
					Toast.makeText(getApplicationContext(), sqle.getMessage(),
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

				db.delete(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE,
						DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
								+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
								+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { ChapterID, ExamID, QuestionID });

				ContentValues Answervalues = new ContentValues();

				if (Answervalues != null) {
					Answervalues.put(DatabaseHelper.FLD_ID_CHAPTER, ChapterID);
					Answervalues.put(DatabaseHelper.FLD_ID_EXAM, ExamID);
					Answervalues
							.put(DatabaseHelper.FLD_ID_QUESTION, QuestionID);
					Answervalues.put(
							DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1,
							Attribute1);
					db.insert(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE, null,
							Answervalues);
				}

				ContentValues Attempedvalues = new ContentValues();
				if (Attempedvalues != null) {
					if (AnswerAttribute1.trim().toUpperCase()
							.equals(Attribute1.trim().toUpperCase())) {
						Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT,
								"T");
					} else {
						Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT,
								"F");
					}

					Attempedvalues.put(DatabaseHelper.FLD_QUESTION_ANSWERED,
							"T");
					db.update(DatabaseHelper.TBL_EXAM, Attempedvalues,
							DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
									+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
							new String[] { ChapterID, ExamID, QuestionID });
				}
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			TextView tvTextView = (TextView) findViewById(R.id.txClock);

			switch (msg.what) {
			case MSG_START_TIMER:
				timer.start();
				mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
				break;

			case MSG_UPDATE_TIMER:
				ExamTimeElapsed = timer.getElapsedTimeMilli() / 1000
						+ +ExamTimeElapsedPrev;
				ExamTimeLeft = ExamTime - ExamTimeElapsed;

				if (ExamTimeElapsed > ExamTime) {
					mHandler.removeMessages(MSG_UPDATE_TIMER);
					timer.stop();

					adExamTermination.setIcon(R.drawable.warning);
					adExamTermination
							.setMessage("Examination time is over. Please press Ok to complete.");
					adExamTermination
							.setTitle("Mock status")
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											try {
												ContentValues Attempedvalues = new ContentValues();
												Attempedvalues
														.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
																"T");
												db.update(
														DatabaseHelper.TBL_CHAPTER,
														Attempedvalues,
														DatabaseHelper.FLD_ID_SUBJECT
																+ " = ? AND "
																+ DatabaseHelper.FLD_ID_CHAPTER
																+ " = ?",
														new String[] {
																SelectedSubjectID,
																SelectedChapterID });
											} catch (SQLiteException sqle) {
												Toast.makeText(
														getApplicationContext(),
														sqle.getMessage(),
														Toast.LENGTH_LONG)
														.show();
											} catch (Exception e) {
												Toast.makeText(
														getApplicationContext(),
														e.getMessage(),
														Toast.LENGTH_SHORT)
														.show();
											}

											Intent intent = new Intent();
											intent.setClass(
													getApplicationContext(),
													Score_Card.class);
											intent.putExtra(
													DatabaseHelper.FLD_ID_SUBJECT,
													SelectedSubjectID);
											intent.putExtra(
													DatabaseHelper.FLD_ID_CHAPTER,
													SelectedChapterID);
											intent.putExtra(
													DatabaseHelper.FLD_ID_EXAM,
													SelectedExamID);
											startActivity(intent);
										}
									});
					AlertDialog altEndExam = adExamTermination.create();
					altEndExam.show();
				} else {
					int[] tm = StopWatch.splitToComponentTimes(ExamTimeLeft);
					String tHour = null;
					String tMinute = null;
					String tSecond = null;

					if (tm[0] < 10) {
						tHour = "0" + tm[0];
					} else {
						tHour = "" + tm[0];
					}

					if (tm[1] < 10) {
						tMinute = "0" + tm[1];
					} else {
						tMinute = "" + tm[1];
					}

					if (tm[2] < 10) {
						tSecond = "0" + tm[2];
					} else {
						tSecond = "" + tm[2];
					}

					tvTextView.setText("Time left : " + tHour + ":" + tMinute
							+ ":" + tSecond);
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
							REFRESH_RATE);
				}
				break;

			case MSG_STOP_TIMER:
				mHandler.removeMessages(MSG_UPDATE_TIMER);
				timer.stop();
				ExamTimeElapsed = timer.getElapsedTimeMilli() / 1000
						+ +ExamTimeElapsedPrev;
				ExamTimeLeft = ExamTime - ExamTimeElapsed;
				int[] tm1 = StopWatch.splitToComponentTimes(ExamTimeLeft);
				String tHour = null;
				String tMinute = null;
				String tSecond = null;

				if (tm1[0] < 10) {
					tHour = "0" + tm1[0];
				} else {
					tHour = "" + tm1[0];
				}

				if (tm1[1] < 10) {
					tMinute = "0" + tm1[1];
				} else {
					tMinute = "" + tm1[1];
				}

				if (tm1[2] < 10) {
					tSecond = "0" + tm1[2];
				} else {
					tSecond = "" + tm1[2];
				}

				tvTextView.setText("Time left : " + tHour + ":" + tMinute + ":"
						+ tSecond);
				break;

			default:
				break;
			}
		}
	};
}
