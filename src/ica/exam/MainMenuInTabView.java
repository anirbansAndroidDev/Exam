package ica.exam;


import ica.tab.view.SyncActivity;

import java.util.Calendar;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.placement.PlacementSelectorActivity;

public class MainMenuInTabView extends TabActivity {
	TabHost tabHost; 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabview);
		Resources ressources = getResources(); 
		
		try {
			tabHost = getTabHost(); 
			final Context context = this;

			//==================================================================================================================================================
			// Placement tab
			//==================================================================================================================================================		

			Intent ResultCoverFlowIntent = new Intent(this,PlacementSelectorActivity.class);
			//startActivityForResult(ResultCoverFlowIntent,IndexActivity.PlacementStatusCode);
			ResultCoverFlowIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//Intent intentPlacement = new Intent().setClass(this, PlacementActivity.class);
			TabSpec tabSpecPlacement = tabHost
					.newTabSpec("Placement")
					.setIndicator("Placement", ressources.getDrawable(R.drawable.new_icon_placement))
					.setContent(ResultCoverFlowIntent);

			//==================================================================================================================================================
			// Exam tab
			//==================================================================================================================================================		
			//Intent ExamIntent = new Intent(this,ica.exam.ExamActivity.class);
			Intent ExamIntent = new Intent(this,ica.exam.ExamTab.class);
			ExamIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			Calendar mToday = Calendar.getInstance();

			ExamIntent.putExtra("date", mToday.get(Calendar.YEAR)
					+ "-" + mToday.get(Calendar.MONTH) + "-"
					+ mToday.get(Calendar.DAY_OF_MONTH));
			//startActivityForResult(ExamIntent, ExamStatusCode);


			//Intent intentExam = new Intent().setClass(this, ExamActivity.class);
			TabSpec tabSpecExam = tabHost
					.newTabSpec("Exam")
					.setIndicator("Exam", ressources.getDrawable(R.drawable.new_icon_exam))
					.setContent(ExamIntent);
			//==================================================================================================================================================
			// Progress tab
			//==================================================================================================================================================		
			Intent intent = new Intent(this, StudentProgress.class);
			//startActivityForResult(intent, 13);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//Intent intentProgress = new Intent().setClass(this, ProgressActivity.class);
			TabSpec tabSpecProgress = tabHost
					.newTabSpec("Progress")
					.setIndicator("Progress", ressources.getDrawable(R.drawable.new_icon_progress))
					.setContent(intent);

			//==================================================================================================================================================
			// Sync tab
			//==================================================================================================================================================		
			final Intent intentSync = new Intent().setClass(this, SyncActivity.class);
			intentSync.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			TabSpec tabSpecSync = tabHost
					.newTabSpec("Sync")
					.setIndicator("Sync", ressources.getDrawable(R.drawable.new_icon_sync))
					.setContent(intentSync);

			//==================================================================================================================================================
			// Home tab
			//==================================================================================================================================================		

			Intent home = new Intent().setClass(this, IndexActivity.class);
			home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			TabSpec tabHome = tabHost
					.newTabSpec("Home")
					.setIndicator("Home", ressources.getDrawable(R.drawable.new_icon_home))
					.setContent(home);

			// add all tabs 
			tabHost.addTab(tabHome);
			tabHost.addTab(tabSpecSync);
			tabHost.addTab(tabSpecExam);
			tabHost.addTab(tabSpecProgress);
			tabHost.addTab(tabSpecPlacement);


			tabHost.setCurrentTab(2);
		} catch (Throwable e) {
			
			Toast.makeText(this,e+"",Toast.LENGTH_LONG).show();
		}
		
//		tabHost.setOnTabChangedListener(new OnTabChangeListener(){
//			int i = getTabHost().getCurrentTab();
//
//			@Override
//			public void onTabChanged(String tabId) 
//			{
//			    if(i == 4)
//			    {
//			    	LocalActivityManager manager = getLocalActivityManager();
//			        manager.destroyActivity("4", true);
//			        manager.startActivity("4", intentSync);
//
//			    }
//			}
//			
//			});

	}

}