package com.xmichel.android.contactsManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Toast;

import com.xmichel.android.dataManagement.DataManager;

/**
 * Ecran qui va afficher la liste des contacts exlus de l'application avec la possibilité de les remettres
 */
public class ContactList extends ListActivity {

	/**
	 * Liste des contacts affichés
	 */
	private List<Contact> contacts;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		drawContactsList();

		// gestion du clique pour remettre un contact
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
				builder.setMessage("Voulez-vous remettre " + c.getName() + " dans les contacts visibles de cette application ?")
				.setCancelable(false)
				.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						DataManager.removeExcludedContacts(getApplicationContext(), c);
						drawContactsList();
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
	
	
	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * Obtient la liste des contacts associés
	 *
	 * @param a
	 * 			L'activité qui demande la liste
	 *
	 * @return La liste des contacts associés à l'application
	 */
	public static List<Contact> getExcludedContactList(Activity a)
	{
		Set<String> excludedIDs = DataManager.loadExcludedContacts(a.getApplicationContext());
    	
        List<Contact> lc = new ArrayList<Contact>();
        
        Uri dataUri = ContactsContract.Data.CONTENT_URI;
    	
    	String[] projection = new String[] { 
    			ContactsContract.Contacts.DISPLAY_NAME,           
	            ContactsContract.CommonDataKinds.Event.CONTACT_ID,
	            ContactsContract.CommonDataKinds.Event.START_DATE
	            };

    	Cursor c = a.getApplicationContext().getContentResolver().query(
    		       dataUri,
    		       projection,
    		       ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
    		       , null, null);
    	
    	ArrayList<String> todayBirthdays = new ArrayList<String>();
    	
    	if (c.moveToFirst()) {
	    	do {
	    		
	    		String id = c.getString(1);
	
	    		// exclu ?
	    		if ( ! excludedIDs.contains(id) ) {
	    			continue;
	    		}
	    		
	    		String name = c.getString(0);	  
	    		String birthdayDate = c.getString(2);
	    		
		        Pattern pattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
				Matcher matcher = pattern.matcher(birthdayDate);
		        if(matcher.matches()){
		        	
		        	Bitmap photo = Contact.loadContactPhoto(a.getApplicationContext(), Long.decode(c.getString(1)));
		        	
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
    	
    	return lc;
	}


	/**
	 * Affiche la liste des contacts dans le tableau
	 */
	private void drawContactsList() {
		contacts = getExcludedContactList(this);
		setListAdapter(new MyAdapter(this, contacts));
		if ( contacts.isEmpty() ) {
			Toast.makeText(getApplicationContext(), "Aucun contact exclu de l'application", Toast.LENGTH_SHORT).show();
		}
	}
  

}


