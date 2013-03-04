package com.xmichel.android.contactsManagement;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

import com.xmichel.android.R;

/**
 * @author xavier
 * Classe qui modélise un contact
 */
public class Contact implements Comparable<Contact> {
	
	/**
	 * Date d'anniversaire du contact
	 */
	private GregorianCalendar m_birthday;
	
	/**
	 * Date de comparaison, la prochaine date d'anniversaire du contact
	 */
	private GregorianCalendar m_compareDate;
    
	/**
	 * Le nom de la personne
	 */
	private String name;
	
	/**
	 * L'image de la personne, si aucune alors elle prendra l'icône de l'application
	 */
    private Bitmap bitmap;
    
    /**
     * L'identifiant android du contact
     */
    private String id;
    
    
    /**
     * Constuit un contact
     * @param id
     * 			L'identifiant android du contact
     * @param nom
     * 			Le nom du contact
     * @param img
     * 			L'image du contact
     * @param year
     * 			L'année de naissance du contact
     * @param month
     * 			Le mois de naissance du contact
     * @param day
     * 			Le jour de naissance du contact
     */
    public Contact(String id, String nom, Bitmap img, int year, int month, int day) {
    	setID(id);
        setName(nom);
        setBitmap(img);
        setBirthday(year, month, day); 
    }
    
    
    /**
     * Défini l'id du contact
     * 
     * @param id
     * 			L'idenfifiant android du contact
     */
    private void setID(String id) {
    	this.setId(id);
    }
    
    
    /**
     * Obient l'image de la personne
     * 
     * @return L'image de la personne
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    
    /**
     * Défini l'image de la personne
     * 
     * @param bitmap L'image de la personne
     */
    private void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    

    /**
     * Obient le nom du contact
     * 
     * @return Le nom du contact
     */
    public String getName() {
        return name;
    }

    
    /**
     * Défini le nom du contact
     * 
     * @param name 
     * 			Le nom du contact
     */
    private void setName(String name) {
        this.name = name;
    }

    
    public String getInfo() {
    	
		GregorianCalendar today = Today.get();
		
		long diffMs = m_compareDate.getTime().getTime() - today.getTime().getTime();
		
		// voir http://www.developpez.net/forums/d280905/java/general-java/apis/java-util/calculer-nombre-mois-entre-2-dates/
		int dayCount = (int) (diffMs/1000/60/60/24);

		String ret = String.valueOf(dayCount);
		
		if (today.equals(m_compareDate)) {
			return "Anniversaire aujourd'hui ! (" + age() + " ans)";
		}
		return age() + " ans " + (dayCount>1 ? "dans " + ret + " jours" : "demain !");
    }
    
    
    public boolean birthdayIsToday() {
		GregorianCalendar today = Today.get();
		return (today.equals(m_compareDate)); 
    }
    
    
    /**
     * Renvoi l'age d'un contact sous la forme d'une chaine
     * 
     * @return L'age du contact, par exemple : 18
     */
	public String age() {
		return String.valueOf(m_compareDate.get(Calendar.YEAR) - m_birthday.get(Calendar.YEAR));
	}
    
	
	/**
	 * Obtient un chaine représentant la date d'anniversaire
	 * 
	 * @return La date d'anniversaire au format dd month yyyy, par exemple 16 juillet 1995
	 */
    public String getBirthday() {
        return m_birthday.get(Calendar.DAY_OF_MONTH) + " " + MyMonth.monthLabel(m_birthday.get(Calendar.MONTH)) + " " + m_birthday.get(Calendar.YEAR);
    }
    
    
    /**
     * Défini la date d'anniversaire d'un contact
     * 
     * @param year
     * 				L'année de naissance
     * @param month
     * 				Le mois de naissance
     * @param day
     * 				Le jour de naissance
     */
    private void setBirthday(int year, int month, int day) {
		m_birthday = new GregorianCalendar();	
		m_birthday.set(year, month, day, 0, 0, 0);
		m_birthday.set(Calendar.MILLISECOND, 0);
		fixCompare();
    }

    
	private void fixCompare() {
		
		m_compareDate = Today.get();	
		m_compareDate.set(m_birthday.get(Calendar.YEAR), m_birthday.get(Calendar.MONTH), m_birthday.get(Calendar.DAY_OF_MONTH));
		m_compareDate.set(Calendar.MILLISECOND, 0);
		
		GregorianCalendar today = Today.get();
		
		//Log.d("DATE=========================================  => ",Today.get().toString());
		
		m_compareDate.set(Calendar.YEAR, today.get(Calendar.YEAR));
		
		if ( m_compareDate.before(today) ) {
			m_compareDate.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1 );
		}
	}
 
	
	
	public int compareTo(Contact another) { 
		return m_compareDate.compareTo(another.m_compareDate);
	}
	
	
	/**
	 * Classe qui défini ce qui est aujourd'hui
	 * @author xavier
	 *
	 */
	private static class Today {
		/**
		 * Renvoi le jour courant
		 * 
		 * @return Le jour courant
		 */
		public static GregorianCalendar get() {
			GregorianCalendar today = new GregorianCalendar();
			today.set(Calendar.HOUR, 12);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MILLISECOND, 0);
			return today;
		}
	}
	
	
	/**
	 * Renvoi la photo du contact donné
	 * 
	 * @param context
	 * 				Le context de l'appli qui veut la photo du contact
	 * @param contactId
	 * 				L'identifiant du contact
	 * 
	 * @return La photo du contact ou l'icône de l'application si le contact n'a pas de photos
	 */
    public static Bitmap loadContactPhoto(Context context, Long contactId) {
        Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        ContentResolver cr=(ContentResolver) context.getContentResolver();
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, contactPhotoUri);
        Bitmap b = BitmapFactory.decodeStream(input);
        if(b==null){
                return ((BitmapDrawable)context.getResources().getDrawable(R.drawable.icone)).getBitmap();
        }else{
                return b;
        }
    }
	
	
	/**
	 * Les mois de l'année en Français
	 * @author xavier
	 *
	 */
	private static class MyMonth {
		
		/**
		 * Récupère le mois donné en français
		 * 
		 * @param month
		 * 				Le mois dont on souhaite avoir le nom [0-11]
		 * 
		 * @return Le nom du mois donné
		 */
		static String monthLabel(int month) {
			switch(month) {
			case 0 :
				return "janvier";
			case 1 :
				return "février";
			case 2 : 
				return "mars";
			case 3 :
				return "avril";
			case 4 :
				return "mai";
			case 5 :
				return "juin";
			case 6 :
				return "juillet";
			case 7 : 
				return "août";
			case 8 :
				return "septembre";
			case 9 :
				return "octobre";
			case 10 :
				return "novembre";
			case 11 : 
				return "décembre";
			default :
				return "WTF ? (myMonth)";
			}
		}
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getId() {
		return id;
	}
	
}
