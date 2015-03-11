/*
 * Manager class is managing the connection between the ( Database | XML-Files ) 
 * and the ( Prayer Model | Setting Screen | Main Screen  )
 *  
 *  
 * these are the Main functionalities: ( #MAIN FUNCTION , %HOW DOES IT WORKS , $METHOD APPEARANCE "SERIALLY" ).
 * 	# Store City-Attributes.
 * 		% By the city-ID -> Gets City Attributes -> Stores the City Data in the XML. 
 * 		$ ( setSetting() -> getData() -> xmlWriter() ).
 * 
 * 	# Calculate Prayer-Times.
 * 		% By Reading the City Attributes -> Runs the Prayer-Model to get Prayer Times. 
 * 		$ ( xmlReader() -> getPrayerTimes() ).
 * 
 * 	# Find out Nearest-Prayer-Time.
 * 		% By Calculating Prayer-Times and Comparing current time with them to get the next one.
 * 		$ ( getPrayerTimes() -> nearestPrayerTime() ).
 * 
 * 	# Copy the Country DataBase to the Device -> TODO MAIN FUNCTION (ABDULLAH).
 * 		% TODO HOW DOES IT WORKS .
 * 		$ TODO METHOD APPEARANCE "SERIALLY" .
 * 
 * 	# Find Current City Location -> TODO MAIN FUNCTION (MOHAMMED).
 * 		% TODO HOW DOES IT WORKS .
 * 		$ TODO METHOD APPEARANCE "SERIALLY" .
 * 
 * #.
 * 		%.
 * 		$.
 */
package biz.aldaffah.salaty.manager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import biz.aldaffah.salaty.helper.DatabaseHelper;
import biz.aldaffah.salaty.helper.TimeHelper;
import biz.aldaffah.salaty.moazen.PrayTime;
import biz.aldaffah.salaty.parameters.City;
import biz.aldaffah.salaty.parameters.Preference;
import biz.aldaffah.salaty.services.PrayerReceiver;
import biz.aldaffah.salaty.services.PrayerService;
import biz.aldaffah.salaty.ui.Alert;
import biz.aldaffah.salaty.ui.Main;
import biz.aldaffah.salaty.utils.*;
import biz.aldaffah.salaty.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.*;
import android.media.AudioManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 *  Manager is the main class that works as layer  between the app and database/xml files
 * 
 * @author Abdullah
 * @author Mohammed
 * @author Nazim Boudeffa
 *
 */
public class Manager {

	private Context context;
	DatabaseHelper databaseHelper;
	private static Intent prayerIntet;
	private static PendingIntent prayerPendingIntent;
	private static AlarmManager prayerAlarmManager;
	public static long interval;
	private static PrayerState prayerState;
	private static Service prayerService;
	private static int UNIQUE_ID = 32289;
	public static boolean isPhoneIdle = true;

	public Manager(Context applicationContext) {

		this.context = applicationContext;
		databaseHelper = new DatabaseHelper(applicationContext);
	}

	public static void acquireScreen(Context context) {
		PowerManager pm = (PowerManager) context.getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm
				.newWakeLock(
						(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
								| PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
						"TAG");
		wakeLock.acquire();
	}

	public static void releaseScreen(Context context) {
		KeyguardManager keyguardManager = (KeyguardManager) context
				.getApplicationContext().getSystemService(
						Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();
	}

	public static void initPrayerAlarm(Service service,
			Class<PrayerReceiver> receiver) {
		Manager.prayerService = service; // we may need it ?
		Manager.prayerIntet = new Intent(service, receiver);
		Manager.prayerPendingIntent = PendingIntent
				.getBroadcast(service, 1234432, Manager.prayerIntet,
						PendingIntent.FLAG_UPDATE_CURRENT);
		Manager.prayerAlarmManager = (AlarmManager) service
				.getSystemService(Context.ALARM_SERVICE);
		Manager.prayerAlarmManager.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + 1000, Manager.prayerPendingIntent);
	}

	public static void updatePrayerAlarm(long newTimeInterval) {
		Manager.prayerAlarmManager.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + newTimeInterval,
				Manager.prayerPendingIntent);
	}

	public static void cancelPrayerAlarm() {
		Manager.prayerAlarmManager.cancel(prayerPendingIntent);
	}

	public static void initPrayerState(Context context) {
		Manager.prayerState = new PrayerState(context);

	}

