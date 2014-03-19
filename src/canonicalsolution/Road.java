package canonicalsolution;

import java.awt.geom.Line2D;

public class Road {
	
	private City[] cities = new City[2];
	
	public City[] getCities() {
		return cities;
	}

	private Line2D line;
	
	public Road(City s_city, City e_city) {
		cities[0] = s_city;
		cities[1] = e_city;
		
		line = new java.awt.geom.Line2D.Double(
				s_city.getX(), s_city.getY(), e_city.getX(), e_city.getY());
	}

	public Line2D getLine() {
		return line;
	}

	public void setLine(Line2D line) {
		this.line = line;
	}
}
