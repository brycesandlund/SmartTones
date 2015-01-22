package texting.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class TextingAppActivity extends ListActivity {
	
	/**
	 * Tracks what letter name is being modified. -1 means default.
	 */
	private int currentPosition = -2;
	public static final String PREFS_NAME = "SmartTonesRingtones";
	public static final String PREF_PREFIX = "Ringtone";
	private List<String> names;
	private ArrayAdapter<String> thisAdapter;
	public static int maxLetters = 15;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        names = new ArrayList<String>();
        for (int i = 1; i <= maxLetters; ++i)
        {
        	names.add("");
        }
        
        thisAdapter = new ArrayAdapter<String>(this, R.layout.list_item, names);
        for (int i = 1; i <= maxLetters; ++i)
        {
        	setButton(i);
        }
        setListAdapter(thisAdapter);
        setDefaultButton();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	currentPosition = position + 1;
    	selectRingtone(currentPosition);
    }
    
    public void setDefaultClicked(View v)
    {
    	currentPosition = -1;
    	selectRingtone(currentPosition);
    }
    
    private void selectRingtone(int position)
    {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	String existing = settings.getString(getPrefKey(currentPosition), "");
    	Uri previous;
    	if (existing.length() == 0)
    	{
    		previous = null;
    	}
    	else
    	{
    		previous = Uri.parse(existing);
    	}
    	
    	Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, previous);
    	this.startActivityForResult(intent, 5);
    }
    
    /**
     * Converts number of letters to a String shared preference query
     * @param position
     * @return
     */
    public static String getPrefKey(int position)
    {
    	return PREF_PREFIX + position;
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
             Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

        	 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	 SharedPreferences.Editor editor = settings.edit();
             
             if (uri != null)
             {
            	 String location = uri.toString();
            	 editor.putString(getPrefKey(currentPosition), location);
             }
             else
             {
            	 editor.putString(getPrefKey(currentPosition), "");
             }
             editor.commit();
             if (currentPosition == -1)
             {
             	setDefaultButton();
             }
             else
             {
            	 setButton(currentPosition);
             }
         }
    }
    
    /**
     * Returns the ringtone name as a String given a ringtone Uri
     * @param uri
     * @param context
     * @return
     */
    public static String getRingtoneName(Uri uri, Context context)
    {
    	Ringtone r = RingtoneManager.getRingtone(context, uri);
    	return r.getTitle(context);
    }
    
    /**
     * Sets default text ringtone
     */
    private void setDefaultButton()
    {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	Button defaultButton = (Button)findViewById(R.id.buttonDefaultRingtone);
     	String text = settings.getString(getPrefKey(-1), "");
     	if (text.length() == 0)
     	{
     		defaultButton.setText("Default Notification");
     	}
     	else
     	{
     		defaultButton.setText("Default: " + getRingtoneName(Uri.parse(text), this));
     	}
    }
    
    /**
     * Resets the visual button according the the SharedPreference value
     * @param button
     */
    private void setButton(int button)
    {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	String name = settings.getString(getPrefKey(button), "");
		if (name.length() == 0)
		{
			name = button + " - Default";
		}
		else
		{
			name = button + " - " + getRingtoneName(Uri.parse(name), this);
		}
		names.set(button - 1, name);
		thisAdapter.notifyDataSetChanged();
    }
}