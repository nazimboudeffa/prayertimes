package biz.aldaffah.salaty.ui;



import java.util.List;

import biz.aldaffah.salaty.helper.DatabaseHelper;
import biz.aldaffah.salaty.manager.*;
import biz.aldaffah.salaty.parameters.Preference;
import biz.aldaffah.salaty.services.PrayerService;
import biz.aldaffah.salaty.ui.*;

import biz.aldaffah.salaty.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

// this class is the main class for Settings screen
// It used PreferenceActivity to do the job
// read more on Android Doc about Preference 
public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	Manager m;
	DatabaseHelper databaseHelper ;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		// initialized the Settings activity with new values
		this.init();

	}
	
	public void onDestroy() {
		
		// TODO : Double check
		// is that thread safe ? as I know , LocationListener runs on different
		// thread ?
		/*
		Context context = SettingsActivity.this;
		String title = "It is Pitch Black";
		String message = "You are likely to be eaten by a grue.";
		CharSequence button1String = "Go Back";
		CharSequence button2String = "Move Forward";
		AlertDialog.Builder ad = new AlertDialog.Builder(context);
		ad.setTitle(title);
		ad.setMessage(message);
		OnClickListener Listen1 = new OnClickListener() {
			public void onClick(DialogInterface dialog,
					int arg1) {
				
			}
		};
		ad.setPositiveButton(button1String,Listen1);

		OnClickListener Listen2 = new OnClickListener(){
		public void onClick(DialogInterface dialog,
		int arg1) {
		// do nothing
		}
		};
		ad.setCancelable(true);
		ad.setNegativeButton(button2String,Listen2);
		
		ad.show();
		*/
		
		super.onDestroy();
		
	}
	
	public void init(){
		m = new Manager(getApplicationContext());
		databaseHelper = new DatabaseHelper(getApplicationContext());
		databaseHelper.close();
		try {
			
			Preference preference = m.getPreference();
			preference.fetchCurrentPreferences();
			// country List preference assigned with Country listener 
			// to get know when the country is changed
			// the same thing with city preference
//			ListPreference countryPreference = (ListPreference) findPreference("country");
//			ListPreference cityPreference = (ListPreference) findPreference("city");
//			CountryListener countryListener = new CountryListener(cityPreference,m);
//			countryPreference.setOnPreferenceChangeListener(countryListener);
//			CityListener cityListener = new CityListener(cityPreference,m);
//			cityPreference.setOnPreferenceChangeListener(cityListener);
//			
//			// fill country preference list with country list 
//			fillCountryPreference(countryPreference);
//			String v = countryPreference.getValue();
//			if(v == null)
//				v = DEFAULT_COUNTRY_ID;//TODO 
//			CityListener.fillCityPreference(cityPreference,v ,databaseHelper);
//
			// ok , now let us set summary sections for each preference
			
			String countryName = preference.city.country.name; 	
			String cityName = preference.city.name; //TODO: check default value/ the second parameter		

			//			countryPreference.setSummary(countryName);
//			
//			
//			String cityId = pref.getString("city", "1"); // TODO : check default value/ second parameter			
//			cityPreference.setSummary(databaseHelper.getCity(Integer.parseInt(cityId)).cityName);
//
			// set Summary text for each element in the setting screen
			// Read more about summary code on Android Docs!
			// It used to display the selected value ( current value ) under the element
			// e.g. if the current country "XYZ" the it displays XYZ under the element
			// it reads the value from xml ( preference file) and uses Manager helper functions as well
			PreferenceScreen ps = (PreferenceScreen) findPreference("first_preferencescreen");			
			ps.setSummary(countryName + "/" + cityName);
			ps.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				public boolean onPreferenceClick(
						android.preference.Preference arg0) {
					Intent intent = new Intent(Settings.this,CityFinder.class);
					Settings.this.startActivity(intent);
					return false;
				}
				
			});
			
			/* See preference.xml for enable those preferences
			ListPreference ssLP = (ListPreference) findPreference("silentStart");
			String silentStart = ssLP.getEntry().toString(); 
			ssLP.setSummary(silentStart);
			
			ListPreference sdLP = (ListPreference) findPreference("silentDuration");
			String silentDuration = sdLP.getEntry().toString();
			sdLP.setSummary(silentDuration);		
			*/
			
			ListPreference nsLP = (ListPreference) findPreference("notSound");
			String nsString = nsLP.getEntry().toString();
			nsLP.setSummary(nsString);			
			

			ListPreference mazhabP = (ListPreference) findPreference("mazhab");
			MazhabListener mazhabListener= new MazhabListener();
			mazhabP.setOnPreferenceChangeListener(mazhabListener);
			String mazhabSummary = mazhabP.getEntry().toString();
			mazhabP.setSummary(mazhabSummary);	
			 
			ListPreference seasonP = (ListPreference) findPreference("season");
			SeasonListener seasonListener= new SeasonListener();
			seasonP.setOnPreferenceChangeListener(seasonListener);
			String seasonSummary = seasonP.getEntry().toString();
			seasonP.setSummary(seasonSummary);		

			 
			ListPreference calendarP = (ListPreference) findPreference("calendar");
			CalendarListener calendarListener= new CalendarListener();
			calendarP.setOnPreferenceChangeListener(calendarListener);
			String calSummary = calendarP.getEntry().toString();
			calendarP.setSummary(calSummary);				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
 
//	// read data from database into preference list 
//	private void fillCountryPreference(ListPreference countryPref) {
//		List<Country> countryList = databaseHelper.getCountryList();
//		CharSequence[] countryEntries = new CharSequence[countryList.size()];
//		CharSequence[] countryEntryValues = new CharSequence[countryList.size()];
//		int i = 0;
//		for (Country c : countryList) {
//			countryEntries[i] = c.countryName;
//			countryEntryValues[i] = Integer.toString(c.countryNo);
//			i++;
//		}
//		countryPref.setEntries(countryEntries);
//		countryPref.setDefaultValue("211");
//		countryPref.setEntryValues(countryEntryValues);
//
//	}
	
	protected void onResume() {
	    super.onResume();
	    this.init();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
	}

	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    this.init();
	}
	
	private class MazhabListener implements OnPreferenceChangeListener
	{
		public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
			
			// restart the service when the value changed, by this way
			// the service will calculate the remaining time to the next prayer 
			// based on the new prayer time
			Intent intent = new Intent(Settings.this, PrayerService.class);
			startService(intent);
			return true;
		}

		
	}

	private class CalendarListener implements OnPreferenceChangeListener
	{
		public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
			
			// restart the service when the value changed, by this way
			// the service will calculate the remaining time to the next prayer 
			// based on the new prayer time
//			SharedPreferences pref = PreferenceManager
//			.getDefaultSharedPreferences(getApplicationContext());
//			Editor editor = pref.edit();
//			editor.putString("calendar",newValue.toString());
			Intent intent = new Intent(Settings.this, PrayerService.class);
			startService(intent);
			return true;
		}

	}
	
	private class SeasonListener implements OnPreferenceChangeListener
	{
		public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
			
			// restart the service when the value changed, by this way
			// the service will calculate the remaining time to the next prayer 
			// based on the new prayer time
//			SharedPreferences pref = PreferenceManager
//			.getDefaultSharedPreferences(SettingsActivity.this);
//			Editor editor = pref.edit();
//			editor.putString("season",newValue.toString());
			Intent intent = new Intent(Settings.this, PrayerService.class);
			startService(intent);
			return true;
		}
	}
	
	

}