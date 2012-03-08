package edu.caltech.cs141b.hw5.android;

import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DocumentDisplayActivity extends Activity {

	private static String TAG = "DocumentDisplayActivity";
	private CollabServiceWrapper service;
	private MyObjects mObjects;
	private Button save;
	private Button lock;
	private Button refresh;
	private EditText text1;
	private EditText text2;
	
	public void onCreate(Bundle savedInstanceState) {
		// Set the user interface
        super.onCreate(savedInstanceState);
        setContentView(R.layout.documentdisplay);
        save = (Button) findViewById(R.id.save);
        lock = (Button) findViewById(R.id.lock);
        refresh = (Button) findViewById(R.id.refresh);
        text1 = (EditText) findViewById(R.id.title);
		text2 = (EditText) findViewById(R.id.contents);
        
		// Get appropriate objects for accessing service calls
		// and global variables
        service = new CollabServiceWrapper();
        mObjects = (MyObjects)getApplicationContext();
        
        Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		
		// If calling display from the document list
		if (extras.getString("method").equals("list")) {
			
			String key = extras.getString("documentKey");
			if (key == null) {
				return;
			} else if (key.equals("null")) {
				// Create new document.
				discardExisting(null);
				LockedDocument doc = new LockedDocument(null, null, null,
						"Enter the document title.",
						"Enter the document contents.");
				mObjects.setLockedDoc(doc);
				setView(doc);
			} else {
				LockedDocument lockedDoc = mObjects.getLockedDoc();
				if (lockedDoc != null && key.equals(lockedDoc.getKey())) {
					// if the user already start edit one document, this allows
					// the user to get back the same window with unsaved changes.
					setView(lockedDoc);
				} else {
					// if the user hasn't had start editing any document yet, 
					// discard the document is necessary, and get the new document
					// the user choose.
					discardExisting(key);
					getDocument(key);
				}
			}
		} else if (extras.getString("method").equals("main")) {
			// This allow user to see document chosen earlier.
			UnlockedDocument unLockedDoc = mObjects.getUnlockedDoc();
			LockedDocument lockedDoc = mObjects.getLockedDoc();
			if (unLockedDoc != null) {
				setView(unLockedDoc);
			} else if (lockedDoc != null) {
				setView(lockedDoc);
			}
		}
        
    }
	
	/*
	 * This method check if the user select a new document. If yes,
	 * release lock on the previous one is necessary.
	 */
	private void discardExisting(String newKey) {
		LockedDocument lockedDoc = mObjects.getLockedDoc();
		UnlockedDocument unlockedDoc = mObjects.getUnlockedDoc();
		if (lockedDoc != null) {
			if (lockedDoc.getKey() == null) {
				mObjects.addStatusList("Discarding new document.");
				Toast.makeText(this, "Discarding new document.", Toast.LENGTH_LONG).show();
			}
			else if (!lockedDoc.getKey().equals(newKey)) {
				try {
					service.releaseLock(lockedDoc);
				} catch (LockExpired e) {
					e.printStackTrace();
				} catch (InvalidRequest e) {
					e.printStackTrace();
				}
			}
			else {
				// Newly active item is the currently locked item.
				return;
			}
			lockedDoc = null;
		} else if (unlockedDoc != null) {
			if (unlockedDoc.getKey().equals(newKey)) return;
			unlockedDoc = null;
		}
	}
	
	/*
	 * Set window view if displaying a unlocked document.
	 */
	private void setView(UnlockedDocument doc) {
		text1.setText(doc.getTitle());
		text2.setText(doc.getContents());
		text1.setFocusable(false);
		text1.setEnabled(false);
		text2.setFocusable(false);
		text2.setEnabled(false);
		save.setEnabled(false);
		lock.setEnabled(true);
		refresh.setEnabled(true);
	}
	
	/*
	 * Set window view if displaying a unlocked document.
	 */
	private void setView(LockedDocument doc) {
		text1.setText(doc.getTitle());
		text2.setText(doc.getContents());
		text1.setFocusableInTouchMode(true);
		text1.setEnabled(true);
		text2.setFocusableInTouchMode(true);
		text2.setEnabled(true);
		lock.setEnabled(false);
		refresh.setEnabled(false);
		save.setEnabled(true);
	}
	
	/*
	 * Event handler when "Go Back to Main Menu" is called.
	 */
	public void mainMenu(View view) {
		
		// Temporary save if any changes are made, so we can display
		// to the user later.
		LockedDocument mlocked = mObjects.getLockedDoc();
		if (mlocked != null) {
			mlocked.setTitle(text1.getText().toString());
			mlocked.setContents(text2.getText().toString());
		}
    	Intent intent = new Intent().setClass(this, CollaboratorAndroidActivity.class);
    	startActivity(intent);
    }
	
	/*
	 * Event handler for button refresh
	 */
	public void refresh(View view) {
		if (mObjects.getUnlockedDoc() != null) {
			getDocument(mObjects.getCurrentDocumentKey());
		}
    }
    
	/*
	 * Event handler for button lock
	 */
    public void lock(View view) {
    	
    	// Do not perform lock unless there's a unlocked doc chosen.
    	if (mObjects.getUnlockedDoc() == null)
    		return;
    	
    	try {
			mObjects.setWaitingKey(mObjects.getCurrentDocumentKey());
			LockedDocument locked = service.lockDocument(mObjects.getCurrentDocumentKey());
			
			// If expected document is returned.
			if (locked.getKey().equals(mObjects.getCurrentDocumentKey())) {
				mObjects.addStatusList("Lock retrieved for document.");
				mObjects.setLockedDoc(locked);
				setView(locked);
			} else {
				// Otherwise, unlock the locked document.
				mObjects.addStatusList("Got lock for document which is no longer active. Releasing lock.");
				releaseLock(locked);
			}
		} catch (InvalidRequest e) {
			Toast.makeText(this, "Error retrieving lock", Toast.LENGTH_LONG).show();
			mObjects.addStatusList("Error retrieving lock: " + e.getMessage());
		} catch (LockUnavailable e) {
			Toast.makeText(this, "Lock unavailable...Please wait", Toast.LENGTH_LONG).show();
			mObjects.addStatusList("Lock unavailable...Please wait");
		}
    }
    
	/*
	 * Event handler for button save
	 */
    public void save(View view) {
    	
    	// Cannot perform save unless there's a locked document.
    	LockedDocument mlocked = mObjects.getLockedDoc();
    	if (mlocked == null)
    		return;
    	
    	try {
    		mlocked.setTitle(text1.getText().toString());
        	mlocked.setContents(text2.getText().toString());
    		mObjects.addStatusList("Attemping to save document.");
    		UnlockedDocument unlocked = service.saveDocument(mlocked);
    		
    		// If expected document is saved, display the document.
	    	if (mObjects.getWaitingKey() == null || 
	    			mObjects.getWaitingKey().equals(unlocked.getKey())) {
	    		mObjects.addStatusList("Document '" + unlocked.getTitle()
	    				+ "' successfully saved.");
	    		setView(unlocked);
				mObjects.setUnlockedDoc(unlocked);
			} else {
				// Otherwise, lock the error.
				Log.d(TAG,"Saved document is not the anticipated document.");
			}
		} catch (InvalidRequest e) {
			Toast.makeText(this, "Error saving document due to invalid request", 
					Toast.LENGTH_LONG).show();
			mObjects.addStatusList("Error saving document: " + e.getMessage());
			
			// Release the lock if there's error saving document.
			releaseLock(mlocked);
		} catch (LockExpired e) {
			Toast.makeText(this, "Lock expired", Toast.LENGTH_LONG).show();
			mObjects.addStatusList("Lock had already expired; save failed.");
		}
		
		// If saving is not successful, modify client's local documents state as well.
		// This part will only get called if exception were caught. mlocked is set
		// to null if document is saved correctly. 
		if (mlocked != null) {
			mObjects.setUnlockedDoc(mlocked.unlock());
			setView(mlocked.unlock());
		}
    }
    
    /*
     * Release the lock of a locked document.
     */
    public void releaseLock(LockedDocument lockedDoc) {
        try {
        	service.releaseLock(lockedDoc);
        	mObjects.addStatusList("Document lock released.");
        } catch (InvalidRequest e) {
        	mObjects.addStatusList("Error saving document: " + e.getMessage());
        	Toast.makeText(this, "Error releasing document due to invalid request", 
                                    Toast.LENGTH_LONG).show();
        } catch (LockExpired e) {
        	mObjects.addStatusList("Lock had already expired; release failed.");
        	Toast.makeText(this, "Lock expired", Toast.LENGTH_LONG).show();
        }
    }
    
    /*
     * Get document based on the document key.
     */
	public void getDocument(String key) {
		try {
			mObjects.setWaitingKey(key);
			UnlockedDocument unlocked = service.getDocument(key);
			
			// If expected document is returned, display the document.
			if (unlocked.getKey().equals(key)) {
				mObjects.setUnlockedDoc(unlocked);
				mObjects.addStatusList("Document " + unlocked.getTitle() + " successfully retrieved");
				setView(unlocked);
			} else {
				mObjects.addStatusList("Returned document that is no longer expected; discarding.");
			}
		} catch (InvalidRequest e) {
			mObjects.addStatusList("Error retrieving lock: " + e.getMessage());
		}
	}

    @Override
    public void onBackPressed() {
        // disable the back button on this page.
        return;
    }

}
