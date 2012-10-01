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


public class ReactiveAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateConstants();
		String ourColor = bzrc.constants.get("team");
		
		while(true) {
			Date start = new Date();
			bzrc.updateAll();
			
			ArrayList<Command> commands = new ArrayList<Command>();
			
			for(int j = 0; j < bzrc.myTanks.size(); j++) {
				MyTank t = bzrc.myTanks.get(j+"");
				Point2D.Float goal = null;
				if(t.flag != null) {
					goal = bzrc.teams.get(ourColor).getBaseCenter();
				} else {
					double minDist = Double.MAX_VALUE;
					for(int i = 0; i < bzrc.flags.size(); i++) {
						Flag f = bzrc.flags.get(i);
						
						//If it is our flag in our possession,
						//or another flag in our possession, don't go for it
						if((f.getColor().equals(ourColor) && f.getPossessingColor() == null) || ourColor.equals(f.getPossessingColor())) {
							continue;
						}
						
						double dist = f.getPosition().distance(t.getX(), t.getY());
						if(dist < minDist) {
							minDist = dist;
							goal = f.getPosition();
						}
					}
					
					if(goal == null) {
						for(int i = 0; i < bzrc.otherTanks.size(); i++) {
							OtherTank o = bzrc.otherTanks.get(i);
							
							double dist = o.getPosition().distance(t.getX(), t.getY());
							if(dist < minDist) {
								minDist = dist;
								goal = o.getPosition();
							}
						}
					}
					
					//if all else fails... go back home for now.
					//this will probably need to change when the entire field is not visible
					//ie. go explore somewhere
					if(goal == null) {
						goal = bzrc.teams.get(ourColor).getBaseCenter();
					}
				}
				
				double distance = goal.distance(t.getX(), t.getY());
				double radius = Double.parseDouble(bzrc.constants.get("flagradius"));
				double angle = Math.atan2(goal.y - t.y, goal.x - t.x);
				
				if(distance > radius) {

					double deltaX = (distance - radius) * Math.cos(angle);
					double deltaY = (distance - radius) * Math.sin(angle);
					
					//determine effect of obstacles on path
					int obstacleSpread = 50;
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
						Coordinate[] tankPositionArray = {new Coordinate(t.getX(), t.getY())};
						Point tankPoint = new Point(new CoordinateArraySequence(tankPositionArray), fact);
						double objAngle = Math.atan2(poly.getCentroid().getY() - t.getY(), poly.getCentroid().getX() - t.getX());
						Coordinate nearestPoint = DistanceOp.closestPoints(poly, tankPoint)[0];
						double obstacleRadius = Math.sqrt(Math.pow(poly.getCentroid().getX() - nearestPoint.x, 2) +
														  Math.pow(poly.getCentroid().getY() - nearestPoint.y, 2)); 
						double obDist = Math.sqrt(Math.pow(poly.getCentroid().getX() - tankPoint.getX(), 2) +
								  				  Math.pow(poly.getCentroid().getY() - tankPoint.getY(), 2)); 
						if(obDist < obstacleRadius) {
							//It should never get into this block
							deltaX += 2 * ((obDist - obstacleRadius) * Math.cos(objAngle));
							deltaY += 2 * ((obDist - obstacleRadius) * Math.sin(objAngle));
						} else if (obstacleRadius <= obDist && obDist <= obstacleSpread + obstacleRadius) {
							double beta = 10;
							double gamma = 2;
							
							//This gives a repulsive potential field for obstacles
							double deltaXRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.cos(objAngle);
							double deltaYRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.sin(objAngle);
							
							deltaX += beta * deltaXRepel;
							deltaY += beta * deltaYRepel;
							
							// This is to give a tangential potential field causing the tanks to be slightly
							// inclined to going in a clockwise direction around an object.
							double deltaXTan = deltaYRepel;
							double deltaYTan = - deltaXRepel;
							
							deltaX += gamma * deltaXTan;
							deltaY += gamma * deltaYTan;
						}
					}
					
					
//					double deltaAngle = toDegrees(Math.atan2(deltaY, deltaX));
//					double tankAngle = toDegrees(t.getAngle());
//					double angleDifference = tankAngle - deltaAngle;
//					if(angleDifference > 180) {
//						angleDifference = (tankAngle - 360) - deltaAngle;
//					} if(angleDifference == 0) {
//						angleDifference = .0001;
//					}
					
					double angleDifference = Angle.toDegrees(Angle.normalize(t.getAngle() - Math.atan2(deltaY, deltaX)));
					
					//TODO: adjust to not move until a certain degree
					if(Math.abs(angleDifference) > 60) {
						//bzrc.speed(t.getId(), .1f);
						commands.add(new Command(Command.SPEED, t.getId(), .1f));
					} else {
						//float speed = (float) (Math.min(3/Math.abs(angleDifference), Math.min(distance/20, 1)));
						//bzrc.speed(t.getId(), (float)Math.min(distance/20, 1));
						commands.add(new Command(Command.SPEED, t.getId(), (float)Math.min(distance/75, 1)));
					}
					
					//bzrc.angvel(t.getId(), ((float)-(angleDifference/180)));
					commands.add(new Command(Command.ANGVEL, t.getId(), (float)-(angleDifference/180)));
					if(distance < 150 || t.flag != null) {
						//bzrc.shoot(t.getId());
						commands.add(new Command(Command.SHOOT, t.getId()));
					}
				}
			}
			
			bzrc.doBulkCommands(commands);
			Date end = new Date();
			System.out.println("Loop time: " + (end.getTime() - start.getTime()));
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
