
public class Occgrid {

	private int x;
	private int y;
	private int width;
	private int height;
	private int[][] grid;
	
	public Occgrid(int x, int y, int width, int height, int[][] grid) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.grid = grid;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int[][] getGrid() {
		return grid;
	}

	public void setGrid(int[][] grid) {
		this.grid = grid;
	}
}
