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
package msi.gaml.expressions;

import static msi.gama.precompiler.ITypeProvider.*;
import java.util.*;
import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.compilation.GamaHelper;
import msi.gaml.descriptions.IDescription;
import msi.gaml.operators.Cast;
import msi.gaml.types.*;

/**
 * The Class UnaryOpCustomExpr.
 */
public class BinaryOperator extends AbstractNAryOperator {

	protected final boolean lazy;
	protected final boolean canBeConst;
	protected final GamaHelper helper;
	protected final int typeProvider;
	protected final int contentTypeProvider;
	protected final int keyTypeProvider;
	protected final int[] expectedContentType;

	@Override
	public boolean isConst() {
		return canBeConst && left().isConst() && right().isConst();
	}

	public final IExpression left() {
		if ( exprs == null ) { return null; }
		return exprs[0];
	}

	public IExpression right() {
		if ( exprs == null ) { return null; }
		return exprs[1];
	}

	public BinaryOperator(final IType ret, final GamaHelper exec, final boolean canBeConst, final int tProv,
		final int ctProv, final int iProv, final boolean lazy, final int[] expectedContentType) {
		this.lazy = lazy;
		this.canBeConst = canBeConst;
		type = ret;
		helper = exec;
		typeProvider = tProv;
		contentTypeProvider = ctProv;
		keyTypeProvider = iProv;
		this.expectedContentType = expectedContentType;
	}

	@Override
	public BinaryOperator init(final String operator, final IDescription context, final IExpression ... args) {
		setName(operator);
		this.exprs = args;
		computeType(context);
		computeContentType(context);
		computeKeyType(context);
		return this;
	}

	static List<String> symbols = Arrays.asList("+", "-", "/", "*", "^", "**", "<", ">", "<=", ">=", "?", ":", ".",
		"where", "select", "collect", "first_with", "last_with", "overlapping", "at_distance", "in", "inside",
		"with_max_of", "with_min_of", "of_species", "of_generic_species", "sort_by");

	@Override
	public String toGaml() {
		if ( symbols.contains(name) ) { return parenthesize(left()) + name + parenthesize(right()); }
		return name + parenthesize(left(), right());
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		Object leftVal = "(nil)", rightVal = "(nil)";
		try {
			leftVal = left().value(scope);
			rightVal = lazy ? right() : right().value(scope);

			final Object result = helper.run(scope, leftVal, rightVal);
			return result;
		} catch (final RuntimeException ex) {
			final GamaRuntimeException e1 = GamaRuntimeException.create(ex);
			e1.addContext("when applying the " + literalValue() + " operator on " + leftVal + " and " + rightVal);
			throw e1;
		}
	}

	private IType computeType(final IDescription context, final int t, final IType def, final int kind) {
		switch (t) {
			case NONE:
				return def;
			case BOTH:
				final List<IExpression> expressions = Arrays.asList(exprs);
				return findCommonType(expressions, kind);
			case FIRST_TYPE:
				return left().getType();
			case FIRST_CONTENT_TYPE_OR_TYPE:
				final IType t2 = left().getContentType();
				if ( t2 == Types.NO_TYPE ) { return left().getType(); }
				return t2;
			case SECOND_TYPE:
				return right().getType();
			case FIRST_CONTENT_TYPE:
				return left().getContentType();
			case FIRST_KEY_TYPE:
				return left().getKeyType();
			case SECOND_CONTENT_TYPE:
				return right().getContentType();
			case SECOND_CONTENT_TYPE_OR_TYPE:
				final IType t3 = right().getContentType();
				if ( t3 == Types.NO_TYPE ) { return right().getType(); }
				return t3;
			case SECOND_KEY_TYPE:
				return right().getKeyType();
			default:
				return t >= 0 ? Types.get(t) : def;
		}
	}

	public void computeType(final IDescription context) {
		type = computeType(context, typeProvider, type, _type);
	}

	public void computeContentType(final IDescription context) {
		contentType = computeType(context, contentTypeProvider, type.defaultContentType(), _content);
	}

	public void computeKeyType(final IDescription context) {
		keyType = computeType(context, keyTypeProvider, type.defaultKeyType(), _key);
	}

	@Override
	public BinaryOperator copy() {
		final BinaryOperator copy =
			new BinaryOperator(type, helper, canBeConst, typeProvider, contentTypeProvider, keyTypeProvider, lazy,
				expectedContentType);
		copy.doc = doc;
		return copy;
	}

	@Override
	public IOperator resolveAgainst(final IScope scope) {
		final BinaryOperator copy = copy();
		copy.exprs = new IExpression[2];
		copy.exprs[0] = left().resolveAgainst(scope);
		copy.exprs[1] = right().resolveAgainst(scope);
		return copy;
	}

	public static class BinaryVarOperator extends BinaryOperator implements IVarExpression {

		public BinaryVarOperator(final IType ret, final GamaHelper exec, final boolean canBeConst, final int type,
			final int contentType, final int keyType, final boolean lazy, final int[] expectedContentType) {
			super(ret, exec, canBeConst, type, contentType, keyType, lazy, expectedContentType);
		}

		@Override
		public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
			final IAgent agent = Cast.asAgent(scope, left().value(scope));
			if ( agent == null || agent.dead() ) { return; }
			scope.setAgentVarValue(agent, right().literalValue(), v);
		}

		@Override
		public boolean isNotModifiable() {
			return right().isNotModifiable();
		}

		@Override
		public BinaryVarOperator init(final String operator, final IDescription context, final IExpression ... args) {
			if ( args.length < 2 ) {
				context.error("No attributes found after the '.'", IGamlIssue.UNKNOWN_VAR);
			} else if ( !(args[1] instanceof IVarExpression) ) {
				context.error("Attribute " + args[1].literalValue() + " unknown for " + args[0].getType() +
					" instances");
				return null;
			}
			return (BinaryVarOperator) super.init(operator, context, args);
		}

		@Override
		public IVarExpression right() {
			if ( exprs == null ) { return null; }
			return (IVarExpression) exprs[1];
		}

		@Override
		public String toGaml() {
			return left().toGaml() + "." + right().toGaml();
		}

		@Override
		public BinaryVarOperator copy() {
			final BinaryVarOperator copy =
				new BinaryVarOperator(type, helper, canBeConst, typeProvider, contentTypeProvider, keyTypeProvider,
					lazy, expectedContentType);
			copy.doc = doc;
			return copy;
		}
	}

}
