package cmsc420.meeshquest.part2 ;


import java.awt.geom.Point2D;
import java.util.ArrayList;

import cmsc420.geom.Circle2D;

/**
 * City class is an analogue to a real-world city in 2D space. Each city
 * contains a location ((x,y) coordinates), name, radius, and color.
 * <p>
 * Useful <code>java.awt.geom.Point2D</code> methods (such as distance()) can
 * be utilized by calling toPoint2D(), which creates a Point2D copy of this
 * city's location.
 * 
 * @author Ben Zoller
 * @version 1.0, 19 Feb 2007
 */
public class City implements Comparable<City>{
	/** name of this city */
	protected String name;

	/** 2D coordinates of this city */
	protected Point2D.Float pt;

	/** radius of this city */
	protected int radius;
	
	/**
	 * Gets the cities point
	 * @return
	 */
	public Point2D.Float getPt() {
		return pt;
	}

	public void setPt(Point2D.Float pt) {
		this.pt = pt;
	}

	protected Circle2D.Float circle;

	/** color of this city */
	protected String color;

	public ArrayList<Road> roads = new ArrayList<Road>();
	
	
	/**
	 * Constructs a city.
	 * 
	 * @param name
	 *            name of the city
	 * @param x
	 *            X coordinate of the city
	 * @param y
	 *            Y coordinate of the city
	 * @param radius
	 *            radius of the city
	 * @param color
	 *            color of the city
	 */
	public City(final String name, final int x, final int y, final int radius,
			final String color) {
		this.name = name;
		pt = new Point2D.Float(x, y);
		this.radius = radius;
		circle = new Circle2D.Float(pt, radius);
		this.color = color;
	}

	public Circle2D.Float getCircle() {
		return circle;
	}

	public void setCircle(Circle2D.Float circle) {
		this.circle = circle;
	}

	/**
	 * Gets the name of this city.
	 * 
	 * @return name of this city
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the X coordinate of this city.
	 * 
	 * @return X coordinate of this city
	 */
	public int getX() {
		return (int) pt.x;
	}

	/**
	 * Gets the Y coordinate of this city.
	 * 
	 * @return Y coordinate of this city
	 */
	public int getY() {
		return (int) pt.y;
	}

	/**
	 * Gets the color of this city.
	 * 
	 * @return color of this city
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Gets the radius of this city.
	 * 
	 * @return radius of this city.
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Determines if this city is equal to another object. The result is true if
	 * and only if the object is not null and a City object that contains the
	 * same name, X and Y coordinates, radius, and color.
	 * 
	 * @param obj
	 *            the object to compare this city against
	 * @return <code>true</code> if cities are equal, <code>false</code>
	 *         otherwise
	 */
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;
			return (pt.equals(c.pt) && (radius == c.radius) && color
					.equals(c.color));
		}
		return false;
	}

	/**
	 * Returns a hash code for this city.
	 * 
	 * @return hash code for this city
	 */
	public int hashCode() {
		int hash = 12;
		hash = 37 * hash + name.hashCode();
		hash = 37 * hash + pt.hashCode();
		hash = 37 * hash + radius;
		hash = 37 * hash + color.hashCode();
		return hash;
	}

	/**
	 * Returns an (x,y) representation of the city. Important: casts the x and y
	 * coordinates to integers.
	 * 
	 * @return string representing the location of the city
	 */
	public String getLocationString() {
		final StringBuilder location = new StringBuilder();
		location.append("(");
		location.append(getX());
		location.append(",");
		location.append(getY());
		location.append(")");
		return location.toString();

	}

	/**
	 * Returns a Point2D instance representing the City's location.
	 * 
	 * @return location of this city
	 */
	public Point2D toPoint2D() {
		return new Point2D.Float(pt.x, pt.y);
	}
	
	public String toString() {
		//return name; // + "(" + pt.x + ", " + pt.y + ")";
		return "(" + (int)pt.x + "," + (int)pt.y + ")";
	}

	// Compares only on name lexographically
	public int compareTo(City o) {
		return name.compareTo(o.name);
		
	}

	public void addRoad(Road road) {
		roads.add(road);
		
	}
}