import java.util.Timer;
import java.util.TimerTask;


public class DumbAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateAll();
		
		Timer moveTimer = new Timer();
		moveTimer.schedule(new MoveTask(bzrc, bzrc.myTanks.get(8)), 0, 10000);
		
		Timer shootTimer = new Timer();
		shootTimer.schedule(new ShootTask(bzrc, bzrc.myTanks.get(8)), 0, 2000);
	}
}

class MoveTask extends TimerTask {
    private BZRController bzrc;
    private MyTank tank;
 
    public MoveTask(BZRController bzrc, MyTank tank) {
    	this.bzrc = bzrc;
    	this.tank = tank;
    }
 
    public void run() {
        try {
			bzrc.speed(tank.getId());
			Thread.sleep(5000);
			bzrc.speed(tank.getId(), 0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

class ShootTask extends TimerTask {
    private BZRController bzrc;
    private MyTank tank;
 
    public ShootTask(BZRController bzrc, MyTank tank) {
    	this.bzrc = bzrc;
    	this.tank = tank;
    }
 
    public void run() {
        try {
			bzrc.shoot(tank.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
