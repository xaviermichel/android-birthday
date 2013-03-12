package com.xmichel.android.preferences;

import com.xmichel.android.MyAlarmManager;
import com.xmichel.android.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String DEFAULT_HOUR = "8:00";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            
            PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MyAlarmManager.sheduleDailyAlarm(getApplicationContext(), getHourOfAlarm(getApplicationContext()));
        Log.i("nfb", "heure sauvegardee : " + getHourOfAlarm(getApplicationContext()));
        Toast.makeText(this, "Heure de répétition sauvegardée", Toast.LENGTH_SHORT).show();
    }
    
    
    public static String getHourOfAlarm(Context ctx) {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	
    	// set default value if not exists
    	String update_hour = prefs.getString("refresh_hour", "NOT_SETTED_YET");
    	
    	if (update_hour.equals("NOT_SETTED_YET")) {
    		SharedPreferences.Editor prefsEditor = prefs.edit();
    		prefsEditor.putString("refresh_hour", DEFAULT_HOUR);
    		prefsEditor.commit();
    		
    		Log.i("nfb", "Alarme initiale enregistrée !");
    	}

    	// return the current value
    	return prefs.getString("refresh_hour", DEFAULT_HOUR);
    }
}
