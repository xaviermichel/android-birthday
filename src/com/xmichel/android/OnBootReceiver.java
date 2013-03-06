package com.xmichel.android;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
 
@Deprecated
public class OnBootReceiver extends BroadcastReceiver {
 
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this.getClass().getName(), "Got the Boot Event>>>");
        Log.d(this.getClass().getName(), "Starting Birthday service>>>");
        startBirthdayService(context);
    }
    
    
    /**
     * Lance le service qui vérifie s'il y a des annivs
     * 
     * @param context
     * 				
     */
    public static void startBirthdayService(Context context)
    {
        context.startService(
        						new Intent().setComponent(
												new ComponentName(
															context.getPackageName(), 
															BirthdayService.class.getName()
												)
        						)
        );
    }
    
}
