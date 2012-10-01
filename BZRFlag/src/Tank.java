import java.awt.geom.Point2D;


public class Tank {

	protected String callsign;
	protected String status;
	protected String flag;
	protected float x;
	protected float y;
	protected float angle;
	
	public Tank() {
		
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
	
	public Point2D.Float getPosition() {
		return new Point2D.Float(x, y);
	}

}
