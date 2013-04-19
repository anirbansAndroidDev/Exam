package ica.exam;

import java.util.ArrayList;

import ica.ICAServiceHandler.ExamSyncService;
import ica.ProfileInfo.QuestionDetails;
import ica.ProfileInfo.StatusMessage;
import ica.ProfileInfo.StudentDetails;
import ica.exam.DatabaseHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExamResultActivity extends Activity {

	ExamSyncService examSyncService = null;
	ArrayList<QuestionDetails> lstQuestions = new ArrayList<QuestionDetails>();

	Context CurContext;
	StudentDetails studentDetails = null;

	LinearLayout llResultSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.resultmain);

		CurContext = this;

		examSyncService = new ExamSyncService(CurContext);

		StudentDetails.initInstance(CurContext);

		studentDetails = StudentDetails.getInstance();

		String facultyCode = studentDetails.getStudentID();

		if (facultyCode != null) {
			setTitle("Result List- [" + studentDetails.getStudentFname() + " "
					+ studentDetails.getStudentLname() + "]");
		}

		llResultSet = (LinearLayout) findViewById(R.id.resultHolder);

		new AsyncFetchQuestion().execute("");

	}

	String ChapterID;
	String BatchID;
	String SessionNo;

	public class AsyncFetchQuestion extends
			AsyncTask<String, Void, StatusMessage> {

		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pd = new ProgressDialog(CurContext);
			pd.setMessage("Downloading exam result details ....");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);

			pd.show();

		}

		@Override
		protected StatusMessage doInBackground(String... params) {

			StatusMessage info = examSyncService
					.SyncServerToDBCheckedQuestion(studentDetails);

			lstQuestions = examSyncService
					.getAllCheckedQuestion(studentDetails);
			return info;
		}

		@Override
		protected void onPostExecute(StatusMessage result) {
			super.onPostExecute(result);

			llResultSet.removeAllViews();

			if (lstQuestions != null && lstQuestions.size() > 0) {

				int idx = 1;
				for (QuestionDetails item : lstQuestions) {
					SyncViewToDb(item, idx);
					idx++;
				}
			}

			pd.dismiss();
		}

		public void SyncViewToDb(QuestionDetails qItem, int idx) {

			LayoutInflater inflator = ((ExamResultActivity) CurContext)
					.getLayoutInflater();
			View view = inflator.inflate(R.layout.resultrow, null);

			TextView qtext = (TextView) view.findViewById(R.id.txtQtext);
			qtext.setText(qItem.getText());

			TextView txtIdx = (TextView) view.findViewById(R.id.txtIdx);
			txtIdx.setText(Integer.toString(idx) + ".");
			TextView qRightPercentage = (TextView) view
					.findViewById(R.id.txtRightPercent);
			qRightPercentage.setText(Integer.toString(qItem
					.getRightPercentage()));

			View vuInfo = (View) qRightPercentage.getParent();
			vuInfo.setTag(qItem);

			qRightPercentage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					ViewGroup vuGrp = (ViewGroup) v.getParent();

					QuestionDetails qItem = (QuestionDetails) vuGrp.getTag();
					ExamApperanceDetails(false, qItem);
				}
			});

			TextView qWrongPercentage = (TextView) view
					.findViewById(R.id.txtWrongPercent);
			qWrongPercentage.setText(Integer.toString(qItem
					.getWrongPercentage()));
			qWrongPercentage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ViewGroup vuGrp = (ViewGroup) v.getParent();
					QuestionDetails qItem = (QuestionDetails) vuGrp.getTag();

					ExamApperanceDetails(true, qItem);
				}
			});

			llResultSet.addView(view);

		}

	}

	public void ExamApperanceDetails(Boolean isWrong, QuestionDetails qItem) {

		setStudentIntent(qItem.getID(), isWrong);

	}

	public void setStudentIntent(String QuestionID, Boolean isWrong) {

		Intent intent = new Intent(this, ExamStudentDetails.class);
		intent.putExtra(DatabaseHelper.FLD_QUESTION_ID, QuestionID);

		if (isWrong) {
			// Wrong Percentage
			intent.putExtra("isWrong", "T");
		} else {
			// Right Percentage
			intent.putExtra("isWrong", "F");
		}

		startActivityForResult(intent, 0);
	}
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, MainMenuInTabView.class);
		startActivity(i);
	}

}
