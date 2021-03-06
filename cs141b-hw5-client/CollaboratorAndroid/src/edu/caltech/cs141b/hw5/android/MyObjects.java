package edu.caltech.cs141b.hw5.android;

import java.util.ArrayList;

import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import android.app.Application;
import android.content.res.Configuration;

/**
 * Maintains a global application state. Global variables will be
 * instantiated when the process for Collaborator Document is created.
 * 
 * @author Judy Mou, Tuling Ma, Lucia Ahn
 *
 */
public class MyObjects extends Application {
	private LockedDocument lockedDoc;
	private UnlockedDocument readOnlyDoc;
	private static MyObjects singleton;
	private String waitingKey;
	private ArrayList<String> statusList = new ArrayList<String>();

	/**
	 * Get MyObjects instance.
	 * 
	 * @return MyObjects class object
	 */
	public MyObjects getInstance() {
		return singleton;
	}
	
	/**
	 * Called by the system when the device configuration changes 
	 * while the program component is running
	 * 
	 * @param newConfig
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Called when MyObject is first created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}

	/**
	 * Called when the overall system is running low in memory
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	/**
	 * Get a document by key
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	
	/**
	 * Get locked document
	 *
	 * @return locked document
	 */
	public LockedDocument getLockedDoc(){
		return lockedDoc;
	}

	/**
	 * Get unlocked document
	 *
	 * @return read-only document
	 */
	public UnlockedDocument getUnlockedDoc(){
		return readOnlyDoc;
	}
	
	/**
	 * Get status list 
	 *
	 * @return status list
	 */
	public ArrayList<String> getStatusList(){
		return statusList;
	}
	
	/**
	 * Set locked document
	 * 
	 * @param doc
	 */
	public void setLockedDoc(LockedDocument doc) {
		lockedDoc = doc;
		readOnlyDoc = null;
	}
	
	/**
	 * Set unlocked document
	 * 
	 * @param doc
	 */
	public void setUnlockedDoc(UnlockedDocument doc) {
		readOnlyDoc = doc;
		lockedDoc = null;
	}
	
	/**
	 * Get a key of the current document
	 *
	 * @return a string key
	 */
	public String getCurrentDocumentKey() {
		if (readOnlyDoc != null) {
			return readOnlyDoc.getKey();
		} else {
			return lockedDoc.getKey();
		}
	}
	
	/**
	 * Add a status update to a status list
	 * 
	 * @param status
	 */
	public void addStatusList(String status){
		//A status list can hold up to 20 statuses
		if (statusList.size() > 20){
			//If the list has already 20 statuses when adding a new one,
			//remove the oldest status from the list
			statusList.remove(0);
		} else {
			statusList.add(status);
		}
	}

	/**
	 * Set a waiting key
	 * 
	 * @param waitingKey
	 */
	public void setWaitingKey(String waitingKey) {
		this.waitingKey = waitingKey;
	}

	/**
	 * Get a waiting key
	 * 
	 * @return waiting key
	 */
	public String getWaitingKey() {
		return waitingKey;
	}
	
}
