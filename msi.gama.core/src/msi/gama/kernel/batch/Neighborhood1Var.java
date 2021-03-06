/*********************************************************************************************
 * 
 * 
 * 'Neighborhood1Var.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.kernel.batch;

import java.util.*;
import msi.gama.kernel.experiment.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

public class Neighborhood1Var extends Neighborhood {

	public Neighborhood1Var(final List<IParameter.Batch> variables) {
		super(variables);
	}

	@Override
	public List<ParametersSet> neighbor(final IScope scope, final ParametersSet solution) throws GamaRuntimeException {
		final List<ParametersSet> neighbors = new ArrayList<ParametersSet>();
		for ( final IParameter.Batch var : variables ) {
			var.setValue(scope, solution.get(var.getName()));
			final Set<Object> neighbourValues = var.neighbourValues();
			for ( final Object val : neighbourValues ) {
				final ParametersSet newSol = new ParametersSet(solution);
				newSol.put(var.getName(), val);
				neighbors.add(newSol);
			}
		}
		neighbors.remove(solution);
		return neighbors;
	}
}
