package ica.exam;

import ica.ProfileInfo.StudentDetails;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Receipt extends Activity {

	private SQLiteDatabase db;
	Context CurContext;

	int CurrentCourseID = -1;
	private StudentDetails studentDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.receipt);
		CurContext = this;

		
		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();
		
		setTitle("Receipt Details- [" + studentDetails.getStudentID()
				+ "]");

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Intent prevIntent = getIntent();
		Bundle data = prevIntent.getExtras();

		if (data != null) {
			if (data.containsKey("CourseID")) {
				Object objval = data.get("CourseID");

				CurrentCourseID = Integer.parseInt(objval.toString());

				if (CurrentCourseID != -1) {
					GetCourseData(CurrentCourseID);
				} else {

				}

			}

		}
	}

	public void FetchReciptDB() {

	}

	public void GetCourseData(int CourseID) {

		Cursor cursorCourseDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_COURSE_FEE + " where "
				+ DatabaseHelper.FlD_COURSE_ID + "='" + CourseID + "'", null);

		try {
			startManagingCursor(cursorCourseDtl);

			if (cursorCourseDtl != null) {
				int userCount = cursorCourseDtl.getCount();

				if (userCount > 0 && cursorCourseDtl.moveToFirst()) {

					int columnidx = cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FlD_COURSE_ID);

					columnidx = cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FLD_COURSE_NAME);

					String CourseName = cursorCourseDtl.getString(columnidx);

					columnidx = cursorCourseDtl
							.getColumnIndex(DatabaseHelper.FLD_TOTAL_RECVD_AMT);

					String CrsTotalRcvd = cursorCourseDtl.getString(columnidx);

					TextView txtTotalFeeAmt = (TextView) findViewById(R.id.txtTotalRctAmt);
					txtTotalFeeAmt.setText(CrsTotalRcvd);

					SyncViewToData(CourseID, CourseName);
				}

			}

			cursorCourseDtl.close();

		} catch (SQLiteException sqle) {
			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorCourseDtl != null) {
				try {
					cursorCourseDtl.close();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

	}

	public void SyncViewToData(int CourseID, String CourseName) {

		Cursor cursorUser = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_RECIEPT + " where "
				+ DatabaseHelper.FlD_COURSE_ID + "='" + CourseID + "'", null);

		try {
			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				int userCount = cursorUser.getCount();

				if (userCount > 0 && cursorUser.moveToFirst()) {

					do {

						int columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FlD_COURSE_ID);

						String curCourseID = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_COURSE_NAME);
						String curCourseName = CourseName;

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_RECIEPT_DATE);
						String RctDt = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_RECIEPT_MR);
						String RctMrNo = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_RECEIPT_FEES);
						String RctFee = cursorUser.getString(columnidx);

						ShowCourseData(curCourseID, curCourseName, RctDt,
								RctMrNo, RctFee);

					} while (cursorUser.moveToNext());

				}

			}

		} catch (SQLiteException sqle) {
			if (cursorUser != null) {
				try {
					cursorUser.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorUser != null) {
				try {
					cursorUser.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorUser != null) {
				try {
					cursorUser.close();

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		}

	}

	public void ShowCourseData(String CourseID, String CourseNm,
			String CourseFee, String TotalRcvd, String TotalDue) {

		LayoutInflater factory = LayoutInflater.from(CurContext);

		TableLayout tblReceiptParent = (TableLayout) findViewById(R.id.tblReceiptLayout);

		// //ROW

		View FeeSumaryRow = factory.inflate(R.layout.receiptrow, null);
		TableRow llFeeRow = (TableRow) FeeSumaryRow
				.findViewById(R.id.single_receipt_row);

		llFeeRow.setTag(CourseID);

		TextView txtCourseName = (TextView) FeeSumaryRow
				.findViewById(R.id.txtReceiptCourseName);

		txtCourseName.setText(CourseNm);

		txtCourseName.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				View parentvu = (View) v.getParent();
				String courseId = (String) parentvu.getTag();

				courseId.charAt(0);

			}
		});

		TextView txtReceiptMrdt = (TextView) FeeSumaryRow
				.findViewById(R.id.txtReceiptMRDt);

		txtReceiptMrdt.setText(CourseFee);

		TextView txtReceiptMrno = (TextView) FeeSumaryRow
				.findViewById(R.id.txtReceiptMRNo);

		txtReceiptMrno.setText(TotalRcvd);
		
		TextView txtReceiptFees = (TextView) FeeSumaryRow
				.findViewById(R.id.txtReceiptFees);

		txtReceiptFees.setText(TotalDue);
	
		// /ROW///

		tblReceiptParent.addView(llFeeRow);
		tblReceiptParent.refreshDrawableState();

		tblReceiptParent.requestLayout();
	}

}
