package cmsc420.meeshquest.part2;

import org.w3c.dom.Element;

    // Basic node stored in AVL trees
    // Note that this class is not accessible outside
    // of package DataStructures

    class AvlNode<K extends Comparable<K>,V> implements java.util.Map.Entry<K,V>
    {
            @Override
		public String toString() {
			return "AvlNode [Key=" + key +", Value= " + value + ", height=" + height + "]";
		}

		// Constructors
        AvlNode( Comparable K, Object V )
        {
            this( K, V, null, null );
        }

        AvlNode( Comparable K, Object V, AvlNode lt, AvlNode rt )
        {
            key  = K;
            value = (V) V;
            left     = lt;
            right    = rt;
            height   = 0;
        }

            // Friendly data; accessible by other package routines
        Comparable key;      // The data in the node
        V value;
        AvlNode    left;         // Left child
        AvlNode    right;        // Right child
        int        height;       // Height
		
        @Override
		public K getKey() {
			return (K) key;
		}

		@Override
		public V getValue() {
			return (V) value;
		}

		@Override
		public V setValue(V value) {
			V old = (V) this.value;
			
			this.value = value;
			return value;
		}
        

        
    }
