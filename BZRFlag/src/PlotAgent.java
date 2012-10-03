import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.overlay.PolygonBuilder;


public class PlotAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateConstants();
		bzrc.updateAll();
		String ourColor = bzrc.constants.get("team");
			
		for(int x = -400; x < 400; x += 32) {
			for(int y = -400; y < 400; y += 32) {
				Point2D.Float goal = null;
				double minDist = Double.MAX_VALUE;
				for(int i = 0; i < bzrc.flags.size(); i++) {
					Flag f = bzrc.flags.get(i);
					
					//If it is our flag in our possession,
					//or another flag in our possession, don't go for it
					if((f.getColor().equals(ourColor) && f.getPossessingColor() == null) || ourColor.equals(f.getPossessingColor())) {
						continue;
					}
					
					double dist = f.getPosition().distance(x, y);
					if(dist < minDist) {
						minDist = dist;
						goal = f.getPosition();
					}
				}
					
				double distance = goal.distance(x, y);
				double radius = Double.parseDouble(bzrc.constants.get("flagradius"));
				double angle = Math.atan2(goal.y - y, goal.x - x);
				
				if(distance > radius) {

					double deltaX = 0;//(distance - radius) * Math.cos(angle);
					double deltaY = 0;//(distance - radius) * Math.sin(angle);
					
					//determine effect of obstacles on path
					int obstacleSpread = 70;
					for(int i = 0; i < bzrc.obstacles.size(); i++) {
						Obstacle o = bzrc.obstacles.get(i);
						Coordinate[] coordinates = new Coordinate[o.getCorners().size() + 1];
						for (int k = 0; k < o.getCorners().size(); k++) {
							coordinates[k] = new Coordinate(o.getCorners().get(k).getX(), o.getCorners().get(k).getY());
						}
						coordinates[o.getCorners().size()] = new Coordinate(o.getCorners().get(0).getX(), o.getCorners().get(0).getY());
						GeometryFactory fact = new GeometryFactory();
						LinearRing ring = new GeometryFactory().createLinearRing(coordinates);
						Polygon poly = new Polygon(ring, null, fact);
						Coordinate[] tankPositionArray = {new Coordinate(x, y)};
						Point tankPoint = new Point(new CoordinateArraySequence(tankPositionArray), fact);
						double objAngle = Math.atan2(poly.getCentroid().getY() - y, poly.getCentroid().getX() - x);
						Coordinate nearestPoint = DistanceOp.closestPoints(poly, tankPoint)[0];
						double obstacleRadius = Math.sqrt(Math.pow(poly.getCentroid().getX() - nearestPoint.x, 2) +
														  Math.pow(poly.getCentroid().getY() - nearestPoint.y, 2)); 
						double obDist = Math.sqrt(Math.pow(poly.getCentroid().getX() - tankPoint.getX(), 2) +
								  				  Math.pow(poly.getCentroid().getY() - tankPoint.getY(), 2)); 
						if(obstacleRadius > obDist) {
							deltaX = 0;
							deltaY = 0;
						} else if (obstacleRadius <= obDist && obDist <= obstacleSpread + obstacleRadius) {
							double beta = 8;
							double gamma = 3;
							
							//This gives a repulsive potential field for obstacles
							double deltaXRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.cos(objAngle);
							double deltaYRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.sin(objAngle);
							deltaX = 0;
							deltaY = 0;
							//deltaX += beta * deltaXRepel;
							//deltaY += beta * deltaYRepel;
							
							// This is to give a tangential potential field causing the tanks to be slightly
							// inclined to going in a clockwise direction around an object.
							double deltaXTan = deltaYRepel;
							double deltaYTan = - deltaXRepel;
							
							deltaX += gamma * deltaXTan;
							deltaY += gamma * deltaYTan;
						}
					}
					
					double VEC_LEN = 0.004 * 800 / 25;
					
					System.out.println((x - (deltaX * VEC_LEN / 2)) + " " + (y - (deltaY * VEC_LEN / 2)) + " " +
				            (deltaX * VEC_LEN) + " " + (deltaY * VEC_LEN));
				}
			}
		}
	}
	
	private static double toDegrees(double radian) {
    	double degree = Math.toDegrees(radian);
    	if(degree < 0) {
    		degree += 360;
    	}
    	return degree;
    }
}
