import java.awt.geom.Point2D;


public class Flag {

	private String color;
	private String possessingColor;
	private Point2D.Float position;
	
	public Flag() {
		
	}
	
	public Flag(String color, String possessingColor, Point2D.Float position) {
		this.color = color;
		this.possessingColor = possessingColor;
		this.position = position;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getPossessingColor() {
		return possessingColor;
	}

	public void setPossessingColor(String possessingColor) {
		this.possessingColor = possessingColor;
	}

	public Point2D.Float getPosition() {
		return position;
	}

	public void setPosition(Point2D.Float position) {
		this.position = position;
	}
}
