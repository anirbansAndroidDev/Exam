package ica.exam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String TBL_USER = "tb_user";
	public static final String TBL_SUBJECT = "tb_subject";
	public static final String TBL_CHAPTER = "tb_chapter";
	public static final String TBL_EXAM = "tb_exam";
	public static final String TBL_QUESTION_ATTRIBUTE = "tb_question_attribute";
	public static final String TBL_ANSWER_ATTRIBUTE = "tb_answer_attribute";
	public static final String TBL_USER_ANSWER_ATTRIBUTE = "tb_user_answer_attribute";

	public static final String FLD_ROWID = "_id";

	// /COURSE TABLE
	public static final String TBL_USER_COURSE_FEE = "tb_user_course_fee";
	public static final String TBL_USER_INSTALLMENT_DTLS = "tb_user_installement_dtls";
	public static final String TBL_USER_RECIEPT = "tb_user_reciept";

	public static final String FLD_ID_USER = "s_user_id";
	public static final String FLD_PWD = "s_pwd";
	public static final String FLD_STUDENT_CODE = "s_student_code";
	public static final String FLD_STUDENT_MOBILE = "s_student_mobile";
	public static final String FLD_STUDENT_FIRST_NM = "s_student_f_nm";
	public static final String FLD_STUDENT_LAST_NM = "s_student_last_nm";
	public static final String FLD_STUDENT_IMG_PATH = "s_student_img_path";

	public static final String FLD_ID_SUBJECT = "s_subject_id";
	public static final String FLD_NAME_SUBJECT = "s_subject_name";

	public static final String FLD_ID_CHAPTER = "s_chapter_id";
	public static final String FLD_NAME_CHAPTER = "s_chapter_name";
	public static final String FLD_EXAM_DOWNLOADED_CHAPTER = "s_chapter_downloaded";
	public static final String FLD_EXAM_COMPLETED_CHAPTER = "s_chapter_examed";

	// /COURSE TABLE

	// /RESULT TABLE
	public static final String TBL_USER_EXAM_RESULT = "tb_user_exam_result";

	public static final String FLD_EXAM_SUBJECT_ID = "s_exam_subject_Id";
	public static final String FLD_EXAM_SUBJECT_NAME = "s_exam_subject_name";
	public static final String FLD_EXAM_SUBJECT_RESULT = "s_exam_subject_result";

	// /RESULT TABLE

	public static final String FlD_COURSE_ID = "s_course_id";
	public static final String FLD_COURSE_NAME = "s_course_name";
	public static final String FLD_COURSE_FEE = "s_course_fee";
	public static final String FLD_TOTAL_RECVD_AMT = "s_course_total_rcvd_amt";
	public static final String FLD_TOTAL_DUE = "s_course_total_due";

	public static final String FLD_INSTALLMENT_DATE = "s_installment_dt";
	public static final String FLD_INSTALLMENT_DUE_AMT = "s_installment_due_amt";

	public static final String FLD_RECIEPT_DATE = "s_reciept_dt";
	public static final String FLD_RECIEPT_MR = "s_reciept_mr";
	public static final String FLD_RECEIPT_FEES = "s_reciept_fees";
	public static final String FLD_RECIEPT_LATE_FINE = "s_reciept_late_fine";

	// /COURSE TABLE

	// /MODULE INFO TABLE
	public static final String TBL_MODULE_EXAM_DETAILS = "tb_module_exam_details";

	public static final String FLD_MODULE_EXAM_ID = "s_module_exam_Id";
	public static final String FLD_MODULE_EXAM_UserID = "s_module_exam_UserId";

	public static final String FLD_MODULE_EXAM_NAME = "s_module_exam_name";
	public static final String FLD_MODULE_EXAM_STATUS = "s_module_exam_status";

	public static final String FLD_MODULE_EXAM_MARKS = "s_module_exam_marks";
	public static final String FLD_MODULE_EXAM_HIMARKS = "s_module_exam_himarks";
	// /MODULE INFO TABLE

	// /DB SCHEDULE TABLE
	public static final String TBL_STUDENT_SCHEDULE = "tb_student_schedule_details";

	public static final String FLD_SCHEDULE_ID = "s_student_schedule_id";
	public static final String FLD_SCHEDULE_DATE_YEAR = "s_student_schedule_year";
	public static final String FLD_SCHEDULE_DATE_MONTH = "s_student_schedule_month";
	public static final String FLD_SCHEDULE_DATE_DAY_OF_MONTH = "s_student_schedule_dom";
	public static final String FLD_SCHEDULE_NOTIFICATION_TYPE = "s_student_schedule_type";
	public static final String FLD_SCHEDULE_MESSAGE = "s_student_schedule_msg";
	public static final String FLD_SCHEDULE_COMPLETED = "s_student_schedule_completed";
	public static final String FLD_SCHEDULE_IS_SYNCED = "s_student_schedule_is_synced";
	// /DB SCHEDULE TABLE

	// /DB SCHEDULE TABLE
	public static final String TBL_PLACEMENT_DTLS = "tb_placement_details";

	public static final String FLD_PLACEMENT_ID = "s_placement_id";

	public static final String FLD_PLACEMENT_DURATION_TYPE_ALL_INDIA = "s_placement_duration_type_all_india";
	public static final String FLD_PLACEMENT_DURATION_TYPE_MONTH = "s_placement_duration_type_month";
	public static final String FLD_PLACEMENT_DURATION_TYPE_DAY = "s_placement_duration_type_day";

	public static final String FLD_PLACEMENT_DATE_YEAR = "s_placement_year";
	public static final String FLD_PLACEMENT_DATE_MONTH = "s_placement_month";
	public static final String FLD_PLACEMENT_DATE_DAY_OF_MONTH = "s_placement_dom";

	public static final String FLD_PLACEMENT_CENTER_CODE = "s_placement_center_code";
	public static final String FLD_PLACEMENT_EMPLOYER_NAME = "s_placement_employer_name";
	public static final String FLD_PLACEMENT_STUDENT_NAME = "s_placement_student_name";
	public static final String FLD_PLACEMENT_STUDENT_CODE = "s_placement_student_code";
	public static final String FLD_PLACEMENT_SALARY = "s_placement_salary";
	public static final String FLD_PLACEMENT_CONTACT_PERSON = "s_placement_contact_person";
	public static final String FLD_PLACEMENT_PHOTO_LOC = "s_placement_photo_loc";
	public static final String FLD_PLACEMENT_IMAGE = "s_placement_photo_chunk";

	// /DB SCHEDULE TABLE

	// /Exam Upload
	public static final String TBL_EXAM_UPLOAD_INFO = "tb_exam_upload_info";

	public static final String FLD_EXAM_ON = "s_exam_on";

	public static final String FLD_EXAM_TYPE = "s_exam_type";

	public static final String FLD_EXAM_ON_ID = "s_exam_on_id";
	// /Exam Upload

	// /SUBJECT INFO TABLE
	public static final String TBL_SUBJECT_EXAM_DETAILS = "tb_subject_exam_details";

	public static final String FLD_SUBJECT_EXAM_ID = "s_chapter_exam_Id";

	public static final String FLD_SUBJECT_EXAM_NAME = "s_chapter_exam_name";
	public static final String FLD_SUBJECT_EXAM_STATUS = "s_chapter_exam_status";

	public static final String FLD_SUBJECT_EXAM_MARKS = "s_chapter_exam_marks";
	public static final String FLD_SUBJECT_EXAM_HIMARKS = "s_chapter_exam_himarks";
	// /SUBJECT INFO TABLE

	// /CHAPTER INFO TABLE

	public static final String TBL_CHAPTER_EXAM_DETAILS = "tb_chapter_exam_details";

	public static final String FLD_CHAPTER_EXAM_SUBJECT_ID = "s_chapter_exam_subject_Id";
	public static final String FLD_CHAPTER_EXAM_SUBJECT_NAME = "s_chapter_exam_subject_Name";

	public static final String FLD_CHAPTER_EXAM_ID = "s_chapter_exam_Id";

	public static final String FLD_CHAPTER_EXAM_NAME = "s_chapter_exam_name";
	public static final String FLD_CHAPTER_EXAM_STATUS = "s_chapter_exam_status";

	public static final String FLD_CHAPTER_EXAM_MARKS = "s_chapter_exam_marks";
	public static final String FLD_CHAPTER_EXAM_HIMARKS = "s_chapter_exam_himarks";

	// /CHAPTER INFO TABLE

	public static final String FLD_ID_EXAM = "s_exam_id";
	public static final String FLD_EXAM_NAME = "s_exam_name";
	public static final String FLD_EXAM_TIME = "i_exam_time";
	public static final String FLD_ID_QUESTION = "i_question_id";
	public static final String FLD_QUESTION_TYPE = "s_question_type";
	public static final String FLD_QUESTION_MARKS = "s_question_marks";
	public static final String FLD_QUESTION_BODY = "s_question_body";
	public static final String FLD_QUESTION_ANSWERED = "s_answered";
	public static final String FLD_ANSWER_CORRECT = "s_answer_correct";

	public static final String FLD_QUESTION_ATTRIBUTE_1 = "s_question_attribute_1";
	public static final String FLD_QUESTION_ATTRIBUTE_2 = "s_question_attribute_2";
	public static final String FLD_QUESTION_ATTRIBUTE_3 = "s_question_attribute_3";

	public static final String FLD_ANSWER_ATTRIBUTE_1 = "s_answer_attribute_1";
	public static final String FLD_ANSWER_ATTRIBUTE_2 = "s_answer_attribute_2";
	public static final String FLD_ANSWER_ATTRIBUTE_3 = "s_answer_attribute_3";

	public static final String FLD_QUESTION_ROW_ID = "s_question_rowid";
	public static final String FLD_USER_ANSWER_ATTRIBUTE_1 = "s_user_answer_attribute_1";
	public static final String FLD_USER_ANSWER_ATTRIBUTE_2 = "s_user_answer_attribute_2";
	public static final String FLD_USER_ANSWER_ATTRIBUTE_3 = "s_user_answer_attribute_3";

	// /STUDY MATERIAL TABLE
	public static final String TBL_SUBJECT_CHAPTER_STUDY_MATERIAL = "tb_subject_chapter_study_mat";

	public static final String FLD_STUDYMAT_SUBJECT_ID = "s_study_mat_subject_id";
	public static final String FLD_STUDYMAT_CHAPTER_ID = "s_study_mat_chapter_id";
	public static final String FLD_STUDYMAT_PDF_NAME = "s_study_mat_pdf_name";
	public static final String FLD_STUDYMAT_PDF_PATH = "s_study_mat_pdf_path";
	// /

	// /// PRACTICE EXAM RESULT

	public static final String TBL_QUESTION_DETAILS = "tb_question_details";

	public static final String FLD_QUESTION_ID = "s_question_id";
	public static final String FLD_QUESTION_TEXT = "s_question_text";

	public static final String FLD_QUESTION_CHECKED = "s_question_is_checked";

	public static final String FLD_QUESTION_RIGHT_PERCENTAGE = "s_question_right_percentage";
	public static final String FLD_QUESTION_WRONG_PERCENTAGE = "s_question_wrong_percentage";

	public static final String TBL_QUESTION_STUDENT_DETAILS = "tb_question_student_details";

	public static final String FLD_QUESTION_ATTEMPT_STATUS = "s_question_attempt_status";
	public static final String FLD_QUESTION_STUDENT_CODE = "s_question_student_code";
	public static final String FLD_QUESTION_STUDENT_NAME = "s_question_student_name";
	public static final String FLD_QUESTION_STUDENT_PHOTO = "s_question_student_photo";

	public static final String FLD_QUESTION_STUDENT_PHOTO_BLOB = "s_question_student_photo_chunk";

	// QUESTION TABLE

	public final static String CREATE_TBL_QUESTION_DETAILS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_QUESTION_DETAILS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ FLD_QUESTION_ID
			+ " VARCHAR(15)  NOT NULL , "
			+ FLD_ID_USER
			+ " VARCHAR(100) NOT NULL, "
			+ FLD_QUESTION_TEXT
			+ " VARCHAR(100) NOT NULL , "
			+ FLD_QUESTION_CHECKED
			+ " VARCHAR(1) NOT NULL , "
			+ FLD_QUESTION_RIGHT_PERCENTAGE
			+ " INTEGER ," + FLD_QUESTION_WRONG_PERCENTAGE + " INTEGER " + ");";

	public final static String DROP_TBL_QUESTION_DETAILS = "DROP TABLE IF EXISTS "
			+ TBL_QUESTION_DETAILS;

	// QUESTION TABLE

	public static final String CREATE_TBL_QUESTION_STUDENT_DETAILS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_QUESTION_STUDENT_DETAILS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_QUESTION_ID
			+ " VARCHAR(15) NOT NULL, "
			+ FLD_QUESTION_ATTEMPT_STATUS
			+ " VARCHAR(7) NOT NULL, "
			+ FLD_QUESTION_STUDENT_CODE
			+ "  VARCHAR(7) NOT NULL, "
			+ FLD_QUESTION_STUDENT_NAME
			+ " VARCHAR(15) NOT NULL ,"
			+ FLD_QUESTION_STUDENT_PHOTO
			+ " VARCHAR(50) NOT NULL ,"
			+ FLD_QUESTION_STUDENT_PHOTO_BLOB
			+ " blob " + ");";

	public final static String DROP_TBL_QUESTION_STUDENT_DETAILS = "DROP TABLE IF EXISTS "
			+ TBL_QUESTION_STUDENT_DETAILS;

	// /// PRACTICE EXAM RESULT

	public static final String TRUNCATE_TBL_USER = "DELETE FROM " + TBL_USER;
	public static final String TRUNCATE_TBL_SUBJECT = "DELETE FROM "
			+ TBL_SUBJECT;
	public static final String TRUNCATE_TBL_CHAPTER = "DELETE FROM "
			+ TBL_CHAPTER;

	public static final String FIB_DELIMITER = "###";

	public static String DB_PATH = "/data/data/ica.exam/databases/";
	private static String DB_NAME = "db_ica_exam";
	private static final int DB_VERSION = 1;

	private final String CREATE_TBL_USER = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FLD_ID_USER
			+ " VARCHAR(100) NOT NULL, " + FLD_PWD + " VARCHAR(25) NOT NULL ,"
			+ FLD_STUDENT_CODE + " VARCHAR(25) NOT NULL," + FLD_STUDENT_MOBILE
			+ " VARCHAR(25) ," + FLD_STUDENT_FIRST_NM + " VARCHAR(25) ,"
			+ FLD_STUDENT_LAST_NM + " VARCHAR(25) ," + FLD_STUDENT_IMG_PATH
			+ " VARCHAR(25));";

	final static String CREATE_TBL_EXAM_RESULT = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER_EXAM_RESULT + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FLD_EXAM_SUBJECT_ID
			+ " INTEGER NOT NULL, " + FLD_EXAM_SUBJECT_NAME
			+ " VARCHAR(100) NOT NULL, " + FLD_EXAM_SUBJECT_RESULT
			+ " REAL NOT NULL);";

	private final String CREATE_TBL_SUBJECT = "CREATE TABLE IF NOT EXISTS "
			+ TBL_SUBJECT + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FLD_ID_SUBJECT
			+ " INTEGER NOT NULL, " + FLD_NAME_SUBJECT + " TEXT);";

	private final String CREATE_TBL_CHAPTER = "CREATE TABLE IF NOT EXISTS "
			+ TBL_CHAPTER + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FLD_ID_SUBJECT
			+ " INTEGER NOT NULL, " + FLD_ID_CHAPTER + " INTEGER NOT NULL, "
			+ FLD_NAME_CHAPTER + " TEXT, " + FLD_EXAM_DOWNLOADED_CHAPTER
			+ " VARCHAR(1) NOT NULL, " + FLD_EXAM_COMPLETED_CHAPTER
			+ " VARCHAR(1) NOT NULL);";

	private final String CREATE_TBL_EXAM = "CREATE TABLE IF NOT EXISTS "
			+ TBL_EXAM + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FLD_ID_CHAPTER
			+ " INTEGER NOT NULL, " + FLD_ID_EXAM + " INTEGER NOT NULL, "
			+ FLD_EXAM_NAME + " TEXT, " + FLD_EXAM_TIME + " INTEGER NOT NULL, "
			+ FLD_ID_QUESTION + " INTEGER NOT NULL, " + FLD_QUESTION_TYPE
			+ " VARCHAR(4) NOT NULL, " + FLD_QUESTION_MARKS
			+ " INTEGER NOT NULL, " + FLD_QUESTION_BODY + " TEXT, "
			+ FLD_QUESTION_ANSWERED + " VARCHAR(1) NOT NULL, "
			+ FLD_ANSWER_CORRECT + " VARCHAR(1) NOT NULL" + ");";

	final static String CREATE_TBL_EXAM_UPLOAD_INFO = "CREATE TABLE IF NOT EXISTS "
			+ TBL_EXAM_UPLOAD_INFO
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_ID_EXAM
			+ " INTEGER NOT NULL, "
			+ FLD_EXAM_ON
			+ " VARCHAR(15) NOT NULL, "
			+ FLD_EXAM_ON_ID
			+ " INTEGER , "
			+ FLD_EXAM_TYPE
			+ " VARCHAR(1) NOT NULL );";

	private final String CREATE_TBL_QUESTION_ATTRIBUTE = "CREATE TABLE IF NOT EXISTS "
			+ TBL_QUESTION_ATTRIBUTE
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_ID_CHAPTER
			+ " INTEGER NOT NULL, "
			+ FLD_ID_EXAM
			+ " INTEGER NOT NULL, "
			+ FLD_ID_QUESTION
			+ " INTEGER NOT NULL, "
			+ FLD_QUESTION_ATTRIBUTE_1
			+ " TEXT, "
			+ FLD_QUESTION_ATTRIBUTE_2
			+ " TEXT, " + FLD_QUESTION_ATTRIBUTE_3 + " TEXT);";

	private final String CREATE_TBL_ANSWER_ATTRIBUTE = "CREATE TABLE IF NOT EXISTS "
			+ TBL_ANSWER_ATTRIBUTE
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_ID_CHAPTER
			+ " INTEGER NOT NULL, "
			+ FLD_ID_EXAM
			+ " INTEGER NOT NULL, "
			+ FLD_ID_QUESTION
			+ " INTEGER NOT NULL, "
			+ FLD_ANSWER_ATTRIBUTE_1
			+ " TEXT, "
			+ FLD_ANSWER_ATTRIBUTE_2
			+ " TEXT, "
			+ FLD_ANSWER_ATTRIBUTE_3 + " TEXT);";

	private final String CREATE_TBL_USER_ANSWER_ATTRIBUTE = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER_ANSWER_ATTRIBUTE
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_ID_CHAPTER
			+ " INTEGER NOT NULL, "
			+ FLD_ID_EXAM
			+ " INTEGER NOT NULL, "
			+ FLD_ID_QUESTION
			+ " INTEGER NOT NULL, "
			+ FLD_QUESTION_ROW_ID
			+ " INTEGER, "
			+ FLD_USER_ANSWER_ATTRIBUTE_1
			+ " TEXT, "
			+ FLD_USER_ANSWER_ATTRIBUTE_2
			+ " TEXT, "
			+ FLD_USER_ANSWER_ATTRIBUTE_3 + " TEXT);";

	// /COURSE TABLE

	final static String CREATE_TBL_COURSE_FEE = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER_COURSE_FEE + " ( " + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FlD_COURSE_ID
			+ " INTEGER NOT NULL, " + FLD_COURSE_NAME
			+ " VARCHAR(50) NOT NULL, " + FLD_COURSE_FEE + " REAL , "
			+ FLD_TOTAL_RECVD_AMT + " REAL," + FLD_TOTAL_DUE + " REAL );";

	final static String CREATE_TBL_INSTALLMENT = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER_INSTALLMENT_DTLS + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FlD_COURSE_ID
			+ " INTEGER NOT NULL, " + FLD_INSTALLMENT_DATE + " TEXT , "
			+ FLD_INSTALLMENT_DUE_AMT + " INTEGER );";

	final static String CREATE_TBL_RECIEPT = "CREATE TABLE IF NOT EXISTS "
			+ TBL_USER_RECIEPT + " (" + FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FlD_COURSE_ID
			+ " INTEGER NOT NULL, " + FLD_RECIEPT_DATE + " TEXT, "
			+ FLD_RECIEPT_MR + "  VARCHAR(20), " + FLD_RECEIPT_FEES + " REAL);";
	// /COURSE TABLE

	// //EXAM TABLE
	// MODULE TABLE

	public final static String CREATE_TBL_MODULE_EXAM_DTLS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_MODULE_EXAM_DETAILS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_MODULE_EXAM_ID
			+ " INTEGER NOT NULL, "
			+ FLD_MODULE_EXAM_UserID
			+ " VARCHAR(50) NOT NULL, "
			+ FLD_MODULE_EXAM_NAME
			+ "  VARCHAR(50), "
			+ FLD_MODULE_EXAM_STATUS
			+ "  VARCHAR(50), "
			+ FLD_MODULE_EXAM_MARKS
			+ " REAL ,"
			+ FLD_MODULE_EXAM_HIMARKS
			+ " REAL);";

	// SUBJECT TABLE

	public final static String CREATE_TBL_SUBJECT_EXAM_DTLS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_SUBJECT_EXAM_DETAILS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			// + FLD_SUBJECT_EXAM_MODULE_ID
			// + " INTEGER NOT NULL, "
			+ FLD_SUBJECT_EXAM_ID
			+ " INTEGER NOT NULL, "
			+ FLD_SUBJECT_EXAM_NAME
			+ "  VARCHAR(50), "
			+ FLD_SUBJECT_EXAM_STATUS
			+ "  VARCHAR(50), "
			+ FLD_SUBJECT_EXAM_MARKS
			+ " REAL ,"
			+ FLD_SUBJECT_EXAM_HIMARKS
			+ " REAL);";

	// CHAPTER TABLE
	// //EXAM TABLE

	public final static String CREATE_TBL_CHAPTER_EXAM_DTLS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_CHAPTER_EXAM_DETAILS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_CHAPTER_EXAM_SUBJECT_ID
			+ " INTEGER NOT NULL, "
			+ FLD_CHAPTER_EXAM_ID
			+ " INTEGER NOT NULL, "
			+ FLD_CHAPTER_EXAM_NAME
			+ "  VARCHAR(50), "
			+ FLD_CHAPTER_EXAM_SUBJECT_NAME
			+ "  VARCHAR(50), "
			+ FLD_CHAPTER_EXAM_STATUS
			+ "  VARCHAR(50), "
			+ FLD_CHAPTER_EXAM_MARKS
			+ " REAL ,"
			+ FLD_CHAPTER_EXAM_HIMARKS
			+ " REAL);";

	public final static String CREATE_TBL_STUDY_MAT_DTLS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_SUBJECT_CHAPTER_STUDY_MATERIAL
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLD_STUDYMAT_SUBJECT_ID
			+ " INTEGER NOT NULL, "
			+ FLD_STUDYMAT_CHAPTER_ID
			+ " INTEGER NOT NULL, "
			+ FLD_STUDYMAT_PDF_NAME
			+ "  VARCHAR(150),"
			+ FLD_STUDYMAT_PDF_PATH + "  VARCHAR(150)" + ");";

	public final static String CREATE_TBL_STUDENT_SCHEDULE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TBL_STUDENT_SCHEDULE
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT , "
			+ FLD_SCHEDULE_ID
			+ " INTEGER  NOT NULL , "
			+ FLD_ID_USER
			+ " VARCHAR(100) NOT NULL, "
			+ FLD_SCHEDULE_DATE_YEAR
			+ " INTEGER NOT NULL,"
			+ FLD_SCHEDULE_DATE_MONTH
			+ " INTEGER NOT NULL,"
			+ FLD_SCHEDULE_DATE_DAY_OF_MONTH
			+ " INTEGER NOT NULL,"
			+ FLD_SCHEDULE_NOTIFICATION_TYPE
			+ " VARCHAR(1) ,"
			+ FLD_SCHEDULE_COMPLETED
			+ " VARCHAR(1) ,"
			+ FLD_SCHEDULE_IS_SYNCED
			+ " VARCHAR(1) ,"
			+ FLD_SCHEDULE_MESSAGE
			+ " VARCHAR(100));";

	// EXAM TABLE
	// PLACEMENT TABLE

	public final static String CREATE_TBL_PLACEMENT_DTLS = "CREATE TABLE IF NOT EXISTS "
			+ TBL_PLACEMENT_DTLS
			+ " ("
			+ FLD_ROWID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ FLD_PLACEMENT_ID
			+ " INTEGER  NOT NULL ,"
			+ FLD_PLACEMENT_DURATION_TYPE_ALL_INDIA
			+ " VARCHAR(1) ,"
			+ FLD_PLACEMENT_DURATION_TYPE_MONTH
			+ " VARCHAR(1)  ,"
			+ FLD_PLACEMENT_DURATION_TYPE_DAY
			+ " VARCHAR(1) ,"
			+ FLD_PLACEMENT_STUDENT_CODE
			+ " VARCHAR(15) NOT NULL ,"
			+ FLD_PLACEMENT_DATE_YEAR
			+ " INTEGER NOT NULL ,"
			+ FLD_PLACEMENT_DATE_MONTH
			+ " INTEGER NOT NULL ,"
			+ FLD_PLACEMENT_DATE_DAY_OF_MONTH
			+ " INTEGER NOT NULL ,"
			+ FLD_PLACEMENT_CENTER_CODE
			+ " VARCHAR(15) NOT NULL ,"
			+ FLD_PLACEMENT_STUDENT_NAME
			+ " VARCHAR(15) NOT NULL ,"
			+ FLD_PLACEMENT_EMPLOYER_NAME
			+ " VARCHAR(15) NOT NULL , "
			+ FLD_PLACEMENT_SALARY
			+ " REAL ,"
			+ FLD_PLACEMENT_CONTACT_PERSON
			+ " VARCHAR(15) ,"
			+ FLD_PLACEMENT_PHOTO_LOC
			+ " VARCHAR(50) , "
			+ FLD_PLACEMENT_IMAGE
			+ " blob " + ");";
	// PLACEMENT TABLE

	private final String DROP_TBL_USER = "DROP TABLE IF EXISTS " + TBL_USER;
	private final String DROP_TBL_SUBJECT = "DROP TABLE IF EXISTS "
			+ TBL_SUBJECT;
	private final String DROP_TBL_CHAPTER = "DROP TABLE IF EXISTS "
			+ TBL_CHAPTER;
	private final String DROP_TBL_EXAM = "DROP TABLE IF EXISTS " + TBL_EXAM;
	private final String DROP_TBL_QUESTION_ATTRIBUTE = "DROP TABLE IF EXISTS "
			+ TBL_QUESTION_ATTRIBUTE;
	private final String DROP_TBL_ANSWER_ATTRIBUTE = "DROP TABLE IF EXISTS "
			+ TBL_ANSWER_ATTRIBUTE;
	private final String DROP_TBL_USER_ANSWER_ATTRIBUTE = "DROP TABLE IF EXISTS "
			+ TBL_USER_ANSWER_ATTRIBUTE;

	final static String DROP_TBL_COURSE_FEE = "DROP TABLE IF EXISTS "
			+ TBL_USER_COURSE_FEE;
	final static String DROP_TBL_INSTALLMENT = "DROP TABLE IF EXISTS "
			+ TBL_USER_INSTALLMENT_DTLS;

	final static String DROP_TBL_RECIEPT = "DROP TABLE IF EXISTS "
			+ TBL_USER_RECIEPT;

	final static String DROP_TBL_EXAM_RESULT = "DROP TABLE IF EXISTS "
			+ TBL_USER_EXAM_RESULT;

	public final static String DROP_TBL_CHAPTER_EXAM_INFO = "DROP TABLE IF EXISTS "
			+ TBL_CHAPTER_EXAM_DETAILS;

	public final static String DROP_TBL_SUBJECT_EXAM_INFO = "DROP TABLE IF EXISTS "
			+ TBL_SUBJECT_EXAM_DETAILS;

	public final static String DROP_TBL_MODULE_EXAM_INFO = "DROP TABLE IF EXISTS "
			+ TBL_MODULE_EXAM_DETAILS;

	public final static String DROP_TBL_SUBJECT_CHAPTER_STUDY_MATERIAL = "DROP TABLE IF EXISTS "
			+ TBL_SUBJECT_CHAPTER_STUDY_MATERIAL;

	public final static String DROP_TBL_STUDENT_SCHEDULE = "DROP TABLE IF EXISTS "
			+ TBL_STUDENT_SCHEDULE;

	public final static String DROP_TBL_EXAM_UPLOAD_INFO = "DROP TABLE IF EXISTS "
			+ TBL_EXAM_UPLOAD_INFO;

	public final static String DROP_TBL_PLACEMENT_DTLS = "DROP TABLE IF EXISTS "
			+ TBL_PLACEMENT_DTLS;

	private final Context context;

	public DatabaseHelper(Context c) {
		super(c, DB_NAME, null, DB_VERSION);
		context = c;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_TBL_USER);
			db.execSQL(CREATE_TBL_SUBJECT);
			db.execSQL(CREATE_TBL_CHAPTER);
			db.execSQL(CREATE_TBL_EXAM);
			db.execSQL(CREATE_TBL_QUESTION_ATTRIBUTE);
			db.execSQL(CREATE_TBL_ANSWER_ATTRIBUTE);
			db.execSQL(CREATE_TBL_USER_ANSWER_ATTRIBUTE);

			// /COURSE TABLE
			db.execSQL(CREATE_TBL_COURSE_FEE);
			db.execSQL(CREATE_TBL_RECIEPT);
			db.execSQL(CREATE_TBL_INSTALLMENT);
			// /COURSE TABLE

			// /EXAM RESULT TABLE

			db.execSQL(CREATE_TBL_EXAM_RESULT);

			// /EXAM RESULT TABLE

			db.execSQL(CREATE_TBL_MODULE_EXAM_DTLS);
			db.execSQL(CREATE_TBL_SUBJECT_EXAM_DTLS);
			db.execSQL(CREATE_TBL_CHAPTER_EXAM_DTLS);

			db.execSQL(CREATE_TBL_STUDY_MAT_DTLS);

			db.execSQL(CREATE_TBL_EXAM_UPLOAD_INFO);

			db.execSQL(CREATE_TBL_STUDENT_SCHEDULE_TABLE);

			db.execSQL(CREATE_TBL_PLACEMENT_DTLS);

		} catch (SQLiteException sqle) {
			Toast.makeText(context, sqle.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			db.execSQL(DROP_TBL_USER);
			db.execSQL(DROP_TBL_SUBJECT);
			db.execSQL(DROP_TBL_CHAPTER);
			db.execSQL(DROP_TBL_EXAM);
			db.execSQL(DROP_TBL_QUESTION_ATTRIBUTE);
			db.execSQL(DROP_TBL_ANSWER_ATTRIBUTE);
			db.execSQL(DROP_TBL_USER_ANSWER_ATTRIBUTE);

			// /COURSE TABLE
			db.execSQL(DROP_TBL_COURSE_FEE);
			db.execSQL(DROP_TBL_INSTALLMENT);
			db.execSQL(DROP_TBL_RECIEPT);
			// /COURSE TABLE

			// /EXAM RESULT TABLE
			db.execSQL(DROP_TBL_EXAM_RESULT);

			// /EXAM RESULT TABLE

			db.execSQL(DROP_TBL_CHAPTER_EXAM_INFO);
			db.execSQL(DROP_TBL_SUBJECT_EXAM_INFO);
			db.execSQL(DROP_TBL_MODULE_EXAM_INFO);

			db.execSQL(DROP_TBL_SUBJECT_CHAPTER_STUDY_MATERIAL);

			db.execSQL(DROP_TBL_STUDENT_SCHEDULE);

			db.execSQL(DROP_TBL_EXAM_UPLOAD_INFO);

			db.execSQL(DROP_TBL_PLACEMENT_DTLS);

			onCreate(db);
		} catch (SQLiteException sqle) {
			Toast.makeText(context, sqle.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}
}