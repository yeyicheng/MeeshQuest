package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;

/**
 * PR Quadtree is a region quadtree capable of storing points.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class PRQuadtree {
	/** root of the PR Quadtree */
	protected Node root;

	/** bounds of the spatial map */
	protected Point2D.Float spatialOrigin;

	/** width of the spatial map */
	protected int spatialWidth;

	/** height of the spatial map */
	protected int spatialHeight;

	/** used to keep track of cities within the spatial map */
	protected HashSet<String> cityNames;

	/** Used to store all of the roads in the spatial map */
	protected TreeSet<Road> roads;

	/** empty PR Quadtree node */
	protected EmptyNode emptyNode = new EmptyNode();

	/** graphical representation of spatial map */
	protected CanvasPlus canvas;
	
	// Adjacency List structure
	TreeMap<City,HashSet<City>> adjacency_list = new TreeMap<City,HashSet<City>>();

	/**
	 * Constructs an empty PR Quadtree.
	 */
	public PRQuadtree() {
		root = emptyNode;
		cityNames = new HashSet<String>();
		roads = new TreeSet<Road>();
		spatialOrigin = new Point2D.Float(0, 0);
	}

	/**
	 * Sets the width and height of the spatial map.
	 * 
	 * @param spatialWidth
	 *            width of the spatial map
	 * @param spatialHeight
	 *            height of the spatial map
	 */
	public void setRange(int spatialWidth, int spatialHeight) {
		this.spatialWidth = spatialWidth;
		this.spatialHeight = spatialHeight;
	}

	/**
	 * Sets the graphical representation of the spatial map.
	 * 
	 * @param canvas
	 *            graphical map
	 */
	public void setCanvas(CanvasPlus canvas) {
		this.canvas = canvas;
	}

	/**
	 * Gets the height of the spatial map
	 * 
	 * @return height of the spatial map
	 */
	public float getSpatialHeight() {
		return spatialHeight;
	}

	/**
	 * Gets the width of the spatial map
	 * 
	 * @return width of the spatial map
	 */
	public float getSpatialWidth() {
		return spatialWidth;
	}

	/**
	 * Gets the root node of the PR Quadtree.
	 * 
	 * @return root node of the PR Quadtree
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Whether the PR Quadtree has zero or more elements.
	 * 
	 * @return <code>true</code> if the PR Quadtree has no non-empty nodes.
	 *         Otherwise returns <code>false</code>
	 */
	public boolean isEmpty() {
		return (root == emptyNode);
	}

	/**
	 * Inserts a city into the spatial map.
	 * 
	 * @param city
	 *            city to be added
	 * @throws CityAlreadyMappedException
	 *             city is already in the spatial map
	 * @throws CityOutOfBoundsException
	 *             city's location is outside the bounds of the spatial map
	 */
	public void add(City city) throws CityAlreadyMappedException,
			CityOutOfBoundsException {
		if (cityNames.contains(city.getName())) {
			/* city already mapped */
			throw new CityAlreadyMappedException();
		}

		/* check bounds */
		int x = (int) city.getX();
		int y = (int) city.getY();
		if (x < spatialOrigin.x || x > spatialWidth || y < spatialOrigin.y
				|| y > spatialHeight) {
			/* city out of bounds */
			throw new CityOutOfBoundsException();
		}

		/* insert city into PRQuadTree */
		cityNames.add(city.getName());
		root = root.add(city, spatialOrigin, spatialWidth, spatialHeight);
	}

	/**
	 * Recursively Adds road to specified nodes/regions it crosses through
	 * 
	 * @param road
	 */
	public void add(Road road) {
		roads.add(road);
		root = root.add(road, spatialOrigin, spatialWidth, spatialHeight);
	}

	/**
	 * Removes a given city from the spatial map.
	 * 
	 * @param city
	 *            city to be removed
	 * @throws CityNotMappedException
	 *             city is not in the spatial map
	 */
	public boolean remove(City city) {
		final boolean success = cityNames.contains(city.getName());
		if (success) {
			cityNames.remove(city.getName());
			root = root
					.remove(city, spatialOrigin, spatialWidth, spatialHeight);
		}
		return success;
	}

	/**
	 * Clears the PR Quadtree so it contains no non-empty nodes.
	 */
	public void clear() {
		root = emptyNode;
		cityNames.clear();
		roads.clear();
		adjacency_list.clear();
	}

	/**
	 * Returns if the PR Quadtree contains a city with the given name.
	 * 
	 * @return true if the city is in the spatial map. false otherwise.
	 */
	public boolean contains(String name) {
		return cityNames.contains(name);
	}

	/**
	 * Returns if a point lies within a given rectangular bounds according to
	 * the rules of the PR Quadtree.
	 * 
	 * @param point
	 *            point to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */
	protected boolean intersects(Point2D point, Rectangle2D rect) {
		return (point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() <= rect
				.getMaxY());
	}

	/**
	 * Returns if any part of a circle lies within a given rectangular bounds
	 * according to the rules of the PR Quadtree.
	 * 
	 * @param circle
	 *            circular region to be checked
	 * @param rect
	 *            rectangular bounds the point is being checked against
	 * @return true if the point lies within the rectangular bounds, false
	 *         otherwise
	 */
	public boolean intersects(Circle2D circle, Rectangle2D rect) {
		final double radiusSquared = circle.getRadius() * circle.getRadius();

		/* translate coordinates, placing circle at origin */
		final Rectangle2D.Double r = new Rectangle2D.Double(rect.getX()
				- circle.getCenterX(), rect.getY() - circle.getCenterY(),
				rect.getWidth(), rect.getHeight());

		if (r.getMaxX() < 0) {
			/* rectangle to left of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper left corner */
				return ((r.getMaxX() * r.getMaxX() + r.getMinY() * r.getMinY()) < radiusSquared);
			} else {
				/* rectangle due west of circle */
				return (Math.abs(r.getMaxX()) < circle.getRadius());
			}
		} else if (r.getMinX() > 0) {
			/* rectangle to right of circle center */
			if (r.getMaxY() < 0) {
				/* rectangle in lower right corner */
				return ((r.getMinX() * r.getMinX() + r.getMaxY() * r.getMaxY()) < radiusSquared);
			} else if (r.getMinY() > 0) {
				/* rectangle in upper right corner */
				return ((r.getMinX() * r.getMinX() + r.getMinY() * r.getMinY()) <= radiusSquared);
			} else {
				/* rectangle due east of circle */
				return (r.getMinX() <= circle.getRadius());
			}
		} else {
			/* rectangle on circle vertical centerline */
			if (r.getMaxY() < 0) {
				/* rectangle due south of circle */
				return (Math.abs(r.getMaxY()) < circle.getRadius());
			} else if (r.getMinY() > 0) {
				/* rectangle due north of circle */
				return (r.getMinY() <= circle.getRadius());
			} else {
				/* rectangle contains circle center point */
				return true;
			}
		}
	}

	/**
	 * Thrown if city is already in the spatial map upon attempted insertion.
	 */
	public class CityAlreadyMappedException extends Throwable {
		private static final long serialVersionUID = -4096614031875292057L;

		public CityAlreadyMappedException() {
		}

		public CityAlreadyMappedException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown if a city attempted to be mapped is outside the bounds of the
	 * spatial map.
	 */
	public class CityOutOfBoundsException extends Throwable {
		private static final long serialVersionUID = -6878077114302943595L;

		public CityOutOfBoundsException() {
		}

		public CityOutOfBoundsException(String message) {
			super(message);
		}
	}
	
	/**
	 * Updates adjacency_list with a given roads information (adds a bidirectional link)
	 * @param road
	 */
	public void update_adjacency_list(Road road){
		
		if (adjacency_list.isEmpty()){
			HashSet<City> next_to = new HashSet<City>();
			next_to.add(road.getCities()[0]);
			HashSet<City> next_to2 = new HashSet<City>();
			next_to2.add(road.getCities()[1]);
			
			// Adds both cities to list and creates hashmaps for them
			adjacency_list.put(road.getCities()[0], next_to2);
			adjacency_list.put(road.getCities()[1], next_to);
		} else {
			// If start city is in adj_list already, add end city to starts list of adj cities
			if (adjacency_list.containsKey(road.getCities()[0])){
				HashSet<City> new_set = adjacency_list.get(road.getCities()[0]);
				new_set.add(road.getCities()[1]);
				adjacency_list.put(road.getCities()[0], new_set);
				
			} else { // if the start city is not in the adj list yet
				HashSet<City> new_set = new HashSet<City>();
				new_set.add(road.getCities()[1]);
				adjacency_list.put(road.getCities()[0], new_set);				
			}
			
			// If end city is in adj_list already, add start city to ends list of adj cities
			if (adjacency_list.containsKey(road.getCities()[1])){
				HashSet<City> new_set = adjacency_list.get(road.getCities()[1]);
				new_set.add(road.getCities()[0]);
				adjacency_list.put(road.getCities()[1], new_set);
				
			} else { // If the end city is not in adj list yet
				HashSet<City> new_set = new HashSet<City>();
				new_set.add(road.getCities()[0]);
				adjacency_list.put(road.getCities()[1], new_set);				
			}	
			
			
		}
		
	}

	/**
	 * Node abstract class for a PR Quadtree. A node can either be an empty
	 * node, a leaf node, or an internal node.
	 */
	public abstract class Node {
		/** Type flag for an empty PR Quadtree node */
		public static final int EMPTY = 0;

		/** Type flag for a PR Quadtree leaf node */
		public static final int LEAF = 1;

		/** Type flag for a PR Quadtree internal node */
		public static final int INTERNAL = 2;

		/** type of PR Quadtree node (either empty, leaf, or internal) */
		protected final int type;

		/**
		 * Constructor for abstract Node class.
		 * 
		 * @param type
		 *            type of the node (either empty, leaf, or internal)
		 */
		protected Node(final int type) {
			this.type = type;
		}

		/**
		 * Adds a city to the node. If an empty node, the node becomes a leaf
		 * node. If a leaf node already, the leaf node becomes an internal node
		 * and both cities are added to it. If an internal node, the city is
		 * added to the child whose quadrant the city is located within.
		 * 
		 * @param city
		 *            city to be added to the PR Quadtree
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return this node after the city has been added
		 */
		public abstract Node add(City city, Point2D.Float origin, int width,
				int height);

		/**
		 * Adds a Road to a given node or group of nodes
		 * 
		 * @param road
		 * @param origin
		 * @param width
		 * @param height
		 * @return this node after the road has been added
		 */
		public abstract Node add(Road road, Point2D.Float origin, int width,
				int height);

		/**
		 * Removes a city from the node. If this is a leaf node and the city is
		 * contained in it, the city is removed and the node becomes a leaf
		 * node. If this is an internal node, then the removal command is passed
		 * down to the child node whose quadrant the city falls in.
		 * 
		 * @param city
		 *            city to be removed
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 * @return this node after the city has been removed
		 */
		public abstract Node remove(City city, Point2D.Float origin, int width,
				int height);

		/**
		 * Gets the type of the node (either empty, leaf, or internal).
		 * 
		 * @return type of the node
		 */
		public int getType() {
			return type;
		}
	}

	/**
	 * Represents an empty leaf node of a PR Quadtree.
	 */
	public class EmptyNode extends Node {

		/**
		 * Constructs and initializes an empty node.
		 */
		public EmptyNode() {
			super(Node.EMPTY);
		}

		public Node add(City city, Point2D.Float origin, int width, int height) {
			Node leafNode = new LeafNode();
			return leafNode.add(city, origin, width, height);
		}

		public Node remove(City city, Point2D.Float origin, int width,
				int height) {
			/* should never get here, nothing to remove */
			throw new IllegalArgumentException();
		}

		/**
		 * Adds Road to leaf nodes rodes list
		 */
		public Node add(Road road, java.awt.geom.Point2D.Float origin,
				int width, int height) {
			Node leafNode = new LeafNode();
			return leafNode.add(road, origin, width, height);
		}

	}

	/**
	 * Represents a leaf node of a PR Quadtree.
	 */
	public class LeafNode extends Node {
		/** city contained within this leaf node */
		protected City city;

		// List of roads in Node
		public ArrayList<Road> roads = new ArrayList<Road>();

		// Rectangle2D object representing quadrant area
		public Rectangle2D rect;

		/**
		 * Constructs and initializes a leaf node.
		 */
		public LeafNode() {
			super(Node.LEAF);
		}

		/**
		 * Gets the city contained by this node.
		 * 
		 * @return city contained by this node
		 */
		public City getCity() {
			return city;
		}

		public Node add(City newCity, Point2D.Float origin, int width,
				int height) {
			if (city == null) {
				/* node is empty, add city */
				city = newCity;
				return this;
			} else {
				/* node is full, partition node and then add city */
				InternalNode internalNode = new InternalNode(origin, width,
						height);
				internalNode.add(city, origin, width, height);
				internalNode.add(newCity, origin, width, height);
				for (Road r : roads) {
					internalNode.add(r, origin, width, height);
				}
				return internalNode;
			}
		}

		@Override
		public Node add(Road road, java.awt.geom.Point2D.Float origin,
				int width, int height) {
			roads.add(road);
			City[] cities = road.getCities();
			
			cities[0].addRoad(road);
			cities[1].addRoad(road);
			
			update_adjacency_list(road);
			
			/*
			// TODO Highlights Region that road passes through (FOR TESTING)
			canvas.addRectangle(rect.getX(), regions[i].getY(),
					regions[i].getWidth(), regions[i].getHeight(),
					Color.RED, false);
*/			
			return this;
		}

		public Node remove(City city, Point2D.Float origin, int width,
				int height) {
			if (this.city != city) {
				/* city not here */
				throw new IllegalArgumentException();
			} else {
				/* remove city, node becomes empty */
				this.city = null;
				return emptyNode;
			}
		}
	}

	/**
	 * Represents an internal node of a PR Quadtree.
	 */
	public class InternalNode extends Node {
		/** children nodes of this node */
		protected Node[] children;

		/** rectangular quadrants of the children nodes */
		protected Rectangle2D.Float[] regions;

		/** origin of the rectangular bounds of this node */
		protected Point2D.Float origin;

		/** origins of the rectangular bounds of each child node */
		protected Point2D.Float[] origins;

		/** width of the rectangular bounds of this node */
		protected int width;

		/** height of the rectangular bounds of this node */
		protected int height;
		
		protected Rectangle2D.Float rect;

		/** half of the width of the rectangular bounds of this node */
		protected int halfWidth;

		/** half of the height of the rectangular bounds of this node */
		protected int halfHeight;

		/**
		 * Constructs and initializes this internal PR Quadtree node.
		 * 
		 * @param origin
		 *            origin of the rectangular bounds of this node
		 * @param width
		 *            width of the rectangular bounds of this node
		 * @param height
		 *            height of the rectangular bounds of this node
		 */
		public InternalNode(Point2D.Float origin, int width, int height) {
			super(Node.INTERNAL);

			rect = new Rectangle2D.Float(origin.x, origin.y,width, height);
			
			this.origin = origin;

			children = new Node[4];
			for (int i = 0; i < 4; i++) {
				children[i] = emptyNode;
			}

			this.width = width;
			this.height = height;

			halfWidth = width >> 1;
			halfHeight = height >> 1;

			origins = new Point2D.Float[4];
			origins[0] = new Point2D.Float(origin.x, origin.y + halfHeight);
			origins[1] = new Point2D.Float(origin.x + halfWidth, origin.y
					+ halfHeight);
			origins[2] = new Point2D.Float(origin.x, origin.y);
			origins[3] = new Point2D.Float(origin.x + halfWidth, origin.y);

			regions = new Rectangle2D.Float[4];
			int i = 0;
			while (i < 4) {
				// Generates rectangle for specific quadrant
				regions[i] = new Rectangle2D.Float(origins[i].x, origins[i].y,
						halfWidth, halfHeight);
				i++;
			}

			/* add a cross to the drawing panel */
			if (canvas != null) {
				// canvas.addCross(getCenterX(), getCenterY(), halfWidth,
				// Color.BLACK);
				int cx = getCenterX();
				int cy = getCenterY();
				// System.out.println(cx + ":X  " + cy + ":Y  " );
				canvas.addLine(cx - halfWidth, cy, cx + halfWidth, cy,
						Color.BLACK);
				canvas.addLine(cx, cy - halfHeight, cx, cy + halfHeight,
						Color.BLACK);
			}
		}

		public Node add(City city, Point2D.Float origin, int width, int height) {
			final Point2D cityLocation = city.toPoint2D();
			for (int i = 0; i < 4; i++) {
				if (intersects(cityLocation, regions[i])) {
					children[i] = children[i].add(city, origins[i], halfWidth,
							halfHeight);
					//break;
				}
			}
			return this;
		}

		@Override
		public Node add(Road road, java.awt.geom.Point2D.Float origin,
				int width, int height) {

			// Recursively adds roads if they are in the specific region
			// if EmptyNode it creates a new LeafNode
			// if LeafNode it just adds the road to its roads list
			// if InternalNode it adds it loops over children recursing on
			// other InternalNodes.
			for (int i = 0; i < 4; i++) {
				if (Inclusive2DIntersectionVerifier.intersects(road.getLine(),
						regions[i])) {
					children[i] = children[i].add(road, origin, width, height);

					/*
					// TODO Highlights Region that road passes through (FOR TESTING)
					canvas.addRectangle(regions[i].getX(), regions[i].getY(),
							regions[i].getWidth(), regions[i].getHeight(),
							Color.RED, false);*/
				}
			}

			return this;
		}

		public Node remove(City city, Point2D.Float origin, int width,
				int height) {
			final Point2D cityLocation = city.toPoint2D();
			for (int i = 0; i < 4; i++) {
				if (intersects(cityLocation, regions[i])) {
					children[i] = children[i].remove(city, origins[i],
							halfWidth, halfHeight);
				}
			}

			if (getNumEmptyNodes() == 4) {
				/* remove cross from the drawing panel */
				if (canvas != null)
					canvas.removeCross(getCenterX(), getCenterY(), halfWidth,
							Color.BLACK);
				// TODO During removal of a node Must return all of the roads
				// that have pass through from each of the children
				return emptyNode;

			} else if (getNumEmptyNodes() == 3 && getNumLeafNodes() == 1) {
				/* remove cross from the drawing panel */
				if (canvas != null)
					canvas.removeCross(getCenterX(), getCenterY(), halfWidth,
							Color.BLACK);
				for (Node node : children) {
					if (node.getType() == Node.LEAF) {
						return node;
					}
				}
				/* should never get here */
				return null;

			} else {
				return this;
			}

		}

		/**
		 * Gets the number of empty child nodes contained by this internal node.
		 * 
		 * @return the number of empty child nodes
		 */
		protected int getNumEmptyNodes() {
			int numEmptyNodes = 0;
			for (Node node : children) {
				if (node.getType() == Node.EMPTY) {
					numEmptyNodes++;
				}
			}
			return numEmptyNodes;
		}

		/**
		 * Gets the number of leaf child nodes contained by this internal node.
		 * 
		 * @return the number of leaf child nodes
		 */
		protected int getNumLeafNodes() {
			int numLeafNodes = 0;
			for (Node node : children) {
				if (node.getType() == Node.LEAF) {
					numLeafNodes++;
				}
			}
			return numLeafNodes;
		}

		/**
		 * Gets the child node of this node according to which quadrant it falls
		 * in
		 * 
		 * @param quadrant
		 *            quadrant number (top left is 0, top right is 1, bottom
		 *            left is 2, bottom right is 3)
		 * @return child node
		 */
		public Node getChild(int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return children[quadrant];
			}
		}

		/**
		 * Gets the rectangular region for the specified child node of this
		 * internal node.
		 * 
		 * @param quadrant
		 *            quadrant that child lies within
		 * @return rectangular region for this child node
		 */
		public Rectangle2D.Float getChildRegion(int quadrant) {
			if (quadrant < 0 || quadrant > 3) {
				throw new IllegalArgumentException();
			} else {
				return regions[quadrant];
			}
		}

		/**
		 * Gets the rectangular region contained by this internal node.
		 * 
		 * @return rectangular region contained by this internal node
		 */
		public Rectangle2D.Float getRegion() {
			return new Rectangle2D.Float(origin.x, origin.y, width, height);
		}

		/**
		 * Gets the center X coordinate of this node's rectangular bounds.
		 * 
		 * @return center X coordinate of this node's rectangular bounds
		 */
		public int getCenterX() {
			return (int) origin.x + halfWidth;
		}

		/**
		 * Gets the center Y coordinate of this node's rectangular bounds.
		 * 
		 * @return center Y coordinate of this node's rectangular bounds
		 */
		public int getCenterY() {
			return (int) origin.y + halfHeight;
		}

		/**
		 * Gets half the width of this internal node.
		 * 
		 * @return half the width of this internal node
		 */
		public int getHalfWidth() {
			return halfWidth;
		}

		/**
		 * Gets half the height of this internal node.
		 * 
		 * @return half the height of this internal node
		 */
		public int getHalfHeight() {
			return halfHeight;
		}
		
		/**
		 * Returns the rectangle
		 */
		public Rectangle2D.Float getRect(){
			return rect;
		}
	}
}
