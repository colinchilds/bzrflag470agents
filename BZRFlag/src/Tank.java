
public class Tank {

	private String id;
	private String callsign;
	private String status;
	private int shotsAvailable;
	private float timeToReload;
	private String flag;
	private float x;
	private float y;
	private float angle;
	private float vx;
	private float vy;
	private float angvel;
	
	public Tank() {
		
	}
	
	public Tank(String id, String callsign) {
		this.id = id;
		this.callsign = callsign;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean isAlive() {
		return "alive".equalsIgnoreCase(status);
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

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
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
