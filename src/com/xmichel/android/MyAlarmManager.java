package com.xmichel.android;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyAlarmManager {

	private static final int ALARM_IDENTIFIER = 5232654;
	
	/**
	 * 
	 * @param time	hh:mm
	 */
	public static void sheduleDailyAlarm(Context ctx, String time) {
		// get a Calendar object with current time
		 Calendar cal = Calendar.getInstance();
		
		 cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
		 cal.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
		 
		 Intent intent = new Intent(ctx, OnAlarmReceiver.class);

		 PendingIntent sender = PendingIntent.getBroadcast(ctx, ALARM_IDENTIFIER, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		 // Get the AlarmManager service
		 AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		 am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
	
		 Log.i("nfb", "Alarme enregistree pour " + time);
	}
}
