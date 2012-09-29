
public class MyTank extends Tank {

	private String id;
	private int shotsAvailable;
	private float timeToReload;
	private float vx;
	private float vy;
	private float angvel;
	
	public MyTank(String id, String callsign) {
		this.id = id;
		this.callsign = callsign;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getShotsAvailable() {
		return shotsAvailable;
	}

	public void setShotsAvailable(int shotsAvailable) {
		this.shotsAvailable = shotsAvailable;
	}

	public float getTimeToReload() {
		return timeToReload;
	}

	public void setTimeToReload(float timeToReload) {
		this.timeToReload = timeToReload;
	}

	public float getVx() {
		return vx;
	}

	public void setVx(float vx) {
		this.vx = vx;
	}

	public float getVy() {
		return vy;
	}

	public void setVy(float vy) {
		this.vy = vy;
	}

	public float getAngvel() {
		return angvel;
	}

	public void setAngvel(float angvel) {
		this.angvel = angvel;
	}
}
