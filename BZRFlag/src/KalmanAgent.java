import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

import com.vividsolutions.jts.algorithm.Angle;


public class KalmanAgent extends Agent {

	// Constants
    private static SimpleMatrix F;
    private static SimpleMatrix FT;
    private static SimpleMatrix H;
    private static SimpleMatrix HT;
    private static SimpleMatrix SZ;
    private static SimpleMatrix SX;

    // system state estimate
    private static SimpleMatrix XT;
    private static SimpleMatrix ST;
    
    private static float[][] map;
    private static MapTopologyCanvas canvas;
    private static int worldSize = 400;
    private static int shotSpeed = 100;
    
    private static int px = 0;
    private static int py = 0;
    private static int rx = 0;
    private static int ry = 0;
    
	public static void main(String[] args) throws Exception {
		connect(args);
		initMatrices();
		
		createMap();
		
		begin();
	}

	private static void initMatrices() {
		double dt = 0.05;
		double c = 0.1;
		double f[][] = {{1, dt, Math.pow(dt, 2)/2, 0, 0, 0},
						{0, 1, dt, 0, 0, 0},
						{0, -c, 1, 0, 0, 0},
						{0, 0, 0, 1, dt, Math.pow(dt, 2)/2},
						{0, 0, 0, 0, 1, dt},
						{0, 0, 0, 0, -c, 1}};
		F = new SimpleMatrix(new DenseMatrix64F(f));
		FT = F.transpose();
		
		double h[][] = {{1, 0, 0, 0, 0, 0},
						{0, 0, 0, 1, 0, 0}};
		H = new SimpleMatrix(new DenseMatrix64F(h));
		HT = H.transpose();
		
		double sz[][] = {{25, 0},
						 {0, 25}};
		SZ = new SimpleMatrix(new DenseMatrix64F(sz));
		
		double sx[][] = {{0.1, 0, 0, 0, 0, 0},
						 {0, 0.1, 0, 0, 0, 0},
						 {0, 0, 100, 0, 0, 0},
						 {0, 0, 0, 0.1, 0, 0},
						 {0, 0, 0, 0, 0.1, 0},
						 {0, 0, 0, 0, 0, 100}};
		SX = new SimpleMatrix(new DenseMatrix64F(sx));
		
		double xt[][] = {{0}, {0}, {0}, {0}, {0}, {0}};
		XT = new SimpleMatrix(new DenseMatrix64F(xt));
		
		double st[][] = {{100, 0, 0, 0, 0, 0},
				 		 {0, 0.1, 0, 0, 0, 0},
				 		 {0, 0, 0.1, 0, 0, 0},
				 		 {0, 0, 0, 100, 0, 0},
				 		 {0, 0, 0, 0, 0.1, 0},
				 		 {0, 0, 0, 0, 0, 0.1}};
		ST = new SimpleMatrix(new DenseMatrix64F(st));
	}
	
	private static void update() {
		SimpleMatrix predictedState = (F.mult(ST).mult(FT)).plus(SX);
		SimpleMatrix K = (predictedState.mult(HT)).mult((((H.mult(predictedState)).mult(HT)).plus(SZ)).invert());
		
		OtherTank t = bzrc.otherTanks.get(0);
		if(t.isAlive()) {
			double zt[][] = {{t.getX()}, {t.getY()}};
			SimpleMatrix ZT = new SimpleMatrix(new DenseMatrix64F(zt));
			
			map[px][py] = 0;
			
			px = Math.max(Math.min((int)XT.getMatrix().data[0] + 200, 399), 0);
			py = Math.max(Math.min((int)XT.getMatrix().data[3] + 200, 399), 0);
			
			XT = F.mult(XT).plus(K.mult(ZT.minus((H.mult(F).mult(XT)))));
			map[px][py] = 1;
			
			ST = predictedState.minus(K.mult(H).mult(predictedState));
			rx = (int)ST.getMatrix().data[0];
			ry = (int)ST.getMatrix().data[21];
		}
	}
	
	private static void turnAndShoot() throws Exception {
		OtherTank ot = bzrc.otherTanks.get(0);
		MyTank t = bzrc.myTanks.get("0");
		
		if(ot.isAlive()) {
			
			SimpleMatrix future = XT;
			int futureX = (int)future.getMatrix().data[0];
			int futureY = (int)future.getMatrix().data[3];
			
			Point2D otherTank = new Point2D.Double(futureX, futureY);
			double angle = Math.atan2(otherTank.getY() - t.y, otherTank.getX() - t.x);
			double deltaX = Math.cos(angle);
			double deltaY = Math.sin(angle);
			double angleDifference = Angle.toDegrees(Angle.normalize(t.getAngle() - Math.atan2(deltaY, deltaX)));
			int timeToTurn = Math.abs((int)(8000 * (angleDifference/30)));
			
			boolean shouldStop = false;
			int iterations = 1;
			while(shouldStop) {
				future = F.mult(future);
				futureX = (int)future.getMatrix().data[0];
				futureY = (int)future.getMatrix().data[3];
				
				otherTank = new Point2D.Double(futureX, futureY);
				angle = Math.atan2(otherTank.getY() - t.y, otherTank.getX() - t.x);
				deltaX = Math.cos(angle);
				deltaY = Math.sin(angle);
				angleDifference = Angle.toDegrees(Angle.normalize(t.getAngle() - Math.atan2(deltaY, deltaX)));
				timeToTurn = Math.abs((int)(8000 * (angleDifference/30)));
				
				double distance = t.getPosition().distance(futureX, futureY);
				double time = (distance / shotSpeed) * 1000;
				if(time < (iterations++ * .00005)) {
					break;
				}
			}

			canvas.drawCircle(futureX+200, 200-futureY, rx, ry, .5);
			bzrc.angvel("0", (float)-(angleDifference/30));
			if (Math.abs(angleDifference) < .5) {
				bzrc.shoot("0");
			}
			
		} else {
			bzrc.angvel("0", 0);
		}
	}
	
	private static void begin() throws Exception {
		bzrc.updateConstants();
		shotSpeed = Integer.parseInt(bzrc.constants.get("shotspeed"));
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					bzrc.updateMyTanks();
					bzrc.updateOtherTanks();
					update();

					canvas.clearCircles();
					turnAndShoot();
					printMap();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, 0, 50);
//		while(true) {
//			Date start = new Date();
//			
//			
//			
//			Date end = new Date();
//			System.out.println("Loop time: " + (end.getTime() - start.getTime()));
//		}
	}
	
	private static void updateMap() {
		/*
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
		*/
	}
	
	private static void createMap() {
		JFrame frame = new JFrame("Create a JPanel");
		canvas = new MapTopologyCanvas(worldSize, worldSize);
		frame.add(canvas);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(worldSize + 100, worldSize + 100);
		frame.setResizable(false);
		frame.setVisible(true);
		
		map = new float[worldSize][worldSize];
		for(int i = 0; i < worldSize; i++) {
			for(int j = 0; j < worldSize; j++) {
				map[i][j] = 0;
			}
		}
	}
	
	private static void printMap() throws IOException {
//		for(int i = 0; i < map.length; i++) {
//			for(int j = 0; j < map.length; j++) {
//				canvas.colorPixel(i, worldSize - j - 1, map[i][j]);
//			}
//		}
		
		canvas.drawCircle(px, 400-py, rx, ry, 1);
		
		canvas.redraw();
	}
	
}
