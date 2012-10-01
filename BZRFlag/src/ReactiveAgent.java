import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;

import com.vividsolutions.jts.algorithm.Angle;


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
						
						//If it is our flag, don't go for it
						if(f.getColor().equals(ourColor) || ourColor.equals(f.getPossessingColor())) {
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
					int obstacleRadius = 100;
					for(int i = 0; i < bzrc.obstacles.size(); i++) {
						Obstacle o = bzrc.obstacles.get(i);
						for(int k = 0; k < o.getCorners().size(); k++) {
							Point2D.Float p = o.getCorners().get(k);
							double obDist = p.distance(t.x, t.y);
							if(obDist < obstacleRadius) {
								double objAngle = Math.atan2(p.y - t.y, p.x - t.x);
								deltaX += 3 * ((obDist - obstacleRadius) * Math.cos(objAngle));
								deltaY += 3 * ((obDist - obstacleRadius) * Math.sin(objAngle));
							}
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
						commands.add(new Command(Command.SPEED, t.getId(), (float)Math.min(distance/30, 1)));
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
