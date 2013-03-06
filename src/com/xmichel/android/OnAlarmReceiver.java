package com.xmichel.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.xmichel.android.contactsManagement.Contact;
import com.xmichel.android.dataManagement.DataManager;

/**
 * Recoit un signal de l'alarmManager avec ordre de créer une notification à l'heure donnée avec les anniversaires du jour.
 * @author xavier
 *
 */
public class OnAlarmReceiver extends BroadcastReceiver {

	private static final int ID_NOTIFICATION = 1989;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("xmichel >>>>", "OnAlarmReceiver > received !");
		checkForBirthdayToday(context);
	}

	
	
	private void checkForBirthdayToday(Context context) {
		
		// lance le scan des contacts avec vérificaiton des anniversaires
		// le second parametre correspond à "afficher une notification"
		getContacts(context, true);
		
	}
	
	
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

    	cancelNotification(context);
    	
    	if (todayBirthdays.isEmpty()) {
    		return;
    	}
		
		//On créer la notification
    	//Avec son icône et son texte défilant (optionel si l'on veut pas de texte défilant on met cet argument à null)
    	Notification notification = new Notification(R.drawable.icone, "Il y a un anniversaire aujourd'hui !", System.currentTimeMillis());  

    	//Le PendingIntent c'est ce qui va nous permettre d'atteindre notre deuxième Activity
    	//ActivityNotification sera donc le nom de notre seconde Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, NeverForgetBirthday.class), Intent.FLAG_ACTIVITY_NEW_TASK);
        //On définit le titre de la notif
        String titreNotification = "Anniversaire" + (todayBirthdays.size()>1 ? "s" : "" ) + " du jour :";
        
        //On définit le texte qui caractérise la notif
        String texteNotification = new String();
        for (int i=0; i<todayBirthdays.size(); ++i) {
        	texteNotification += todayBirthdays.get(i) + "\n";         
        }
 
        //On configure notre notification avec tous les paramètres que l'on vient de créer
        notification.setLatestEventInfo(context, titreNotification, texteNotification, pendingIntent);
        //On ajoute un style de vibration à notre notification
        //L'utilisateur est donc également averti par les vibrations de son téléphone
        //Ici les chiffres correspondent à 0sec de pause, 0.2sec de vibration, 0.1sec de pause, 0.2sec de vibration, 0.1sec de pause, 0.2sec de vibration
        //Vous pouvez bien entendu modifier ces valeurs à votre convenance
        //notification.vibrate = new long[] {0,200,100,200,100,200};
 
    	NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); 
        notificationManager.notify(OnAlarmReceiver.ID_NOTIFICATION, notification);
    }
   
    
    

    private static void cancelNotification(Context context){
    	//On créé notre gestionnaire de notfication
    	NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	//on supprime la notification grâce à son ID
    	notificationManager.cancel(ID_NOTIFICATION);
    }
}
