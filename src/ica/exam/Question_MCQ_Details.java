package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppPreferenceStatus;

import java.util.ArrayList;

import android.app.ListActivity;
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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Question_MCQ_Details extends ListActivity {
	private SQLiteDatabase db;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private String SelectedExamID;
	private String SelectedQuestionID;
	private String Answered = null;
	private String PreviousQuestionID;
	private String NextQuestionID;
	private long ExamTime;
	private long ExamTimeElapsed;
	private long ExamTimeLeft;
	private long ExamTimeElapsedPrev;
	private StopWatch timer = new StopWatch();
	private AlertDialog.Builder adExamTermination;
	private ArrayList<DataObject_mcq> content;

	final int MSG_START_TIMER = 0;
	final int MSG_STOP_TIMER = 1;
	final int MSG_UPDATE_TIMER = 2;
	final int REFRESH_RATE = 100;

	Context CurContext;
	String ActionType = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_mcq_details);

		CurContext = this;

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
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

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
			adExamTermination = new AlertDialog.Builder(this);

			db = (new DatabaseHelper(this)).getWritableDatabase();

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

					StudentDetails.initInstance(CurContext);
					StudentDetails studentInfo = StudentDetails.getInstance();

					String sEmail = studentInfo.getStudentID();
					if (sEmail != null) {
						setTitle(ActionType
								+ ":"
								+ cursorExam
										.getString(cursorExam
												.getColumnIndex(DatabaseHelper.FLD_EXAM_NAME))
								+ " - [" + sEmail + "]");
					}

					// TextView t = (TextView)
					// findViewById(R.id.tvQuestionType);
					// t.setText("Mix & match");

					do {
						idx++;
						if (cursorExam.getString(colQID).equals(
								SelectedQuestionID)) {

							TextView txtQuestionSerial = (TextView) findViewById(R.id.tvQuestionSerial);
							txtQuestionSerial.setText(idx + " of " + iCount);

							TextView txtQuestionMarks = (TextView) findViewById(R.id.tvQuestionMarks);
							txtQuestionMarks.setText("(Point - "
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

	 if (Answered.equals("T")) {
		try {
			Cursor cursorUserAnswerAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + ", "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2 + " FROM "
					+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					SelectedChapterID, SelectedExamID, SelectedQuestionID });
			startManagingCursor(cursorUserAnswerAttribute);

			int colAtt1 = cursorUserAnswerAttribute
					.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);
			int colAtt2 = cursorUserAnswerAttribute
					.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2);

			content = new ArrayList<DataObject_mcq>();

			if (cursorUserAnswerAttribute != null) {
				if (cursorUserAnswerAttribute.moveToFirst()) {
					do {
						DataObject_mcq rowdata = new DataObject_mcq();

						rowdata.setQuestion(cursorUserAnswerAttribute
								.getString(colAtt1));
						rowdata.setAnswer(cursorUserAnswerAttribute
								.getString(colAtt2));
						content.add(rowdata);
					} while (cursorUserAnswerAttribute.moveToNext());
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
		} else {
			try {
				Cursor cursorQuestionAttribute = db.rawQuery("SELECT "
						+ DatabaseHelper.FLD_ROWID + ", "
						+ DatabaseHelper.FLD_ID_QUESTION + ", "
						+ DatabaseHelper.FLD_ID_EXAM + ","
						+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1 + ", "
						+ DatabaseHelper.FLD_QUESTION_ATTRIBUTE_2 + " FROM "
						+ DatabaseHelper.TBL_QUESTION_ATTRIBUTE + " WHERE "
						+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
						+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
						new String[] { SelectedChapterID, SelectedExamID,
								SelectedQuestionID });

				startManagingCursor(cursorQuestionAttribute);

				int colAtt1 = cursorQuestionAttribute
						.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_1);
				int colAtt2 = cursorQuestionAttribute
						.getColumnIndex(DatabaseHelper.FLD_QUESTION_ATTRIBUTE_2);

				content = new ArrayList<DataObject_mcq>();

				if (cursorQuestionAttribute != null) {
					if (cursorQuestionAttribute.moveToFirst()) {
						do {
							DataObject_mcq rowdata = new DataObject_mcq();

							rowdata.setQuestion(cursorQuestionAttribute
									.getString(colAtt1));
							rowdata.setAnswer(cursorQuestionAttribute
									.getString(colAtt2));
							content.add(rowdata);
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
		}

		setListAdapter(new DragNDropAdaptermcq(this,
				new int[] { R.layout.question_mcq_row }, new int[] {
						R.id.tvQuestionAttribute, R.id.tvAnswerAttribute },
				content));
		ListView listView = getListView();

		if (listView instanceof DragNDropListViewmcq) {
			((DragNDropListViewmcq) listView).setDropListener(mDropListener);
			((DragNDropListViewmcq) listView)
					.setRemoveListener(mRemoveListener);
			((DragNDropListViewmcq) listView).setDragListener(mDragListener);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
		return;
	}

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdaptermcq) {
				((DragNDropAdaptermcq) adapter).onDrop(from, to);
				getListView().invalidateViews();

				for (int idx = 0; idx < content.size(); idx++) {
					DataObject_mcq data = content.get(idx);
					updateAnswer(SelectedChapterID, SelectedExamID,
							SelectedQuestionID, data.getQuestion(),
							data.getAnswer());
				}
			}
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdaptermcq) {
				((DragNDropAdaptermcq) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = 0xe0103010;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);

			ImageView iv = (ImageView) itemView.findViewById(R.id.ivDrag);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);

			TextView tvA = (TextView) itemView
					.findViewById(R.id.tvAnswerAttribute);
			if (tvA != null)
				tvA.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);

			ImageView iv = (ImageView) itemView.findViewById(R.id.ivDrag);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);

			TextView tvA = (TextView) itemView
					.findViewById(R.id.tvAnswerAttribute);
			if (tvA != null)
				tvA.setVisibility(View.VISIBLE);
		}

	};

	@Override
	public void onBackPressed() {
		goQuestion();
		return;
	}

	private void goPrevious() {
		Cursor cursorQuestion;
		boolean bFound = false;
		boolean bPrevious = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;

		mHandler.sendEmptyMessage(MSG_STOP_TIMER);
		PreviousQuestionID = null;

		try {
			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?"
			// + " AND "
			// + DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'"
					, new String[] { SelectedChapterID, SelectedExamID });

			int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();

			// if (iQCount == 0) {
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
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return;
	}

	private void goDone() {
		try {
			adExamTermination.setIcon(R.drawable.bt_question);
			adExamTermination
					.setMessage("This concludes your "
							+ ActionType
							+ ". Are you sure? Yes will complete the exam and No will continue the exam.");
			adExamTermination
					.setTitle(ActionType + " status")
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
		Cursor cursorQuestion;
		boolean bFound = false;
		boolean bNext = false;
		String QID = null;
		String QType = null;
		String QAnswered = null;

		mHandler.sendEmptyMessage(MSG_STOP_TIMER);
		NextQuestionID = null;

		try {
			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ " FROM " + DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?"
			// + " AND "
			// + DatabaseHelper.FLD_QUESTION_ANSWERED + " = 'F'"
					, new String[] { SelectedChapterID, SelectedExamID });

			int iQCount = cursorQuestion.getCount();
			cursorQuestion.close();
			//
			// if (iQCount == 0) {
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
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
			String QuestionID, String Attribute1, String Attribute2) {
		int iUserAnswerCnt = 0;
		boolean bCorrect = false;

		try {
			db.delete(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE,
					DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
							+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ? AND "
							+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1
							+ " = ?", new String[] { ChapterID, ExamID,
							QuestionID, Attribute1 });
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		if (Attribute1 != null && Attribute2 != null) {
			try {
				ContentValues Answervalues = new ContentValues();
				Answervalues.put(DatabaseHelper.FLD_ID_CHAPTER, ChapterID);
				Answervalues.put(DatabaseHelper.FLD_ID_EXAM, ExamID);
				Answervalues.put(DatabaseHelper.FLD_ID_QUESTION, QuestionID);
				Answervalues.put(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1,
						Attribute1);
				Answervalues.put(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2,
						Attribute2);

				db.insert(DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE, null,
						Answervalues);
			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		try {
			Cursor cursorUserAnswewAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ID_QUESTION + " FROM "
					+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					ChapterID, ExamID, QuestionID });

			startManagingCursor(cursorUserAnswewAttribute);
			iUserAnswerCnt = cursorUserAnswewAttribute.getCount();
			cursorUserAnswewAttribute.close();

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			ContentValues Attempedvalues = new ContentValues();

			if (iUserAnswerCnt > 0) {
				Attempedvalues.put(DatabaseHelper.FLD_QUESTION_ANSWERED, "T");
			} else {
				Attempedvalues.put(DatabaseHelper.FLD_QUESTION_ANSWERED, "F");
			}

			db.update(DatabaseHelper.TBL_EXAM, Attempedvalues,
					DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
							+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
					new String[] { ChapterID, ExamID, QuestionID });

		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			Cursor cursorUsrAnsAttribute = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1 + ", "
					+ DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2 + " FROM "
					+ DatabaseHelper.TBL_USER_ANSWER_ATTRIBUTE + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_EXAM + " = ?" + " AND "
					+ DatabaseHelper.FLD_ID_QUESTION + " = ?", new String[] {
					ChapterID, ExamID, QuestionID });

			int colAtt_1 = cursorUsrAnsAttribute
					.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_1);
			int colAtt_2 = cursorUsrAnsAttribute
					.getColumnIndex(DatabaseHelper.FLD_USER_ANSWER_ATTRIBUTE_2);

			String Att_1 = null;
			String Att_2 = null;

			startManagingCursor(cursorUsrAnsAttribute);

			if (cursorUsrAnsAttribute != null) {
				if (cursorUsrAnsAttribute.moveToFirst()) {
					do {
						Att_1 = cursorUsrAnsAttribute.getString(colAtt_1);
						Att_2 = cursorUsrAnsAttribute.getString(colAtt_2);

						try {
							Cursor cursorAnsAttribute = db.rawQuery("SELECT "
									+ DatabaseHelper.FLD_ROWID + " FROM "
									+ DatabaseHelper.TBL_ANSWER_ATTRIBUTE
									+ " WHERE " + DatabaseHelper.FLD_ID_CHAPTER
									+ " = ?" + " AND "
									+ DatabaseHelper.FLD_ID_EXAM + " = ?"
									+ " AND " + DatabaseHelper.FLD_ID_QUESTION
									+ " = ?" + " AND "
									+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_1
									+ " = ?" + " AND "
									+ DatabaseHelper.FLD_ANSWER_ATTRIBUTE_2
									+ " = ?", new String[] { ChapterID, ExamID,
									QuestionID, Att_1, Att_2 });

							startManagingCursor(cursorAnsAttribute);
							int iCorrect = cursorAnsAttribute.getCount();
							cursorAnsAttribute.close();

							if (iCorrect > 0) {
								bCorrect = true;
							} else {
								bCorrect = false;
								break;
							}

						} catch (SQLiteException sqle) {
							Toast.makeText(getApplicationContext(),
									sqle.getMessage(), Toast.LENGTH_SHORT)
									.show();
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(),
									e.getMessage(), Toast.LENGTH_SHORT).show();
						}

					} while (cursorUsrAnsAttribute.moveToNext());
				}
			}

			cursorUsrAnsAttribute.close();
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			ContentValues AttempedvaluesCorrert = new ContentValues();

			if (bCorrect) {
				AttempedvaluesCorrert.put(DatabaseHelper.FLD_ANSWER_CORRECT,
						"T");
			} else {
				AttempedvaluesCorrert.put(DatabaseHelper.FLD_ANSWER_CORRECT,
						"F");
			}

			db.update(DatabaseHelper.TBL_EXAM, AttempedvaluesCorrert,
					DatabaseHelper.FLD_ID_CHAPTER + " = ? AND "
							+ DatabaseHelper.FLD_ID_EXAM + " = ? AND "
							+ DatabaseHelper.FLD_ID_QUESTION + " = ?",
					new String[] { ChapterID, ExamID, QuestionID });

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