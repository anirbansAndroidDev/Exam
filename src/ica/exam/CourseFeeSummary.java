package ica.exam;

import ica.ICAConstants.CourseMatIntent;
import ica.ProfileInfo.StudentDetails;
import ica.Utility.AppInfo;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.DeviceInfo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class CourseFeeSummary extends Activity {
	
	Intent MainIntent;
	Context CurContext;

	private SQLiteDatabase db;

	AsyncResultDetails AsyncResultDownloder;

	StudentDetails StudentInfo = null;

	private ProgressDialog pgBusyWait;

	TextView txtTotalDue;
	private String ActionType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.course_fee_summary);
		
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

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		StudentDetails.initInstance(CurContext);

		StudentInfo = StudentDetails.getInstance();

		try {
			String sEmail = StudentInfo.getStudentID();
			if (sEmail != null) {

				setTitle("ICA Student Connect (Ver: "
						+ AppInfo.versionInfo(CurContext).getVersionName()
						+ ")-Progress Report- [" + sEmail + "]");

				if (StudentInfo.getStudentStatusCode() != null) {

					if (DeviceInfo.haveNetworkConnection(CurContext)) {
						AsyncResultDownloder = new AsyncResultDetails();
						AsyncResultDownloder.execute(StudentInfo
								.getStudentStatusCode());
					} else {
						SyncViewToData();

					}

				}
			}

			MainIntent = new Intent(this, ExamActivity.class);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

	}

	public void SyncViewToData() {

		Cursor cursorUser = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_COURSE_FEE, null);

		try {
			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				int userCount = cursorUser.getCount();

				if (userCount > 0 && cursorUser.moveToFirst()) {

					do {

						int columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FlD_COURSE_ID);

						String CourseID = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_COURSE_NAME);
						String CourseName = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_COURSE_FEE);
						String CourseFee = cursorUser.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_TOTAL_RECVD_AMT);
						String CourseTotalRcvd = cursorUser
								.getString(columnidx);

						columnidx = cursorUser
								.getColumnIndex(DatabaseHelper.FLD_TOTAL_DUE);
						String CourseTotalAmt = cursorUser.getString(columnidx);

						// if (oddBall % 2 == 0) {
						ShowCourseData(CourseID, CourseName, CourseFee,
								CourseTotalRcvd, CourseTotalAmt, true);
						/*
						 * } else { ShowCourseData(CourseID, CourseName,
						 * CourseFee, CourseTotalRcvd, CourseTotalAmt, false);
						 * 
						 * }
						 */

					} while (cursorUser.moveToNext());

					syncFooter();

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
			String CourseFee, String TotalRcvd, String TotalDue, Boolean isOdd) {

		LayoutInflater factory = LayoutInflater.from(CurContext);

		TableLayout llFeeParent = (TableLayout) findViewById(R.id.tblFeeLayout);

		// //ROW

		try {
			View FeeSumaryRow = factory.inflate(R.layout.courserow, null);
			TableRow llFeeRow = (TableRow) FeeSumaryRow
					.findViewById(R.id.single_course_row);

			llFeeRow.setTag(CourseID);

			/*
			 * if (isOdd) { llFeeRow.setBackgroundColor(0xFFADD8E6); }else {
			 * llFeeRow.setBackgroundColor(0xFFFFFFFF); }
			 */

			TextView txtCourseName = (TextView) FeeSumaryRow
					.findViewById(R.id.txtCourseName);

			txtCourseName.setText(CourseNm);

			TextView txtCourseFee = (TextView) FeeSumaryRow
					.findViewById(R.id.txtCourseFee);

			txtCourseFee.setText(CourseFee);

			TextView txtCourseTotalRcvd = (TextView) FeeSumaryRow
					.findViewById(R.id.txtTotalRcvd);

			txtCourseTotalRcvd.setText(TotalRcvd);

			txtCourseTotalRcvd.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					View parentvu = (View) v.getParent();
					String SelectedCourseId = (String) parentvu.getTag();

					Intent receiptIntent = new Intent(CurContext, Receipt.class);
					receiptIntent.putExtra("CourseID", SelectedCourseId);

					startActivityForResult(receiptIntent, 2);

				}
			});

			txtTotalDue = (TextView) FeeSumaryRow
					.findViewById(R.id.txtTotalDue);

			txtTotalDue.setText(TotalDue);

			txtTotalDue.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					View parentvu = (View) v.getParent();
					String SelectedCourseId = (String) parentvu.getTag();

					int Due = 0;

					try {
						Due = Integer
								.parseInt(txtTotalDue.getText().toString());
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}

					if (Due > 0) {
						Intent receiptIntent = new Intent(CurContext,
								Installment.class);
						receiptIntent.putExtra("CourseID", SelectedCourseId);

						startActivityForResult(receiptIntent, 1);
					} else {
						Toast.makeText(CurContext,
								"No dues left for the selected course.",
								Toast.LENGTH_LONG).show();
					}

				}
			});

			// /ROW///

			llFeeParent.addView(llFeeRow);

			llFeeParent.requestLayout();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	public void cleanDB() {

		try {
			db.execSQL(DatabaseHelper.DROP_TBL_COURSE_FEE);
			db.execSQL(DatabaseHelper.DROP_TBL_INSTALLMENT);
			db.execSQL(DatabaseHelper.DROP_TBL_RECIEPT);

			db.execSQL(DatabaseHelper.CREATE_TBL_COURSE_FEE);
			db.execSQL(DatabaseHelper.CREATE_TBL_RECIEPT);
			db.execSQL(DatabaseHelper.CREATE_TBL_INSTALLMENT);

		} catch (Exception e) {

		}
	}

	private int get_Result_Details(String s_user_id) {
		SoapObject request = new SoapObject(
				CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
				CurContext.getString(R.string.FEE_METHOD_NAME));

		int Status = -99;

		PropertyInfo inf_email = new PropertyInfo();
		inf_email.setName("StudentCode");
		inf_email.setValue(s_user_id);

		request.addProperty(inf_email);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(
				CurContext.getString(R.string.SOAP_URL));

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.FEE_SOAP_ACTION), envelope);
		} catch (Exception e) {
			Status = -1;
			return Status;
		}

		try {
			cleanDB();

			SoapObject soapResult = (SoapObject) envelope.bodyIn;

			if (soapResult != null) {

				// Payment
				SoapObject RootObj = (SoapObject) soapResult.getProperty(0);
				SoapObject PaymentBlock = (SoapObject) RootObj.getProperty(0);

				if (PaymentBlock != null && PaymentBlock.getPropertyCount() > 0) {

					int CourseCount = PaymentBlock.getPropertyCount();

					for (int crsCnt = 0; crsCnt < CourseCount; crsCnt++) {

						// Course
						SoapObject courseBlock = (SoapObject) PaymentBlock
								.getProperty(crsCnt);

						if (courseBlock != null
								&& courseBlock.getPropertyCount() > 0) {

							String CourseID = (String) courseBlock
									.getAttributeSafelyAsString("cid");
							String CourseName = (String) courseBlock
									.getAttributeSafelyAsString("cName");
							String CourseFee = (String) courseBlock
									.getAttributeSafelyAsString("cFee");
							String CourseRecievedAmt = (String) courseBlock
									.getAttributeSafelyAsString("cReceived");
							String CourseDueAmt = (String) courseBlock
									.getAttributeSafelyAsString("cDue");

							if (!CourseID.trim().equals("")) {
								
								DBProcCourse(CourseID, CourseName, CourseFee,
										CourseRecievedAmt, CourseDueAmt);

							}

							// /////DUES//////////////////////////////////////////////
							SoapObject DuesBlock = (SoapObject) courseBlock
									.getProperty("dues");

							if (DuesBlock != null) {
								if (DuesBlock.getPropertyCount() > 0
										&& !CourseID.trim().equals("")) {
									DueSoapParsing(DuesBlock, CourseID);

								}
							}
							// //////////////////////////////////////////////////////

							// /////RECIEPTS//////////////////////////////////////////////

							SoapObject ReceiptsObject = (SoapObject) courseBlock
									.getProperty("receipts");
							if (ReceiptsObject != null) {
								if (ReceiptsObject.getPropertyCount() > 0
										&& !CourseID.trim().equals("")) {
									RecieptSoapParsing(ReceiptsObject, CourseID);
								}
							}

							// /////RECIEPTS//////////////////////////////////////////////

						}

					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Status;
	}

	public Double ParseFloatNull(String Value) {

		Double fval = 0.00;

		try {

			fval = Double.parseDouble(Value);

		} catch (Exception e) {
			e.printStackTrace();

			fval = 0.00;

		}

		return fval;

	}

	public int ParseIntNull(String Value) {
		int ival = 0;

		try {
			ival = Integer.parseInt(Value);
		} catch (Exception e) {
			e.printStackTrace();
			ival = 0;
		}

		return ival;
	}

	public long DBProcCourse(String CourseID, String CourseName,
			String CourseFee, String TotalRcvdAmt, String FldTotalAmt) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FlD_COURSE_ID,
					ParseIntNull(CourseID.trim()));
			values.put(DatabaseHelper.FLD_COURSE_NAME, CourseName.trim());

			values.put(DatabaseHelper.FLD_COURSE_FEE,
					Math.round(ParseFloatNull(CourseFee.trim())));

			values.put(DatabaseHelper.FLD_TOTAL_RECVD_AMT,
					Math.round(ParseFloatNull(TotalRcvdAmt.trim())));

			values.put(DatabaseHelper.FLD_TOTAL_DUE,
					Math.round(ParseFloatNull(FldTotalAmt.trim())));

			lreturn = db.insert(DatabaseHelper.TBL_USER_COURSE_FEE, null,
					values);

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	public void DueSoapParsing(SoapObject DuesBlock, String CourseID) {
		if (DuesBlock != null && DuesBlock.getPropertyCount() > 0) {
			int DuesCount = DuesBlock.getPropertyCount();

			for (int ducnt = 0; ducnt < DuesCount; ducnt++) {
				SoapObject Due = (SoapObject) DuesBlock.getProperty(ducnt);

				String AttribDueDt = (String) Due.getAttribute("dueDate");
				String AttribDueAmt = (String) Due.getAttribute("dueAmount");

				// /DB Call to Save Due
				DBProcDues(CourseID, AttribDueDt, AttribDueAmt);
			}
		}
	}

	public long DBProcDues(String CourseID, String DueDt, String DueAmt) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FlD_COURSE_ID,
					ParseIntNull(CourseID.trim()));
			values.put(DatabaseHelper.FLD_INSTALLMENT_DATE, DueDt);
			values.put(DatabaseHelper.FLD_INSTALLMENT_DUE_AMT,
					Math.round(ParseFloatNull(DueAmt.trim())));

			lreturn = db.insert(DatabaseHelper.TBL_USER_INSTALLMENT_DTLS, null,
					values);

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lreturn;
	}

	public void RecieptSoapParsing(SoapObject RecieptBlock, String CourseID) {
		if (RecieptBlock != null && RecieptBlock.getPropertyCount() > 0) {
			int RecieptCount = RecieptBlock.getPropertyCount();

			for (int rctcnt = 0; rctcnt < RecieptCount; rctcnt++) {
				SoapObject Receipt = (SoapObject) RecieptBlock
						.getProperty(rctcnt);

				String AttribMRDt = (String) Receipt.getAttribute("MRdate");
				String AttribMRNo = (String) Receipt.getAttribute("MRno");
				String AttribMRAmt = (String) Receipt.getAttribute("MRamount");

				// /DB Call to Save Due

				DBProcRecipts(CourseID, AttribMRDt, AttribMRNo, AttribMRAmt);

			}
		}
	}

	public long DBProcRecipts(String CourseID, String MRdate, String MRno,
			String MRamount) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FlD_COURSE_ID,
					ParseIntNull(CourseID.trim()));
			values.put(DatabaseHelper.FLD_RECIEPT_DATE, MRdate);
			values.put(DatabaseHelper.FLD_RECIEPT_MR, MRno.trim());
			values.put(DatabaseHelper.FLD_RECEIPT_FEES,
					Math.round(ParseFloatNull(MRamount.trim())));

			lreturn = db.insert(DatabaseHelper.TBL_USER_RECIEPT, null, values);

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lreturn;
	}

	Handler mProgressHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);

			AlertDialog.Builder dlgMsgbuilder = new AlertDialog.Builder(CurContext);

			switch (msg.what) {
			case 0:
				if (pgBusyWait.isShowing()) {
					pgBusyWait.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.information);
				dlgMsgbuilder.setTitle("Download status");
				dlgMsgbuilder
						.setMessage("Exam has been successfully downloaded to your device. To start "
								+ ActionType + " select Mock from home.");
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									startActivity(MainIntent);
									finish();
								} catch (SQLiteException sqle) {
									Toast.makeText(getApplicationContext(),
											"1 : " + sqle.getMessage(),
											Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(getApplicationContext(),
											"2 : " + e.getMessage(),
											Toast.LENGTH_SHORT).show();
								}
							}
						});
				AlertDialog altEndDownload = dlgMsgbuilder.create();

				altEndDownload.show();

				break;
			case -1:
				if (pgBusyWait.isShowing()) {
					pgBusyWait.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.error);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Application error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			case -2:
				if (pgBusyWait.isShowing()) {
					pgBusyWait.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.error);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Connection error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			case -3:
				if (pgBusyWait.isShowing()) {
					pgBusyWait.dismiss();
				}

				dlgMsgbuilder.setIcon(R.drawable.warning);
				dlgMsgbuilder.setTitle("Application status");
				dlgMsgbuilder.setMessage("Application error! Try again.");
				dlgMsgbuilder.setPositiveButton("Ok", null).create();
				dlgMsgbuilder.setCancelable(false);
				dlgMsgbuilder.show();

				break;
			}
		}
	};

	public class AsyncResultDetails extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pgBusyWait = new ProgressDialog(CurContext);
			pgBusyWait.setMessage("Please wait while downloading...");
			pgBusyWait.setIndeterminate(true);
			pgBusyWait.setCancelable(false);
			pgBusyWait.setCanceledOnTouchOutside(false);
			pgBusyWait.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String StudentstatusCode = StudentInfo.getStudentStatusCode();
			if (StudentstatusCode != null) {

				get_Result_Details(StudentstatusCode);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			SyncViewToData();

			if (pgBusyWait != null && pgBusyWait.isShowing()) {
				pgBusyWait.dismiss();
				pgBusyWait.cancel();
			}

			try {
				if (db != null) {
					db.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void syncFooter() {

		try {
			Double totalCourseFee = 0.0;

			// /Total Course Fee
			Cursor cursor = db.rawQuery("SELECT SUM( "
					+ DatabaseHelper.FLD_COURSE_FEE + " ) FROM "
					+ DatabaseHelper.TBL_USER_COURSE_FEE, null);
			if (cursor.moveToFirst()) {
				totalCourseFee = cursor.getDouble(0);
			}

			cursor.close();

			Double totalRecvdFee = 0.0;

			// /Total Received
			cursor = db.rawQuery("SELECT SUM( "
					+ DatabaseHelper.FLD_TOTAL_RECVD_AMT + " ) FROM "
					+ DatabaseHelper.TBL_USER_COURSE_FEE, null);
			if (cursor.moveToFirst()) {
				totalRecvdFee = cursor.getDouble(0);
			}

			cursor.close();

			Double totalDueFee = 0.0;

			// /Total Received
			cursor = db.rawQuery("SELECT SUM( " + DatabaseHelper.FLD_TOTAL_DUE
					+ " ) FROM " + DatabaseHelper.TBL_USER_COURSE_FEE, null);
			if (cursor.moveToFirst()) {
				totalDueFee = cursor.getDouble(0);
			}

			cursor.close();

			LayoutInflater factory = LayoutInflater.from(CurContext);

			// //ROW

			View FeeFooterRow = factory.inflate(R.layout.coursefooterrow, null);
			TableRow llFeeRow = (TableRow) FeeFooterRow
					.findViewById(R.id.single_footer_course_row);

			TextView txtTotalCourseFee = (TextView) FeeFooterRow
					.findViewById(R.id.txtFtrTotalCourseFee);

			txtTotalCourseFee
					.setText(Long.toString(Math.round(totalCourseFee)));

			TextView txtFooterTtlRcvd = (TextView) FeeFooterRow
					.findViewById(R.id.txtFooterTotalRcvd);

			txtFooterTtlRcvd.setText(Long.toString(Math.round(totalRecvdFee)));

			TextView txtFtrTtlDue = (TextView) FeeFooterRow
					.findViewById(R.id.txtFooterTotalDue);

			txtFtrTtlDue.setText(Long.toString(Math.round(totalDueFee)));

			TableLayout llFeeParent = (TableLayout) findViewById(R.id.tblFeeLayout);

			llFeeParent.addView(llFeeRow);

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}
