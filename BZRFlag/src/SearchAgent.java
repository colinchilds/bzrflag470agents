import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.vividsolutions.jts.algorithm.Angle;


public class SearchAgent extends Agent {

	private static float[][] map;
	
	private static MapCanvas canvas;
	
	public static void main(String[] args) throws Exception {
		connect(args);
		
		JFrame frame = new JFrame("Create a JPanel");
		canvas = new MapCanvas(800, 800);
		frame.add(canvas);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setResizable(false);
		frame.setVisible(true);
		
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateConstants();
		int worldSize = Integer.parseInt(bzrc.constants.get("worldsize"));
		
		map = new float[worldSize][worldSize];
//		for(int i = 0; i < worldSize; i++) {
//			for(int j = 0; j < worldSize; j++) {
//				map[i][j] = (float)Math.random();
//			}
//		}
		
		while(true) {
			for(int i = 0; i < worldSize; i++) {
				for(int j = 0; j < worldSize; j++) {
					map[i][j] = (float)Math.random();
				}
			}
			Date start = new Date();
			//bzrc.updateAll();
			bzrc.updateMyTanks();
			
			ArrayList<Command> commands = new ArrayList<Command>();
			
			//for(int j = 0; j < bzrc.myTanks.size(); j++) {
			for(int j = 0; j < 1; j++) {
				MyTank t = bzrc.myTanks.get(j+"");
				Occgrid grid = bzrc.getOccgrid(t.getId());
				
				Point2D.Float goal = null;
				float x = (float)(Math.random() * (worldSize/2) * (Math.random() > .5 ? -1 : 1));
				float y = (float)(Math.random() * (worldSize/2) * (Math.random() > .5 ? -1 : 1));
				goal = new Point2D.Float(x, y);
				
				double distance = goal.distance(t.getX(), t.getY());
				double radius = Double.parseDouble(bzrc.constants.get("flagradius"));
				double angle = Math.atan2(goal.y - t.y, goal.x - t.x);
				
				if(distance > radius) {

					double deltaX = (distance - radius) * Math.cos(angle);
					double deltaY = (distance - radius) * Math.sin(angle);
					
					/*
					//determine effect of obstacles on path
					int obstacleSpread = 70;
					for(int i = 0; i < bzrc.obstacles.size(); i++) {
						Obstacle o = bzrc.obstacles.get(i);
						Coordinate[] coordinates = new Coordinate[o.getCorners().size() + 1];
						for (int k = 0; k < o.getCorners().size(); k++) {
							coordinates[k] = new Coordinate(o.getCorners().get(k).getX(), o.getCorners().get(k).getY());
						}
						coordinates[o.getCorners().size()] = new Coordinate(o.getCorners().get(0).getX(), o.getCorners().get(0).getY());
						GeometryFactory fact = new GeometryFactory();
						LinearRing ring = new GeometryFactory().createLinearRing(coordinates);
						Polygon poly = new Polygon(ring, null, fact);
						Coordinate[] tankPositionArray = {new Coordinate(t.getX(), t.getY())};
						Point tankPoint = new Point(new CoordinateArraySequence(tankPositionArray), fact);
						double objAngle = Math.atan2(poly.getCentroid().getY() - t.getY(), poly.getCentroid().getX() - t.getX());
						Coordinate nearestPoint = DistanceOp.closestPoints(poly, tankPoint)[0];
						double obstacleRadius = Math.sqrt(Math.pow(poly.getCentroid().getX() - nearestPoint.x, 2) +
														  Math.pow(poly.getCentroid().getY() - nearestPoint.y, 2)); 
						double obDist = Math.sqrt(Math.pow(poly.getCentroid().getX() - tankPoint.getX(), 2) +
								  				  Math.pow(poly.getCentroid().getY() - tankPoint.getY(), 2)); 
						if (obstacleRadius <= obDist && obDist <= obstacleSpread + obstacleRadius) {
							double beta = 8;
							double gamma = 3;
							
							//This gives a repulsive potential field for obstacles
							double deltaXRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.cos(objAngle);
							double deltaYRepel = - (obstacleSpread + obstacleRadius - obDist) * Math.sin(objAngle);
							
							deltaX += beta * deltaXRepel;
							deltaY += beta * deltaYRepel;
							
							// This is to give a tangential potential field causing the tanks to be slightly
							// inclined to going in a clockwise direction around an object.
							double deltaXTan = deltaYRepel;
							double deltaYTan = - deltaXRepel;
							
							deltaX += gamma * deltaXTan;
							deltaY += gamma * deltaYTan;
						}
					}
					*/
					
					double angleDifference = Angle.toDegrees(Angle.normalize(t.getAngle() - Math.atan2(deltaY, deltaX)));
					
					if(Math.abs(angleDifference) > 60) {
						commands.add(new Command(Command.SPEED, t.getId(), .1f));
					} else {
						commands.add(new Command(Command.SPEED, t.getId(), (float)Math.min(distance/75, 1)));
					}
					
					commands.add(new Command(Command.ANGVEL, t.getId(), (float)-(angleDifference/180)));
				}
			}
			
			bzrc.doBulkCommands(commands);
			Date end = new Date();
			System.out.println("Loop time: " + (end.getTime() - start.getTime()));
			printMap();
		}
	}
	
	private static void printMap() throws IOException {
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map.length; j++) {
				canvas.colorPixel(i, j, map[i][j]);
			}
		}
		canvas.redraw();
	}
}
