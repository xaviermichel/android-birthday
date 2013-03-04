package com.xmichel.android;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Recoit un signal de l'alarmManager avec ordre de créer une notification à l'heure donnée avec les anniversaires du jour.
 * @author xavier
 *
 */
public class OnAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("xmichel >>>>", "OnAlarmReceiver > received !");
		
		ArrayList<String> todayBirthdays = intent.getStringArrayListExtra("todays_birthdays");
		
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
        notificationManager.notify(BirthdayService.ID_NOTIFICATION, notification);
		
	}

}
