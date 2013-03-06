package com.xmichel.android.preferences;

import com.xmichel.android.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    }
    
}