	public static PrayerState getPrayerState() {
		return prayerState;
	}
	
	/**
	 *  Get nearest prayer time based on current time
	 * @param context
	 * @param hour
	 * @param min
	 * @param sec
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 * @throws Exception
	 */

	public static int computeNearestPrayerTime(Context context, int hour,
			int min, int sec, int year, int month, int day) throws Exception {
		ArrayList<String> prayerTimes = getPrayerTimes(context, day, month,	year);
		int[] prayerTimeInSeconds = new int[5];
	

		prayerTimeInSeconds[0] = TimeHelper.getSec(prayerTimes.get(0));
		//prayerTimeInSeconds[1] = TimeHelper.getSec(prayerTimes.get(1));
		prayerTimeInSeconds[1] = TimeHelper.getSec(prayerTimes.get(2));
		prayerTimeInSeconds[2] = TimeHelper.getSec(prayerTimes.get(3));
		//prayerTimeInSeconds[4] = TimeHelper.getSec(prayerTimes.get(4));
		prayerTimeInSeconds[3] = TimeHelper.getSec(prayerTimes.get(5));
		prayerTimeInSeconds[4] = TimeHelper.getSec(prayerTimes.get(6));
	
		// sort ascending
		Arrays.sort(prayerTimeInSeconds);
		// default value is the first prayer in the day
		int nearestPrayer = prayerTimeInSeconds[0];
		// convert current time to seconds
		int currentTime = hour * 3600 + min * 60 + sec;

		for (Integer prayertime : prayerTimeInSeconds) {
			int pt = prayertime;
			if (pt >= currentTime)// return first prayer after this time (nearest prayer)
				return pt;
		}
		return nearestPrayer;
	}


	/**
	 * getPrayerTimes now uses a java class found on www.praytimes.com
	 */	
	public static ArrayList<String> getPrayerTimes(Context context, int dd,
			int mm, int yy) throws IOException, Exception {

		ArrayList<String> prayerList = new ArrayList<String>();
		
		Manager manager = new Manager(context);
		Preference preference = manager.getPreference();
		preference.fetchCurrentPreferences();
		
		String cal = preference.calender;
		String maz = preference.mazhab;
		String sea = preference.season;
		
		
		PrayTime prayTime = new PrayTime();
        
		//Setting Calendar preferences		
		
		if (cal.equals("Jafari")) {prayTime.setCalcMethod(prayTime.Jafari);} 
		
		if (cal.equals("Karachi")){prayTime.setCalcMethod(prayTime.Karachi);}
		
		if (cal.equals("ISNA")){prayTime.setCalcMethod(prayTime.ISNA);}
		
		if (cal.equals("MWL")){prayTime.setCalcMethod(prayTime.MWL);}
		
		if (cal.equals("Makkah")){prayTime.setCalcMethod(prayTime.Makkah);}

		if (cal.equals("Egypt")){prayTime.setCalcMethod(prayTime.Egypt);}
		
		if (cal.equals("Tehran")){prayTime.setCalcMethod(prayTime.Tehran);}
		
		
		// This last setting in case of a first start
		
		if (!cal.equals("Jafari") && !cal.equals("Karachi") && !cal.equals("ISNA") && !cal.equals("MWL") && !cal.equals("Makkah") && !cal.equals("Egypt") && !cal.equals("Tehran")) 
		{prayTime.setCalcMethod(prayTime.Custom);}
		
		/**
		 * TODO : Set shafii or hanafi 
		 * Default is Hanafi in PrayTime.java
		 *
		 * if (maz.equals("Hanafi")) {
		 * 		prayTime.setHanafi(1);prayTime.setShafii(0);
		 * } else {
		 * 		prayTime.setHanafi(0);prayTime.setShafii(1);
		 * }
		*/
		prayerList = prayTime.getDatePrayerTimes(yy, mm, dd,
				Double.parseDouble(preference.city.latitude), Double.parseDouble(preference.city.longitude), preference.city.timeZone);			
		
		ArrayList<String> prayerListSummer = new ArrayList<String>();
		if (sea.equals("Summer")){
			
			 Calendar summer = Calendar.getInstance();
			
			 SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			 Date d; 
			 
			 d= df.parse(prayerList.get(0)); 			 
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String fajr = df.format(summer.getTime());
			 prayerListSummer.add(fajr);
			 
			 
			 d = df.parse(prayerList.get(1));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String shourouk = df.format(summer.getTime());
			 prayerListSummer.add(shourouk);
			 
			 d = df.parse(prayerList.get(2));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String duhur = df.format(summer.getTime());
			 prayerListSummer.add(duhur);
			 
			 d = df.parse(prayerList.get(3));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String asr = df.format(summer.getTime());
			 prayerListSummer.add(asr);
			 
			 d = df.parse(prayerList.get(4));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String coucher = df.format(summer.getTime());
			 prayerListSummer.add(coucher);
			 
			 d = df.parse(prayerList.get(5));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String maghrib = df.format(summer.getTime());
			 prayerListSummer.add(maghrib);
			 
			 d = df.parse(prayerList.get(6));
			 summer.setTime(d);
			 summer.add(Calendar.HOUR, 1);
			 String isha = df.format(summer.getTime());
			 prayerListSummer.add(isha);
			 
			 
		}else{
			prayerListSummer=prayerList;
		};
		
		return prayerListSummer;
	}

	
	public Context getContext() {
		return context;
	}

