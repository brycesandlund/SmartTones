package texting.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	
	/**
	 * No longer used default hardcoded values.
	 */
	public static final String defaultSound = "Proxima";
	public static final String directory = "file:///system/media/audio/notifications/";
	public static final String[] sounds = new String[]{"", "", "", "Bellatrix", "Hojus", "Tejat", "Lalande"};

	/**
	 * Gets the contact name given an SmsMessage. Returns null if no contact is found.
	 * @param context
	 * @param message
	 * @return
	 */
	public static String getContactByAddr(Context context, SmsMessage message) 
	{  
	    Uri personUri = Uri.withAppendedPath(  
	            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, message.getOriginatingAddress());  
	    Cursor cur = context.getContentResolver().query(personUri,  
	            new String[] { PhoneLookup.DISPLAY_NAME },  
	            null, null, null );  
	    if( cur.moveToFirst() ) 
	    {  
	        int nameIdx = cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);  
	        String name = cur.getString(nameIdx);
	       cur.close();  
	       return name;  
	   }  
	   return null; 
	}
	
	/**
	 * Returns the default ringtone Uri, or the default notification Uri if no default ringtone Uri has been set
	 * @param context
	 * @return
	 */
	private Uri getDefaultTone(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(TextingAppActivity.PREFS_NAME, 0);
		
		Uri returnValue;
		String tone = settings.getString(TextingAppActivity.getPrefKey(-1), "");
		
		if (tone.length() == 0)
		{
			returnValue = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		}
		else
		{
			returnValue = Uri.parse(tone);
		}
		
		return returnValue;
	}
	
	/**
	 * Retrieves the ringtone Uri given the number of letters in the contact's first name
	 * @param nLetters
	 * @param context
	 * @return
	 */
	private Uri getTone(int nLetters, Context context)
	{		
		SharedPreferences settings = context.getSharedPreferences(TextingAppActivity.PREFS_NAME, 0);
		String tone = settings.getString(TextingAppActivity.getPrefKey(nLetters), "");
		
		Uri returnValue;
		
		if (tone.length() == 0)
		{
			returnValue = getDefaultTone(context);
		}
		else
		{
			returnValue = Uri.parse(tone);
		}

		return returnValue;
	}
	
	public void onReceive(Context context, Intent intent) {

		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			Object[] pdusObj = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdusObj.length];

			// getting SMS information from Pdu.
			for (int i = 0; i < pdusObj.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
			}

			for (SmsMessage currentMessage : messages) {
				
				String name = getContactByAddr(context, currentMessage);
				Uri tone;
				if (name == null)
				{
					tone = getDefaultTone(context);
				}
				else
				{
					name = name.split("\\s")[0];
					tone = getTone(name.length(), context);
				}
				
				Ringtone r = RingtoneManager.getRingtone(context, tone);
				if (r != null)
				{
					r.play();
				}
			}
		}
	}
}
