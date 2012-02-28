package edu.caltech.cs141b.hw5.gwt.collab.server;

import java.util.List;
import java.util.logging.Logger;

import edu.caltech.cs141b.hw5.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw5.gwt.collab.shared.UnlockedDocument;

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
		return null;
	}

	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {
		return lockDocument(documentKey, getThreadLocalRequest().getRemoteAddr());
	}

	public LockedDocument lockDocument(String documentKey, String ip)
			throws LockUnavailable {
		return null;
	}

	@Override
	public UnlockedDocument getDocument(String documentKey) {
		return null;
	}

	@Override
	public UnlockedDocument saveDocument(LockedDocument doc)
			throws LockExpired {
		return null;
	}
	
	public UnlockedDocument saveDocument(LockedDocument doc, String ip)
			throws LockExpired {
		return null;
	}

	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
		return;
	}

	public void releaseLock(LockedDocument doc, String ip) throws LockExpired {
		return;
	}

}

