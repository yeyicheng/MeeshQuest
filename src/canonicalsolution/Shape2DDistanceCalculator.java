package canonicalsolution ;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Shape2DDistanceCalculator {
	
	public static double distance(Point2D pt, Rectangle2D rect) {
		double distanceSq = 0;
		
		if (pt.getX() < rect.getMinX()) {
			final double xdist = rect.getMinX() - pt.getX();
			distanceSq += xdist * xdist;
		} else if (pt.getX() > rect.getMaxX()) {
			final double xdist = pt.getX() - rect.getMaxX();
			distanceSq += xdist * xdist;
		}
		
		if (pt.getY() < rect.getMinY()) {
			final double ydist = rect.getMinY() - pt.getY();
			distanceSq += ydist * ydist;
		} else if (pt.getY() > rect.getMaxY()) {
			final double ydist = pt.getY() - rect.getMaxY();
			distanceSq += ydist * ydist;
		}
		
		/* circle-rectangle intersection if distance <= radius */
		return Math.sqrt(distanceSq);
	}

}
