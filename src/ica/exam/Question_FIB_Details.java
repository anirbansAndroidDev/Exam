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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Question_FIB_Details extends Activity {
	private SQLiteDatabase db;
	private Cursor cursorExam;
	private Cursor cursorQuestionAttribute;
	private Cursor cursorUserAnswerAttribute;
	private Cursor cursorAnswerAttribute;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedExamID;
	private String SelectedQuestionID;
	private String PreviousQuestionID;
	private String NextQuestionID;
	private String AnswerAttribute1 = null;
	private String UserAnswerAttribute = null;
	private String Answered = null;
	private int iAnswer;
	private long ExamTime;
	private long ExamTimeElapsed;
	private long ExamTimeLeft;
	private long ExamTimeElapsedPrev;
	private StopWatch timer = new StopWatch();
	private AlertDialog.Builder adExamTermination;

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int REFRESH_RATE = 100;
	final int MSG_CANCEL = -9999;

	Context CurContext;
	private String ActionType;
	private StudentDetails studentDetails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_fib_details);

		CurContext = this;

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

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

		setTitle(ActionType);

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();

		Spinner s = (Spinner) findViewById(R.id.spBlank);
		adExamTermination = new AlertDialog.Builder(CurContext);

		s.setOnItemSelectedListener(new FIBOnItemSelectedListener());

		try {

			SelectedSubjectID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_ID_SUBJECT);
			SelectedChapterID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_ID_CHAPTER);
			SelectedExamID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_ID_EXAM);
			SelectedQuestionID = getIntent().getExtras().getString(
					DatabaseHelper.FLD_ID_QUESTION);
			ExamTime = getIntent().getExtras().getLong(
					DatabaseHelper.FLD_EXAM_TIME);
			ExamTimeElapsedPrev = getIntent().getExtras().getLong(
					"EXAM_ELAPSED");
			ExamTimeElapsed = 0;
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		ImageButton buttonPrevious = (ImageButton) findViewById(R.id.btPrevious);
		buttonPrevious.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goPrevious();
			}
		});

		ImageButton buttonQuestionList = (ImageButton) findViewById(R.id.btQuestionList);
		buttonQuestionList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goQuestion();
			}
		});

		ImageButton buttonDone = (ImageButton) findViewById(R.id.btDone);
		buttonDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goDone();
			}
		});

		ImageButton buttonNext = (ImageButton) findViewById(R.id.btNext);
		buttonNext.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goNext();
			}
		});

		try {

			fillData(SelectedChapterID, SelectedExamID, SelectedQuestionID);

			if (ExamTimeElapsedPrev < ExamTime) {
				mHandler.sendEmptyMessage(MSG_START_TIMER);
			} else {
				ExamTimeElapsed = ExamTimeElapsedPrev;

				TextView tvTextView = (TextView) findViewById(R.id.txClock);
				tvTextView.setText("Time left : 00:00:00");
			}
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
			// Toast.makeText(getApplicationContext(), sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			db.close();
		} catch (Exception e) {
		
			e.printStackTrace();
		}

	}

	@Override
	public void onBackPressed() {
		try {
			goQuestion();
		} catch (Exception e) {
		
			e.printStackTrace();
		}

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

		try {
			cursorExam = db.rawQuery("SELECT " + DatabaseHelper.FLD_EXAM_NAME
					+ ", " + DatabaseHelper.FLD_ID_QUESTION + ", "
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
						// setTitle("ICA Student Connect (Ver: "+AppInfo.versionInfo(CurContext)
						// +")-Result- [" +sEmail + "]");

					}

					do {
						idx++;
						if (cursorExam.getString(colQID).equals(questionid)) {
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
			txtQuestionMarks.setText("(Point : " + sQMarks + ")");

			TextView txtQuestionID = (TextView) findViewById(R.id.tvQuestionID);
			txtQuestionID.setText("(id. " + questionid + ")");

			TextView txtQuestionBodyFirst = (TextView) findViewById(R.id.tvQuestionBodyFirst);
			txtQuestionBodyFirst.setText(sQBody);

			TextView txtQuestionBodyLast = (TextView) findViewById(R.id.tvQuestionBodyLast);
			txtQuestionBodyLast.setText("");

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
			// Toast.makeText(getApplicationContext(), sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		if (Answered != null && !"".equals(Answered) && Answered.equals("T")) {
			try {
				cursorUserAnswerAttribute = db.rawQuery("SELECT "
						+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + " FROM "
						+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
						+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { chapterid, examid, questionid });

				if (cursorUserAnswerAttribute != null) {
					startManagingCursor(cursorUserAnswerAttribute);

					int colAAttribute_1 = cursorUserAnswerAttribute
							.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);

					if (cursorUserAnswerAttribute.moveToFirst()) {
						UserAnswerAttribute = cursorUserAnswerAttribute
								.getString(colAAttribute_1);
					}
				}

				cursorUserAnswerAttribute.close();

			} catch (SQLiteException sqle) {
				sqle.printStackTrace();
			
			} catch (Exception e) {
				e.printStackTrace();
			
			}
		}

		// populate question attribute
		try {
			cursorQuestionAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1 + " FROM "
					+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					chapterid, examid, questionid });

			startManagingCursor(cursorQuestionAttribute);
			int icntQAttribute = cursorQuestionAttribute.getCount();

			String sQAttribute1[] = new String[icntQAttribute + 1];

			int colQAttribute1 = cursorQuestionAttribute
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1);

			if (cursorQuestionAttribute != null) {
				if (cursorQuestionAttribute.moveToFirst()) {
					int i = 0;
					sQAttribute1[i] = "Select...";
					do {
						i++;
						sQAttribute1[i] = cursorQuestionAttribute
								.getString(colQAttribute1);

						if (UserAnswerAttribute != null
								&& !"".equals(UserAnswerAttribute)
								&& sQAttribute1[i].equals(UserAnswerAttribute)) {
							iAnswer = i;
						}
					} while (cursorQuestionAttribute.moveToNext());
				}
			}

			cursorQuestionAttribute.close();

			Spinner s = (Spinner) findViewById(R.id.spBlank);
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
					this, R.layout.spinnerlayout, sQAttribute1);
			s.setAdapter(adapter);

			if (Answered != null && Answered.equals("T")) {
				s.setSelection(iAnswer);
			}

			if (ExamTimeElapsedPrev >= ExamTime) {
				s.setEnabled(false);
			}

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
			// Toast.makeText(getApplicationContext(), sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		try {
			CursorRightAns = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1 + " FROM "
					+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					chapterid, examid, questionid });

			if (CursorRightAns != null) {

				startManagingCursor(CursorRightAns);

				int colAAttribute1 = CursorRightAns
						.getColumnIndex(DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1);
				AnswerAttribute1 = null;

				if (CursorRightAns.moveToFirst() && colAAttribute1 >= 0) {
					AnswerAttribute1 = CursorRightAns.getString(colAAttribute1);
				}
			}

			CursorRightAns.close();

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
			// Toast.makeText(getApplicationContext(), sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

	}

	Cursor CursorRightAns;

	private void goPrevious() {
		boolean bFound = false;
		boolean bPrevious = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;
		Cursor cursorQuestion;

		PreviousQuestionID = null;

		try {

			mHandler.sendEmptyMessage(MSG_STOP_TIMER);
			mHandler.removeMessages(MSG_UPDATE_TIMER);
			mHandler.removeCallbacksAndMessages(null);

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'",
					new String[] { SelectedChapterID, SelectedExamID });

			// int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();

			// if (iQCount == 0) {
			// goDone();
			// return;
			/*
			 * try{ adExamTermination.setMessage(
			 * "This concludes your Mock exam. Are you sure?")
			 * .setCancelable(false) .setPositiveButton("Yes", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int id) { try{ ContentValues
			 * Attempedvalues = new ContentValues();
			 * Attempedvalues.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
			 * "T"); db.update(DatabaseHelper.TBL_CHAPTER, Attempedvalues,
			 * DatabaseHelper.FLD_ID_SUBJECT + " = ? AND " +
			 * DatabaseHelper.FLD_ID_CHAPTER + " = ?", new String[]{
			 * SelectedSubjectID, SelectedChapterID }); }catch(SQLiteException
			 * sqle){ Toast.makeText(getApplicationContext(), sqle.getMessage(),
			 * Toast.LENGTH_LONG).show(); } catch (Exception e) {
			 * Toast.makeText(getApplicationContext(), e.getMessage(),
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * try{ mHandler.sendEmptyMessage(MSG_STOP_TIMER); ExamTimeElapsed =
			 * ExamTime + 1; Intent intent = new Intent();
			 * intent.setClass(getApplicationContext(), Score_Card.class);
			 * intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
			 * SelectedSubjectID);
			 * intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
			 * SelectedChapterID); intent.putExtra(DatabaseHelper.FLD_ID_EXAM,
			 * SelectedExamID); startActivity(intent);
			 * 
			 * finish(); return; } catch (Exception e) {
			 * Toast.makeText(getApplicationContext(), e.getMessage(),
			 * Toast.LENGTH_SHORT).show(); } } }) .setNegativeButton("No", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int id) { dialog.cancel(); } });
			 * AlertDialog altEndExam = adExamTermination.create();
			 * altEndExam.show(); } catch (Exception e) {
			 * Toast.makeText(getApplicationContext(), e.getMessage(),
			 * Toast.LENGTH_SHORT).show(); }
			 */
			// }

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
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
			// Toast.makeText(getApplicationContext(), sqle.getMessage(),
			// Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		try {
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

		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void goQuestion() {
		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);
			mHandler.removeCallbacksAndMessages(null);
			Intent intent = new Intent(this, QuestionList_Exam.class);
			intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT, SelectedSubjectID);
			intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER, SelectedChapterID);
			intent.putExtra(DatabaseHelper.FLD_EXAM_TIME, ExamTime);
			intent.putExtra("EXAM_ELAPSED", ExamTimeElapsed);

			startActivity(intent);
			finish();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}
	}

	private void insertLastExamUploadInfo(int ExamID, int ExamOnId) {

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
			e.printStackTrace();

		}

	}

	private void goDone() {
		try {
			adExamTermination.setIcon(R.drawable.bt_question);
			adExamTermination.setTitle(ActionType + " status");
			adExamTermination
					.setMessage(
							"This concludes your "
									+ ActionType
									+ ". Are you sure? Yes will complete the exam and No will continue the exam.")
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
										ContentValues Attempedvalues = new ContentValues();
										Attempedvalues
												.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
														"T");

										Attempedvalues
												.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
														"T");
										Attempedvalues
												.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
														"T");

										db.update(
												DatabaseHelper.TBL_CHAPTER,
												Attempedvalues,
												DatabaseHelper.FLD_ID_SUBJECT
														+ " = ? AND "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = ?", new String[] {
														SelectedSubjectID,
														SelectedChapterID });

										insertLastExamUploadInfo(Integer
												.parseInt(SelectedExamID), 0);

									} catch (SQLiteException sqle) {
										sqle.printStackTrace();
										// Toast.makeText(getApplicationContext(),
										// sqle.getMessage(),
										// Toast.LENGTH_LONG).show();
									} catch (Exception e) {
										e.printStackTrace();
										// Toast.makeText(getApplicationContext(),
										// e.getMessage(),
										// Toast.LENGTH_SHORT).show();
									}

									try {
										mHandler.sendEmptyMessage(MSG_STOP_TIMER);
										mHandler.removeCallbacksAndMessages(null);
										ExamTimeElapsed = ExamTime + 1;
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
										finish();
										return;
									} catch (Exception e) {
										e.printStackTrace();
										// Toast.makeText(getApplicationContext(),
										// e.getMessage(),
										// Toast.LENGTH_SHORT).show();
									}
								}
							});

			AlertDialog altEndExam = adExamTermination.create();
			altEndExam.show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), e.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}
	}

	private void goNext() {
		boolean bFound = false;
		boolean bNext = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;
		Cursor cursorQuestion;

		NextQuestionID = null;

		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);
			mHandler.removeMessages(MSG_UPDATE_TIMER);
			mHandler.removeCallbacksAndMessages(null);

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'",
					new String[] { SelectedChapterID, SelectedExamID });

			int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();

			// if (iQCount == 0) {

			/*
			 * try{ adExamTermination.setMessage(
			 * "This concludes your Mock exam. Are you sure?")
			 * .setCancelable(false) .setPositiveButton("Yes", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int id) { try{ ContentValues
			 * Attempedvalues = new ContentValues();
			 * Attempedvalues.put(DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER,
			 * "T"); db.update(DatabaseHelper.TBL_CHAPTER, Attempedvalues,
			 * DatabaseHelper.FLD_ID_SUBJECT + " = ? AND " +
			 * DatabaseHelper.FLD_ID_CHAPTER + " = ?", new String[]{
			 * SelectedSubjectID, SelectedChapterID }); }catch(SQLiteException
			 * sqle){ Toast.makeText(getApplicationContext(), sqle.getMessage(),
			 * Toast.LENGTH_LONG).show(); } catch (Exception e) {
			 * Toast.makeText(getApplicationContext(), e.getMessage(),
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * try{ mHandler.sendEmptyMessage(MSG_STOP_TIMER); ExamTimeElapsed =
			 * ExamTime + 1; Intent intent = new Intent();
			 * intent.setClass(getApplicationContext(), Score_Card.class);
			 * intent.putExtra(DatabaseHelper.FLD_ID_SUBJECT,
			 * SelectedSubjectID);
			 * intent.putExtra(DatabaseHelper.FLD_ID_CHAPTER,
			 * SelectedChapterID); intent.putExtra(DatabaseHelper.FLD_ID_EXAM,
			 * SelectedExamID); startActivity(intent); finish(); return; } catch
			 * (Exception e) { Toast.makeText(getApplicationContext(),
			 * e.getMessage(), Toast.LENGTH_SHORT).show(); } } })
			 * .setNegativeButton("No", new DialogInterface.OnClickListener() {
			 * public void onClick(DialogInterface dialog, int id) {
			 * dialog.cancel(); } }); AlertDialog altEndExam =
			 * adExamTermination.create(); altEndExam.show(); } catch (Exception
			 * e) { Toast.makeText(getApplicationContext(), e.getMessage(),
			 * Toast.LENGTH_SHORT).show(); }
			 */

			// goDone();
			// return;
			// }

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
								if ((bFound))// && (QAnswered.equals("F")))
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
		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
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

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void updateAnswer(String ChapterID, String ExamID,
			String QuestionID, String Attribute1) {
		try {
			db.delete(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE,
					DatabaseHelper.FLD_ID_EXAM + " = ? AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
					new String[] { SelectedExamID, SelectedQuestionID });

			if (Attribute1 == null) {
				ContentValues Attempedvalues = new ContentValues();
				Attempedvalues.put(DatabaseHelper.FLD_QUESTION_ANSWERED, "F");
				Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT, "F");
				db.update(DatabaseHelper.TBL_EXAM, Attempedvalues,
						DatabaseHelper.FLD_ID_EXAM + " = ? AND "
								+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { ExamID, QuestionID });

			} else {
				ContentValues Answervalues = new ContentValues();
				Answervalues.put(DatabaseHelper.FLD_ID_CHAPTER, ChapterID);
				Answervalues.put(DatabaseHelper.FLD_ID_EXAM, ExamID);
				Answervalues.put(DatabaseHelper.FLD_ID_QUESTION, QuestionID);
				Answervalues.put(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1,
						Attribute1);

				db.insert(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE, null,
						Answervalues);

				ContentValues Attempedvalues = new ContentValues();

				if (AnswerAttribute1 != null
						&& AnswerAttribute1.trim().toUpperCase()
								.equals(Attribute1.trim().toUpperCase())) {
					Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT, "T");
				} else {
					Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT, "F");
				}

				Attempedvalues.put(DatabaseHelper.FLD_QUESTION_ANSWERED, "T");
				db.update(DatabaseHelper.TBL_EXAM, Attempedvalues,
						DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
								+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
								+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { ChapterID, ExamID, QuestionID });
			}

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private class FIBOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {

			try {
				if (id > 0) {
					UserAnswerAttribute = parent.getItemAtPosition(pos)
							.toString();
					updateAnswer(SelectedChapterID, SelectedExamID,
							SelectedQuestionID, UserAnswerAttribute);
				} else {
					UserAnswerAttribute = null;
					updateAnswer(SelectedChapterID, SelectedExamID,
							SelectedQuestionID, UserAnswerAttribute);
				}
			} catch (Exception e) {
				e.printStackTrace();
				
			}

		}

		public void onNothingSelected(AdapterView<?> parent) {
			UserAnswerAttribute = null;
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_START_TIMER:
				timer.start();
				sendEmptyMessage(MSG_UPDATE_TIMER);
				break;

			case MSG_UPDATE_TIMER:
				ExamTimeElapsed = timer.getElapsedTimeMilli() / 1000
						+ ExamTimeElapsedPrev;
				ExamTimeLeft = ExamTime - ExamTimeElapsed;

				if (ExamTimeElapsed > ExamTime) {
					removeMessages(MSG_UPDATE_TIMER);
					timer.stop();

					adExamTermination.setIcon(R.drawable.warning);
					adExamTermination
							.setMessage("Examination time is over. Please press Ok to complete.");
					adExamTermination
							.setTitle(ActionType + " status")
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
												sqle.printStackTrace();
												// Toast.makeText(
												// getApplicationContext(),
												// sqle.getMessage(),
												// Toast.LENGTH_LONG)
												// .show();
											} catch (Exception e) {
												e.printStackTrace();
												// Toast.makeText(
												// getApplicationContext(),
												// e.getMessage(),
												// Toast.LENGTH_SHORT)
												// .show();
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

					TextView tvTextView = (TextView) findViewById(R.id.txClock);
					tvTextView.setText("Time left : " + tHour + ":" + tMinute
							+ ":" + tSecond);
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,
							REFRESH_RATE);
				}
				break;

			case MSG_STOP_TIMER:
				removeMessages(MSG_UPDATE_TIMER);
				mHandler.removeCallbacksAndMessages(null);
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

				TextView tvTextView = (TextView) findViewById(R.id.txClock);
				tvTextView.setText("Time left : " + tHour + ":" + tMinute + ":"
						+ tSecond);
				break;
			case MSG_CANCEL:
				removeMessages(MSG_UPDATE_TIMER);
				break;
			default:
				break;

			}

		}
	};

}
