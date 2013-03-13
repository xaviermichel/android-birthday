package com.xmichel.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.xmichel.android.contactsManagement.Contact;
import com.xmichel.android.contactsManagement.ContactList;
import com.xmichel.android.contactsManagement.MyAdapter;
import com.xmichel.android.dataManagement.DataManager;
import com.xmichel.android.preferences.Preferences;


/**
 * @author xavier
 * Classe principale du programme
 */
public class NeverForgetBirthday extends ListActivity {
	
	/**
	 * Request code pour l'édition des contacts exclus de l'appli
	 */
	private static final int REQ_UPDATE_EXCLUDED_CONTACTS = 1989;

	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        MyAlarmManager.sheduleDailyAlarm(getApplicationContext(), Preferences.getHourOfAlarm(getApplicationContext()));
        
        updateContactList();
        
		// gestion du clique pour exclure un contact
		getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				
				AdapterView.AdapterContextMenuInfo info; 
		        try { 
		             info = (AdapterView.AdapterContextMenuInfo) menuInfo; 
		        } catch (ClassCastException e) { 
		            return; 
		        } 
				
				// une petite vibration (retour harpique)
				Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(75);
		        
		        
				final Contact c = (Contact)getListAdapter().getItem(info.position);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setMessage("Voulez-vous ajouter " + c.getName() + " aux contacts cachés de cette application ?")
				.setCancelable(false)
				.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						DataManager.addExcludedContacts(getApplicationContext(), c);
						updateContactList();
					}
				})
				.setNegativeButton("Non", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				
				builder.create().show();
				
			}
		});
	}


	/**
	 * Met à jour la liste des contacts
	 */
	public void updateContactList() {
        setListAdapter(new MyAdapter(this, OnAlarmReceiver.getContacts(this)));		
	}

    
    
    /**
     * Création du menu qui permet d'ajouter un contact
     */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /**
     * Sélection d'un item du menu
     */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {  
    	
    	case R.id.refresh_list :
    		updateContactList();
    		return true;
    		
    	case R.id.quit :
    		finish();
    		return true;
    		
		case R.id.editpref :
			Intent myIntent = new Intent(getApplicationContext(), Preferences.class);
	        startActivityForResult(myIntent, 0);
			return true;
			
		case R.id.editexludedcontacts :
    		Intent i = new Intent();
    		i.setClass(getApplicationContext(), ContactList.class);
    		this.startActivityForResult(i, REQ_UPDATE_EXCLUDED_CONTACTS);
			return true;
			
		case R.id.about :

			String html = getString(R.string.str_about);
			String mime = "text/html";
			String encoding = "utf-8";
			
			final WebView message = new WebView(this);
			//message.set(Html.fromHtml(getString(R.string.str_about)));
			message.loadDataWithBaseURL(null, html, mime, encoding, null);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.app_name)
				.setCancelable(true)
				.setIcon(R.drawable.icone)
				.setPositiveButton("Ok", null)
				.setView(message)
				.create();
	    	
	    	AlertDialog alert = builder.create();
    		alert.show();
			
			return true;
			
		default : 
			break;
    	}

    	return false;
    }
    
    /**
     * Pour savoir quand l'édition de contacts exclus est terminée et reconstruire la liste
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if ( requestCode == REQ_UPDATE_EXCLUDED_CONTACTS ) {
    		updateContactList();
    	}
    }
    
}
