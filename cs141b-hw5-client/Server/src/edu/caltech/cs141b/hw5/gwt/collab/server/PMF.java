package edu.caltech.cs141b.hw5.gwt.collab.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public class PMF {
	private static final PersistenceManagerFactory pmfInstance =
		JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	/**
	 * Empty Constructor for PMF object
	 */
	private PMF() {}
	
	/**
	 * Used to return a PersistenceManagerFactory instance
	 * 
	 * @param None
	 * @return pmfInstance : PersistenceManagerFactory instance
	 */
	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}
}
