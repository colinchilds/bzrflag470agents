import java.awt.geom.Point2D;
import java.util.ArrayList;


public class Team {

	private String color;
	private int count;
	private ArrayList<Point2D.Float> baseCorners = new ArrayList<Point2D.Float>();
	
	public Team() {
		
	}
	
	public Team(String color, int count) {
		this.color = color;
		this.count = count;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<Point2D.Float> getBaseCorners() {
		return baseCorners;
	}

	public void setBaseCorners(ArrayList<Point2D.Float> baseCorners) {
		this.baseCorners = baseCorners;
	}
	
	public void addCorner(Point2D.Float corner) {
		baseCorners.add(corner);
	}
	
	public void resetBaseCorners() {
		baseCorners.clear();
	}
}
