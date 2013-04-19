package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppPreferenceStatus;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class QuestionList_Exam extends ListActivity {
	Context IndexContext;
	private Cursor cursorQuestion;
	private SQLiteDatabase db;
	private String SelectedSubjectId;
	private String SelectedChapterId;
	private String QuestionType;
	private Intent intentQSCQ;
	private Intent intentQMCQ;
	private Intent intentQFIB;
	private Intent intentQMMM;
	private Intent intentHome;
	private Intent intentScoreCard;
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

	Context CurContext;
	StudentDetails studentDetails = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_list);

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

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();

		SelectedSubjectId = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_SUBJECT);
		SelectedChapterId = getIntent().getExtras().getString(
				DatabaseHelper.FLD_ID_CHAPTER);
		ExamTime = getIntent().getExtras()
				.getLong(DatabaseHelper.FLD_EXAM_TIME);
		ExamTimeElapsedPrev = getIntent().getExtras().getLong("EXAM_ELAPSED");
		ExamTimeElapsed = 0;

		ImageButton buttonDone = (ImageButton) this.findViewById(R.id.btDone);
		buttonDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goDone();
			}
		});

		adExamTermination = new AlertDialog.Builder(this);
		intentQSCQ = new Intent(this, Question_SCQ_Details.class);
		intentQMCQ = new Intent(this, Question_MCQ_Details.class);
		intentQFIB = new Intent(this, Question_FIB_Details.class);
		intentQMMM = new Intent(this, Question_MM_Details.class);
		intentHome = new Intent(this, ExamActivity.class);
		intentScoreCard = new Intent(this, Score_Card.class);

		db = (new DatabaseHelper(this)).getWritableDatabase();

		try {
			cursorQuestion = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ ", " + DatabaseHelper.FLD_ID_EXAM + ","
					+ DatabaseHelper.FLD_EXAM_NAME + ","
					+ DatabaseHelper.FLD_EXAM_TIME + ","
					+ DatabaseHelper.FLD_ID_QUESTION + ", "
					+ DatabaseHelper.FLD_QUESTION_TYPE + ", "
					+ DatabaseHelper.FLD_QUESTION_MARKS + ", "
					+ DatabaseHelper.FLD_QUESTION_BODY + ", "
					+ DatabaseHelper.FLD_QUESTION_ANSWERED + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?" + " ORDER BY "
					+ DatabaseHelper.FLD_ID_QUESTION,
					new String[] { SelectedChapterId });

			startManagingCursor(cursorQuestion);

			if (cursorQuestion != null) {
				if (cursorQuestion.moveToFirst()) {

					String sEmail = studentDetails.getStudentID();
					if (sEmail != null) {
						setTitle(cursorQuestion.getString(cursorQuestion
								.getColumnIndex(DatabaseHelper.FLD_EXAM_NAME))
								+ " - [" + sEmail + "]");
					}

					ListAdapter ListAdapterquestion = new QuestionAdapter(this,
							cursorQuestion);
					ListView ListViewQuestion = (ListView) findViewById(android.R.id.list);
					ListViewQuestion.setAdapter(ListAdapterquestion);

					ListViewQuestion
							.setOnItemClickListener(new AdapterView.OnItemClickListener() {
								public void onItemClick(AdapterView<?> parent,
										View v, int position, long id) {
									try {
										mHandler.sendEmptyMessage(MSG_STOP_TIMER);

										cursorQuestion.moveToPosition(position);
										QuestionType = cursorQuestion.getString(cursorQuestion
												.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE));

										if (QuestionType.equals("SCQ")) {
											intentQSCQ
													.putExtra(
															DatabaseHelper.FLD_ID_SUBJECT,
															SelectedSubjectId);
											intentQSCQ
													.putExtra(
															DatabaseHelper.FLD_ID_CHAPTER,
															SelectedChapterId);
											intentQSCQ
													.putExtra(
															DatabaseHelper.FLD_ID_EXAM,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
											intentQSCQ
													.putExtra(
															DatabaseHelper.FLD_ID_QUESTION,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
											intentQSCQ
													.putExtra(
															DatabaseHelper.FLD_EXAM_TIME,
															ExamTime);
											intentQSCQ.putExtra("EXAM_ELAPSED",
													ExamTimeElapsed);
											startActivity(intentQSCQ);
											finish();
											return;
										} else if (QuestionType.equals("MCQ")) {
											intentQMCQ
													.putExtra(
															DatabaseHelper.FLD_ID_SUBJECT,
															SelectedSubjectId);
											intentQMCQ
													.putExtra(
															DatabaseHelper.FLD_ID_CHAPTER,
															SelectedChapterId);
											intentQMCQ
													.putExtra(
															DatabaseHelper.FLD_ID_EXAM,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
											intentQMCQ
													.putExtra(
															DatabaseHelper.FLD_ID_QUESTION,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
											intentQMCQ
													.putExtra(
															DatabaseHelper.FLD_EXAM_TIME,
															ExamTime);
											intentQMCQ.putExtra("EXAM_ELAPSED",
													ExamTimeElapsed);
											startActivity(intentQMCQ);
											finish();
											return;
										} else if (QuestionType.equals("FIB")) {
											intentQFIB
													.putExtra(
															DatabaseHelper.FLD_ID_SUBJECT,
															SelectedSubjectId);
											intentQFIB
													.putExtra(
															DatabaseHelper.FLD_ID_CHAPTER,
															SelectedChapterId);
											intentQFIB
													.putExtra(
															DatabaseHelper.FLD_ID_EXAM,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
											intentQFIB
													.putExtra(
															DatabaseHelper.FLD_ID_QUESTION,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
											intentQFIB
													.putExtra(
															DatabaseHelper.FLD_EXAM_TIME,
															ExamTime);
											intentQFIB.putExtra("EXAM_ELAPSED",
													ExamTimeElapsed);
											startActivity(intentQFIB);
											finish();
											return;
										} else if (QuestionType.equals("M&M")) {
											intentQMMM
													.putExtra(
															DatabaseHelper.FLD_ID_SUBJECT,
															SelectedSubjectId);
											intentQMMM
													.putExtra(
															DatabaseHelper.FLD_ID_CHAPTER,
															SelectedChapterId);
											intentQMMM
													.putExtra(
															DatabaseHelper.FLD_ID_EXAM,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_EXAM)));
											intentQMMM
													.putExtra(
															DatabaseHelper.FLD_ID_QUESTION,
															cursorQuestion
																	.getString(cursorQuestion
																			.getColumnIndex(DatabaseHelper.FLD_ID_QUESTION)));
											intentQMMM
													.putExtra(
															DatabaseHelper.FLD_EXAM_TIME,
															ExamTime);
											intentQMMM.putExtra("EXAM_ELAPSED",
													ExamTimeElapsed);
											startActivity(intentQMMM);
											finish();
											return;
										}
									} catch (SQLiteException sqle) {
										Toast.makeText(getApplicationContext(),
												sqle.getMessage(),
												Toast.LENGTH_SHORT).show();
									} catch (Exception e) {
										Toast.makeText(getApplicationContext(),
												e.getMessage(),
												Toast.LENGTH_SHORT).show();
									}
								}
							});

					if (ExamTimeElapsedPrev < ExamTime) {
						
						//Toast.makeText(getApplicationContext(),"Your Exam limit limit is: " + ExamTime,Toast.LENGTH_SHORT).show();
						
						AlertDialog alertDialog = new AlertDialog.Builder(QuestionList_Exam.this).create(); 
						alertDialog.setTitle("Warning ..."); 
						alertDialog.setMessage("Your Exam time limit is:  " + ExamTime/60 +" Min."); 
						alertDialog.setIcon(R.drawable.tick); 
						
						alertDialog.setButton("OK", new DialogInterface.OnClickListener() { 
							public void onClick(DialogInterface dialog, int which) { 

							} 
						}); 
						alertDialog.show();

						mHandler.sendEmptyMessage(MSG_START_TIMER);
					} else {
						ExamTimeElapsed = ExamTimeElapsedPrev;
						TextView tvTextView = (TextView) findViewById(R.id.txClock);
						tvTextView.setText("Time left : 00:00:00");
					}

				}
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
		goDone();
		return;
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
			Toast.makeText(getApplicationContext(),
					"Data Exception" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	String ActionType = "";

	public void goDone() {

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
			if (ExamTimeElapsedPrev < ExamTime) {

				adExamTermination.setIcon(R.drawable.bt_question);
				adExamTermination.setTitle("Mock status");

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
															+ " = ?",
													new String[] {
															SelectedSubjectId,
															SelectedChapterId });

											insertLastExamUploadInfo(
													Integer.parseInt(GetExamID(
															SelectedSubjectId,
															SelectedChapterId)),
													0);

										} catch (SQLiteException sqle) {
											Toast.makeText(
													getApplicationContext(),
													sqle.getMessage(),
													Toast.LENGTH_LONG).show();
										} catch (Exception e) {
											Toast.makeText(
													getApplicationContext(),
													e.getMessage(),
													Toast.LENGTH_SHORT).show();
										}

										ExamTimeElapsed = ExamTime + 1;
										mHandler.sendEmptyMessage(MSG_STOP_TIMER);
										intentScoreCard.putExtra(
												DatabaseHelper.FLD_ID_SUBJECT,
												SelectedSubjectId);
										intentScoreCard.putExtra(
												DatabaseHelper.FLD_ID_CHAPTER,
												SelectedChapterId);
										intentScoreCard.putExtra(
												DatabaseHelper.FLD_ID_EXAM,
												GetExamID(SelectedSubjectId,
														SelectedChapterId));
										startActivity(intentScoreCard);

										finish();
										return;
									}
								});
				AlertDialog altEndExam = adExamTermination.create();
				altEndExam.show();
			} else {
				startActivity(intentHome);
				finish();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return;
	}

	private String GetExamID(String subjectid, String chapterid) {
		String examid = null;

		try {
			Cursor cursorExam;

			cursorExam = db.rawQuery("SELECT " + DatabaseHelper.FLD_ROWID
					+ ", " + DatabaseHelper.FLD_ID_EXAM + " FROM "
					+ DatabaseHelper.TBL_EXAM + " WHERE "
					+ DatabaseHelper.FLD_ID_CHAPTER + " = ?",
					new String[] { chapterid });

			int colID = cursorExam.getColumnIndex(DatabaseHelper.FLD_ID_EXAM);
			startManagingCursor(cursorExam);

			if (cursorExam.moveToFirst()) {
				examid = cursorExam.getString(colID);
			}
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return examid;
	}

	private class QuestionAdapter extends CursorAdapter {
		private final LayoutInflater mInflater;

		public QuestionAdapter(Context context, Cursor cursor) {
			super(context, cursor, true);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView t;
			t = (TextView) view.findViewById(R.id.tvquestionbody);
			t.setText(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_BODY)));

			t = (TextView) view.findViewById(R.id.tvQuestionType);
			if ((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE)))
					.equals("FIB")) {
				t.setText("Fill the blank");
			} else if ((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE)))
					.equals("SCQ")) {
				t.setText("Single choice");
			} else if ((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE)))
					.equals("MCQ")) {
				t.setText("Mix & match");
			} else if ((cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.FLD_QUESTION_TYPE)))
					.equals("M&M")) {
				t.setText("A/C ledger");
			}

			t = (TextView) view.findViewById(R.id.tvQuestionMarks);
			t.setText(" (Point - "
					+ cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.FLD_QUESTION_MARKS))
					+ ")");

			ImageView i = (ImageView) view.findViewById(R.id.imgStatus);

			if (cursor
					.getString(
							cursor.getColumnIndex(DatabaseHelper.FLD_QUESTION_ANSWERED))
					.equals("T")) {
				i.setImageResource(R.drawable.ico_star_green);
			} else {
				i.setImageResource(R.drawable.bt_question);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.question_row, parent,
					false);
			return view;
		}
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
					adExamTermination.setMessage(ActionType
							+ " time is over. Please press Ok to complete.");
					adExamTermination
							.setTitle(ActionType + " status")
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											intentScoreCard
													.putExtra(
															DatabaseHelper.FLD_ID_CHAPTER,
															SelectedChapterId);
											intentScoreCard.putExtra(
													DatabaseHelper.FLD_ID_EXAM,
													GetExamID(
															SelectedSubjectId,
															SelectedChapterId));
											startActivity(intentScoreCard);
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
						+ ExamTimeElapsedPrev;
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
