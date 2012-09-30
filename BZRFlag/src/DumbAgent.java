import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;


public class DumbAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateAll();
		
		String id = "8";
		String id2 = "7";
		
		Calendar nextMove = GregorianCalendar.getInstance();
		Calendar nextShot = GregorianCalendar.getInstance();
		boolean isMoving = false;
		
		Calendar nextMove2 = GregorianCalendar.getInstance();
		Calendar nextShot2 = GregorianCalendar.getInstance();
		boolean isMoving2 = false;
		while(true) {
			if(nextMove.before(GregorianCalendar.getInstance())) {
				if(isMoving) {
					//stop
					bzrc.speed(id, 0);
					
					//rotate
					double initialAngle = toDegrees(bzrc.myTanks.get(id).angle);
					double currentAngle = initialAngle;
					bzrc.angvel(id, 1);
					while(!hasGone60Degrees(initialAngle, currentAngle)) {
						bzrc.updateMyTanks();
						currentAngle = toDegrees(bzrc.myTanks.get(id).angle);
					}
					bzrc.angvel(id, 0);
					
					isMoving = false;
					nextMove = GregorianCalendar.getInstance();
					nextMove.add(GregorianCalendar.MILLISECOND, 50);
				} else {
					//start moving again
					bzrc.speed(id);
					
					isMoving = true;
					nextMove = GregorianCalendar.getInstance();
					nextMove.add(GregorianCalendar.MILLISECOND, (int)(3000 + Math.random() * 5000));
				}
			}
			
			if(nextShot.before(GregorianCalendar.getInstance())) {
				bzrc.shoot(id);
				nextShot = GregorianCalendar.getInstance();
				nextShot.add(GregorianCalendar.MILLISECOND, (int)(1500 + Math.random() * 1000));
			}
			
			if(nextMove2.before(GregorianCalendar.getInstance())) {
				if(isMoving2) {
					//stop
					bzrc.speed(id2, 0);
					
					//rotate
					double initialAngle = toDegrees(bzrc.myTanks.get(id2).angle);
					double currentAngle = initialAngle;
					bzrc.angvel(id2, 1);
					while(!hasGone60Degrees(initialAngle, currentAngle)) {
						bzrc.updateMyTanks();
						currentAngle = toDegrees(bzrc.myTanks.get(id2).angle);
					}
					bzrc.angvel(id2, 0);
					
					isMoving2 = false;
					nextMove2 = GregorianCalendar.getInstance();
					nextMove2.add(GregorianCalendar.MILLISECOND, 50);
				} else {
					//start moving again
					bzrc.speed(id2);
					
					isMoving2 = true;
					nextMove2 = GregorianCalendar.getInstance();
					nextMove2.add(GregorianCalendar.MILLISECOND, (int)(3000 + Math.random() * 5000));
				}
			}
			
			if(nextShot2.before(GregorianCalendar.getInstance())) {
				bzrc.shoot(id2);
				nextShot2 = GregorianCalendar.getInstance();
				nextShot2.add(GregorianCalendar.MILLISECOND, (int)(1500 + Math.random() * 1000));
			}
		}
	}
	
	private static boolean hasGone60Degrees(double initial, double current) {
		if(current < initial) {
			current += 360;
		}
		return (current - initial) > 60;
	}
	
	private static double toDegrees(float radian) {
    	double degree = Math.toDegrees(radian);
    	if(degree < 0) {
    		degree += 360;
    	}
    	return degree;
    }
}


class ShootTask extends TimerTask {
    private BZRController bzrc;
    private String id;
 
    public ShootTask(BZRController bzrc, String id) {
    	this.bzrc = bzrc;
    	this.id = id;
    }
 
    public void run() {
        try {
			bzrc.shoot(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
