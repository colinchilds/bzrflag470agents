import java.util.Timer;
import java.util.TimerTask;


public class NonConformingClayPigeon extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		
		/*while(true) {
			bzrc.speed("0", .5f);
		}*/
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					bzrc.speed("0", (float) Math.random());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 0, 1000);
	}
}
