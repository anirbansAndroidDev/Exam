package ica.ICAServiceHandler;

import ica.ICAConstants.UploadTask;
import ica.ProfileInfo.ScheduleInfo;
import ica.ProfileInfo.StudentDetails;
import ica.ProfileInfo.TaskStatusMsg;
import ica.exam.DatabaseHelper;
import ica.exam.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import static ica.ICAConstants.ICAStaticConstants.BLANK_STRING;

public class ScheduleDataHandler extends Activity {
	private SQLiteDatabase db;

	Context CurContext;

	public ScheduleDataHandler(Context context) {
		CurContext = context;
	}

	public TaskStatusMsg UploadStudentSchedule(ScheduleInfo scheduleInfo,
			StudentDetails studentDetails) {
		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ScheduleList);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();
			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.SCHEDULE_UPLOAD_METHOD_NAME));

			PropertyInfo inf_eventid = new PropertyInfo();
			inf_eventid.setName("EventId");

			if (scheduleInfo.getScheduleID() <= 0) {
				inf_eventid.setValue(BLANK_STRING);
			} else {
				inf_eventid.setValue(Integer.toString(scheduleInfo
						.getScheduleID()));
			}
			request.addProperty(inf_eventid);

			PropertyInfo inf_studentcode = new PropertyInfo();
			inf_studentcode.setName("StudentCode");
			inf_studentcode.setValue(studentDetails.getStudentStatusCode());
			request.addProperty(inf_studentcode);

			PropertyInfo inf_StudentEmail = new PropertyInfo();
			inf_StudentEmail.setName("StudentEmail");
			inf_StudentEmail.setValue(studentDetails.getStudentID());
			request.addProperty(inf_StudentEmail);

			PropertyInfo inf_Year = new PropertyInfo();
			inf_Year.setName("Year");
			inf_Year.setValue(scheduleInfo.getYear());
			request.addProperty(inf_Year);

			PropertyInfo inf_Month = new PropertyInfo();
			inf_Month.setName("Month");
			inf_Month.setValue(scheduleInfo.getMonth());
			request.addProperty(inf_Month);

			PropertyInfo inf_Day = new PropertyInfo();
			inf_Day.setName("Day");
			inf_Day.setValue(scheduleInfo.getDayOfMonth());
			request.addProperty(inf_Day);

			PropertyInfo inf_Synched = new PropertyInfo();
			inf_Synched.setName("Synched");
			inf_Synched.setValue("Y");
			request.addProperty(inf_Synched);

			PropertyInfo inf_Event = new PropertyInfo();
			inf_Event.setName("Event");
			inf_Event.setValue(scheduleInfo.getMessage());
			request.addProperty(inf_Event);

			PropertyInfo inf_EventByType = new PropertyInfo();
			inf_EventByType.setName("EventByType");
			inf_EventByType.setValue(scheduleInfo.getNotificationType());
			request.addProperty(inf_EventByType);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.SCHEDULE_UPLOAD_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			if (soapResult != null) {

				String soapBlock = (String) soapResult
						.getPrimitivePropertyAsString("StoreEventResult");

				if (soapBlock.compareToIgnoreCase("true") == 0) {
					info.setStatus(0);
					info.setMessage("Schedule upload successful");
				} else {
					info.setStatus(-3);
					info.setMessage("Schedule upload not successful");
				}

			} else {
				info.setStatus(-3);
				info.setMessage("Data Exception");
			}
		} catch (Exception e) {
			info.setStatus(-3);
			info.setMessage("Data Exception" + e.toString());
		}

		return info;
	}

	public TaskStatusMsg SyncDBToServer(StudentDetails studentDetails) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ScheduleList);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			if (studentDetails != null) {

				Cursor cursorMonthlySchedule = db.rawQuery(
						"select * from " + DatabaseHelper.TBL_STUDENT_SCHEDULE
								+ " where " + DatabaseHelper.FLD_ID_USER + "='"
								+ studentDetails.getStudentID() + "' AND "
								+ DatabaseHelper.FLD_SCHEDULE_IS_SYNCED + "='"
								+ "N" + "'", null);
				cursorMonthlySchedule.moveToFirst();
				if (cursorMonthlySchedule != null
						&& cursorMonthlySchedule.getCount() > 0) {

					do {
						int columnidx = cursorMonthlySchedule
								.getColumnIndex(DatabaseHelper.FLD_ID_USER);

						String shdlUserId = cursorMonthlySchedule
								.getString(columnidx);

						columnidx = cursorMonthlySchedule
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_IS_SYNCED);

						String issynced = cursorMonthlySchedule
								.getString(columnidx);

						if ("N".equals(issynced)) {

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_ID);

							int curID = cursorMonthlySchedule.getInt(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_ID_USER);

							shdlUserId = cursorMonthlySchedule
									.getString(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_YEAR);

							int yr = cursorMonthlySchedule.getInt(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_MONTH);

							int mth = cursorMonthlySchedule.getInt(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH);

							int dom = cursorMonthlySchedule.getInt(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_NOTIFICATION_TYPE);

							String notifntype = cursorMonthlySchedule
									.getString(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_COMPLETED);

							String iscmpleted = cursorMonthlySchedule
									.getString(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_IS_SYNCED);

							issynced = cursorMonthlySchedule
									.getString(columnidx);

							columnidx = cursorMonthlySchedule
									.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_MESSAGE);

							String msg = cursorMonthlySchedule
									.getString(columnidx);

							ScheduleInfo newSchedule = new ScheduleInfo();
							newSchedule.setScheduleID(curID);
							newSchedule.setUserName(shdlUserId);
							newSchedule.setYear(yr);
							newSchedule.setMonth(mth);
							newSchedule.setDayOfMonth(dom);
							newSchedule.setNotificationType(notifntype);
							newSchedule.setCompletionStatus(iscmpleted);
							newSchedule.setIsSynced(issynced);
							newSchedule.setMessage(msg);

							info = UploadStudentSchedule(newSchedule,
									studentDetails);

							if (info.getStatus() == 0) {
								// /Delete data

								columnidx = cursorMonthlySchedule
										.getColumnIndex(DatabaseHelper.FLD_ROWID);

								int scheduleUniqueId = cursorMonthlySchedule
										.getInt(columnidx);

								db.execSQL("DELETE FROM "
										+ DatabaseHelper.TBL_STUDENT_SCHEDULE
										+ " WHERE " + DatabaseHelper.FLD_ROWID
										+ " = " + scheduleUniqueId);
							}
						} else {
							info.setTaskDone(UploadTask.ScheduleList);
							info.setTitle("Sync Status");
							info.setMessage("No event required to be synced.");
							info.setStatus(0);
						}

					} while (cursorMonthlySchedule.moveToNext());

				} else {
					info.setTaskDone(UploadTask.ScheduleList);
					info.setTitle("Sync Status");
					info.setMessage("No event required to be synced.");
					info.setStatus(0);
				}
			} else {
				info.setTaskDone(UploadTask.ScheduleList);
				info.setTitle("Sync Status");
				info.setMessage("No student information available.Please login and perform sync.");
				info.setStatus(-3);
			}

		} catch (Exception e) {
			info.setTaskDone(UploadTask.ScheduleList);
			info.setTitle("Sync Status");
			info.setMessage("Data Exception" + e.toString());
			info.setStatus(-3);
		}

		return info;
	}

	public TaskStatusMsg SyncServerToDB(StudentDetails studentDetails) {

		TaskStatusMsg info = new TaskStatusMsg();
		info.setTaskDone(UploadTask.ScheduleList);
		info.setTitle("Sync Status");
		info.setMessage("Service Error!");
		info.setStatus(-1);

		SoapObject soapResult = null;
		SoapSerializationEnvelope envelope = null;
		HttpTransportSE androidHttpTransport = null;

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();
			SoapObject request = new SoapObject(
					CurContext.getString(R.string.WEBSERVICE_NAMESPACE),
					CurContext.getString(R.string.SCHEDULE_LIST_METHOD_NAME));

			PropertyInfo inf_studentcode = new PropertyInfo();
			inf_studentcode.setName("StudentCode");
			inf_studentcode.setValue(studentDetails.getStudentStatusCode());
			request.addProperty(inf_studentcode);

			PropertyInfo inf_email = new PropertyInfo();
			inf_email.setName("StudentEmail");
			inf_email.setValue(studentDetails.getStudentID());
			request.addProperty(inf_email);

			envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			androidHttpTransport = new HttpTransportSE(
					CurContext.getString(R.string.SOAP_URL));
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			androidHttpTransport.call(
					CurContext.getString(R.string.SCHEDULE_LIST_SOAP_ACTION),
					envelope);
		} catch (Exception e) {

			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			soapResult = (SoapObject) envelope.bodyIn;
		} catch (Exception e) {
			info.setStatus(-1);
			info.setMessage("Connection error! Please check the connection and try it again.");

			return info;
		}

		try {
			if (soapResult != null) {

				SoapObject soapBlock = (SoapObject) soapResult.getProperty(0);
				SoapObject rootBlock = (SoapObject) soapBlock.getProperty(0);

				if (rootBlock != null && rootBlock.getPropertyCount() > 0) {
					int ScheduleCount = rootBlock.getPropertyCount();

					cleanDB();
					for (int cnt = 0; cnt < ScheduleCount; cnt++) {

						SoapObject scheduleSoap = (SoapObject) rootBlock
								.getProperty(cnt);

						if (scheduleSoap != null
								&& scheduleSoap.getAttributeCount() > 0) {

							int evtid = Integer.parseInt(scheduleSoap
									.getAttributeAsString("EventId"));

							String studentcode = scheduleSoap
									.getAttributeAsString("StudentCode");

							String userid = scheduleSoap
									.getAttributeAsString("StudentEmail");

							int yr = Integer.parseInt(scheduleSoap
									.getAttributeAsString("Year"));

							int mth = Integer.parseInt(scheduleSoap
									.getAttributeAsString("Month"));

							int dom = Integer.parseInt(scheduleSoap
									.getAttributeAsString("Day"));

							String issynced = scheduleSoap
									.getAttributeAsString("Synched");

							String msg = scheduleSoap
									.getAttributeAsString("Event");

							String evttype = scheduleSoap
									.getAttributeAsString("Type");

							ScheduleInfo newschedule = new ScheduleInfo();

							newschedule.setScheduleID(evtid);

							newschedule.setUserName(userid);
							newschedule.setYear(yr);
							newschedule.setMonth(mth);
							newschedule.setDayOfMonth(dom);
							newschedule.setNotificationType(evttype);
							newschedule.setIsSynced(issynced);
							newschedule.setMessage(msg);

							try {
								SaveUpdateSchedule(newschedule);

								info.setStatus(0);
								info.setMessage("Event information downloaded successfully.");

							} catch (Exception e) {

								info.setStatus(-3);
								info.setMessage("Data Exception" + e.toString());

								e.printStackTrace();
							}

						} else {
							info.setStatus(-3);
							info.setMessage("Data Exception"
									+ "No attribute attached");

						}

					}
				} else {
					info.setStatus(-3);
					info.setMessage("No Event information available");
				}
			}

			db.close();
		} catch (Exception e) {
			info.setStatus(-3);
			info.setMessage("Data Exception" + e.toString());
		}

		return info;
	}

	public long SaveUpdateSchedule(ScheduleInfo schedule) {

		long lreturn = 0;

		try {
			db = (new DatabaseHelper(CurContext)).getWritableDatabase();

			if (schedule != null) {

				Cursor cursorMonthlySchedule = db.rawQuery(
						"select * from " + DatabaseHelper.TBL_STUDENT_SCHEDULE
								+ " where " + DatabaseHelper.FLD_SCHEDULE_ID
								+ "='" + schedule.getScheduleID() + "'", null);

				ContentValues values = new ContentValues();
				cursorMonthlySchedule.moveToFirst();

				values.put(DatabaseHelper.FLD_SCHEDULE_ID,
						schedule.getScheduleID());

				values.put(DatabaseHelper.FLD_ID_USER, schedule.getUserName());

				values.put(DatabaseHelper.FLD_SCHEDULE_DATE_YEAR,
						schedule.getYear());

				values.put(DatabaseHelper.FLD_SCHEDULE_DATE_MONTH,
						schedule.getMonth());

				values.put(DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH,
						schedule.getDayOfMonth());

				values.put(DatabaseHelper.FLD_SCHEDULE_NOTIFICATION_TYPE,
						schedule.getNotificationType());

				values.put(DatabaseHelper.FLD_SCHEDULE_COMPLETED,
						schedule.getCompletionStatus());

				values.put(DatabaseHelper.FLD_SCHEDULE_IS_SYNCED,
						schedule.getIsSynced());

				values.put(DatabaseHelper.FLD_SCHEDULE_MESSAGE,
						schedule.getMessage());

				if (cursorMonthlySchedule != null
						&& cursorMonthlySchedule.getCount() > 0
						&& schedule.getScheduleID() != -999) {
					// update

					lreturn = db.update(DatabaseHelper.TBL_STUDENT_SCHEDULE,
							values, DatabaseHelper.FLD_SCHEDULE_ID + " = ? ",
							new String[] { Integer.toString(schedule
									.getScheduleID()) });

				} else {
					// insert

					lreturn = db.insertWithOnConflict(
							DatabaseHelper.TBL_STUDENT_SCHEDULE, null, values,
							SQLiteDatabase.CONFLICT_IGNORE);

				}

			}

		} catch (SQLiteException sqle) {

			sqle.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return lreturn;
	}

	private void cleanDB() {

		try {
			// /Delete Module Table
			db.delete(DatabaseHelper.TBL_STUDENT_SCHEDULE, null, null);

			db.execSQL(DatabaseHelper.DROP_TBL_STUDENT_SCHEDULE);
			db.execSQL(DatabaseHelper.CREATE_TBL_STUDENT_SCHEDULE_TABLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ScheduleInfo> getAllSchedules(String UserId) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		List<ScheduleInfo> ScheduleList = new ArrayList<ScheduleInfo>();

		Cursor cursorScheduleDtl = db.rawQuery("select * from "
				+ DatabaseHelper.TBL_STUDENT_SCHEDULE + " where "
				+ DatabaseHelper.FLD_ID_USER + "='" + UserId + "'", null);

		try {
			startManagingCursor(cursorScheduleDtl);

			if (cursorScheduleDtl != null) {

				int scheduleCount = cursorScheduleDtl.getCount();

				if (scheduleCount > 0 && cursorScheduleDtl.moveToFirst()) {

					do {

						int columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_ID);

						int curID = cursorScheduleDtl.getInt(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_ID_USER);

						String shdlUserId = cursorScheduleDtl
								.getString(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_YEAR);

						int yr = cursorScheduleDtl.getInt(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_MONTH);

						int mth = cursorScheduleDtl.getInt(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH);

						int dom = cursorScheduleDtl.getInt(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_NOTIFICATION_TYPE);

						String notifntype = cursorScheduleDtl
								.getString(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_COMPLETED);

						String iscmpleted = cursorScheduleDtl
								.getString(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_IS_SYNCED);

						String issynced = cursorScheduleDtl
								.getString(columnidx);

						columnidx = cursorScheduleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_MESSAGE);

						String msg = cursorScheduleDtl.getString(columnidx);

						ScheduleInfo newSchedule = new ScheduleInfo();
						newSchedule.setScheduleID(curID);
						newSchedule.setUserName(UserId);
						newSchedule.setYear(yr);
						newSchedule.setMonth(mth);
						newSchedule.setDayOfMonth(dom);
						newSchedule.setNotificationType(notifntype);
						newSchedule.setCompletionStatus(iscmpleted);
						newSchedule.setIsSynced(issynced);
						newSchedule.setMessage(msg);

						ScheduleList.add(newSchedule);
					} while (cursorScheduleDtl.moveToNext());
				}
				cursorScheduleDtl.close();
			}

		} catch (SQLiteException sqle) {
			if (cursorScheduleDtl != null) {
				try {
					cursorScheduleDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorScheduleDtl != null) {
				try {
					cursorScheduleDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorScheduleDtl != null) {
				try {
					cursorScheduleDtl.close();

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

		return ScheduleList;

	}

	public ArrayList<ScheduleInfo> getAllSchedulesByMonthofYear(
			StudentDetails studentDtl, Calendar dateData) {

		db = (new DatabaseHelper(CurContext)).getWritableDatabase();

		Cursor cursorModuleDtl = null;
		ArrayList<ScheduleInfo> ScheduleList = new ArrayList<ScheduleInfo>();

		try {

			cursorModuleDtl = db.rawQuery(
					"select * from " + DatabaseHelper.TBL_STUDENT_SCHEDULE
							+ " where " + DatabaseHelper.FLD_ID_USER + "='"
							+ studentDtl.getStudentID() + "' AND "
							+ DatabaseHelper.FLD_SCHEDULE_DATE_YEAR + "='"
							+ dateData.get(Calendar.YEAR) + "' AND "
							+ DatabaseHelper.FLD_SCHEDULE_DATE_MONTH + "='"
							+ dateData.get(Calendar.MONTH) + "' AND "
							+ DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH
							+ "='" + dateData.get(Calendar.DAY_OF_MONTH) + "'",
					null);

			startManagingCursor(cursorModuleDtl);

			if (cursorModuleDtl != null) {

				int userCount = cursorModuleDtl.getCount();

				if (userCount > 0 && cursorModuleDtl.moveToFirst()) {

					do {

						int columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_ID);

						int curID = cursorModuleDtl.getInt(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_ID_USER);

						String shdlUserId = cursorModuleDtl
								.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_YEAR);

						int yr = cursorModuleDtl.getInt(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_MONTH);

						int mth = cursorModuleDtl.getInt(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_DATE_DAY_OF_MONTH);

						int dom = cursorModuleDtl.getInt(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_NOTIFICATION_TYPE);

						String notifntype = cursorModuleDtl
								.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_COMPLETED);

						String iscmpleted = cursorModuleDtl
								.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_IS_SYNCED);

						String issynced = cursorModuleDtl.getString(columnidx);

						columnidx = cursorModuleDtl
								.getColumnIndex(DatabaseHelper.FLD_SCHEDULE_MESSAGE);

						String msg = cursorModuleDtl.getString(columnidx);

						ScheduleInfo newSchedule = new ScheduleInfo();
						newSchedule.setScheduleID(curID);
						newSchedule.setUserName(studentDtl.getStudentID());
						newSchedule.setYear(yr);
						newSchedule.setMonth(mth);
						newSchedule.setDayOfMonth(dom);
						newSchedule.setNotificationType(notifntype);
						newSchedule.setCompletionStatus(iscmpleted);
						newSchedule.setIsSynced(issynced);
						newSchedule.setMessage(msg);

						ScheduleList.add(newSchedule);
					} while (cursorModuleDtl.moveToNext());
				}
				cursorModuleDtl.close();
			}

		} catch (SQLiteException sqle) {
			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();
				} catch (Exception ex) {

					ex.printStackTrace();
				}
			}

		}

		finally {

			if (cursorModuleDtl != null) {
				try {
					cursorModuleDtl.close();

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

		return ScheduleList;

	}

}
