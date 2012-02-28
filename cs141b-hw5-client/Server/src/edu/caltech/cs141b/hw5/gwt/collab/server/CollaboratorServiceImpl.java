package edu.caltech.cs141b.hw5.gwt.collab.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import edu.caltech.cs141b.hw5.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw5.gwt.collab.shared.UnlockedDocument;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jdo.Query;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {
	
	private static final Logger log = Logger.getLogger(CollaboratorServiceImpl.class.toString());

	/* (non-Javadoc)
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocumentList()
	 */
	@Override
	public List<DocumentMetadata> getDocumentList() {
		// Instantiate JDO-aware application components
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		Query query = pm.newQuery(DocumentJDO.class);
		
		// Store list of the metadata of the current documents
		List<DocumentMetadata> docsList = new ArrayList<DocumentMetadata>();
		try {
			// Start transaction
			tx.begin();
			log.fine("Getting a list of currently available documents");
			
			// Get documents from query
			List<DocumentJDO> results = ((List<DocumentJDO>) query.execute());
			if (!results.isEmpty()) {
				// Add metadata of each document to docsList
				for (DocumentJDO d: results) {
					docsList.add(d.getDocumentMetdataObject());
				}
			}
			tx.commit();
		} finally {
			query.closeAll();
		}
		return docsList;
	}

	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {

		return lockDocument(documentKey, getThreadLocalRequest().getRemoteAddr());
	}

	public LockedDocument lockDocument(String documentKey, String ip)
			throws LockUnavailable {
		// Instantiate JDO-aware application components
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        LockedDocument lockedDoc = null;
        try {
        	// Start transaction
            tx.begin();   
            
            // Get document from datastore with given key
            DocumentJDO document = pm.getObjectById(DocumentJDO.class, KeyFactory.stringToKey(documentKey));
            log.fine("Attemping to lock " + document.getTitle() + ".");
            
            // Mark current date
            Date now = new Date();
            
            // Allow access only if document has no owner or if expiration date has passed
            // for previous owner. Otherwise, throw exception
            if (document.getLockedBy() == null || document.getLockedUntil().before(now)) {
            	// Lock document. Use client's IP address to determine document ownership
                lockedDoc = document.lock(getThreadLocalRequest().getRemoteAddr());
            } else {
            	log.fine("Lock for " + document.getTitle() + 
            			" is unavailable. Locked until: " + document.getLockedUntil().toString());
            	throw new LockUnavailable("Lock for " + document.getTitle() + 
            			" is unavailable. The lock will be released at or before " +
            			document.getLockedUntil().toString());
            }
            
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        } 
        return lockedDoc;
	}

	@Override
	public UnlockedDocument getDocument(String documentKey) {
		// Instantiate JDO-aware application components
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        UnlockedDocument unlockedDoc = null;
        try {
        	// Start transaction
            tx.begin();
            
            // Get document with given key and create UnlockedDocument object to return
            DocumentJDO document = pm.getObjectById(DocumentJDO.class, KeyFactory.stringToKey(documentKey));
            unlockedDoc = document.getUnlockedDocumentVersion();
            
            log.fine("Obtaining document: " + document.getTitle() + " for read-only purpose.");
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return unlockedDoc;
	}

	@Override
	public UnlockedDocument saveDocument(LockedDocument doc)
			throws LockExpired {
		return saveDocument(doc, getThreadLocalRequest().getRemoteAddr());
	}
	
	public UnlockedDocument saveDocument(LockedDocument doc, String ip)
			throws LockExpired {
		// Instantiate JDO-aware application components
		PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        UnlockedDocument unlockedDoc;
        DocumentJDO document;
        try {
        	// Start transaction
            tx.begin();
            
            // If there is no key, assume new document. Otherwise, try to save document
            if (doc.getKey() == null) {
                log.info("Adding document " + doc.getTitle() + " to persistence manager.");
                
            	// Create new document and store object in datastore
            	document = new DocumentJDO(doc.getTitle(), doc.getContents(),
            		doc.getLockedBy(), doc.getLockedUntil());
            	pm.makePersistent(document);
            } else {
            	log.fine("Updating document " + doc.getTitle() + " in persistence manager.");
            	
            	// Mark current date
            	Date now = new Date();
            	
            	// Get document from datastore with given key
            	document = pm.getObjectById(DocumentJDO.class, KeyFactory.stringToKey(doc.getKey()));
            	
            	// If time has not expired, update document contents. Otherwise, throw exception
            	if (now.before(doc.getLockedUntil())) {
            		document.setContents(doc.getContents());
            	} else {
            		throw new LockExpired("Document " + doc.getTitle() + " lock expired");
            	}
            }
            // Create read-only version and unlock document
            unlockedDoc = document.getUnlockedDocumentVersion();
            document.unlock();
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
		return unlockedDoc;
	}

	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
		releaseLock(doc, getThreadLocalRequest().getRemoteAddr());
		return;
	}

	public void releaseLock(LockedDocument doc, String ip) throws LockExpired {
		// Instantiate JDO-aware application components
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		
		DocumentJDO document;
		try {
			// Start transaction
			tx.begin();
			log.fine("Releasing " + doc.getTitle() + "'s lock.");
			
			// Mark current date
			Date currentDate = new Date();
			
			// If document already expired, throw exception. Otherwise, unlock document
			if (doc.getLockedUntil().before(currentDate)) {
				throw new LockExpired("Lock expired before attempting to release " +
						"document: " + doc.getTitle());
			} else {
				// Get document from datastore with given key
				document = pm.getObjectById(DocumentJDO.class, KeyFactory.stringToKey(doc.getKey()));
				document.unlock();
			}
			tx.commit();
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
		}
		return;
	}

}

