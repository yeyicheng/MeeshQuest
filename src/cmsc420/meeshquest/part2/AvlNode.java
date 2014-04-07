package cmsc420.meeshquest.part2;

    // Basic node stored in AVL trees
    // Note that this class is not accessible outside
    // of package DataStructures

    class AvlNode<K,V>
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
            value = V;
            left     = lt;
            right    = rt;
            height   = 0;
        }

            // Friendly data; accessible by other package routines
        Comparable key;      // The data in the node
        Object value;
        AvlNode    left;         // Left child
        AvlNode    right;        // Right child
        int        height;       // Height
    }
