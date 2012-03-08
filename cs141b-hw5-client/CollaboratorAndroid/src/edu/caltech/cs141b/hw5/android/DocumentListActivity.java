package edu.caltech.cs141b.hw5.android;

import java.util.ArrayList;
import java.util.List;

import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DocumentListActivity extends ListActivity {

	private static String TAG = "DocumentListActivity";
	List<DocumentMetadata> metas = null;
	private MyObjects mObjects;
	
    public void onCreate(Bundle savedInstanceState) {
    	// Set up the window UI.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.documentlist);
        
        Log.d(TAG, "starting activity");
        // Enable "global variable" access.
        mObjects = (MyObjects)getApplicationContext();
        retrieveList();
	}
    
	private void retrieveList() {
		ArrayList<String> titles = new ArrayList<String>();
        
        // Test getting the document list and print it out on screen
		mObjects.addStatusList("Fetching document list");
        CollabServiceWrapper service = new CollabServiceWrapper();      
        metas = service.getDocumentList();
        
        // Do not display anything is no document is avail.
		if (metas == null || metas.size() == 0) {
			mObjects.addStatusList("No documents available.");
		}
		
		if (metas != null) {
			// Display the document as a list view.
			for (DocumentMetadata meta : metas) {
	        	titles.add(meta.getTitle());
	        }
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, titles);
			setListAdapter(adapter);
			mObjects.addStatusList("Document list updated.");
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Get the selected string.
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		
		// Start document display and send the document key over.
		Intent intent = new Intent().setClass(this, DocumentDisplayActivity.class);
		intent.putExtra("method", "list");
		intent.putExtra("documentKey", metas.get(position).getKey());
		startActivity(intent);
	}
	
	/*
	 * Event handler for create new button.
	 */
	public void createNew(View view) {
		// Displaying some default text for new document.
		Intent intent = new Intent().setClass(this, DocumentDisplayActivity.class);
		intent.putExtra("method", "list");
		intent.putExtra("documentKey", "null");
		startActivity(intent);
	}
	
	/*
	 * Event handler for refresh list button.
	 */
	public void refreshList(View view) {
		retrieveList();
	}
	
	/*
	 * Event handler for main menu button.
	 */
	public void mainMenu(View view) {
        Intent intent = new Intent().setClass(this, CollaboratorAndroidActivity.class);
        startActivity(intent);
	}
}
