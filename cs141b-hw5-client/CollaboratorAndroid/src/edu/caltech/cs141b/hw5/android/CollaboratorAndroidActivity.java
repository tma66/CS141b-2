package edu.caltech.cs141b.hw5.android;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class CollaboratorAndroidActivity extends Activity {
	
	private static String TAG = "AndroidActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "starting activity");
        String docsInfo = "";
        
        // Test getting the document list and print it out on screen
        CollabServiceWrapper service = new CollabServiceWrapper();      
        List<DocumentMetadata> metas = service.getDocumentList();
      
        for (DocumentMetadata meta : metas) {
        	docsInfo += meta.getKey() + ": " + meta.getTitle() + "\n"; 
        }
        
        // Try lock and unlocking a document
        try {
			LockedDocument ld = service.lockDocument(metas.get(0).getKey());
			Log.i(TAG, "locked");
			
			// try modify and save the document
			LockedDocument mld = new LockedDocument(ld.getLockedBy(), 
					ld.getLockedUntil(), ld.getKey(), ld.getTitle() + " mod1", ld.getContents());
			service.saveDocument(mld);
			Log.i(TAG, "saved");
			
			// Should get lock expired here
			service.releaseLock(ld);
			Log.i(TAG, "unlocked");
        } catch (LockExpired e) {
        	Log.i(TAG, "lock expired when attemping release.");
		} catch (LockUnavailable e) {
			Log.i(TAG, "Lock unavailable.");
		} catch (InvalidRequest e) {
			Log.i(TAG, "Invalid request");
		}
        
        TextView tv = new TextView(this);
        tv.setText(docsInfo);
        setContentView(tv);
        //setContentView(R.layout.main);
    }
}