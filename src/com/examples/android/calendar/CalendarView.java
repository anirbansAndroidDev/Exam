package com.examples.android.calendar;

import ica.exam.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class CalendarView extends Activity {

	public Calendar month;
	public CalendarAdapter calendarAdapter;
	public Handler calendarHandler;
	public ArrayList<String> calendarItems; // container to store some random
											// calendar

	// items

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);
		month = Calendar.getInstance();
		onNewIntent(getIntent());

		calendarItems = new ArrayList<String>();
		calendarAdapter = new CalendarAdapter(this, month);
		calendarAdapter.setToday(Calendar.getInstance());
		GridView gridview = (GridView) findViewById(R.id.calendarGridview);
		gridview.setAdapter(calendarAdapter);

		calendarHandler = new Handler();
		calendarHandler.post(calendarUpdater);

		TextView title = (TextView) findViewById(R.id.calendarTitle);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

		TextView previous = (TextView) findViewById(R.id.calendarPrevious);
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (month.get(Calendar.MONTH) == month
						.getActualMinimum(Calendar.MONTH)) {
					month.set((month.get(Calendar.YEAR) - 1),
							month.getActualMaximum(Calendar.MONTH), 1);
				} else {
					month.set(Calendar.MONTH, month.get(Calendar.MONTH) - 1);
				}
				refreshCalendar();
			}
		});

		TextView next = (TextView) findViewById(R.id.calendarNext);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (month.get(Calendar.MONTH) == month
						.getActualMaximum(Calendar.MONTH)) {
					month.set((month.get(Calendar.YEAR) + 1),
							month.getActualMinimum(Calendar.MONTH), 1);
				} else {
					month.set(Calendar.MONTH, month.get(Calendar.MONTH) + 1);
				}
				refreshCalendar();

			}
		});

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView date = (TextView) v.findViewById(R.id.calendarDate);
				if (date instanceof TextView && !date.getText().equals("")) {

					Intent intent = new Intent();
					String day = date.getText().toString();
					if (day.length() == 1) {
						day = "0" + day;
					}
					// return chosen date as string format
					intent.putExtra(
							"date",
							android.text.format.DateFormat.format("yyyy-MM",
									month) + "-" + day);
					setResult(RESULT_OK, intent);
					finish();
				}

			}
		});
	}

	public void refreshCalendar() {
		TextView title = (TextView) findViewById(R.id.calendarTitle);

		calendarAdapter.setToday(Calendar.getInstance());
		calendarAdapter.refreshDays();
		calendarAdapter.notifyDataSetChanged();
		calendarHandler.post(calendarUpdater); // generate some random calendar
												// items

		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
	}

	public void onNewIntent(Intent intent) {
		String date = intent.getStringExtra("date");
		String[] dateArr = date.split("-"); // date format is yyyy-mm-dd
		month.set(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]),
				Integer.parseInt(dateArr[2]));
	}

	public class AsyncFetchScheduleDates extends
			AsyncTask<Calendar, Void, Void> {
		@Override
		protected void onPreExecute() {			
			super.onPreExecute();
			
			
		}

		@Override
		protected Void doInBackground(Calendar... params) {
						
			calendarItems.clear();
			// format random values. You can implement a dedicated class to
			// provide real values
			for (int i = 0; i < 31; i++) {
				Random r = new Random();

				if (r.nextInt(10) > 6) {
					calendarItems.add(Integer.toString(i));
				}
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {			
			super.onPostExecute(result);
			
			calendarAdapter.setToday(Calendar.getInstance());
			calendarAdapter.setItems(calendarItems);
			calendarAdapter.notifyDataSetChanged();			
		}
	}

	public Runnable calendarUpdater = new Runnable() {

		@Override
		public void run() {
			calendarItems.clear();
			// format random values. You can implement a dedicated class to
			// provide real values
			for (int i = 0; i < 31; i++) {
				Random r = new Random();

				if (r.nextInt(10) > 6) {
					calendarItems.add(Integer.toString(i));
				}
			}
			calendarAdapter.setToday(Calendar.getInstance());
			calendarAdapter.setItems(calendarItems);
			calendarAdapter.notifyDataSetChanged();
		}
	};
}
