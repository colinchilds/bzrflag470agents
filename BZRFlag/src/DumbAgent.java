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
		moveTimer.schedule(new MoveTask(bzrc, bzrc.myTanks.get(4)), 0, 5000);
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
			bzrc.shoot(tank.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
