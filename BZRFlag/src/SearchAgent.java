import java.awt.Point;
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
	private static int worldSize;
	private static int halfWorldSize;
	private static float truePositive;
	private static float trueNegative;
	
	public static void main(String[] args) throws Exception {
		connect(args);
		
		begin();
	}

	private static void begin() throws Exception {
		bzrc.updateConstants();
		worldSize = Integer.parseInt(bzrc.constants.get("worldsize"));
		halfWorldSize = (worldSize/2);
		truePositive = Float.parseFloat(bzrc.constants.get("truepositive"));
		trueNegative = Float.parseFloat(bzrc.constants.get("truenegative"));
		
		createMap();
		
		map = new float[worldSize][worldSize];
		for(int i = 0; i < worldSize; i++) {
			for(int j = 0; j < worldSize; j++) {
				map[i][j] = .5f;
			}
		}
		
		int iterations = 0;
		while(true) {
			Date start = new Date();
			//bzrc.updateAll();
			bzrc.updateMyTanks();
			
			ArrayList<Command> commands = new ArrayList<Command>();
			
			for(int j = 0; j < bzrc.myTanks.size(); j++) {
			//for(int j = 0; j < 1; j++) {
				MyTank t = bzrc.myTanks.get(j+"");
				Occgrid grid = bzrc.getOccgrid(t.getId());
				updateMap(grid);
				
				Point2D.Float goal = null;
				
				/*
				boolean goalFound = false;
				for(int x = worldSize - 10; x > 10 && !goalFound; x--) {
					for(int y = 10; y < worldSize - 10 && !goalFound; y++) {
						if(map[x][y] == .5) {
							goal = new Point2D.Float(x - halfWorldSize, y - halfWorldSize);
							goalFound = true;
						}
					}
				}
				*/
				
				int tankX = (int)t.getX() + halfWorldSize;
				int tankY = (int)t.getY() + halfWorldSize;
				boolean goalFound = false;
				int direction = 1;
				for(int i = 1; i < worldSize && !goalFound; i++) {
					for(int x = 0; x < i && !goalFound; x++) {
						tankX += direction;
						if(tankX < worldSize -1  && tankX >= 0
								&& tankY < worldSize - 1 && tankY >= 0
								&& map[tankX][tankY] == .5) {
							goal = new Point2D.Float(tankX - halfWorldSize, tankY - halfWorldSize);
							goalFound = true;
						}
					}
					for(int y = 0; y < i && !goalFound; y++) {
						tankY += direction;
						if(tankX < worldSize -1  && tankX >= 0
								&& tankY < worldSize - 1 && tankY >= 0
								&& map[tankX][tankY] == .5) {
							goal = new Point2D.Float(tankX - halfWorldSize, tankY - halfWorldSize);
							goalFound = true;
						}
					}
					direction = -1 * direction;
				}
				
				if(goal == null) {
					break;
				}
				
				double distance = goal.distance(t.getX(), t.getY());
				double radius = Double.parseDouble(bzrc.constants.get("flagradius"));
				double angle = Math.atan2(goal.y - t.y, goal.x - t.x);
				
				if(distance > radius) {

					double deltaX = (distance - radius) * Math.cos(angle);
					double deltaY = (distance - radius) * Math.sin(angle);
					
					//determine effect of obstacles on path
					int obstacleSpread = 40;
					for(int x = 0; x < worldSize; x++) {
						for(int y = 0; y < worldSize; y++) {
							if(map[x][y] > .95) {
								double obstacleRadius = 1;
								int pointX = x - halfWorldSize;
								int pointY = y - halfWorldSize;
								
								double obDist = Math.sqrt(Math.pow(pointX - t.getX(), 2) +
										  				  Math.pow(pointY - t.getY(), 2));
								double objAngle = Math.atan2(pointY - t.getY(), pointX - t.getX());
								if (obstacleRadius <= obDist && obDist <= obstacleSpread + obstacleRadius) {
									double beta = .01;
									double gamma = .015;
									
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
						}
					}
					
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
			
			if(iterations % 2 == 0) {
				printMap();
			}
		}
	}
	
	private static void updateMap(Occgrid grid) {
		float falseAlarm = 1 - trueNegative;
		
		for(int x = 0; x < grid.getHeight(); x++) {
			for(int y = 0; y < grid.getWidth(); y++) {
				int mapX = halfWorldSize + grid.getX() + x;
				int mapY = halfWorldSize + grid.getY() + y;
				int val = grid.getGrid()[x][y];
				
				if(val == 1) {
					float occ = truePositive * map[mapX][mapY];
					float unocc = falseAlarm * (1 - map[mapX][mapY]);
					
					map[mapX][mapY] = occ / (occ + unocc);
				} else {
					float occ = (1 - truePositive) * map[mapX][mapY];
					float unocc = trueNegative * (1 - map[mapX][mapY]);
					
					map[mapX][mapY] = occ / (occ + unocc);
				}
			}
		}
	}
	
	private static void createMap() {
		JFrame frame = new JFrame("Create a JPanel");
		canvas = new MapCanvas(worldSize, worldSize);
		frame.add(canvas);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(worldSize, worldSize);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	private static void printMap() throws IOException {
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map.length; j++) {
				canvas.colorPixel(i, worldSize - j - 1, map[i][j]);
			}
		}
		canvas.redraw();
	}
}
