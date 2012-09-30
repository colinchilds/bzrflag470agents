import java.awt.Polygon;
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
	
	//TODO: Make this better. Could give inaccurate results
	public Point2D.Float getBaseCenter() {
		if(baseCorners.size() == 0) {
			System.out.println("No base points found! ERROR!");
			return null;
		}
		
		float sumx = 0;
		float sumy = 0;
		
		for(int i = 0; i < baseCorners.size(); i++) {
			Point2D.Float p = baseCorners.get(i);
			sumx += p.x;
			sumy += p.y;
		}
		
		return new Point2D.Float(sumx/baseCorners.size(), sumy/baseCorners.size());
	}
}
