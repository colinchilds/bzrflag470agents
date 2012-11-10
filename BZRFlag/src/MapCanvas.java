import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class MapCanvas extends JPanel {

	private BufferedImage canvas;
	
	public MapCanvas(int width, int height) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
	
	public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }
    
    public void colorPixel(int x, int y, float value) {
    	int color = (int)(255 * (1 - value));
    	canvas.setRGB(x, y, (new Color(color, color, color)).getRGB());
    }
    
    public void redraw() {
    	repaint();
    }
    
}
