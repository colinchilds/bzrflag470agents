import java.awt.geom.Point2D;


public class ReactiveAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateConstants();
		String ourColor = bzrc.constants.get("team");
		
		while(true) {
			bzrc.updateAll();
			
			for(int j = 0; j < bzrc.myTanks.size(); j++) {
				MyTank t = bzrc.myTanks.get(j+"");
				Point2D.Float goal;
				if(t.flag != null) {
					goal = bzrc.teams.get(ourColor).getBaseCenter();
				} else {
					double minDist = Double.MAX_VALUE;
					Flag minDistFlag = null;
					for(int i = 0; i < bzrc.flags.size(); i++) {
						Flag f = bzrc.flags.get(i);
						
						//If it is our flag, don't go for it
						if(f.getColor().equals(ourColor) || ourColor.equals(f.getPossessingColor())) {
							continue;
						}
						
						double dist = f.getPosition().distance(t.getX(), t.getY());
						if(dist < minDist) {
							minDist = dist;
							minDistFlag = f;
						}
					}
					
					if(minDistFlag == null) {
						bzrc.speed(t.getId(), 0);
						continue;
					}
					goal = minDistFlag.getPosition();
				}
				
				double distance = goal.distance(t.getX(), t.getY());
				double radius = Double.parseDouble(bzrc.constants.get("flagradius"));
				double angle = Math.atan2(goal.y - t.y, goal.x - t.x);
				
				if(distance > radius) {

					double deltaX = (distance - radius)*Math.cos(angle);
					double deltaY = (distance - radius)*Math.sin(angle);
					double deltaAngle = toDegrees(Math.atan2(deltaY, deltaX));
					double tankAngle = toDegrees(t.getAngle());
					double angleDifference = tankAngle - deltaAngle;
					if(angleDifference > 180) {
						angleDifference = (tankAngle - 360) - deltaAngle;
					} if(angleDifference == 0) {
						angleDifference = .0001;
					}
					
					
					//TODO: adjust to not move until a certain degree
					float speed = (float) (Math.min(8/Math.abs(angleDifference), Math.min(distance/20, 1)));
					bzrc.speed(t.getId(), speed);
					
					bzrc.angvel(t.getId(), ((float)-(angleDifference/180)));
					if(distance < 75 || t.flag != null) {
						bzrc.shoot(t.getId());
					}
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
