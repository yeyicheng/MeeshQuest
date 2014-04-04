package cmsc420.meeshquest.part2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

public class Road implements Comparable<Road>{

	@Override
	public String toString() {
		return "Road [cities=" + Arrays.toString(cities) + ", line=" + line
				+ "]";
	}

	private City[] cities = new City[2];

	public City[] getCities() {
		return cities;
	}

	private Line2D line;

	public Road(City s_city, City e_city) {
		if (s_city.compareTo(e_city) <= 0){
			cities[0] = s_city;
			cities[1] = e_city;
		} else {
			cities[1] = s_city;
			cities[0] = e_city;	
		}
		

		line = new java.awt.geom.Line2D.Double(s_city.getX(), s_city.getY(),
				e_city.getX(), e_city.getY());
	}

	public Line2D getLine() {
		return line;
	}

	public void setLine(Line2D line) {
		this.line = line;
	}

	/**
	 * Determines if point is close enough to the road to be actually on it
	 * 
	 * @param p
	 * @return
	 */
	public boolean point_close_enough(Point2D p) {
		double normalLength = Math.sqrt((cities[1].getX() - cities[0].getX())
				* (cities[1].getX() - cities[0].getX())
				+ (cities[1].getY() - cities[0].getY())
				* (cities[1].getY() - cities[0].getY()));

		double distance = Math.abs((p.getX() - cities[0].getX())
				* (cities[1].getY() - cities[0].getY())
				- (p.getY() - cities[0].getY())
				* (cities[1].getX() - cities[0].getX()))
				/ normalLength;
		
		System.out.println(distance);

		if (distance == 0.0){
			return true;
		} else {
			return false;
		}
		
	}

/*	@Override
	public boolean equals(Road rd) {
		if (cities[0].compareTo(rd.cities[0]) == 0 && cities[1].compareTo(cities[1]) == 0){
			return true;
		} else {
			return false;
		}
	}*/

	/**
	 * Compares based on the start city, tie break goes to end city
	 */
	public int compareTo(Road o) {	
		if (cities[0].compareTo(o.cities[0]) > 0){
			return 1;
		} else if (cities[0].compareTo(o.cities[0]) < 0){
			return -1;
		} else {
			return cities[1].compareTo(o.cities[1]);
		}
	}
}
