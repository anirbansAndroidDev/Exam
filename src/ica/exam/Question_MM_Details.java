package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppPreferenceStatus;

import java.util.ArrayList;
import java.util.List;

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
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

public class Question_MM_Details extends Activity {
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
	private String Answered = null;
	private StopWatch timer = new StopWatch();
	private AlertDialog.Builder adExamTermination;

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int REFRESH_RATE = 100;

	Context CurContext;
	// Activity CurActivity;

	StudentDetails studentDetails = null;
	private String ActionType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_mm_details);

		CurContext = this;

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();

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

		// /Fill Data

		try {
			db = (new DatabaseHelper(this)).getWritableDatabase();

			adExamTermination = new AlertDialog.Builder(this);

			Cursor cursorExam = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_EXAM_NAME + ","
					+ DatabaseHelper.FLD_ID_QUESTION + ", "
					+ DatabaseHelper.FLD_QUESTION_MARKS + ","
					+ DatabaseHelper.FLD_QUESTION_BODY + ", "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_ID_QUESTION, new String[] {
					SelectedChapterID, SelectedExamID });

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

							TextView txtQuestionSerial = (TextView) findViewById(R.id.tvQuestionSerial);
							txtQuestionSerial.setText(idx + " of " + iCount);

							TextView txtQuestionMarks = (TextView) findViewById(R.id.tvQuestionMarks);
							txtQuestionMarks.setText("(Point : "
									+ cursorExam.getString(colQMarks) + ")");

							TextView txtQuestionID = (TextView) findViewById(R.id.tvQuestionID);
							txtQuestionID.setText("(id. " + SelectedQuestionID
									+ ")");

							TextView txtQuestionBody = (TextView) findViewById(R.id.tvQuestionBody);
							txtQuestionBody.setText(cursorExam
									.getString(colQBody));
							Answered = cursorExam.getString(colQAnswered);

							break;
						}
					} while (cursorExam.moveToNext());
				}
			}

			cursorExam.close();
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			ListView ListViewQuestion = (ListView) findViewById(R.id.lvOptions);
			ArrayAdapter<DataObject_mm> ListAdapterquestion = new DataAdapter_mm(
					this, getDataObject_mm(SelectedChapterID, SelectedExamID,
							SelectedQuestionID, Answered));
			ListViewQuestion.setAdapter(ListAdapterquestion);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		if (ExamTimeElapsedPrev < ExamTime) {
			mHandler.sendEmptyMessage(MSG_START_TIMER);
		} else {
			ExamTimeElapsed = ExamTimeElapsedPrev;
			TextView tvTextView = (TextView) findViewById(R.id.txClock);
			tvTextView.setText("Time left : 00:00:00");
		}

		// /Fill Data
		return;
	}

	private List<DataObject_mm> getDataObject_mm(String chapterid,
			String examid, String questionid, String answered) {
		List<DataObject_mm> list = new ArrayList<DataObject_mm>();

		try {
			Cursor cursorQuestionAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ROWID + ", "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1 + " FROM "
					+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1, new String[] {
					chapterid, examid, questionid });

			startManagingCursor(cursorQuestionAttribute);

			int colRowid = cursorQuestionAttribute
					.getColumnIndex(DatabaseHelper.FLD_ROWID);
			int colQuestionAttribute_1 = cursorQuestionAttribute
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1);

			if (cursorQuestionAttribute != null) {
				if (cursorQuestionAttribute.moveToFirst()) {
					do {
						list.add(get(cursorQuestionAttribute
								.getString(colRowid), 0,
								cursorQuestionAttribute
										.getString(colQuestionAttribute_1), "",
								""));
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

		if (answered.equals("T")) {
			try {
				Cursor cursorAnswer = db.rawQuery("SELECT "
						+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + ", "
						+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2 + ", "
						+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3 + " FROM "
						+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
						+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { chapterid, examid, questionid });

				startManagingCursor(cursorAnswer);

				int colUserAnswerAttribute_1 = cursorAnswer
						.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);
				int colUserAnswerAttribute_2 = cursorAnswer
						.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2);
				int colUserAnswerAttribute_3 = cursorAnswer
						.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3);

				int idx = 0;

				if (cursorAnswer != null) {
					if (cursorAnswer.moveToFirst()) {
						do {
							DataObject_mm temp = list.get(idx);
							list.remove(idx);
							DataObject_mm data = new DataObject_mm(
									temp.getRowId(),
									getAccountPosition(
											chapterid,
											examid,
											questionid,
											cursorAnswer
													.getString(colUserAnswerAttribute_1)),
									temp.getAnswerAttribute1(),
									cursorAnswer
											.getString(colUserAnswerAttribute_2),
									cursorAnswer
											.getString(colUserAnswerAttribute_3));
							list.add(idx, data);

							idx++;
						} while (cursorAnswer.moveToNext());
					}
				}

				cursorAnswer.close();

			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		return list;
	}

	private DataObject_mm get(String rowid, int answeredid, String attribute1,
			String attribute2, String attribute3) {
		return new DataObject_mm(rowid, answeredid, attribute1, attribute2,
				attribute3);
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
	}

	private void goPrevious() {
		try {
			updateAnswerAll(SelectedSubjectID, SelectedChapterID,
					SelectedExamID, SelectedQuestionID);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		Cursor cursorQuestion;
		boolean bFound = false;
		boolean bPrevious = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;

		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);
			PreviousQuestionID = null;

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" 
					//+ " AND "
				//	+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'"
					,
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
			updateAnswerAll(SelectedSubjectID, SelectedChapterID,
					SelectedExamID, SelectedQuestionID);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

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
		try {
			updateAnswerAll(SelectedSubjectID, SelectedChapterID,
					SelectedExamID, SelectedQuestionID);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "5 : " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

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
								db.update(
										DatabaseHelper.TBL_CHAPTER,
										Attempedvalues,
										DatabaseHelper.FLD_ID_SUBJECT
												+ " = ? AND "
												+ DatabaseHelper.FLD_ID_CHAPTER
												+ " = ?", new String[] {
												SelectedSubjectID,
												SelectedChapterID });

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
		try {
			updateAnswerAll(SelectedSubjectID, SelectedChapterID,
					SelectedExamID, SelectedQuestionID);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		Cursor cursorQuestion;
		boolean bFound = false;
		boolean bNext = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;

		try {
			mHandler.sendEmptyMessage(MSG_STOP_TIMER);
			NextQuestionID = null;

			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" 
					//+ " AND "
					//+ DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'"
					,
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

	private void updateAnswerAll(String SubjectID, String ChapterID,
			String ExamID, String QuestionID) {
		try {
			ListView listview = (ListView) findViewById(R.id.lvOptions);
			for (int idx = 0; idx < listview.getCount(); idx++) {
				View viewrow = listview.getChildAt(idx);
				if (viewrow != null) {
					TextView rowid = (TextView) viewrow
							.findViewById(R.id.tvRow);
					Button attribute1 = (Button) viewrow
							.findViewById(R.id.btAnswerAttribute1);
					EditText attribute2 = (EditText) viewrow
							.findViewById(R.id.etAnswerAttribute2);
					EditText attribute3 = (EditText) viewrow
							.findViewById(R.id.etAnswerAttribute3);

					// Toast.makeText(getApplicationContext(),
					// rowid.getText().toString() + " / " +
					// attribute1.getText().toString() + " / " +
					// attribute2.getText().toString() + " / " +
					// attribute3.getText().toString(),
					// Toast.LENGTH_LONG).show();

					updateAnswer(SubjectID, ChapterID, ExamID, QuestionID,
							rowid.getText().toString(), attribute1.getText()
									.toString(), attribute2.getText()
									.toString(), attribute3.getText()
									.toString());
				}
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void updateAnswer(String SubjectID, String ChapterID,
			String ExamID, String QuestionID, String RowID, String Attribute1,
			String Attribute2, String Attribute3) {
		try {
			boolean bCorrect = false;
			int iAnsewrCount = 0;
			int iUserAnswerCount = 0;
			int count = 0;
			Cursor cursorAnswewAttribute;
			Cursor cursorUserAnswewAttribute;

			if (Attribute1 != null && Attribute2 != null && Attribute3 != null) {
				if (Attribute1.equals("Select...") == false) {

					db.delete(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE,
							DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
									+ DatabaseHelper.FLD_ID_QUESTION
									+ " = ? AND "
									+ DatabaseHelper.FLD_QUESTION_ROW_ID
									+ " = ?", new String[] { ChapterID, ExamID,
									QuestionID, RowID });

					ContentValues Answervalues = new ContentValues();
					Answervalues.put(DatabaseHelper.FLD_ID_CHAPTER, ChapterID);
					Answervalues.put(DatabaseHelper.FLD_ID_EXAM, ExamID);
					Answervalues
							.put(DatabaseHelper.FLD_ID_QUESTION, QuestionID);
					Answervalues.put(DatabaseHelper.FLD_QUESTION_ROW_ID, RowID);
					Answervalues.put(
							DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1,
							Attribute1);
					Answervalues.put(
							DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2,
							Attribute2);
					Answervalues.put(
							DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3,
							Attribute3);
					db.insert(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE, null,
							Answervalues);

					cursorAnswewAttribute = db.rawQuery("SELECT "
							+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1 + ", "
							+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_2 + ", "
							+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_3 + " FROM "
							+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE + " WHERE "
							+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
							+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
							new String[] { ChapterID, ExamID, QuestionID });

					iAnsewrCount = cursorAnswewAttribute.getCount();
					startManagingCursor(cursorAnswewAttribute);
					cursorAnswewAttribute.close();

					cursorUserAnswewAttribute = db.rawQuery("SELECT "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + ", "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2 + ", "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3
							+ " FROM "
							+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE
							+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
							+ " = ?" + " AND " + DatabaseHelper.FLD_ID_EXAM
							+ " = ?" + " AND " + DatabaseHelper.FLD_ID_QUESTION
							+ " = ?", new String[] { ChapterID, ExamID,
							QuestionID });

					startManagingCursor(cursorUserAnswewAttribute);
					iUserAnswerCount = cursorUserAnswewAttribute.getCount();

					if (iUserAnswerCount == iAnsewrCount) {
						bCorrect = true;
					}

					if (cursorUserAnswewAttribute != null) {
						if (cursorUserAnswewAttribute.moveToFirst()) {
							do {
								cursorAnswewAttribute = db
										.rawQuery(
												"SELECT "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1
														+ ", "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_2
														+ ", "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_3
														+ " FROM "
														+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
														+ " WHERE "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ " = ?"
														+ " AND "
														+ DatabaseHelper.FLD_ID_EXAM
														+ " = ?"
														+ " AND "
														+ DatabaseHelper.FLD_ID_QUESTION
														+ " = ?"
														+ " AND "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1
														+ " = ?"
														+ " AND "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_2
														+ " = ?"
														+ " AND "
														+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_3
														+ " = ?",
												new String[] {
														ChapterID,
														ExamID,
														QuestionID,
														cursorUserAnswewAttribute
																.getString(cursorUserAnswewAttribute
																		.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1)),
														cursorUserAnswewAttribute
																.getString(cursorUserAnswewAttribute
																		.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2)),
														cursorUserAnswewAttribute
																.getString(cursorUserAnswewAttribute
																		.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_3)) });

								count = cursorAnswewAttribute.getCount();
								cursorAnswewAttribute.close();

								if (count != 1) {
									bCorrect = false;
									break;
								} else {
									bCorrect = true;
								}
							} while (cursorUserAnswewAttribute.moveToNext());
						}
					}

					cursorUserAnswewAttribute.close();

					ContentValues Attempedvalues = new ContentValues();

					if (iUserAnswerCount > 0) {
						Attempedvalues.put(
								DatabaseHelper.FLD_QUESTION_ANSWERED, "T");
					} else {
						Attempedvalues.put(
								DatabaseHelper.FLD_QUESTION_ANSWERED, "F");
					}

					if (bCorrect == false) {
						Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT,
								"F");
					} else {
						Attempedvalues.put(DatabaseHelper.FLD_ANSWER_CORRECT,
								"T");
					}

					db.update(DatabaseHelper.TBL_EXAM, Attempedvalues,
							DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
									+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
							new String[] { ChapterID, ExamID, QuestionID });
				}
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), "2 : " + sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "3 : " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private int getAccountPosition(String chapterid, String examid,
			String questionid, String accountname) {
		int accountposition = 0;

		try {
			Cursor cursorAccount = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + " FROM "
					+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1, new String[] {
					chapterid, examid, questionid });

			startManagingCursor(cursorAccount);

			int colAttribute_1 = cursorAccount
					.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);

			int idx = 1;

			if (cursorAccount != null) {
				if (cursorAccount.moveToFirst()) {
					do {
						if (cursorAccount.getString(colAttribute_1).equals(
								accountname)) {
							accountposition = idx;
							break;
						}
						idx++;
					} while (cursorAccount.moveToNext());
				}
			}

			cursorAccount.close();

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return accountposition;
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
											finish();
											return;
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