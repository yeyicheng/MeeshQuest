package canonicalsolution;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/**
 * Determines if a line specified by two points intersects a circle Note that
 * the two points are not a line segment but an infinite line extending in both
 * directions
 * 
 * @author arne.b (screen name from stackoverflow.com
 * 
 */
public class CircleLine {
	
	/**
	 * Returns intersection of line (pointA, pointB) and circle at center with radius
	 * @param pointA
	 * @param pointB
	 * @param center
	 * @param radius
	 * @return
	 */
	public static List<Point> getCircleLineIntersectionPoint(Point pointA,
			Point pointB, Point center, double radius) {
		double baX = pointB.x - pointA.x;
		double baY = pointB.y - pointA.y;
		double caX = center.x - pointA.x;
		double caY = center.y - pointA.y;

		double a = baX * baX + baY * baY;
		double bBy2 = baX * caX + baY * caY;
		double c = caX * caX + caY * caY - radius * radius;

		double pBy2 = bBy2 / a;
		double q = c / a;

		double disc = pBy2 * pBy2 - q;
		if (disc < 0) {
			return Collections.emptyList();
		}
		// if disc == 0 ... dealt with later
		double tmpSqrt = Math.sqrt(disc);
		double abScalingFactor1 = -pBy2 + tmpSqrt;
		double abScalingFactor2 = -pBy2 - tmpSqrt;

		Point p1 = new Point(pointA.x - baX * abScalingFactor1, pointA.y - baY
				* abScalingFactor1);
		if (disc == 0) { // abScalingFactor1 == abScalingFactor2
			return Collections.singletonList(p1);
		}
		Point p2 = new Point(pointA.x - baX * abScalingFactor2, pointA.y - baY
				* abScalingFactor2);
		return Arrays.asList(p1, p2);
	}
	
	/**
	 * Overload: instead of taking in CircleLine.Point's it takes in Point2D's
	 * @param p1
	 * @param p2
	 * @param pt
	 * @param radius
	 * @author Dylan Zingler
	 * @return List of Point2D's that are intersection points
	 */
	public static List<Point2D> getCircleLineIntersectionPoint(Point2D p1, Point2D p2,
			Point2D pt, int radius) {
		Point point1 = new Point(p1.getX(), p1.getY());
		Point point2 = new Point(p2.getX(), p2.getY());
		Point center = new Point(pt.getX(), pt.getY());

		ArrayList<Point2D> intersections = new ArrayList<Point2D>();
		for (Point r : getCircleLineIntersectionPoint(point1, point2, center, radius)){
			intersections.add(r.to_Point2D());
		}
		
		return intersections;

	}

	static class Point {
		double x, y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + "]";
		}
		
		public Point2D to_Point2D(){
			return new Point2D.Double(x,y);
			
		}
	}

	/*
	 * public static void main(String[] args) { //
	 * System.out.println(getCircleLineIntersectionPoint(new Point(-3, -3), //
	 * new Point(-3, 3), new Point(0, 0), 5)); //
	 * System.out.println(getCircleLineIntersectionPoint(new Point(0, -2), //
	 * new Point(1, -2), new Point(1, 1), 5)); //
	 * System.out.println(getCircleLineIntersectionPoint(new Point(1, -1), //
	 * new Point(-1, 0), new Point(-1, 1), 5));
	 * System.out.println(getCircleLineIntersectionPoint(new Point(1,0), new
	 * Point(2,0), new Point(0, 0), 3)); }
	 */

}