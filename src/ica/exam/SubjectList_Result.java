package ica.exam;

import ica.ProfileInfo.StudentDetails;
import android.app.Activity;
import android.app.AlertDialog;
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

public class SubjectList_Result extends Activity {

	private SQLiteDatabase db;
	private String[] aChapterID = null;
	private CharSequence[] aChapterName = null;
	private int idx;
	private AlertDialog.Builder adChapter;
	private String SelectedSubjectID;
	private String SelectedChapterID;
	private Intent intentQuestion;
	private Context CurContext;
	private StudentDetails studentDetails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.subject_list_result);

		TextView t = (TextView) findViewById(R.id.txHeader);
		t.setText("Subject(s)");

		adChapter = new AlertDialog.Builder(this);
		CurContext = this;

		intentQuestion = new Intent(this, QuestionList_Result.class);

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();
		
		
		try {
			db = (new DatabaseHelper(this)).getWritableDatabase();

			String sEmail = studentDetails.getStudentID();
			if (sEmail != null) {
			
				setTitle("Subject Result- [" + sEmail + "]");

			}

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			Cursor cursorSubject = db.query(DatabaseHelper.TBL_SUBJECT,
					new String[] { DatabaseHelper.FLD_ROWID,
							DatabaseHelper.FLD_ID_SUBJECT,
							DatabaseHelper.FLD_NAME_SUBJECT }, null, null,
					null, null, null);

			ListAdapter ListAdaptersubject = new SubjectAdapter(this,
					cursorSubject);
			ListView ListViewSubject = (ListView) findViewById(R.id.lvSubject);
			ListViewSubject.setAdapter(ListAdaptersubject);

			ListViewSubject
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							int iCount = 0;
							try {
								Cursor cursorSubject = db
										.query(DatabaseHelper.TBL_SUBJECT,
												new String[] {
														DatabaseHelper.FLD_ROWID,
														DatabaseHelper.FLD_ID_SUBJECT,
														DatabaseHelper.FLD_NAME_SUBJECT },
												null, null, null, null, null);

								cursorSubject.moveToPosition(position);
								SelectedSubjectID = cursorSubject.getString(cursorSubject
										.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT));
								cursorSubject.close();
							} catch (SQLiteException sqle) {
								Toast.makeText(getApplicationContext(),
										sqle.getMessage(), Toast.LENGTH_SHORT)
										.show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(), Toast.LENGTH_SHORT)
										.show();
							}

							try {
								Cursor cursorChapter = db
										.rawQuery(
												"SELECT "
														+ DatabaseHelper.FLD_ROWID
														+ ", "
														+ DatabaseHelper.FLD_ID_CHAPTER
														+ ","
														+ DatabaseHelper.FLD_NAME_CHAPTER
														+ " FROM "
														+ DatabaseHelper.TBL_CHAPTER
														+ " WHERE "
														+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER
														+ " = 'T'"
														+ " AND "
														+ DatabaseHelper.FLD_ID_SUBJECT
														+ " = ?",
												new String[] { SelectedSubjectID });

								iCount = cursorChapter.getCount();
								int colID = cursorChapter
										.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER);
								int colChapterName = cursorChapter
										.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER);

								startManagingCursor(cursorChapter);

								aChapterID = new String[iCount];
								aChapterName = new CharSequence[iCount];

								if (cursorChapter != null) {
									if (cursorChapter.moveToFirst()) {
										idx = 0;
										do {
											aChapterID[idx] = cursorChapter
													.getString(colID);
											aChapterName[idx] = cursorChapter
													.getString(colChapterName);

											idx++;
										} while (cursorChapter.moveToNext());
									}
								}

								cursorChapter.close();
							} catch (SQLiteException sqle) {
								Toast.makeText(getApplicationContext(),
										sqle.getMessage(), Toast.LENGTH_SHORT)
										.show();
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(), Toast.LENGTH_SHORT)
										.show();
							}

							try {
								if (iCount > 0) {
									adChapter.setTitle("Chapter(s)");
									adChapter.setIcon(R.drawable.folder_yellow);
									adChapter
											.setItems(
													aChapterName,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int item) {
															try {
																SelectedChapterID = GetChapterID(
																		SelectedSubjectID,
																		aChapterName[item]
																				.toString());

																intentQuestion
																		.putExtra(
																				DatabaseHelper.FLD_ID_SUBJECT,
																				SelectedSubjectID);
																intentQuestion
																		.putExtra(
																				DatabaseHelper.FLD_ID_CHAPTER,
																				SelectedChapterID);
																startActivity(intentQuestion);
																finish();
																return;
															} catch (Exception e) {
																Toast.makeText(
																		getApplicationContext(),
																		e.getMessage(),
																		Toast.LENGTH_SHORT)
																		.show();
															}
														}
													});
									AlertDialog altChapter = adChapter.create();
									altChapter.show();
								}
							} catch (Exception e) {
								Toast.makeText(getApplicationContext(),
										e.getMessage(), Toast.LENGTH_SHORT)
										.show();
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
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
		
		finish();
		return;
	}

	private String GetChapterID(String SubjectID, String ChapterName) {
		String ChapterID = null;

		try {
			Cursor cursorChapter = db.rawQuery("SELECT "
					+ DatabaseHelper.FLD_ROWID + ", "
					+ DatabaseHelper.FLD_ID_CHAPTER + ","
					+ DatabaseHelper.FLD_NAME_CHAPTER + " FROM "
					+ DatabaseHelper.TBL_CHAPTER + " WHERE "
					+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER + " = 'T'"
					+ " AND " + DatabaseHelper.FLD_ID_SUBJECT + " = ?",
					new String[] { SubjectID });

			int colID = cursorChapter
					.getColumnIndex(DatabaseHelper.FLD_ID_CHAPTER);
			int colChapter = cursorChapter
					.getColumnIndex(DatabaseHelper.FLD_NAME_CHAPTER);

			startManagingCursor(cursorChapter);

			if (cursorChapter != null) {
				if (cursorChapter.moveToFirst()) {
					idx = 0;
					do {
						if (ChapterName.equals(cursorChapter
								.getString(colChapter))) {
							ChapterID = cursorChapter.getString(colID);
						}
						idx++;
					} while (cursorChapter.moveToNext());
				}
			}

			cursorChapter.close();
		} catch (SQLiteException sqle) {
			Toast.makeText(getApplicationContext(), sqle.getMessage(),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		return ChapterID;
	}

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
								"SELECT "
										+ DatabaseHelper.FLD_ID_CHAPTER
										+ " FROM "
										+ DatabaseHelper.TBL_CHAPTER
										+ " WHERE "
										+ DatabaseHelper.FLD_ID_SUBJECT
										+ " = ?"
										+ " AND "
										+ DatabaseHelper.FLD_EXAM_COMPLETED_CHAPTER
										+ " = 'T'",
								new String[] { cursor.getString(cursor
										.getColumnIndex(DatabaseHelper.FLD_ID_SUBJECT)) });

				t = (TextView) view.findViewById(R.id.tvChapterCount);
				t.setText("Available Mock (" + cursorChapter.getCount() + ")");
				cursorChapter.close();
			} catch (SQLiteException sqle) {
				Toast.makeText(getApplicationContext(), sqle.getMessage(),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.subject_row, parent,
					false);
			return view;
		}
	}

}
