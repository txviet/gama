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
package msi.gaml.types;

import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.compilation.AbstractGamlAdditions;
import msi.gaml.expressions.TypeFieldExpression;
import msi.gaml.factories.DescriptionFactory;
import msi.gaml.types.Tree.Order;

/**
 * Written by drogoul Modified on 9 juin 2010
 * 
 * @todo Description
 * 
 */
public class Types {

	public final static HashMap<Integer, Set<String>> keywordsToVariableType = new HashMap();
	public final static IType[] typeToIType = new IType[256];
	public final static HashMap<String, IType> stringToIType = new HashMap();
	public final static HashMap<Class, IType> classToIType = new HashMap();

	public final static IType NO_TYPE = new GamaNoType();

	public static void initType(final String keyword, IType typeInstance, final int id,
		final int varKind, final Class ... wraps) {
		if ( keyword.equals(IKeyword.UNKNOWN) ) {
			typeInstance = NO_TYPE;
		}
		typeInstance.init(varKind, id, keyword, wraps);
		typeToIType[id] = typeInstance;
		stringToIType.put(keyword, typeInstance);
		// Hack to allow types to be declared with their id as string
		stringToIType.put(String.valueOf(id), typeInstance);
		for ( Class cc : wraps ) {
			classToIType.put(cc, typeInstance);
		}
		if ( !keywordsToVariableType.containsKey(varKind) ) {
			keywordsToVariableType.put(varKind, new HashSet());
		}
		keywordsToVariableType.get(varKind).add(keyword);
	}

	public static Object coerce(final IScope scope, final Object value, final IType type,
		final Object param) throws GamaRuntimeException {
		return type.cast(scope, value, param);
	}

	public static IType get(final int type) {
		IType t = typeToIType[type];
		return t == null ? Types.NO_TYPE : t;
	}

	public static IType get(final String type) {
		IType t = stringToIType.get(type);
		return t == null ? Types.NO_TYPE : t;
	}

	public static <T> IType<T> get(final Class<T> type) {
		IType t = classToIType.get(type);
		if ( t != null ) { return t; }
		// Set<Class> classes = classToIType.keySet();
		if ( !type.isInterface() ) {
			for ( Map.Entry<Class, IType> c : classToIType.entrySet() ) {
				if ( c.getKey() != Object.class && c.getKey().isAssignableFrom(type) ) {
					t = c.getValue();
					// classToIType.put(type, t);
					return t;
				}
			}
		}
		if ( !type.isInterface() ) {
			classToIType.put(type, Types.NO_TYPE);
		}
		return Types.NO_TYPE;
	}

	public static void initFieldGetters(final IType t) {
		Map<String, TypeFieldExpression> vars = AbstractGamlAdditions.getAllFields(t.toClass());
		t.setFieldGetters(vars);
	}

	public static void init() {
		Tree<IType> hierarchy = buildHierarchy();
		for ( Node<IType> node : hierarchy.build(Order.PRE_ORDER) ) {
			IType type = node.getData();
			DescriptionFactory.addNewTypeName(type.toString(), type.getVarKind());
			initFieldGetters(type);
			type.setParent(node.getParent() == null ? null : node.getParent().getData());
		}
	}

	private static Tree<IType> buildHierarchy() {
		Node<IType> root = new Node(NO_TYPE);
		Tree<IType> hierarchy = new Tree();
		hierarchy.setRoot(root);
		List<IType>[] depths = typesWithDepths();
		for ( int i = 1; i < 6; i++ ) {
			List<IType> types = depths[i];
			for ( IType t : types ) {
				place(t, hierarchy);
			}
		}
		return hierarchy;
	}

	private static List<IType>[] typesWithDepths() {
		List<IType>[] depths = new ArrayList[6];
		for ( int i = 0; i < 6; i++ ) {
			depths[i] = new ArrayList();
		}
		Set<IType> list = new HashSet(classToIType.values());
		for ( IType t : list ) {
			int depth = 0;
			for ( IType other : list ) {
				if ( other.isAssignableFrom(t) && other != t ) {
					depth++;
				}
			}
			depths[depth].add(t);
		}
		return depths;
	}

	private static void place(final IType t, final Tree<IType> hierarchy) {
		Map<Node<IType>, Integer> map = hierarchy.buildWithDepth(Order.PRE_ORDER);
		int max = 0;
		Node<IType> parent = hierarchy.getRoot();
		for ( Node<IType> current : map.keySet() ) {
			if ( current.getData().isAssignableFrom(t) && map.get(current) > max ) {
				max = map.get(current);
				parent = current;
			}
		}
		parent.addChild(new Node(t));
	}

}