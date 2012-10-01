
public class Command {

	private int type;
	private String tank;
	private float arg1;
	
	public static final int SPEED = 1;
	public static final int ANGVEL = 2;
	public static final int SHOOT = 3;
	
	public Command(int type, String tank) {
		this.type = type;
		this.tank = tank;
	}
	
	public Command(int type, String tank, float arg1) {
		this.type = type;
		this.tank = tank;
		this.arg1 = arg1;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTank() {
		return tank;
	}

	public void setTank(String tank) {
		this.tank = tank;
	}

	public float getArg1() {
		return arg1;
	}

	public void setArg1(float arg1) {
		this.arg1 = arg1;
	}
}