	/** 
	 * find the current city based on its latitude and longtiude
	 * I DON'T KNOW HOW THE METHOD WORKS !?
	 */
	public City findCurrentCity(double latitude, double longitude) {
		try {
			double min = 0;
			int i = 0, pos = 0;
			ArrayList<City> cityList = databaseHelper.getCityList(-1);
			for (City city : cityList) {
				double lat = Double.parseDouble(city.latitude);
				double lon = Double.parseDouble(city.longitude);
				double pk = (180 / 3.14159);
				double a1 = (lat / pk);
				double a2 = (lon / pk);

				double b1 = (latitude / pk);
				double b2 = (longitude / pk);

				double t1 = (Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math
						.cos(b2));
				double t2 = (Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math
						.sin(b2));
				double t3 = (Math.sin(a1) * Math.sin(b1));
				double tt = Math.acos(t1 + t2 + t3);
				double dist = (6366000 * tt);
				if (dist < min || i == 0) {
					min = dist;
					pos = i;
				}
				i++;
			}
			if (pos < cityList.size() && cityList.get(pos) != null) {
				String cityId = cityList.get(pos).id;
				Integer cityNo = -1;
				if (cityId != null) {
					cityNo = Integer.parseInt(cityId);
				}
				if (cityNo == -1)
					cityNo = 1;

				City city = databaseHelper.getCity(cityNo);
				databaseHelper.close();
				return city;

			}

		} catch (Exception e) {
		} finally {
			databaseHelper.close();
		}
		return null;

	}

	public static void playAzanNotification(Context context) {
		Intent intent;
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String azanMode = pref.getString("notSound", "short");
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		if (azanMode.equals("full")
				&& am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL
				&& Manager.isPhoneIdle == true) {
			intent = new Intent(context, Alert.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.putExtra("runFromService", true);
			context.startActivity(intent);
		} else if (!(azanMode.equals("disable"))
				&& (azanMode.equals("short") || (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am
						.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE))
				|| Manager.isPhoneIdle == false) {
			
			// Add type of notification according to the prayer time
			 
			CharSequence contentTitle = context.getString(R.string.notTitle);
			CharSequence contentText = context.getString(R.string.notContent);
			
			
			long when = System.currentTimeMillis();

			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Intent notificationIntent = new Intent(context, Main.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);

			Notification notification = new Notification(
					biz.aldaffah.salaty.R.drawable.icon, contentText, when);
			notification.sound = Uri
					.parse("android.resource://biz.aldaffah.salaty/raw/notification");
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			mNotificationManager.notify(UNIQUE_ID, notification);
		}

	}

	public Preference getPreference() {

		return new Preference(this.context);
	}

	public void updateCity(City city, Activity activity) {
		Preference pref = this.getPreference();
		pref.setCityName(city.name);
		pref.setCityNo(city.id);
		pref.setCountryName(city.country.name);
		pref.setCountryNo(city.country.id);
		pref.setLongitude(city.longitude);
		pref.setLatitude(city.latitude);
		pref.setTimeZone(city.timeZone);

    	Manager.cancelPrayerAlarm();
    	Manager.initPrayerState(Manager.prayerService);
		Manager.initPrayerAlarm(Manager.prayerService,PrayerReceiver.class);
    	
	}
	
	// it does not work ? 
	public void restartPrayerService(Activity activty) {
		Intent intent = new Intent(activty, PrayerService.class);
		context.startService(intent);
	}

}