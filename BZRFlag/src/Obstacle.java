import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Obstacle {

	private ArrayList<Point2D.Float> corners = new ArrayList<Point2D.Float>();
	
	public Obstacle() {
		
	}
	
	public Obstacle(ArrayList<Point2D.Float> corners) {
		this.corners = corners;
	}

	public ArrayList<Point2D.Float> getCorners() {
		return corners;
	}

	public void setCorners(ArrayList<Point2D.Float> corners) {
		this.corners = corners;
	}
	
	public void addCorner(Point2D.Float corner) {
		corners.add(corner);
	}
}
