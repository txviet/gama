/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.statements;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.StringUtils;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IContainer;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.*;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;

/**
 * Written by drogoul Modified on 24 ao�t 2010
 * 
 * @todo Description
 * 
 */
public abstract class AbstractContainerStatement extends AbstractStatement {

	protected final IExpression item, index, list, all;
	boolean asAll = false;

	public AbstractContainerStatement(final IDescription desc) {
		super(desc);
		item = getFacet(IKeyword.ITEM, getFacet(IKeyword.EDGE, getFacet(IKeyword.VERTEX)));
		index = getFacet(IKeyword.INDEX, getFacet(IKeyword.AT, getFacet(IKeyword.KEY)));
		all = getFacet(IKeyword.ALL);
		list = getFacet(IKeyword.TO, getFacet(IKeyword.FROM, getFacet(IKeyword.IN)));

	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		IContainer container = createContainer(scope);
		Object position = createKey(scope, container);
		Object object = createItem(scope, container);
		apply(scope, object, position, asAll, container);
		scope.setStatus(ExecutionStatus.skipped);
		if ( list instanceof IVarExpression ) {
			((IVarExpression) list).setVal(scope, container, false);
		}
		return container;

	}

	/**
	 * @throws GamaRuntimeException
	 * @return the container to which this command will be applied
	 */
	private IContainer createContainer(final IScope scope) throws GamaRuntimeException {
		final Object cont = list.value(scope);
		if ( cont instanceof IContainer ) { return (IContainer) cont; }
		if ( cont instanceof ISpecies ) { return (ISpecies) cont; }
		if ( cont instanceof IShape ) { return ((IShape) cont).getAttributes(); }
		throw new GamaRuntimeException("Cannot use " + list.getType().toString() + " " +
			list.toGaml() + " as a container", true);
	}

	private Object createKey(final IScope scope, final IContainer container)
		throws GamaRuntimeException {
		Object position = index == null ? null : index.value(scope);
		// AD 29/02/13 : Normally taken in charge by the parser, now.
		// if ( index != null && !container.checkIndex(position) ) { throw new GamaIndexTypeWarning(
		// position, index.getType(), list); }
		if ( index != null ) {
			IType t = list.getKeyType();
			IType i = index.getType();
			if ( !i.isTranslatableInto(t) ) {
				position = t.cast(scope, position, null);
			}
		}
		return position;
	}

	private Object createItem(final IScope scope, final IContainer container)
		throws GamaRuntimeException {
		Object result = null;
		if ( all == null ) {
			// Case add item: ITEM to: LIST
			if ( item != null ) {
				result = item.value(scope);
			}
		} else {
			Object whole = all.value(scope);
			if ( item != null ) {
				if ( whole instanceof Boolean ) {
					// Case add item: ITEM all: true to: LIST
					asAll = (Boolean) whole;
					result = item.value(scope);
				} else {
					// Case add item: ITEM all: ITEMS to: LIST
					// Impossible
					throw new GamaRuntimeException("'all: " + StringUtils.toGaml(whole) +
						"' cannot be used in " + getName(), true);
				}
			} else {
				// Case add all: [...] to: LIST
				asAll = true;
				result = whole;
			}
		}
		return result;

		// if ( all == null ) {
		// if ( item == null ) { return null; }
		// final Object object = item.value(scope);
		// // AD 29/02/13 : Normally taken in charge by the parser, now.
		// // if ( !container.checkValue(object) ) { throw new GamaValueTypeWarning(object,
		// // item.getType(), list); }
		// return object;
		// }
		// Object whole = all.value(scope);
		// if ( item != null ) {
		// if ( whole instanceof Boolean ) {
		// asAll = (Boolean) whole;
		// final Object object = item.value(scope);
		// // AD 29/02/13 : Normally taken in charge by the parser, now.
		// // if ( !container.checkValue(object) ) { throw new GamaValueTypeWarning(object,
		// // item.getType(), list); }
		// return asAll ? GamaList.with(object) : object;
		// }
		// throw new GamaRuntimeException("'all: " + StringUtils.toGaml(whole) +
		// "' cannot be used in " + getName(), true);
		// }
		// asAll = true;
		// if ( !(whole instanceof IContainer) ) {
		// whole = GamaList.with(whole);
		// }
		// // AD 29/02/13 : Normally taken in charge by the parser, now.
		// // for ( Object o : (IContainer) whole ) {
		// // if ( !container.checkValue(o) ) { throw new GamaValueTypeWarning(o, all.getType(),
		// list);
		// // }
		// // }
		// return whole;
	}

	protected abstract void apply(IScope stack, Object object, Object position, Boolean whole,
		IContainer container) throws GamaRuntimeException;

}