/*********************************************************************************************
 * 
 * 
 * 'ExperimentDescription.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.descriptions;

import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.kernel.experiment.ExperimentAgent;
import msi.gama.util.TOrderedHashMap;
import msi.gaml.factories.ChildrenProvider;
import msi.gaml.statements.Facets;
import msi.gaml.types.IType;
import org.eclipse.emf.ecore.EObject;

public class ExperimentDescription extends SpeciesDescription {

	private Map<String, VariableDescription> parameters;

	// final ModelDescription model;

	// We assume experiments are firstly created *within* a model, in which case we can gather the enclosing argument
	// and keep it for when the relationship will be reversed (i.e. when the model will be *inside* the experiment)
	public ExperimentDescription(final String keyword, final IDescription enclosing, final ChildrenProvider cp,
		final EObject source, final Facets facets) {
		super(keyword, null, enclosing, null, cp, source, facets);
	}

	private void addParameterNoCheck(final VariableDescription var) {
		if ( parameters == null ) {
			parameters = new TOrderedHashMap();
		}
		parameters.put(var.getName(), var);
	}

	@Override
	protected void addVariableNoCheck(final VariableDescription var) {
		if ( var.getName().equals(SIMULATION) ) {
			// We are dealing with the built-in variable. We gather the precise type of the model itself and pass it
			// to the variable (instead of its generic "agent" type).
			final IType t = enclosing.getModelDescription().getType();
			var.setType(t);
		}
		super.addVariableNoCheck(var);
	}

	@Override
	public void addInheritedVariable(final VariableDescription var) {
		if ( var.getKeyword().equals(PARAMETER) ) {
			if ( parameters == null || !parameters.containsKey(var.getName()) ) {
				addParameterNoCheck(var);
			}
		} else {
			super.addInheritedVariable(var);
		}
	}

	@Override
	public void addOwnVariable(final VariableDescription var) {
		if ( !var.getKeyword().equals(PARAMETER) ) {
			super.addOwnVariable(var);
		} else {
			addParameterNoCheck(var);
		}
	}

	@Override
	public String getTitle() {
		return "experiment " + getName();
	}

	@Override
	public boolean isExperiment() {
		return true;
	}

	@Override
	public List<IDescription> getChildren() {
		List<IDescription> result = super.getChildren();
		if ( parameters != null ) {
			result.addAll(parameters.values());
		}
		return result;
	}

	@Override
	public Map<String, VariableDescription> getVariables() {
		if ( variables == null ) {
			variables = new TOrderedHashMap<String, VariableDescription>();
			// Trick to have these two variables always at the beginning.
			variables.put(ExperimentAgent.PROJECT_PATH, null);
			variables.put(ExperimentAgent.MODEL_PATH, null);
		}
		return variables;
	}

	/**
	 * @return
	 */
	public Boolean isBatch() {
		return IKeyword.BATCH.equals(getFacets().getLabel(IKeyword.TYPE));
	}

}
