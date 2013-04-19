
package com.examples.android.calendar;

import ica.exam.R;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CalendarAdapter extends BaseAdapter {
	static final int FIRST_DAY_OF_WEEK = 0; // Sunday = 0, Monday = 1

	private Context mContext;

	private java.util.Calendar month;
	private Calendar selectedDate;
	private ArrayList<String> items;

	Calendar today;

	public CalendarAdapter(Context c, Calendar monthCalendar) {
		month = monthCalendar;
		selectedDate = (Calendar) monthCalendar.clone();
		mContext = c;
		month.set(Calendar.DAY_OF_MONTH, 1);

		today = Calendar.getInstance();

		this.items = new ArrayList<String>();
		refreshDays();
	}

	
	public void setToday(Calendar curCal) {
		today=curCal;
	}
	public void setItems(ArrayList<String> items) {
		this.items = items;
	}

	public int getCount() {
		return days.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new view for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		TextView dayView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.calendar_item, null);

		}
		dayView = (TextView) v.findViewById(R.id.calendarDate);
				
		// disable empty days from the beginning
		if (days[position].equals("")) {
			v.setBackgroundResource(R.drawable.list_item_background);
			dayView.setClickable(false);
			dayView.setFocusable(false);
		} else {
			// mark current day as focused

			if (month.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
					&& month.get(Calendar.MONTH) == selectedDate
							.get(Calendar.MONTH)
					&& days[position].equals(""
							+ selectedDate.get(Calendar.DAY_OF_MONTH))) {
				
								
				if (month.get(Calendar.YEAR) == today.get(Calendar.YEAR)
						&& month.get(Calendar.MONTH) == today
								.get(Calendar.MONTH)
						&& days[position].equals(""
								+ today.get(Calendar.DAY_OF_MONTH))) {
					v.setBackgroundResource(R.drawable.typeb_calendar_today);
				}
			} else {
				v.setBackgroundResource(R.drawable.list_item_background);
				
				if (month.get(Calendar.YEAR) == today.get(Calendar.YEAR)
						&& month.get(Calendar.MONTH) == today
								.get(Calendar.MONTH)
						&& days[position].equals(""
								+ today.get(Calendar.DAY_OF_MONTH))) {
					v.setBackgroundResource(R.drawable.typeb_calendar_today);
				}
			}
		}
		dayView.setText(days[position]);

		// create date string for comparison
		String date = days[position];

		if (date.length() == 1) {
			date = "0" + date;
		}
		String monthStr = "" + (month.get(Calendar.MONTH) + 1);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}

		// show icon if date is not empty and it exists in the items array
		ImageView iw = (ImageView) v.findViewById(R.id.calendarDate_icon);
		if (date.length() > 0 && items != null && items.contains(date)) {
			iw.setVisibility(View.VISIBLE);
		} else {
			iw.setVisibility(View.INVISIBLE);
		}
		return v;
	}

	public void refreshDays() {
		// clear items
		items.clear();

		int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
		int firstDay = (int) month.get(Calendar.DAY_OF_WEEK);

		// figure size of the array
		if (firstDay == 1) {
			days = new String[lastDay + (FIRST_DAY_OF_WEEK * 6)];
		} else {
			days = new String[lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1)];
		}

		int j = FIRST_DAY_OF_WEEK;

		// populate empty days before first real day
		if (firstDay > 1) {
			for (j = 0; j < firstDay - FIRST_DAY_OF_WEEK; j++) {
				days[j] = "";
			}
		} else {
			for (j = 0; j < FIRST_DAY_OF_WEEK * 6; j++) {
				days[j] = "";
			}
			j = FIRST_DAY_OF_WEEK * 6 + 1; // sunday => 1, monday => 7
		}

		// populate days
		int dayNumber = 1;
		for (int i = j - 1; i < days.length; i++) {
			days[i] = "" + dayNumber;
			dayNumber++;
		}
	}

	// references to our items
	public String[] days;
}