/*********************************************************************************************
 * 
 * 
 * 'ErrorCollector.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.descriptions;

import static com.google.common.collect.Iterables.*;
import gnu.trove.set.hash.TLinkedHashSet;
import java.util.*;
import msi.gama.common.GamaPreferences;
import msi.gaml.compilation.GamlCompilationError;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class ErrorCollector implements Iterable<GamlCompilationError> {

	final static int MAX_SIZE = 1000;
	public static final ErrorCollector BuiltIn = new ErrorCollector();
	boolean hasSyntaxErrors;
	final URI resourceURI;
	final TLinkedHashSet<GamlCompilationError> importedErrors = new TLinkedHashSet();
	final TLinkedHashSet<GamlCompilationError> internalErrors = new TLinkedHashSet();
	final TLinkedHashSet<GamlCompilationError> warnings = new TLinkedHashSet();
	final TLinkedHashSet<GamlCompilationError> infos = new TLinkedHashSet();

	public ErrorCollector() {
		this(null);
	}

	public ErrorCollector(final Resource resource) {
		this(resource, false);
	}

	public ErrorCollector(final Resource resource, final boolean syntax) {
		this.resourceURI = resource == null ? URI.createURI("builtin://gaml", false) : resource.getURI();
		hasSyntaxErrors = syntax;
	}

	public void add(final GamlCompilationError error) {
		if ( error.isWarning() ) {
			if ( !GamaPreferences.WARNINGS_ENABLED.getValue() ) { return; }
		} else if ( error.isInfo() ) {
			if ( !GamaPreferences.INFO_ENABLED.getValue() ) { return; }
		}
		EObject object = error.getStatement();
		boolean sameResource = object == null || object.eResource().getURI().equals(resourceURI);
		if ( sameResource && error.isInfo() ) {
			infos.add(error);
		} else if ( sameResource && error.isWarning() ) {
			warnings.add(error);
		} else if ( error.isError() ) {
			if ( sameResource ) {
				internalErrors.add(error);
			} else {
				importedErrors.add(error);
			}
		}
	}

	public boolean hasInternalSyntaxErrors() {
		return hasSyntaxErrors;
	}

	public boolean hasErrors() {
		return hasSyntaxErrors || hasInternalErrors() || hasImportedErrors();
	}

	public boolean hasInternalErrors() {
		return internalErrors.size() > 0;
	}

	public boolean hasImportedErrors() {
		return importedErrors.size() > 0;
	}

	public Collection<GamlCompilationError> getInternalErrors() {
		return internalErrors;
	}

	public Collection<GamlCompilationError> getImportedErrors() {
		return importedErrors;
	}

	public Collection<GamlCompilationError> getWarnings() {
		return warnings;
	}

	public Collection<GamlCompilationError> getInfos() {
		return infos;
	}

	public void clear() {
		importedErrors.clear();
		internalErrors.clear();
		warnings.clear();
		infos.clear();
		hasSyntaxErrors = false;
	}

	/**
	 * Method iterator()
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<GamlCompilationError> iterator() {
		return limit(concat(getInternalErrors(), getImportedErrors(), getWarnings(), getInfos()), MAX_SIZE).iterator();
	}

}
