/*********************************************************************************************
 * 
 *
 * 'SVNUtils.java', in plugin 'msi.gama.documentation', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.doc.util;

import msi.gama.doc.Constants;

import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.*;

public class SVNUtils {
	
	/**
	 * @param args
	 * @throws SVNException 
	 */
	public static void main(String[] args) throws SVNException {
		checkoutSVNGamaDoc();
	}
	
	public static void checkoutSVNGamaDoc() throws SVNException {
		DAVRepositoryFactory.setup();
		File localPath = new File(Constants.SVN_FOLDER);
		BasicAuthenticationManager authManager = new BasicAuthenticationManager("", "");
		SVNURL url = SVNURL.parseURIEncoded(Constants.ADR_SVNGAMA);

		// Check out
		SVNUpdateClient	updateClient = new SVNUpdateClient(authManager, null);
		updateClient.setIgnoreExternals(false);

		updateClient.doCheckout(url, localPath, SVNRevision.HEAD, SVNRevision.HEAD, true);
		System.out.println("CHECKOUT Finished");
	}
}

