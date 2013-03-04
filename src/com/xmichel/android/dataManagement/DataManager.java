package com.xmichel.android.dataManagement;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.xmichel.android.contactsManagement.Contact;

import android.content.Context;
import android.util.Log;

/**
 * Classe qui va s'occuper de toute la gestion de l'enregistrement des données sur le téléphone
 */
public final class DataManager {

	
	/**
	 * Charge la liste des contacts à exclure de l'application
	 * 
	 * @return La liste des contacts à exclure de l'application sous la forme d'un set d'ID
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> loadExcludedContacts(Context c) {
		
		Set<String> data = new HashSet<String>();

		FileInputStream fIn = null; 
        ObjectInputStream isr = null; 
 
        try{ 
            fIn = c.openFileInput("contacts.dat");       
            isr = new ObjectInputStream(fIn); 
            data = (Set<String>) isr.readObject(); 
           
            Log.d(c.getClass().getName(), "loadAssociatedContacts -> ok");
        } 
        catch (Exception e) {
        	Log.d(c.getClass().getName(), "loadAssociatedContacts -> error : " + e.getMessage());
        }
        
		return data;
	}
	
	
	
	/**
	 * Ajout un contact à la liste des exlus de l'application
	 * 
	 * @param newContact
	 * 					Le contact à exclure
	 */
	public static void addExcludedContacts(Context c, Contact newContact) {
		
		Set<String> data = loadExcludedContacts(c);

		if ( data.contains(newContact.getId()) ) {
	        return;
		}
		
		data.add(newContact.getId());
		
        FileOutputStream fOut = null; 
        ObjectOutputStream osw = null; 
 
        try{ 
           fOut = c.openFileOutput("contacts.dat", Context.MODE_PRIVATE);       
            osw = new ObjectOutputStream(fOut); 
            osw.writeObject(data); 
            osw.flush(); 
          
            Log.d("DataManager", "saveAssociatedContacts -> ok");
        } 
        catch (Exception e) {       
        	Log.d("DataManager", "saveAssociatedContacts -> error : " + e.getMessage());
        } 
        finally { 
           try { 
                  osw.close(); 
                  fOut.close(); 
              } catch (IOException e) { 
            	  Log.d("DataManager", "saveAssociatedContacts -> error : " + e.getMessage());
              } 
        }
	}
	
	
	
	/**
	 * Enleve un contact qui avait été exclu de l'application
	 * 
	 * @param id
	 * 				Le contact a remettre dans l'appli
	 */
	public static void removeExcludedContacts(Context c, Contact contact) {

		Set<String> data = loadExcludedContacts(c);
		data.remove(contact.getId());
		
        FileOutputStream fOut = null; 
        ObjectOutputStream osw = null; 
 
        try{ 
           fOut = c.openFileOutput("contacts.dat", Context.MODE_PRIVATE);       
            osw = new ObjectOutputStream(fOut); 
            osw.writeObject(data); 
            osw.flush(); 
          
            Log.d(c.getClass().getName(), "saveAssociatedContacts -> ok");
        } 
        catch (Exception e) {       
        	Log.d(c.getClass().getName(), "saveAssociatedContacts -> error : " + e.getMessage());
        } 
        finally { 
           try { 
                  osw.close(); 
                  fOut.close(); 
              } catch (IOException e) { 
            	  Log.d("DataManager", "saveAssociatedContacts -> error : " + e.getMessage());
              } 
        }
	}
	
}
