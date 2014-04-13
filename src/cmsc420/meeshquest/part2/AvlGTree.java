package cmsc420.meeshquest.part2;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

// BinarySearchTree class
//
// CONSTRUCTION: with no initializer
//
// ******************PUBLIC OPERATIONS*********************
// void insert( x )       --> Insert x
// void remove( x )       --> Remove x (unimplemented)
// Comparable find( x )   --> Return item that matches x
// Comparable findMin( )  --> Return smallest item
// Comparable findMax( )  --> Return largest item
// boolean isEmpty( )     --> Return true if empty; else false
// void makeEmpty( )      --> Remove all items
// void printTree( )      --> Print tree in sorted order

/**
 * Implements an AVL tree. Note that all "matching" is based on the compareTo
 * method.
 * 
 * @author Mark Allen Weiss
 */
public class AvlGTree<K extends Comparable<K>, V> implements
SortedMap<K, V> {
	// Balance factor
	int g;
	int size = 0;
	int modCount = 0;
	final Comparator<? super K> kComparator;

	/**
	 * Construct the tree.
	 */
	public AvlGTree(int g) {
		this.kComparator = new DefaultComp<K>();
		this.g = g;
		root = null;
		size = 0;
	}
	
	public AvlGTree(Comparator<? super K> comp, int g){
		this.kComparator = comp;
		this.g = g;
	}

	public AvlGTree() {
		this.kComparator = new DefaultComp<K>();
		root = null;
		size = 0;
	}
	
	/**
	 * Insert into the tree; duplicates are ignored.
	 * 
	 * @param x
	 *            the item to insert.
	 */
	public void insert(Comparable x, Object v) {
		root = insert(x, v, root);
		size++;
	}

	/**
	 * Internal method to insert into a subtree.
	 * 
	 * @param x
	 *            the item to insert.
	 * @param t
	 *            the node that roots the tree.
	 * @return the new root.
	 */
	private AvlNode insert(Comparable x, Object v, AvlNode t) {
		if (t == null) {
			t = new AvlNode(x, v, null, null);
		} else if (x.compareTo(t.key) < 0) {
			t.left = insert(x, v, t.left);
			//System.out.println(height(t.left));
			//System.out.println(height(t.right));

			//System.out.println(Math.abs(height(t.left) - height(t.right)));
			if (Math.abs(height(t.left) - height(t.right)) >= g) {
				if (x.compareTo(t.left.key) < 0) {
					t = rotateWithLeftChild(t);
				} else {
					t = doubleWithLeftChild(t);
				}
			}
		} else if (x.compareTo(t.key) > 0) {
			t.right = insert(x, v, t.right);
			if (Math.abs(height(t.right) - height(t.left)) >= g) {
				if (x.compareTo(t.right.key) > 0) {
					t = rotateWithRightChild(t);
				} else {
					t = doubleWithRightChild(t);
				}
			}
		} else
			; // Duplicate; do nothing
		t.height = max(height(t.left), height(t.right)) + 1;
		return t;
	}

	/**
	 * Remove from the tree. Nothing is done if x is not found.
	 * 
	 * @param x
	 *            the item to remove.
	 */
	public void remove(Comparable x) {
		throw new UnsupportedOperationException(
				"Invalid operation Have not implemented remove yet");
		// System.out.println( "Sorry, remove unimplemented" );
	}

	/**
	 * Find the smallest item in the tree.
	 * 
	 * @return smallest item or null if empty.
	 */
	public Comparable findMin() {
		return elementAt(findMin(root));
	}

	/**
	 * Find the largest item in the tree.
	 * 
	 * @return the largest item of null if empty.
	 */
	public Comparable findMax() {
		return elementAt(findMax(root));
	}

	/**
	 * Find an item in the tree.
	 * 
	 * @param x
	 *            the item to search for.
	 * @return the matching item or null if not found.
	 */
	public Object find(K x) {
		return getValue(find(x, root));
	}

	/**
	 * Internal method to find an item in a subtree.
	 * 
	 * @param key
	 *            is item to search for.
	 * @param t
	 *            the node that roots the tree.
	 * @return node containing the matched item.
	 */
	private AvlNode find(K key, AvlNode t) {
		while (t != null)
			if (((Comparable) key).compareTo(t.key) < 0)
				t = t.left;
			else if (((Comparable) key).compareTo(t.key) > 0)
				t = t.right;
			else
				return t; // Match

		return null; // No match
	}

	/**
	 * Make the tree logically empty.
	 */
	public void makeEmpty() {
		root = null;
	}

	/**
	 * Test if the tree is logically empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return root == null;
	}

	/**
	 * Print the tree contents in sorted order.
	 */
	public void printTree() {
		if (isEmpty())
			System.out.println("Empty tree");
		else
			printTree(root);
	}

	/**
	 * Internal method to get element field.
	 * 
	 * @param t
	 *            the node.
	 * @return the element field or null if t is null.
	 */
	private Comparable elementAt(AvlNode t) {
		return t == null ? null : t.key;
	}

	private V getValue(AvlNode t) {
		return t == null ? null : (V) t.value;
	}

	/**
	 * Internal method to find the smallest item in a subtree.
	 * 
	 * @param t
	 *            the node that roots the tree.
	 * @return node containing the smallest item.
	 */
	private AvlNode findMin(AvlNode t) {
		if (t == null)
			return t;

		while (t.left != null)
			t = t.left;
		return t;
	}

	/**
	 * Internal method to find the largest item in a subtree.
	 * 
	 * @param t
	 *            the node that roots the tree.
	 * @return node containing the largest item.
	 */
	private AvlNode findMax(AvlNode t) {
		if (t == null)
			return t;

		while (t.right != null)
			t = t.right;
		return t;
	}

	/**
	 * Internal method to print a subtree in sorted order.
	 * 
	 * @param t
	 *            the node that roots the tree.
	 */
	private void printTree(AvlNode t) {
		if (t != null) {
			printTree(t.left);
			//System.out.println(t.key);
			printTree(t.right);
		}
	}

	/**
	 * Return the height of node t, or -1, if null.
	 */
	private static int height(AvlNode t) {
		return t == null ? -1 : t.height;
	}
	
	/**
	 *  Gets height of the tree
	 */
	public int getHeight(){
		return root.height + 1;
	}

	/**
	 * Return maximum of lhs and rhs.
	 */
	private static int max(int lhs, int rhs) {
		return lhs > rhs ? lhs : rhs;
	}

	/**
	 * Rotate binary tree node with left child. For AVL trees, this is a single
	 * rotation for case 1. Update heights, then return new root.
	 */
	private static AvlNode rotateWithLeftChild(AvlNode k2) {
		if (k2.left != null) {
			AvlNode k1 = k2.left;
			k2.left = k1.right;
			k1.right = k2;
			k2.height = max(height(k2.left), height(k2.right)) + 1;
			k1.height = max(height(k1.left), k2.height) + 1;
			return k1;
		} else {
			return k2;
		}
	}

	/**
	 * Rotate binary tree node with right child. For AVL trees, this is a single
	 * rotation for case 4. Update heights, then return new root.
	 */
	private static AvlNode rotateWithRightChild(AvlNode k1) {
		if (k1.right != null) {
			AvlNode k2 = k1.right;
			k1.right = k2.left;
			k2.left = k1;
			k1.height = max(height(k1.left), height(k1.right)) + 1;
			k2.height = max(height(k2.right), k1.height) + 1;
			return k2;
		} else {
			return k1;
		}

	}

	/**
	 * Double rotate binary tree node: first left child with its right child;
	 * then node k3 with new left child. For AVL trees, this is a double
	 * rotation for case 2. Update heights, then return new root.
	 */
	private static AvlNode doubleWithLeftChild(AvlNode k3) {
		k3.left = rotateWithRightChild(k3.left);
		return rotateWithLeftChild(k3);
	}

	/**
	 * Double rotate binary tree node: first right child with its left child;
	 * then node k1 with new right child. For AVL trees, this is a double
	 * rotation for case 3. Update heights, then return new root.
	 */
	private static AvlNode doubleWithRightChild(AvlNode k1) {
		k1.right = rotateWithLeftChild(k1.right);
		return rotateWithRightChild(k1);
	}

	/** The tree root. */
	private AvlNode root;
	
	public AvlNode getRoot(){
		return root;
	}
	
	@Override
	public void clear() {
		this.makeEmpty();
	}

	@Override
	public boolean containsKey(Object arg0) {
		for (Map.Entry<K, V> entry : entrySet()){
			if (entry.getKey().equals(arg0)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object arg0) {
		for (Map.Entry<K, V> entry : entrySet()){
			if (entry.getValue().equals(arg0)){
				return true;
			}
		}
		return false;
		
	}

	@Override
	public V get(Object arg0) {
		if (keySet().contains(arg0)){
			return (V) find((K) arg0);
		}
		throw new NullPointerException();
	}

	@Override
	public V put(K arg0, V arg1) {
		if (arg0 == null) {
			throw new NullPointerException();
		}
		
		if (containsKey(arg0)){
			V 
		}
		
		this.insert(arg0, arg1);
		
		// Return null if there was not previous value associated with key
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		throw new UnsupportedOperationException(
				"Invalid operation Have not implemented yet");
		
	}

	@Override
	public V remove(Object arg0) {
		throw new UnsupportedOperationException(
				"Invalid operation Have not implemented remove yet");
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		ArrayList<V> values = new ArrayList<V>();
		
		for (K k : keySet()){
			values.add(get(k));
		}
		return values;
	}

	@Override
	public Comparator<? super K> comparator() {
		return new DefaultComp();
	}

	@Override
	public K firstKey() {
		return (K) findMax(root).key;
	}

	@Override
	public K lastKey() {
		return (K) findMin(root).key;
	}	
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Set<K> keySet(){
		return new KeySet();
	}
	
	protected class DefaultComp<K extends Comparable<K>> implements Comparator<K>{

		@Override
		public int compare(K arg0, K arg1) {
			return arg0.compareTo(arg1);
		}
		
	}
	
	protected class KeySet implements Set<K>{

		@Override
		public boolean add(K arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends K> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			AvlGTree.this.clear();
		}

		@Override
		public boolean contains(Object arg0) {
			return AvlGTree.this.containsKey(arg0);
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			for (Object k : arg0){
				if (!contains(k)){
					return false;
				}
			}
			return true;
			
		}

		@Override
		public boolean isEmpty() {
			return AvlGTree.this.isEmpty();
		}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator<K>();
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public int size() {
			return AvlGTree.this.size;
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
		
	private class KeyIterator<K> implements Iterator<K>{
		int modCount;
	    private Stack<AvlNode<? extends Comparable, V>> stack = new Stack<AvlNode<? extends Comparable,V>>();
	    private AvlNode<? extends Comparable<K>,V> current;
	    AvlGTree<? extends Comparable<K> , V> tree;

	    public KeyIterator(){
	    	modCount = AvlGTree.this.modCount;
	    	tree = (AvlGTree<? extends Comparable<K>, V>) AvlGTree.this;
	    	
	    }
		
	    public KeyIterator(AvlNode argRoot) {
	        current = argRoot;
	    }

		public Iterator iterator(AvlNode root) {
	        return new KeyIterator(root);
	    }
		
		@Override
	    public boolean hasNext() {
	        return (!stack.isEmpty() || current != null);
	    }

		@Override
		public K next() {
			if (modCount != AvlGTree.this.modCount){
				throw new ConcurrentModificationException();
			}
				
			if (!hasNext()){
				throw new NoSuchElementException();
			}
	    	
	        while (current != null) {
	            stack.push((AvlNode<? extends Comparable, V>) current);
	            current = current.left;
	        }

	        current = (AvlNode) stack.pop();
	        AvlNode node = current;
	        current = current.right;

	        return (K) ((AvlNode)node).key;
	    }

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented yet");
		}
		
	}

	private class EntrySet implements Set<Map.Entry<K, V>> {

		@Override
		public boolean add(java.util.Map.Entry<K, V> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented yet");
		}

		@Override
		public boolean addAll(
				Collection<? extends java.util.Map.Entry<K, V>> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented yet");
		}

		@Override
		public void clear() {
			AvlGTree.this.makeEmpty();

		}

		@Override
		public boolean contains(Object arg0) {
			if (arg0 instanceof Map.Entry) {
				Map.Entry<K, V> entry = (Map.Entry<K, V>) arg0;
				Object value = AvlGTree.this.get(entry.getKey());
				return entry.getValue() == null ? value == null : entry
						.getValue().equals(value);
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			// Boolean to check if each value is in the structure
			boolean cont = true;
			// && with boolean, so if false it stays false
			for (Object o : arg0){
				cont = cont && contains(o);
			}
			return cont;
		}

		@Override
		public boolean isEmpty() {
			return AvlGTree.this.isEmpty();
		}

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new AvlIterator(root);
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException(
					"Invalid operation Have not implemented remove yet");
		}

		@Override
		public int size() {
			return size; 
		}

		@Override
		public Object[] toArray() {
			Iterator<Map.Entry<K, V>> iter = new EntrySet.AvlIterator(root);
			Object[] arr = new Object[size];
			for (int i = 0; iter.hasNext(); i++) {
				arr[i] = iter.next();
			}
			
			return arr;
			
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * Iterator taken from
		 * http://stackoverflow.com/questions/4581576/implementing
		 * -an-iterator-over-a-binary-search-tree
		 * @author picks
		 *
		 */
		private class AvlIterator implements Iterator {
			int modCount;
		    private Stack<AvlNode<K,V>> stack = new Stack<AvlNode<K,V>>();
		    private AvlNode current;
		    AvlGTree<K, V> tree;

		    public AvlIterator(){
		    	modCount = AvlGTree.this.modCount;
		    	tree = AvlGTree.this;
		    	
		    }
		    
		    private AvlIterator(AvlNode argRoot) {
		        current = argRoot;
		    }

		    public AvlNode next() {
				if (modCount != AvlGTree.this.modCount){
					throw new ConcurrentModificationException();
				}
					
				if (!hasNext()){
					throw new NoSuchElementException();
				}
		    	
		        while (current != null) {
		            stack.push(current);
		            current = current.left;
		        }

		        current = stack.pop();
		        AvlNode node = current;
		        current = current.right;

		        return node;
		    }

		    public boolean hasNext() {
		        return (!stack.isEmpty() || current != null);
		    }

		    public Iterator iterator(AvlNode root) {
		        return new AvlIterator(root);
		    }

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Invalid operation Have not implemented remove yet");				
			}
		}
	}
}
