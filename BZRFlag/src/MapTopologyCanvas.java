import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class MapTopologyCanvas extends JPanel {

	int width = 400;
	int height = 400;
	ArrayList<int[]> circles = new ArrayList<int[]>();
	
	public MapTopologyCanvas(int width, int height) {
        this.width = width;
        this.height = height;
    }
	
	public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        for (int i = 0; i < circles.size(); i++) {
        	int[] values = circles.get(i);
        	int color = values[4];
        	g2.setColor(new Color(color, color, color));
        	g2.drawOval(values[0] - values[2]/2, values[1] - values[3]/2, values[2], values[3]);
        }
    }
    
    public void drawCircle(int x, int y, int radiusX, int radiusY, double d) {
    	int color = (int)(255 * (1 - d));
    	int values[] = {x, y, radiusX, radiusY, color};
    	circles.add(values);
    }
    
    public void clearCircles() {
    	circles.clear();
    }
    
    public void redraw() {
    	repaint();
    }
}
