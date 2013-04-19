package ica.exam;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.GraphicalView;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ica.ChartView.BarChart;
import ica.ProfileInfo.*;
import ica.Utility.DeviceInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SubjectVsMarks extends Activity {

	private SubjectVsMarks CurContext;
	private TextView txtChartHeader;
	private GraphicalView BarChartView;
	private SQLiteDatabase db;

	StudentDetails StudentInfo = null;
	private AsyncExamDetails AsyncExamDownloder;

	private ProgressDialog pgBusyWait;

	List<ExamDetail> lstSubjectExamDetails = new ArrayList<ExamDetail>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.subjectvsmarks);
		CurContext = this;

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		StudentDetails.initInstance(CurContext);

		StudentInfo = StudentDetails.getInstance();

		try {
			String sEmail = StudentInfo.getStudentID();
			if (sEmail != null) {
				setTitle("Student Progress- [" + sEmail + "]");
				if (StudentInfo.getStudentID() != null) {

					if (DeviceInfo.haveNetworkConnection(CurContext)) {
						AsyncExamDownloder = new AsyncExamDetails();
						AsyncExamDownloder.execute(StudentInfo.getStudentID());
					} else {
						SyncViewToData();
					}

				}
			}

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public long DBProcExamDtls(ExamDetail ExamDtls) {
		long lreturn = 0;

		try {
			ContentValues values = new ContentValues();

			values.put(DatabaseHelper.FLD_EXAM_SUBJECT_ID,
					ExamDtls.getSubjectID());
			values.put(DatabaseHelper.FLD_EXAM_SUBJECT_NAME,
					ExamDtls.getSubjectName());
			values.put(DatabaseHelper.FLD_EXAM_SUBJECT_RESULT,
					ExamDtls.getSubjectMarks());

			lreturn = db.insert(DatabaseHelper.TBL_USER_EXAM_RESULT, null,
					values);

		} catch (SQLiteException sqle) {
			sqle.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lreturn;
	}

	public void SyncViewToData() {

		Cursor cursorResults = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_USER_EXAM_RESULT, null);

		lstSubjectExamDetails.clear();
		try {
			startManagingCursor(cursorResults);

			if (cursorResults != null) {
				int userCount = cursorResults.getCount();

				if (userCount > 0 && cursorResults.moveToFirst()) {

					do {

						int columnidx = cursorResults
								.getColumnIndex(DatabaseHelper.FLD_EXAM_SUBJECT_ID);

						String SubjectID = cursorResults.getString(columnidx);

						columnidx = cursorResults
								.getColumnIndex(DatabaseHelper.FLD_EXAM_SUBJECT_NAME);
						String SubjectName = cursorResults.getString(columnidx);

						columnidx = cursorResults
								.getColumnIndex(DatabaseHelper.FLD_EXAM_SUBJECT_RESULT);
						String SubjectAvgMarks = cursorResults
								.getString(columnidx);

						ExamDetail examDtls = new ExamDetail();

						examDtls.setSubjectID(ParseIntNull(SubjectID));
						examDtls.setSubjectName(SubjectName);
						examDtls.setSubjectMarks(ParseFloatNull(SubjectAvgMarks));

						if (examDtls.getSubjectID() > 0) {
							lstSubjectExamDetails.add(examDtls);
						}

					} while (cursorResults.moveToNext());
				}

			}

		} catch (SQLiteException sqle) {
			if (cursorResults != null) {
				try {
					cursorResults.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorResults != null) {
				try {
					cursorResults.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorResults != null) {
				try {
					cursorResults.close();

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

		PlotSubjectVsResult();
	}

	public class AsyncExamDetails extends AsyncTask<String, Void, Boolean> {
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
			String StudentID = StudentInfo.getStudentID();
			if (StudentID != null) {

				get_Result_Details(StudentID);
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

	public void PlotSubjectVsResult() {

		txtChartHeader = (TextView) findViewById(R.id.txtChartHeader);

		double[] SubjectID = null;
		double[] ResultSet = null;

		List<String> lstsubjectName = new ArrayList<String>();

		if (lstSubjectExamDetails != null && lstSubjectExamDetails.size() > 0) {

			SubjectID = null;
			ResultSet = null;

			SubjectID = new double[lstSubjectExamDetails.size()];
			ResultSet = new double[lstSubjectExamDetails.size()];

			int i = 1;

			for (ExamDetail exam : lstSubjectExamDetails) {

				SubjectID[i - 1] = exam.getSubjectID();
				ResultSet[i - 1] = exam.getSubjectMarks();
				lstsubjectName.add(exam.getSubjectName());

				i = i + 1;
			}

			txtChartHeader.setText("");
		} else {

			ResultSet = new double[] { 0, 0, 0 };
			txtChartHeader.setText("No data available.");

		}
		// // === UI === //////////////////////
		BarChart StackedBarChart = new BarChart();
		// chart.getView(this, getResults
		// setContentView(StackedBarChart.getView(CurContext));

		LinearLayout llChartHolder = (LinearLayout) findViewById(R.id.llChartHolderSubjectVsMarks);

		// String[] titles = new String[] { "2008", "2007" };
		// int[] colors = new int[] { Color.BLUE, Color.CYAN };

		// These Must be of the same size

		String[] titles = new String[] { "Average Marks Obtained" };
		int[] colors = new int[] { Color.BLUE };

		BarChartView = StackedBarChart.getView(CurContext, titles, colors,
				ResultSet, lstsubjectName);

		// BarChartView.setBackgroundColor(Color.TRANSPARENT);

		llChartHolder.addView(BarChartView);

		BarChartView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// SeriesSelection seriesSelection = BarChartView
				// .getCurrentSeriesAndPoint();
				// double[] xy = BarChartView.toRealPoint(0);
				// if (seriesSelection == null) {
				// Toast.makeText(CurContext, "No chart element was clicked",
				// Toast.LENGTH_SHORT).show();
				// } else {
				//
				// txtChartHeader.setText("Chart element in series index "
				// + seriesSelection.getSeriesIndex()
				// + " data point index "
				// + seriesSelection.getPointIndex() + " was clicked"
				// + " closest point value X="
				// + seriesSelection.getXValue() + ", Y="
				// + seriesSelection.getValue()
				// + " clicked point value X=" + (float) xy[0]
				// + ", Y=" + (float) xy[1]);

				/*
				 * Toast.makeText( CurContext, "Chart element in series index "
				 * + seriesSelection.getSeriesIndex() + " data point index " +
				 * seriesSelection.getPointIndex() + " was clicked" +
				 * " closest point value X=" + seriesSelection.getXValue() +
				 * ", Y=" + seriesSelection.getValue() +
				 * " clicked point value X=" + (float) xy[0] + ", Y=" + (float)
				 * xy[1], 5000).show();
				 */
				// }

			}
		});

	}

	public void cleanDB() {

		try {
			db.execSQL(DatabaseHelper.DROP_TBL_EXAM_RESULT);
			db.execSQL(DatabaseHelper.CREATE_TBL_EXAM_RESULT);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private int get_Result_Details(String s_user_id) {
		SoapObject request = new SoapObject(
				CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
				CurContext.getString(R.string.RESULT_METHOD_NAME));

		int Status = -99;

		PropertyInfo inf_email = new PropertyInfo();
		inf_email.setName("EmailID");
		inf_email.setValue(s_user_id);
		request.addProperty(inf_email);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE androidHttpTransport = new HttpTransportSE(
				CurContext.getString(R.string.SOAP_URL));

		try {
			androidHttpTransport
					.call(CurContext.getString(R.string.RESULT_SOAP_ACTION),
							envelope);
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
				SoapObject StudentBlock = (SoapObject) RootObj.getProperty(0);

				if (StudentBlock != null && StudentBlock.getPropertyCount() > 0) {

					int SubjectCount = StudentBlock.getPropertyCount();

					if (SubjectCount > 0) {
						for (int SubCnt = 0; SubCnt < SubjectCount; SubCnt++) {
							SoapObject SubjectBlock = (SoapObject) StudentBlock
									.getProperty(SubCnt);

							if (SubjectBlock != null) {

								String attribSubjectName = SubjectBlock
										.getAttributeAsString("Subject");
								String attribSubjectID = SubjectBlock
										.getAttributeAsString("SubjectID");
								String attribSubjectMarks = SubjectBlock
										.getAttributeAsString("Marks");

								ExamDetail examDtl = new ExamDetail();
								examDtl.setSubjectName(attribSubjectName);
								examDtl.setSubjectID(ParseIntNull(attribSubjectID));
								examDtl.setSubjectMarks(Math
										.round(ParseFloatNull(attribSubjectMarks)));

								if (examDtl.getSubjectID() > 0) {
									
									DBProcExamDtls(examDtl);

								}

							}
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			ival = 0;
		}

		return ival;
	}

}
