package biz.aldaffah.salaty.ui;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import biz.aldaffah.salaty.helper.DatabaseHelper;
import biz.aldaffah.salaty.helper.TimeHelper;
import biz.aldaffah.salaty.helper.Typefaces;
import biz.aldaffah.salaty.manager.*;
import biz.aldaffah.salaty.parameters.Preference;
import biz.aldaffah.salaty.utils.ListAdapter;

import biz.aldaffah.salaty.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
 
/**
 * MainActivity represents main screen that is displayed to the user
 * it Contains main data such as prayer times , remaining time until next prayer,
 * city name , and so on ..
 */

public class Main extends Activity {
	private static final int SHOW_PREFERENCES = 1;
	private ListView maListViewPerso;
 
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		try{
		/** 
		 * create Manager object and use it to :
		 * - load database into system folder at the first time
		 * - get some data from database (prayer times .. )
		 * - write some data to the database when necessary .
		 */
		Manager m = new Manager(getApplicationContext());
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		
		try {
			/** 
			 * this method will work just one time as it is implemented
			 * it copies the database file from assets folder to data folder
			 * this step is necessary since that way Android system works :) 
			 */
			databaseHelper.createDatabase();
			databaseHelper.close();
		} catch (IOException e) {
			//Log.e("tomaanina",e.getCause() + ":" + e.getMessage());
		}
		
		// initialize the view objects with the data
		this.init();
		
		// run the application service , read PrayerService for more info 
		m.restartPrayerService(this);
		
		// check xml preference file to check if this is the first run for the app
		// in the user device.
		
		Preference pref = m.getPreference();
        if(pref.isFirstStart())
        {
        	// run some stuff at first time
        	// e.g. search for the use city 
        	// TODO : in the future we should run a wizard 
        	// it improve the usability for our app .
        	this.onFirstStart();
        	
        	// ok , change the flag to false , by this way we prevent onFirstStart method from running again
        	pref.setFirstStart(false);
        }
		}catch(Exception e){
			
		} 
		
		
	}

	public void init() {
		/**
		 * Here we do initialisations of all labels  
		 */
		final Manager manager = new Manager(getApplicationContext());
		Preference preference = manager.getPreference();
		preference.fetchCurrentPreferences();
		
		TextView cityTextView = (TextView) findViewById(R.id.cityName);
		cityTextView.setText(preference.city.name);
		cityTextView.setTypeface(Typefaces.get(this.getBaseContext(), "fonts/DroidNaskh-Regular.ttf"));
		
		/**
		 * Since Date() is depricated 
		 *
		 * Date date = new Date();
		 * final int dd = date.getDate();//calendar.get(Calendar.DAY_OF_MONTH);
		 * final int mm = date.getMonth()+1;//7;//calendar.get(Calendar.MONTH+1);
		 * final int yy = date.getYear()+1900;//calendar.get(Calendar.YEAR);
		*/
		Date date = new Date();   // given date
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		final int dd = calendar.get(Calendar.DAY_OF_MONTH); // gets DAY
		final int mm = calendar.get(Calendar.MONTH)+1;        // gets MONTH
		final int yy = calendar.get(Calendar.YEAR);       // gets YEAR

		try {
			
			/**
			 *  get prayertimes as a List
			 *
			 * index 0 : Fajr time
			 * index 1 : Dhur time 
			 * and so on , until index 4 witch is Isha time			
			 */
			List<String> prayersList = Manager.getPrayerTimes(getApplicationContext(),dd, mm, yy);
			/**
			 * List<String> list = prayersList;
			 * setListAdapter(new ListAdapter(this, list));
			*/
			
	        maListViewPerso = (ListView) findViewById(R.id.list);
	        
	        //Création de la ArrayList qui nous permettra de remplire la listView
	        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
	 
	        //On déclare la HashMap qui contiendra les informations pour un item
	        HashMap<String, String> map, map1, map2, map3, map4, map5;
	   
	        //Création d'une HashMap pour insérer les informations du premier item de notre listView
	        map = new HashMap<String, String>();
	        //on insère un élément titre que l'on récupérera dans le textView titre créé dans le fichier affichageitem.xml
	        map.put("prayer", "Fajr");
	        //on insère un élément description que l'on récupérera dans le textView description créé dans le fichier affichageitem.xml
	        map.put("time", prayersList.get(0));
	        //on insère la référence à l'image (convertit en String car normalement c'est un int) que l'on récupérera dans l'imageView créé dans le fichier affichageitem.xml
	        //map.put("img", String.valueOf(R.drawable.word));
	        //enfin on ajoute cette hashMap dans la arrayList
	        listItem.add(map);
	 
	        //On refait la manip plusieurs fois avec des données différentes pour former les items de notre ListView
	        map = new HashMap<String, String>();
	        map.put("prayer", "Shourouk");
	        map.put("time", prayersList.get(1));
	        //map.put("img", String.valueOf(R.drawable.excel));
	        listItem.add(map);
	 
	        map = new HashMap<String, String>();
	        map.put("prayer", "Dhuhr");
	        map.put("time", prayersList.get(2));
	        //map.put("img", String.valueOf(R.drawable.excel));
	        listItem.add(map);
	 
	        map = new HashMap<String, String>();
	        map.put("prayer", "Asr");
	        map.put("time", prayersList.get(3));
	        //map.put("img", String.valueOf(R.drawable.powerpoint));
	        listItem.add(map);
	 
	        map = new HashMap<String, String>();
	        map.put("prayer", "Coucher");
	        map.put("time", prayersList.get(4));
	        //map.put("img", String.valueOf(R.drawable.outlook));
	        listItem.add(map);
	        
	        map = new HashMap<String, String>();
	        map.put("prayer", "Maghrib");
	        map.put("time", prayersList.get(5));
	        //map.put("img", String.valueOf(R.drawable.outlook));
	        listItem.add(map);
			
	        map = new HashMap<String, String>();
	        map.put("prayer", "Isha");
	        map.put("time", prayersList.get(6));
	        //map.put("img", String.valueOf(R.drawable.outlook));
	        listItem.add(map);
	   
	        SimpleAdapter mSchedule = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.affichageitem,
	                new String[] {"prayer", "time"}, new int[] {R.id.prayer, R.id.time});
	  
	        //On attribut à notre listView l'adapter que l'on vient de créer
	        maListViewPerso.setAdapter(mSchedule);
	        maListViewPerso.setClickable(true); 		
			maListViewPerso.setOnItemClickListener(new OnItemClickListener() {								
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					startActivity(new Intent(getApplicationContext(), Douaa.class));
				}
	         });
					
			// Timer used to decrease the remaining time to next prayer
			updateRemainingTime(yy, mm, dd); //to calculate the nearest  pray 
			Timer myTimer =new Timer();
			TimerTask scanTask ;
			final Handler handler = new Handler();
    
			scanTask = new TimerTask() {
			    public void run() {
			            
						handler.post(new Runnable() {
			                    public void run() {
			                    		try {
											updateRemainingTime(yy, mm, dd);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} 
			                        }
			               });
			        }};

			
			// start the timer
			// 60000 ms == 60 seconds == 1 minutes :)
			myTimer.schedule(scanTask, 0, 60000); 
			
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Log.e("tomaanina",e.getCause() + " : " + e.getMessage());
		}

		
	}
	
	public void updateRemainingTime(int yy, int mm, int dd ) throws Exception{
		/**
		 *  since this is depricated
		 *	Date date = new Date();
		 *
		 *  int h = date.getHours();//calendar.get(Calendar.HOUR_OF_DAY);
		 *  int m = date.getMinutes();//calendar.get(Calendar.MINUTE);
		 * int s = date.getSeconds();//calendar.get(Calendar.SECOND);
		*/
		Date date;   // given date
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		date = calendar.getTime();   // assigns calendar to given date 
		int h = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
		int m = calendar.get(Calendar.MINUTE);        // gets hour in 12h format
		int s = calendar.get(Calendar.SECOND);       // gets month number, NOTE this is zero based!
		// get remaining text view and change its value to " remaining time)
		 TextView remainingTime = (TextView) findViewById(R.id.remainingTime);
		// nearest prayer time ,
		// for example :Asr : 3:10
		// difference : Current time - Asr time == Current Time - 3:10 = remaining time
			
		int time = Manager.computeNearestPrayerTime(getApplicationContext(),h, m, s, yy, mm, dd);
		int def =  TimeHelper.different((h*3600+m*60+s),time);
		remainingTime.setText(TimeHelper.secondsToTime(def));	
		remainingTime.setTypeface(Typefaces.get(this.getBaseContext(), "fonts/Roboto-Regular.ttf"));
	}

	// add main menu items
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, getString(R.string.settings));
		//menu.add(0, 1, 1, getString(R.string.settings));
		// Don't have to see all this only Settings is better for a better interface
		//Gone to main screen
		menu.add(0, 3, 3, getString(R.string.about));
		//find the current city automatically
		// Gone in the settings
		//menu.add(0, 4, 4, getString(R.string.autoCityTitle));
				 
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			// run Settings screen
			Intent myIntent = new Intent(this, Settings.class);
			startActivity(myIntent);
			return true;
		case 3:
			// run About screen 
			Intent MyIntent = new Intent (this , About.class);
			startActivity(MyIntent);
			
			return true;
		case 4:
			// run auto city finder dialog .
			//new AutoCityMainActivity(this, dialog).startSearch();
			//return true;
		case 5:
			// run City Finder Activity
			Intent cityFinderActivity = new Intent (this , CityFinder.class);
			startActivity(cityFinderActivity);
			
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	// update the view on resume
	public void onResume(){
		super.onResume();
		this.init();
	}
	
	public void onStart(){
		super.onStart();
		this.init();
	}
	
	public void onStop(){
		super.onStop();
	}
	
	/**
	 *  this method is triggered at the first time
	 *  put here what you want to execute at the first run for the app on this device 
	 */
	
	public void onFirstStart(){		
		Intent cityFinderActivity = new Intent (this , CityFinder.class);
		startActivity(cityFinderActivity);		   
	}
	
	public void infosClick(View view) {  
		  //Implement image click function  
		Intent aboutActivity = new Intent (this , About.class);
		startActivity(aboutActivity);
	}
	
	

}