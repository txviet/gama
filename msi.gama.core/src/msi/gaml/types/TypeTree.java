package msi.gaml.types;

/*
 * Copyright 2010 Vivin Suresh Paliath
 * Distributed under the BSD License
 */

import java.util.*;

public class TypeTree<T> {

	public static enum Order {
		PRE_ORDER, POST_ORDER
	}

	private TypeNode<T> root;

	public TypeTree() {
		super();
	}

	public TypeTree(TypeNode<T> root) {
		setRoot(root);
	}

	public TypeTree(T root) {
		setRoot(new TypeNode(root));
	}

	public TypeNode<T> getRoot() {
		return this.root;
	}

	public void setRoot(final TypeNode<T> root) {
		this.root = root;
	}

	public TypeNode<T> setRoot(T root) {
		TypeNode<T> result = new TypeNode(root);
		setRoot(result);
		return result;
	}

	public int getNumberOfNodes() {
		int numberOfNodes = 0;

		if ( root != null ) {
			numberOfNodes = auxiliaryGetNumberOfNodes(root) + 1; // 1 for the root!
		}

		return numberOfNodes;
	}

	private int auxiliaryGetNumberOfNodes(final TypeNode<T> node) {
		int numberOfNodes = node.getNumberOfChildren();

		for ( TypeNode<T> child : node.getChildren() ) {
			numberOfNodes += auxiliaryGetNumberOfNodes(child);
		}

		return numberOfNodes;
	}

	public boolean exists(final T dataToFind) {
		return find(dataToFind) != null;
	}

	public TypeNode<T> find(final T dataToFind) {
		TypeNode<T> returnNode = null;

		if ( root != null ) {
			returnNode = auxiliaryFind(root, dataToFind);
		}

		return returnNode;
	}

	private TypeNode<T> auxiliaryFind(final TypeNode<T> currentNode, final T dataToFind) {
		TypeNode<T> returnNode = null;
		int i = 0;

		if ( currentNode.getData().equals(dataToFind) ) {
			returnNode = currentNode;
		}

		else if ( currentNode.hasChildren() ) {
			i = 0;
			while (returnNode == null && i < currentNode.getNumberOfChildren()) {
				returnNode = auxiliaryFind(currentNode.getChildAt(i), dataToFind);
				i++;
			}
		}

		return returnNode;
	}

	public boolean isEmpty() {
		return root == null;
	}

	public List<TypeNode<T>> build(final Order traversalOrder) {
		List<TypeNode<T>> returnList = null;

		if ( root != null ) {
			returnList = build(root, traversalOrder);
		}

		return returnList;
	}

	public List<TypeNode<T>> build(final TypeNode<T> node, final Order traversalOrder) {
		List<TypeNode<T>> traversalResult = new ArrayList<TypeNode<T>>();

		if ( traversalOrder == Order.PRE_ORDER ) {
			buildPreOrder(node, traversalResult);
		}

		else if ( traversalOrder == Order.POST_ORDER ) {
			buildPostOrder(node, traversalResult);
		}

		return traversalResult;
	}

	private void buildPreOrder(final TypeNode<T> node, final List<TypeNode<T>> traversalResult) {
		traversalResult.add(node);

		for ( TypeNode<T> child : node.getChildren() ) {
			buildPreOrder(child, traversalResult);
		}
	}

	private void buildPostOrder(final TypeNode<T> node, final List<TypeNode<T>> traversalResult) {
		for ( TypeNode<T> child : node.getChildren() ) {
			buildPostOrder(child, traversalResult);
		}

		traversalResult.add(node);
	}

	public Map<TypeNode<T>, Integer> buildWithDepth(final Order traversalOrder) {
		Map<TypeNode<T>, Integer> returnMap = null;

		if ( root != null ) {
			returnMap = buildWithDepth(root, traversalOrder);
		}

		return returnMap;
	}

	public Map<TypeNode<T>, Integer> buildWithDepth(final TypeNode<T> node, final Order traversalOrder) {
		Map<TypeNode<T>, Integer> traversalResult = new LinkedHashMap<TypeNode<T>, Integer>();

		if ( traversalOrder == Order.PRE_ORDER ) {
			buildPreOrderWithDepth(node, traversalResult, 0);
		}

		else if ( traversalOrder == Order.POST_ORDER ) {
			buildPostOrderWithDepth(node, traversalResult, 0);
		}

		return traversalResult;
	}

	private void buildPreOrderWithDepth(final TypeNode<T> node, final Map<TypeNode<T>, Integer> traversalResult,
		final int depth) {
		traversalResult.put(node, depth);

		for ( TypeNode<T> child : node.getChildren() ) {
			buildPreOrderWithDepth(child, traversalResult, depth + 1);
		}
	}

	private void buildPostOrderWithDepth(final TypeNode<T> node, final Map<TypeNode<T>, Integer> traversalResult,
		final int depth) {
		for ( TypeNode<T> child : node.getChildren() ) {
			buildPostOrderWithDepth(child, traversalResult, depth + 1);
		}

		traversalResult.put(node, depth);
	}

	@Override
	public String toString() {
		/*
		 * We're going to assume a pre-order traversal by default
		 */

		String stringRepresentation = "";

		if ( root != null ) {
			stringRepresentation = build(Order.PRE_ORDER).toString();

		}

		return stringRepresentation;
	}

	public String toStringWithDepth() {
		/*
		 * We're going to assume a pre-order traversal by default
		 */

		String stringRepresentation = "";

		if ( root != null ) {
			stringRepresentation = buildWithDepth(Order.PRE_ORDER).toString();
		}

		return stringRepresentation;
	}
}