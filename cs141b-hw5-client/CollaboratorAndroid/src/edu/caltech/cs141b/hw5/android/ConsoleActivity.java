package edu.caltech.cs141b.hw5.android;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Creates a console window where status of the application is updated.
 * 
 * @author Judy Mou, Tuling Ma, Lucia Ahn
 *
 */
public class ConsoleActivity extends Activity{
	private MyObjects mObjects;
	
	/** 
	 * Called when the activity is first created
	 * 
	 * @param savedInstanceState 
	 */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mObjects = (MyObjects)getApplicationContext();
        
        // Display text on the user
        TextView textview = new TextView(this);
        textview.setText("Console\n");
        setContentView(textview);
        
        if (mObjects.getStatusList() != null) {
            showStatus(mObjects.getStatusList(), textview);
        } 
    }
    
	/**
	 * Show status stored in a status list on console window
	 * 
	 * @param statusList
	 * @param textview
	 */
    public void showStatus(ArrayList<String> statusList, TextView textview){
    	Iterator<String> itr = statusList.iterator();
    	Toast.makeText(this, "This is console show status.", Toast.LENGTH_LONG).show();
    	// Iterates the status update in the list and prints them in chronological order
    	while (itr.hasNext()){
    		String text = itr.next();
        	textview.append(text + "\n");
    	}
    	
    	setContentView(textview);
    }
}
