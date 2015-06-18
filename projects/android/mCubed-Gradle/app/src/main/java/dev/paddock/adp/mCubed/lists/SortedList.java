package dev.paddock.adp.mCubed.lists;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import dev.paddock.adp.mCubed.model.holders.Holder;
import dev.paddock.adp.mCubed.model.holders.HolderInt;

public class SortedList<E> implements List<E> {
	private static enum AVLTreeHeight {
		Path, Level
	}
	
	private static enum AVLTreeBalance {
		SingleRight, DoubleLR, DoubleRL, SingleLeft
	}
	
	private static class Node<E> {
		public E value;
		public Node<E> left;
		public Node<E> right;
		
		public Node(E value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value != null ? value.toString() : "";
		}
	}
	
	private static interface NodeAction<E> {
		boolean act(Node<E> parentNode, Node<E> currentNode, E item);
	}
	
	public static abstract class Action<E> implements NodeAction<E> {
		public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
			return act(item);
		}
		
		/**
		 * Perform the action on the given item.
		 * @param item The item to perform the action on.
		 * @return True if the iteration should halt, or false otherwise.
		 */
		public abstract boolean act(E item);
	}
	
	private class SortedListIterator implements Iterator<E> {
		private final Stack<Node<E>> nodes = new Stack<Node<E>>();
		
		public SortedListIterator() {
			addNode(root);
		}
		
		private void addNode(Node<E> node) {
			while (node != null) {
				nodes.push(node);
				node = node.left;
			}
		}
		
		@Override
		public boolean hasNext() {
			return !nodes.isEmpty();
		}

		@Override
		public E next() {
			Node<E> node = nodes.pop();
			addNode(node.right);
			return node.value;
		}

		@Override
		public void remove() {
			throw new ConcurrentModificationException("A SortedList<E> may not be altered while iterating.");
		}
	}
	
	private int count;
	private Node<E> root;
	private Comparator<E> comparer;
	
	/**
	 * Construct a new sorted list.
	 * @param comparer The comparer used to sort the items by.
	 */
	public SortedList(Comparator<E> comparer) {
		this.comparer = comparer;
	}
	
	/**
	 * Add an item to the list in order.
	 * @param item The item to be added to the list.
	 * @return Always returns true.
	 */
	@Override
	public boolean add(E item) {
		root = addNode(root, new Node<E>(item));
		count++;
		return true;
	}
	
	/**
	 * Add an item to the list in order.
	 * @param location This parameter is ignored since the list should remain sorted.
	 * @param item The item to be added to the list.
	 * @return Always returns true.
	 */
	@Override
	public void add(int location, E item) {
		add(item);
	}
	
	/**
	 * Adds all the items to the list in order.
	 * @param collection The collection of items that should be added.
	 * @return True if the collection was modified, or false otherwise.
	 */
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		boolean modified = false;
		for (E item : collection) {
			if (add(item)) {
				modified = true;
			}
		}
		return modified;
	}
	
	/**
	 * Adds all the items to the list in order.
	 * @param location This parameter is ignored since the list should remain sorted.
	 * @param collection The collection of items that should be added.
	 * @return True if the collection was modified, or false otherwise.
	 */
	@Override
	public boolean addAll(int location, Collection<? extends E> collection) {
		return addAll(collection);
	}
	
	/**
	 * Adds the node to the tree in the proper place.
	 * @param root The root of the subtree in which the node will be added.
	 * @param add The node to be added to the tree.
	 * @return The new root of the tree.
	 */
	private Node<E> addNode(Node<E> root, Node<E> add) {
		if (root == null) {
			return add;
		}
		Node<E> node = root;
		while (true) {
			if (comparer.compare(node.value, add.value) > 0) {
				if (node.left == null) {
					node.left = add;
					break;
				} else {
					node = node.left;
				}
			} else {
				if (node.right == null) {
					node.right = add;
					break;
				} else {
					node = node.right;
				}
			}
		}
		return root;
	}
	
	/**
	 * Set the item at the given location to be the given object.
	 * Note: in order to preserve the proper sorting within the list,
	 * the item at the given location is removed and the new object
	 * is added with complete disregard to each other.
	 * @param location The location of the item to remove.
	 * @param item The item to add to the list.
	 * @return The item that was at the given location.
	 */
	@Override
	public E set(int location, E item) {
		E oldItem = remove(location);
		add(item);
		return oldItem;
	}
	
	/**
	 * Get the item at the location and remove it from the list.
	 * @param location The location of the item to remove.
	 * @return The item that was removed from the list.
	 */
	@Override
	public E remove(int location) {
		E item = get(location);
		remove(item);
		return item;
	}
	
	/**
	 * Remove the given item from the list.
	 * @param item The item to remove from the list.
	 * @return True if the list was modified, or false otherwise.
	 */
	@Override
	public boolean remove(Object item) {
		final Object findItem = item;
		boolean removedItem = traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				if (item.equals(findItem)) {
					removeNode(parentNode, currentNode);
					count--;
					return true;
				}
				return false;
			}
		});
		return removedItem;
	}
	
	/**
	 * Removes all the given items from the list.
	 * @param collection The collection of items to remove.
	 * @return True if the collection was modified, or false otherwise.
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		for (Object item : collection) {
			if (remove(item)) {
				modified = true;
			}
		}
		return modified;
	}
	
	/**
	 * Removes all the items from the list that are not contained within the given collection.
	 * @param collection The collection of items in which to keep.
	 * @return True if the collection was modified, or false otherwise.
	 */
	@Override
	public boolean retainAll(Collection<?> collection) {
		Collection<Object> removeAll = new ArrayList<Object>(size());
		for (Object object : this) {
			if (!collection.contains(object)) {
				removeAll.add(object);
			}
		}
		return removeAll(removeAll);
	}
	
	/**
	 * Recursively retrieve the node that represents the head of the tree and remove it.
	 * @param root The root of the tree in which to start.
	 * @return The new root of the tree.
	 */
	private void removeNode(Node<E> parentNode, Node<E> removeNode) {
		if (removeNode != null) {
			if (removeNode.left == null) {
				setNode(parentNode, removeNode, removeNode.right);
			} else if (removeNode.right == null) {
				setNode(parentNode, removeNode, removeNode.left);
			} else {
				Node<E> headParentNode = getHeadParentNode(removeNode.right);
				Node<E> headNode = null;
				if (headParentNode == null) {
					headParentNode = removeNode;
					headNode = removeNode.right;
				} else {
					headNode = getHeadNode(headParentNode);
				}
				removeNode.value = headNode.value;
				removeNode(headParentNode, headNode);
			}
		}
	}
	
	/**
	 * Updates the parent node by bypassing the old node and pointing directly to the new node.
	 * @param parentNode The parent node of the node to unset.
	 * @param oldNode The node that will be unset.
	 * @param newNode The new node to set to.
	 */
	private void setNode(Node<E> parentNode, Node<E> oldNode, Node<E> newNode) {
		if (parentNode == null) {
			root = newNode;
		} else if (parentNode.left == oldNode) {
			parentNode.left = newNode;
		} else if (parentNode.right == oldNode) {
			parentNode.right = newNode;
		}
	}
	
	/**
	 * Clears all the items in the list.
	 */
	@Override
	public void clear() {
		root = null;
		count = 0;
	}
	
	/**
	 * Determines if the given item exists in the list or not.
	 * @param item The item to find in the list.
	 * @return True if the item existed, or false otherwise.
	 */
	@Override
	public boolean contains(Object item) {
		return indexOf(item) > -1;
	}
	
	/**
	 * Determines if all the given items exist in the list or not.
	 * @param collection The collection of items to find in the list.
	 * @return True if all the items exist in the list, or false otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> collection) {
		boolean containsAll = true;
		for (Object item : collection) {
			if (!contains(item)) {
				containsAll = false;
				break;
			}
		}
		return containsAll;
	}
	
	/**
	 * Get the position of the item in the list.
	 * @param item The item to find in the list.
	 * @return The index position of the item in the list.
	 */
	@Override
	public int indexOf(Object item) {
		return indexOf(item, true);
	}
	
	/**
	 * Get the last position of the item in the list.
	 * @param item The item to find in the list.
	 * @return The last index position of the item in the list.
	 */
	@Override
	public int lastIndexOf(Object item) {
		return indexOf(item, false);
	}
	
	/**
	 * Get the position of the item in the list.
	 * @param item The item to find in the list.
	 * @param first True if the index of first occurrence should be returned, or false for the last occurrence. 
	 * @return The index position of the item in the list.
	 */
	private int indexOf(Object item, final boolean first) {
		final HolderInt index = new HolderInt(-1);
		final HolderInt count = new HolderInt(-1);
		final Object findItem = item;
		traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				count.value++;
				if (item.equals(findItem))  {
					index.value = count.value;
					if (first) {
						return true;
					}
				}
				return false;
			}
		});
		return index.value;
	}
	
	/**
	 * Get the item at the given location.
	 * @param location The location of the item to return.
	 * @return The item at the given location.
	 */
	@Override
	public E get(int location) {
		Node<E> node = getNode(location);
		return node == null ? null : node.value;
	}
	
	/**
	 * Get the node at the given location.
	 * @param location The location of the node to return.
	 * @return The node at the given location.
	 */
	private Node<E> getNode(int location) {
		final Holder<Node<E>> itemHolder = new Holder<Node<E>>();
		final HolderInt index = new HolderInt();
		final int findLocation = location;
		traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				if (index.value == findLocation) {
					itemHolder.value = currentNode;
					return true;
				}
				index.value++;
				return false;
			}
		});
		return itemHolder.value;
	}
	
	/**
	 * Get the head node of the tree starting at the given root of the tree.
	 * @param node The root node of the tree to get the head node for.
	 * @return The head node for the tree at the given root.
	 */
	private Node<E> getHeadNode(Node<E> node) {
		if (node == null) {
			return null;
		}
		while (node.left != null) {
			node = node.left;
		}
		return node;
	}
	
	/**
	 * Get the parent node of the head node of the tree starting at the given root of the tree.
	 * @param node The root node of the tree to get the parent node of the head node for.
	 * @return The parent node of the head node for the tree at the given root.
	 */
	private Node<E> getHeadParentNode(Node<E> node) {
		if (node == null || node.left == null) {
			return null;
		}
		while (node.left.left != null) {
			node = node.left;
		}
		return node;
	}
	
	/**
	 * Creates an ArrayList of the items from the list at given
	 * location range, where the start index is inclusive and the
	 * end index is exclusive.
	 * @param start The inclusive index of items to start at.
	 * @param end The exclusive index of the items to end at.
	 * @return The list containing the items within the range.
	 */
	@Override
	public List<E> subList(final int start, final int end) {
		final List<E> list = new ArrayList<E>();
		final HolderInt index = new HolderInt();
		if (start <= end) {
			traverseInOrder(new NodeAction<E>() {
				@Override
				public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
					if (index.value >= start) {
						list.add(item);
					}
					if (index.value >= end - 1) {
						return true;
					}
					index.value++;
					return false;
				}
			});
		}
		return list;
	}
	
	/**
	 * Get an iterator to iterate over the items in the list.
	 * @return The iterator to iterate over the items.
	 */
	@Override
	public Iterator<E> iterator() {
		return new SortedListIterator();
	}

	/**
	 * Get a list iterator to iterate over the items in the list.
	 * @return The list iterator to iterate over the items.
	 */
	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}
	
	/**
	 * Get a list iterator to iterate over the items in the list.
	 * @param location The location in the list to start the iterator at.
	 * @return The list iterator to iterate over the items.
	 */
	@Override
	public ListIterator<E> listIterator(int location) {
		return subList(location, size()).listIterator();
	}
	
	/**
	 * Creates an array to place all the item in the list into.
	 * @return The array that contains all the items from the list.
	 */
	@Override
	public Object[] toArray() {
		final Object[] items = new Object[size()];
		final HolderInt index = new HolderInt();
		traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				items[index.value] = item;
				index.value++;
				return false;
			}
		});
		return items;
	}

	/**
	 * Creates or reuses an array of the given type to place all
	 * the items in the list into.
	 * @param array The array to fill with the items. If the array isn't large
	 * enough to contain all the items, a new array will be created.
	 * @return The array that contains all the items from the list.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array) {
		int size = size();
        if (size > array.length) {
            Class<?> ct = array.getClass().getComponentType();
            array = (T[]) Array.newInstance(ct, size);
        }
        final HolderInt index = new HolderInt();
        final T[] items = array;
		traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				items[index.value] = (T)item;
				index.value++;
				return false;
			}
		});
        for (int pos = index.value; pos < array.length; pos++) {
        	array[pos] = null;
        }
        return array;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		traverseInOrder(new NodeAction<E>() {
			@Override
			public boolean act(Node<E> parentNode, Node<E> currentNode, E item) {
				if (builder.length() > 0) {
					builder.append(", ");
				}
				builder.append("[");
				builder.append(currentNode);
				builder.append("]");
				return false;
			}
		});
		return builder.toString();
	}
	
	/**
	 * Determine if the list is empty, meaning its size is 0.
	 * @return True if the list is empty, or false otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * Get the number of elements in the list.
	 * @return The number of elements in the list.
	 */
	@Override
	public int size() {
		return count;
	}

	/**
	 * Get the height of the tree (height meaning the number of levels in the tree).
	 * @return The height of the tree.
	 */
	public int getLevels() {
		return getHeight(root, AVLTreeHeight.Level);
	}

	/**
	 * Get the height of the tree (height meaning the longest path in the tree).
	 * @return The height of the tree.
	 */
	public int getHeight() {
		return getHeight(root, AVLTreeHeight.Path);
	}

	/**
	 * Get the height of the tree starting at a given root.
	 * @param root The root at which to begin calculating the height.
	 * @param height The type of height to calculate.
	 * @return The height of the tree starting at the given root.
	 */
	private int getHeight(Node<E> root, AVLTreeHeight height) {
		int offset = height == AVLTreeHeight.Level ? 0 : -1;
		if (root == null) {
			return offset;
		}
		int maxHeight = Math.max(getHeight(root.left, height), getHeight(root.right, height));
		return maxHeight + 1;
	}
	
	/**
	 * Perform an action on all the elements in the list using the specified traversal method.
	 * @param traversal The traversal method to be used when traversing the tree.
	 * @param action The action to be performed on all the elements.
	 * @return True if the traversal should halt, or false otherwise.
	 */
	public boolean traverseInOrder(NodeAction<E> action) {
		return traverseInOrder(action, root);
	}
	
	/**
	 * Perform an action on all the items in the list using the in-order traversal method.
	 * @param action The action to be performed on all the items.
	 * @param parentNode The parent of the current node.
	 * @param currentNode The node of the tree in which to start traversing.
	 * @return True if the traversal should halt, or false otherwise.
	 */
	private boolean traverseInOrder(NodeAction<E> action, Node<E> node) {
		Stack<Node<E>> stack = new Stack<Node<E>>();
		Stack<Node<E>> parents = new Stack<Node<E>>();
		Node<E> parentNode = null;
		while (!stack.isEmpty() || node != null) {
			if (node == null) {
				node = stack.pop();
				parentNode = parents.isEmpty() ? null : parents.pop();
				if (action.act(parentNode, node, node.value)) {
					return true;
				}
				if (node.right != null) {
					parents.push(node);
				}
				node = node.right;
			} else {
				stack.push(node);
				if (node.left != null) {
					parents.push(node);
				}
				node = node.left;
			}
		}
		return false;
	}

	/**
	 * Balance the tree to be a valid AVL tree.
	 */
	public void balanceTree() {
		root = balanceTree(root);
	}

	/**
	 * Balance the subtree to be a valid AVL tree, starting at the given root.
	 * @param root The root at which to balance the subtree.
	 * @return The new root of the subtree.
	 */
	private Node<E> balanceTree(Node<E> root) {
		if (root != null) {
			root.left = balanceTree(root.left);
			root.right = balanceTree(root.right);
			int balance = balanceHeight(root);
			if (balance >= 2) {
				root = balanceTree(root, balanceHeight(root.left) >= 1 ? AVLTreeBalance.SingleRight : AVLTreeBalance.DoubleLR);
			} else if (balance <= -2) {
				root = balanceTree(root, balanceHeight(root.right) >= 1 ? AVLTreeBalance.DoubleRL : AVLTreeBalance.SingleLeft);
			}
		}
		return root;
	}

	/**
	 * Balance the subtree to be a valid AVL tree, starting at the given root with the given method of balancing.
	 * @param root The root at which to balance the subtree.
	 * @param method The method to use when balancing the subtree
	 * @return The new root of the subtree.
	 */
	private Node<E> balanceTree(Node<E> root, AVLTreeBalance method) {
		Node<E> lrNode = null, rlNode = null, tempRoot = null;
		switch (method) {
			case SingleRight:
				tempRoot = root.left;
				lrNode = root.left.right;
				root.left.right = root;
				root.left = lrNode;
				break;
			case SingleLeft:
				tempRoot = root.right;
				rlNode = root.right.left;
				root.right.left = root;
				root.right = rlNode;
				break;
			case DoubleLR:
				tempRoot = root.left.right;
				lrNode = root.left.right.left;
				rlNode = root.left.right.right;
				root.left.right.left = root.left;
				root.left.right.right = root;
				root.left.right = lrNode;
				root.left = rlNode;
				break;
			case DoubleRL:
				tempRoot = root.right.left;
				lrNode = root.right.left.right;
				rlNode = root.right.left.left;
				root.right.left.right = root.right;
				root.right.left.left = root;
				root.right.left = lrNode;
				root.right = rlNode;
				break;
		}
		return tempRoot;
	}

	/**
	 * Get the height of a given subtree starting at the given root, in which the height is for balancing the tree.
	 * @param root The root at which to find the height of the subtree.
	 * @return The height of the subtree starting at the given root.
	 */
	private int balanceHeight(Node<E> root) {
		return root == null ? -1 : getHeight(root.left, AVLTreeHeight.Path) - getHeight(root.right, AVLTreeHeight.Path);
	}
}