package ica.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarDateUtiliy {

	public static Calendar stringToCalendar(String striDate) {

	//	striDate = "28/04/2012";
		SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy");
		java.util.Date d1 = null;
		Calendar mCalendar;

		try {
			d1 = form.parse(striDate);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		mCalendar = Calendar.getInstance();
	
		mCalendar.setTime(d1);		

		return mCalendar;
	}
}
