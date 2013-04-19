package com.placement;

import ica.ICAServiceHandler.PlacementDownloaderService;
import ica.ProfileInfo.StatusMessage;
import ica.Utility.AppPreferenceStatus;
import ica.Utility.PlacementPreference;
import ica.exam.R;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlacementSelectorActivity extends Activity {
	/** Called when the activity is first created. */

	TextView txtAllIndia;
	TextView txtForDays;
	TextView txtForMonth;

	LinearLayout llAllIndia;
	LinearLayout llAllDay;
	LinearLayout llAllMonth;
	Context IndexContext;

	Context curContext;

	int dayCount;
	String monthValue;

	EditText editDayCount;
	DatePicker dtSelector;

	PlacementType placementType = PlacementType.ALLIndia;

	PlacementDownloaderService mPlacementDownloaderService_AllIndia;
	PlacementDownloaderService mPlacementDownloaderService_Days;
	PlacementDownloaderService mPlacementDownloaderService_Month;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.placementselectorlayout);
		IndexContext = this;
		curContext = this;
		
		setTitle("Placement Information");

		TableLayout llHolder = (TableLayout) findViewById(R.id.tblHolder);
		setLayoutAnim_slidedownfromtop(llHolder, this);
		llHolder.startLayoutAnimation();

		txtAllIndia = (TextView) findViewById(R.id.txtAllIndia);

		txtForDays = (TextView) findViewById(R.id.txtfordays);

		txtForMonth = (TextView) findViewById(R.id.txtformonth);

		setAllIndiaInfoText();
		setDayInfoText();
		setMonthInfoText();

		llAllIndia = (LinearLayout) findViewById(R.id.llAllIndia);
		llAllIndia.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				placementType = PlacementType.ALLIndia;
				setIntentData();
			}
		});

		llAllDay = (LinearLayout) findViewById(R.id.llForDays);
		llAllDay.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				placementType = PlacementType.ForDays;
				setIntentData();
			}
		});

		llAllMonth = (LinearLayout) findViewById(R.id.llForMonth);
		llAllMonth.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				placementType = PlacementType.ForMonth;
				setIntentData();
			}
		});

		mPlacementDownloaderService_AllIndia = new PlacementDownloaderService(
				curContext, PlacementType.ALLIndia);
		mPlacementDownloaderService_Days = new PlacementDownloaderService(
				curContext, PlacementType.ForDays);
		mPlacementDownloaderService_Month = new PlacementDownloaderService(
				curContext, PlacementType.ForMonth);

		AlertDialog.Builder dlgBuilder = new Builder(curContext);
		dlgBuilder.setCancelable(false);
		dlgBuilder.setTitle("Placement Updater");
		dlgBuilder
				.setMessage("Do you wish to sync present placement information?");

		dlgBuilder.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						dialog.cancel();

					}
				});

		dlgBuilder.setNegativeButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						new AsyncDownloadPlacementInfo().execute("");

					}
				});
		dlgBuilder.create().show();

		// Placement Service Call

	}

	public class AsyncDownloadPlacementInfo extends
			AsyncTask<String, StatusMessage, Void> {

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			pd = new ProgressDialog(curContext);
			pd.setMessage("Please wait while placement info sync is in progress...");
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(3);
			pd.setCancelable(false);
			pd.setCanceledOnTouchOutside(false);

			pd.show();

		}

		StatusMessage info = new StatusMessage();

		@Override
		protected void onProgressUpdate(StatusMessage... values) {
			super.onProgressUpdate(values);

			pd.incrementProgressBy(1);

			Toast.makeText(curContext,
					values[0].getTitle() + "\n" + values[0].getMessage(),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Void doInBackground(String... params) {

			PlacementDownloaderService.emptyDB(curContext);

			info = mPlacementDownloaderService_AllIndia.SyncServerToDB("A");
			publishProgress(info);

			info = mPlacementDownloaderService_Days.SyncServerToDB("D");
			publishProgress(info);

			info = mPlacementDownloaderService_Month.SyncServerToDB("M");
			publishProgress(info);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (pd != null && pd.isShowing())
				pd.dismiss();

			setAllIndiaInfoText();
			setDayInfoText();
			setMonthInfoText();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.placementoptions, menu);

		return true;
	}

	public void showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.placementoptions, popup.getMenu());
		popup.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menuPlacementSettings:
			switch (placementType) {
			case ALLIndia:
				Toast.makeText(curContext, "All India Settings",
						Toast.LENGTH_SHORT).show();

				break;
			case ForDays:

				LayoutInflater inflator = ((Activity) curContext)
						.getLayoutInflater();
				View view = inflator.inflate(R.layout.dlgsetdaycount, null);

				editDayCount = (EditText) view.findViewById(R.id.editDayCount);

				String[] presetValue = txtForDays.getText().toString()
						.split("Top 10 last ");

				presetValue = presetValue[1].split(" Days");
				String Value = presetValue[0];
				editDayCount.setText(Value);

				AlertDialog.Builder dlgBuilder = new Builder(curContext);

				dlgBuilder.setView(view);
				dlgBuilder.setCancelable(false);

				dlgBuilder.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();

							}
						});

				dlgBuilder.setNegativeButton("Ok",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								String blank = "0";
								if (!editDayCount.getText().toString()
										.equals("")) {
									blank = editDayCount.getText().toString();
								}

								txtForDays.setText("Top 10 last " + blank
										+ " Days");

								dialog.cancel();
							}
						});
				// dlgBuilder.create().show();
				break;
			case ForMonth:
				inflator = ((Activity) curContext).getLayoutInflater();

				view = inflator.inflate(R.layout.setmonthvalue, null);

				presetValue = txtForMonth.getText().toString()
						.split("Top 10 for the month of ");
				
				monthValue = presetValue[1];

				dtSelector = (DatePicker) view.findViewById(R.id.dtSelector);
				Calendar curCalendar = Calendar.getInstance();

				curCalendar.set(Calendar.MONTH, MonthItems.valueOf(monthValue)
						.getIndex());

				curCalendar.set(Calendar.YEAR,
						PlacementPreference.getPlacementMonthYr(curContext));

				dtSelector.init(curCalendar.get(Calendar.YEAR),
						curCalendar.get(Calendar.MONTH),
						curCalendar.get(Calendar.DAY_OF_MONTH), null);

				dlgBuilder = new Builder(curContext);
				dlgBuilder.setCancelable(false);
				dlgBuilder.setView(view);

				dlgBuilder.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();

							}
						});

				dlgBuilder.setNegativeButton("Ok",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								txtForMonth.setText("Top 10 for the month of "
										+ MonthItems.values()[dtSelector
												.getMonth()]);
						
								dialog.cancel();
							}
						});

				// dlgBuilder.create().show();

				break;
			default:
				break;
			}
			return true;
		case R.id.menuPlacementFetch:

			setIntentData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void setAllIndiaInfoText() {
		txtAllIndia.setText("Top "
				+ PlacementPreference.getPlacementCountAllIndia(curContext)
				+ " All India ");
	}

	private void setDayInfoText() {
		txtForDays.setText("Top "
				+ PlacementPreference.getPlacementCountDay(curContext)
				+ " last "
				+ PlacementPreference.getPlacementDaysCount(curContext)
				+ " Days");
	}

	private void setMonthInfoText() {
		txtForMonth.setText("Top "
				+ PlacementPreference.getPlacementCountMonth(curContext)
				+ " for the month of "
				+ PlacementPreference.getPlacementMonth(curContext).toString());
	}

	private void setIntentData() {
		PlacementDetails = new Intent(curContext,
				PlacementDetailsActivity.class);
		
		switch (placementType) {
		case ALLIndia:
			PlacementDetails.putExtra(PLACEMENT_ITEM_COUNT,
					PlacementPreference.getPlacementCountAllIndia(curContext));
			break;
		case ForDays:
			PlacementDetails.putExtra(PLACEMENT_ITEM_COUNT,
					PlacementPreference.getPlacementCountDay(curContext));
			break;
		case ForMonth:
			PlacementDetails.putExtra(PLACEMENT_ITEM_COUNT,
					PlacementPreference.getPlacementCountMonth(curContext));
			break;
		}

		PlacementDetails.putExtra(PLACEMENT_DURATION, placementType.toString());

		PlacementDetails.putExtra(PLACEMENT_DAYS,
				Integer.toString(PlacementPreference
						.getPlacementDaysCount(curContext)));

		PlacementDetails.putExtra(PLACEMENT_MONTH, PlacementPreference
				.getPlacementMonth(curContext).toString());

		PlacementDetails.putExtra(PLACEMENT_MONTH_YEAR, Integer
				.toString(PlacementPreference.getPlacementMonthYr(curContext)));

		startActivityForResult(PlacementDetails, PlacementDetailsStatusCode);
	}

	public static final int PlacementDetailsStatusCode = 124;

	Intent PlacementDetails;
	public static final String PLACEMENT_DURATION = "PLACEMENT_DURATION";
	public static final String PLACEMENT_ITEM_COUNT = "PLACEMENT_ITEM_COUNT";
	public static final String PLACEMENT_DAYS = "PLACEMENT_DAYS";
	public static final String PLACEMENT_MONTH = "PLACEMENT_MONTH";
	public static final String PLACEMENT_MONTH_YEAR = "PLACEMENT_MONTH_YEAR";

	public enum PlacementType {
		ALLIndia, ForDays, ForMonth;
	}

	public enum MonthItems {
		January, Feburary, March, April, May, June, July, August, September, October, November, December;

		public int getIndex() {
			return ordinal();
		}
	}

	public void setLayoutAnim_slidedownfromtop(ViewGroup panel, Context ctx) {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(1500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.25f);
		panel.setLayoutAnimation(controller);

	}
	@Override
	public void onBackPressed() {
		//super.onBackPressed();

		//finish();
		//Toast.makeText( getApplicationContext(),"Back pressed",Toast.LENGTH_SHORT).show();
		
		//==================================================================================================================
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this); 
		  
        alertDialog.setTitle("Confirm Exit ..."); 
        alertDialog.setMessage("Are you sure to exit ?"); 
        alertDialog.setIcon(R.drawable.tick); 
  
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog,int which) {
            	AppPreferenceStatus.setLoggedOutStatus(IndexContext, true);
        		finish();
        		//System.runFinalizersOnExit(true);
        		//System.exit(0);
        		Intent intent = new Intent(Intent.ACTION_MAIN);
        		intent.addCategory(Intent.CATEGORY_HOME);
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
            } 
        }); 
  
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int which) { 
            dialog.cancel(); 
            } 
        }); 
  
        alertDialog.show();
		//==================================================================================================================
	}

}