package ica.exam;

import java.util.Calendar;

import ica.ICAConstants.CourseMatIntent;
import ica.tab.view.ClassResult;
import ica.tab.view.DownloadActivity;
import ica.tab.view.StudyMaterialActivity;
import ica.tab.view.ClassExersiseActivity;
import ica.tab.view.ScheduleActivity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class ExamTab extends TabActivity {
	
	public static TabHost tabHost;
	public Context CurContext;
	public void onCreate(Bundle savedInstanceState) {
		CurContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.examtabview);
		Resources ressources = getResources(); 
		tabHost = getTabHost(); 

		//==========================================================================================================================================
		// Download tab
		//==========================================================================================================================================
//		Intent intentDownload = new Intent().setClass(this, ica.tab.view.DownloadActivity.class);
//		TabSpec tabSpecDownload = tabHost
//				.newTabSpec("Download")
//				.setIndicator("Download", ressources.getDrawable(R.drawable.new_download))
//				.setContent(intentDownload);
		
		Intent intentDownload = new Intent(CurContext, Exam_Download.class);
		intentDownload.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		int IntentType = CourseMatIntent.DownlaodMock.getNumber();
		intentDownload.putExtra("ExamIntent", IntentType);
		TabSpec tabSpecDownload = tabHost
				.newTabSpec("Download")
				.setIndicator("Download", ressources.getDrawable(R.drawable.new_download))
				.setContent(intentDownload);
		//==========================================================================================================================================
		// StudyMaterial tab
		//==========================================================================================================================================
//		Intent intentStudyMaterial = new Intent().setClass(this, ica.tab.view.StudyMaterialActivity.class);
//		TabSpec tabSpecStudyMaterial = tabHost
//				.newTabSpec("Study Material")
//				.setIndicator("Study Material", ressources.getDrawable(R.drawable.new_study_material))
//				.setContent(intentStudyMaterial);
		Intent intentStudyMaterial = new Intent(CurContext, Exam_Download.class);
		intentStudyMaterial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		int IntentTypeForStudyMat = CourseMatIntent.StudyMaterials.getNumber();
		intentStudyMaterial.putExtra("ExamIntent", IntentTypeForStudyMat);
		TabSpec tabSpecStudyMaterial = tabHost
				.newTabSpec("Study Material")
				.setIndicator("Study Material", ressources.getDrawable(R.drawable.new_study_material))
				.setContent(intentStudyMaterial);
		//==========================================================================================================================================
		// MockTest tab
		//==========================================================================================================================================
		Intent intentMockTest = new Intent().setClass(this, ica.tab.view.MockTestActivity.class);
		intentMockTest.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		TabSpec tabSpecMockTest = tabHost
				.newTabSpec("Mock Test")
				.setIndicator("Mock Test", ressources.getDrawable(R.drawable.new_mock_test))
				.setContent(intentMockTest);
		
//		Intent intentMockTest = new Intent(this,ica.exam.ExamActivity.class);
//		Calendar mToday = Calendar.getInstance();
//
//		intentMockTest.putExtra("date", mToday.get(Calendar.YEAR)
//				+ "-" + mToday.get(Calendar.MONTH) + "-"
//				+ mToday.get(Calendar.DAY_OF_MONTH));
		
//		TabSpec tabSpecMockTest = tabHost
//				.newTabSpec("Mock Test")
//				.setIndicator("Mock Test", ressources.getDrawable(R.drawable.new_mock_test))
//				.setContent(intentMockTest);

		//==========================================================================================================================================
		// Schedule tab
		//==========================================================================================================================================
//		Intent intentMockTest = new Intent().setClass(this, ica.tab.view.MockTestActivity.class);
//		TabSpec tabSpecMockTest = tabHost
//				.newTabSpec("Mock Test")
//				.setIndicator("Mock Test", ressources.getDrawable(R.drawable.new_mock_test))
//				.setContent(intentMockTest);
		
		Intent intentSchedule = new Intent(this,ica.exam.ExamActivity.class);
		intentSchedule.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Calendar mToday = Calendar.getInstance();

		intentSchedule.putExtra("date", mToday.get(Calendar.YEAR)
				+ "-" + mToday.get(Calendar.MONTH) + "-"
				+ mToday.get(Calendar.DAY_OF_MONTH));
		
		TabSpec tabSpecSchedule = tabHost
				.newTabSpec("Schedule")
				.setIndicator("Schedule", ressources.getDrawable(R.drawable.schedule))
				.setContent(intentSchedule);

		//==========================================================================================================================================
		// ClassExersise tab
		//==========================================================================================================================================
		Intent intentClassExersise = new Intent().setClass(this, ica.tab.view.ClassExersiseActivity.class);
		intentClassExersise.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		TabSpec tabSpecClassExersise = tabHost
				.newTabSpec("Class Exercise")
				.setIndicator("Class Exercise", ressources.getDrawable(R.drawable.new_class_exercise))
				.setContent(intentClassExersise);
		//==========================================================================================================================================
		// Result tab
		//==========================================================================================================================================
//		Intent intent = new Intent(CurContext, ExamResultActivity.class);
//		startActivity(intent);
		
		Intent intentResult = new Intent(CurContext, ClassResult.class);
		intentResult.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		TabSpec tabSpecResult = tabHost
				.newTabSpec("Result")
				.setIndicator("Result", ressources.getDrawable(R.drawable.new_result))
				.setContent(intentResult);

		// add all tabs 
		tabHost.addTab(tabSpecDownload);
		tabHost.addTab(tabSpecStudyMaterial);
		tabHost.addTab(tabSpecMockTest);
		tabHost.addTab(tabSpecSchedule);
		tabHost.addTab(tabSpecClassExersise);
		tabHost.addTab(tabSpecResult);
		
		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(3);
	}

}