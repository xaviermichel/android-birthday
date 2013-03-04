package com.xmichel.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.xmichel.android.contactsManagement.Contact;
import com.xmichel.android.dataManagement.DataManager;


public class BirthdayService extends Service {

	/**
	 * Heure de raffraichissement par défaut
	 */
	private static String updateHour = "7:30";
	
	/**
	 * Instance unique du service
	 */
	private static BirthdayService m_instance = null;
	
	/**
	 * Obient l'instance du service
	 * 
	 * @return L'instance du service
	 */
	public static BirthdayService getInstance() {
		return m_instance;
	}
	
	
	
	/**
	 * Identifiant des notifications
	 */
	public static final int ID_NOTIFICATION = 1989;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(this.getClass().getName(), ">>>onRebind()");
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        m_instance = this;
        Log.d(this.getClass().getName(), ">>>onCreate()");
    }

	
	@Override
    public void onStart(Intent intent, int startId) {
		
        super.onStart(intent, startId);
        Log.d(this.getClass().getName(), ">>>onStart()");
        
        // thread de mise à jour de la liste chaque jour
        Thread t = new Thread(new Runnable() {
        	        	
        	public void run() {
        	
        		boolean firstLoop = true;
        		
        		Date now = new Date();
        		
        		Date tomorrow = new Date();
        		
        		tomorrow.setHours(0);
        		tomorrow.setMinutes(0);
        		tomorrow.setSeconds(0);
        		
        		for(;;) {

        			now = new Date();
        			
        			if (firstLoop || tomorrow.before(now)) {
        				firstLoop = false;
        				tomorrow.setDate(tomorrow.getDate()+1); 	
        				getContacts(getBaseContext(), true);
        			}
        			
        			try {
        				
        				now = new Date();
        				
        				long sleepTime = tomorrow.getTime() - now.getTime() + 10000; // + delta pour être quasi sur de pas retomber sur le même jour
        				Log.i(this.getClass().getName(), "=========> is going to sleep for " + sleepTime + "ms");
        				Thread.sleep(sleepTime);
        				
        			} catch(Exception e) {
        			}
        		}
        	}
        	
        });
        
        t.start();
    
    }
	
	@Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

	
	/**
	 * La même chose que ci-dessous, en forçant le param createNotification à false
	 * @param context
	 * @return
	 */
	public static List<Contact> getContacts(Context context) {
		return getContacts(context, false);
	}
	
	
	/**
     * Récupération des contacts et insertion dans le tableau
     * Ce tableau est trié selon l'arrivé plus ou moins proche des prochains anniversaires
     * 
     * @param context
     * 				Le context de l'appli qui veut les contacts
     * @param createNotification
     * 				Faut-il créer une notification s'il y a des anniv aujourd'hui ?
     */
    private static List<Contact> getContacts(Context context, boolean haveToCreateNotification) {
    	
    	Set<String> excludedIDs = DataManager.loadExcludedContacts(context);
    	
        List<Contact> lc = new ArrayList<Contact>();
        
        Uri dataUri = ContactsContract.Data.CONTENT_URI;
    	
    	String[] projection = new String[] { 
    			ContactsContract.Contacts.DISPLAY_NAME,           
	            ContactsContract.CommonDataKinds.Event.CONTACT_ID,
	            ContactsContract.CommonDataKinds.Event.START_DATE
	            };

    	Cursor c = context.getContentResolver().query(
    		       dataUri,
    		       projection,
    		       ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
    		       , null, null);
    	
    	ArrayList<String> todayBirthdays = new ArrayList<String>();
    	
    	if (c.moveToFirst()) {
	    	do {
	    		
	    		String id = c.getString(1);
	
	    		// exclu ?
	    		if ( excludedIDs.contains(id) ) {
	    			continue;
	    		}
	    		
	    		String name = c.getString(0);	  
	    		String birthdayDate = c.getString(2);
	    		
		        Pattern pattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
				Matcher matcher = pattern.matcher(birthdayDate);
		        if(matcher.matches()){
		        	
		        	Bitmap photo = Contact.loadContactPhoto(context, Long.decode(c.getString(1)));
		        	
		        	Contact co = new Contact(
		        			id,
		        			name, 
		        			photo,
		        			Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1, Integer.parseInt(matcher.group(3))
		        		);
		        	
		        	if (co.birthdayIsToday()) {
		        		todayBirthdays.add(name);
		        	}

		        	lc.add(co);		        	
	    		}
	    		
	    	} while(c.moveToNext());
    	}

    	c.close();
    	
    	Collections.sort(lc);
    	
    	// on créé ou efface la notification
    	if (haveToCreateNotification) {
    		createNotification(context, todayBirthdays);
    	} else {
    		cancelNotification(context);
    	}
    	
    	return lc;
    }
	
	
	
    /**
     * Méthode qui relai la création de la notification au OnAlarmReceiver pour l'afficher à l'heure donnée dans les préférences.
     * 
     * C'est fait uniquement si aucune notification n'a déjà été créée aujourd'hui
     * 
     * @param context
     * 				Le context
     * @param todayBirthdays
     * 				Les anniversaires du jours (liste de noms)
     */
    private static void createNotification(Context context, ArrayList<String> todayBirthdays){ 	
    	
		// on récupère l'heure du check up
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // debug réinit
        //SharedPreferences.Editor prefsEditorDebug = prefs.edit();
		//prefsEditorDebug.putInt("last_update_day_of_year", -1);
		//prefsEditorDebug.commit();
		// ---
		
    	// date de la notification
    	GregorianCalendar currentDate = new GregorianCalendar();	

		Log.d("xmichel.createNotification", "Current day of year : " + currentDate.get(GregorianCalendar.DAY_OF_YEAR));
		Log.d("xmichel.createNotification", "Last update day of year : " + prefs.getInt("last_update_day_of_year", -1));
		
    	if ( 
    			 prefs.getInt("last_update_day_of_year", -1) == currentDate.get(GregorianCalendar.DAY_OF_YEAR) // date aujourd'hui
    		  && prefs.getInt("last_update_day_of_year", -1) != -1 // déjà créée au moins une fois
    	) {	// rien à faire
    		Log.d("xmichel.createNotifiation", "Notification ever created today");
    		return;
    	}
        
		// on note que l'on a fait la notification aujourd'hui
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putInt("last_update_day_of_year", currentDate.get(GregorianCalendar.DAY_OF_YEAR));
		prefsEditor.commit();
		Log.i("xmichel.createNotification", "current day updated to " + currentDate.get(GregorianCalendar.DAY_OF_YEAR));

		String update_hour = prefs.getString("refresh_hour", updateHour);
		Log.i("xmichel.createNotification", "Should create notification at " + update_hour);
		
		
		String[] hour_minutes = update_hour.split(":");
		int hour = Integer.parseInt(hour_minutes[0]);
		int mins = Integer.parseInt(hour_minutes[1]);
   
    	cancelNotification(context);
    	
    	if (todayBirthdays.isEmpty()) {
    		return;
    	}
    	
    
		if ( 
				hour < currentDate.get(GregorianCalendar.HOUR)
			 || ( hour <= currentDate.get(GregorianCalendar.HOUR) && mins < currentDate.get(GregorianCalendar.MINUTE) )
		) {
			Log.i("xmichel.createNotification", "Notification hour was before now, so I update");
			hour = 0;
			mins = 0;
		} else {	// on soustrait pour avoir le temps à dormir restant
			hour -= currentDate.get(GregorianCalendar.HOUR);
			mins -= currentDate.get(GregorianCalendar.MINUTE);
		}
		
    	Intent intent = new Intent();
        ComponentName comp = new ComponentName(context.getPackageName(), OnAlarmReceiver.class.getCanonicalName());
        intent.putExtra("todays_birthdays", todayBirthdays);
        intent.setComponent(comp);

    	PendingIntent newPending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	
    	Log.d("xmichel.createNotifiation", "will display notification in : " + ((hour*3600 + mins*60)*1000) + "ms");
    	alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + ((hour*3600 + mins*60)*1000), newPending); // SystemClock.elapsedRealtime() + 30000
    }
   
    
    

    private static void cancelNotification(Context context){
    	//On créé notre gestionnaire de notfication
    	NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	//on supprime la notification grâce à son ID
    	notificationManager.cancel(ID_NOTIFICATION);
    }
    
}
