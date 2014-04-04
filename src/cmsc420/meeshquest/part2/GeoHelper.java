package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;

public class GeoHelper {

	
	/**
	 * Determines if a point intersects a rectangle
	 * @param point
	 * @param rect
	 * @return
	 */
	public static boolean intersects(Rectangle2D rect, Point2D point) {
		return (point.getX() >= rect.getMinX() && point.getX() <= rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() <= rect
				.getMaxY());
	}
}

