package ica.ProfileInfo;

import ica.exam.DatabaseHelper;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class StudentDetails extends Activity {

	public String getStudentID() {
		return StudentID;
	}

	public void setStudentID(String studentID) {
		StudentID = studentID;
	}

	public String getStudentPWD() {
		return StudentPWD;
	}

	public void setStudentPWD(String studentPWD) {
		StudentPWD = studentPWD;
	}

	public String getStudentStatusCode() {
		return StudentStatusCode;
	}

	public void setStudentStatusCode(String studentStatusCode) {
		StudentStatusCode = studentStatusCode;
	}

	public String getStudentMobile() {
		return StudentMobile;
	}

	public void setStudentMobile(String studentMobile) {
		StudentMobile = studentMobile;
	}

	public String getStudentFname() {
		return StudentFname;
	}

	public void setStudentFname(String studentFname) {
		StudentFname = studentFname;
	}

	public String getStudentLname() {
		return StudentLname;
	}

	public void setStudentLname(String studentLname) {
		StudentLname = studentLname;
	}

	public String getStudentImgPath() {
		return StudentImgPath;
	}

	public void setStudentImgPath(String studentImgPath) {
		StudentImgPath = studentImgPath;
	}

	SQLiteDatabase db;

	String StudentID;
	String StudentPWD;
	String StudentStatusCode;
	String StudentMobile;
	String StudentFname;
	String StudentLname;
	String StudentImgPath;

	private StudentDetails getStudentDetails(Context curContext) {

		db = (new DatabaseHelper(curContext)).getWritableDatabase();

		Cursor cursorUser = null;

		try {

			cursorUser = db.rawQuery(
					"select * from " + DatabaseHelper.TBL_USER, null);

			startManagingCursor(cursorUser);

			if (cursorUser != null) {
				if (cursorUser.moveToFirst()) {

					int columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_ID_USER);
					this.StudentID = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_PWD);
					this.StudentPWD = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_STUDENT_CODE);
					this.StudentStatusCode = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_STUDENT_MOBILE);
					this.StudentMobile = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_STUDENT_FIRST_NM);
					this.StudentFname = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_STUDENT_LAST_NM);
					this.StudentLname = cursorUser.getString(columnidx);

					columnidx = cursorUser
							.getColumnIndex(DatabaseHelper.FLD_STUDENT_IMG_PATH);
					this.StudentImgPath = cursorUser.getString(columnidx);

				}
			}

			cursorUser.close();
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

		return this;
	}

	private static StudentDetails curStudentInstance;

	public static void initInstance(Context curContext) {
		if (curStudentInstance == null) {
			// Create the instance
			curStudentInstance = new StudentDetails();
			curStudentInstance = curStudentInstance
					.getStudentDetails(curContext);
		}
	}
	
	public static void refreshSource() {
		curStudentInstance=null;
	}

	public static StudentDetails getInstance() {
		// Return the instance
		return curStudentInstance;
	}

	private StudentDetails() {
		// Constructor hidden because this is a singleton
	}

}
