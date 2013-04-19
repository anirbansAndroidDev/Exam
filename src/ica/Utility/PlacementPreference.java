package ica.Utility;

import java.util.Calendar;

import com.placement.PlacementSelectorActivity.MonthItems;

import android.content.Context;
import android.content.SharedPreferences;

public class PlacementPreference {

	public static final String PLACEMENT_PREF = "PLACEMENT_PREF";
	public static final String PLACEMENT_COUNT_AllIndia = "PLACEMENT_COUNT_AllIndia";
	public static final String PLACEMENT_COUNT_DAY = "PLACEMENT_COUNT_DAY";
	public static final String PLACEMENT_COUNT_MONTH = "PLACEMENT_COUNT_MONTH";

	public static final String PLACEMENT_DAYS_COUNT = "PLACEMENT_DAYS_COUNT";
	public static final String PLACEMENT_MONTH = "PLACEMENT_MONTH";
	public static final String PLACEMENT_MONTH_YEAR = "PLACEMENT_MONTH_YEAR";

	public static int getPlacementCountAllIndia(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		int prefPlacedCount = myPrefs.getInt(PLACEMENT_COUNT_AllIndia, 10);

		return prefPlacedCount;
	}

	public static void setPlacementCountAllIndia(Context context, int Count) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(PLACEMENT_COUNT_AllIndia, Count);

		prefsEditor.commit();

	}

	public static int getPlacementCountMonth(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		int prefPlacedCount = myPrefs.getInt(PLACEMENT_COUNT_MONTH, 10);

		return prefPlacedCount;
	}

	public static void setPlacementCountMonth(Context context, int Count) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(PLACEMENT_COUNT_MONTH, Count);

		prefsEditor.commit();

	}

	public static int getPlacementCountDay(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		int prefPlacedCount = myPrefs.getInt(PLACEMENT_COUNT_DAY, 10);

		return prefPlacedCount;
	}

	public static void setPlacementCountDay(Context context, int Count) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(PLACEMENT_COUNT_DAY, Count);

		prefsEditor.commit();

	}

	public static MonthItems getPlacementMonth(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		String prefPlacedMonth = myPrefs.getString(PLACEMENT_MONTH,
				MonthItems.June.toString());

		MonthItems monthItem = MonthItems.valueOf(MonthItems.class,
				prefPlacedMonth);
		return monthItem;
	}

	public static void setPlacementMonth(Context context, MonthItems monthItems) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putString(PLACEMENT_MONTH, monthItems.toString());

		prefsEditor.commit();

	}

	public static int getPlacementMonthYr(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		int prefPlacedYr = myPrefs.getInt(PLACEMENT_MONTH_YEAR, Calendar
				.getInstance().get(Calendar.YEAR));

		return prefPlacedYr;
	}

	public static void setPlacementMonthYr(Context context, int MonthYear) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(PLACEMENT_MONTH_YEAR, MonthYear);

		prefsEditor.commit();

	}

	public static int getPlacementDaysCount(Context context) {

		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		int prefPlacedCount = myPrefs.getInt(PLACEMENT_DAYS_COUNT, 0);

		return prefPlacedCount;
	}

	public static void setPlacementDaysCount(Context context, int Count) {
		SharedPreferences myPrefs = context.getSharedPreferences(
				PLACEMENT_PREF, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putInt(PLACEMENT_DAYS_COUNT, Count);

		prefsEditor.commit();

	}

}
