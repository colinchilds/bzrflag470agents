import java.io.IOException;

import javax.swing.JFrame;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;


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
    private static MapCanvas canvas;
    private static int worldSize = 800;
    
	public static void main(String[] args) throws Exception {
		connect(args);
		initMatrices();
		
		createMap();
		
		begin();
	}

	private static void initMatrices() {
		double dt = 0.5;
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
		double zt[][] = {{t.getX()}, {0}, {0}, {t.getY()}, {0}, {0}};
		SimpleMatrix ZT = new SimpleMatrix(new DenseMatrix64F(zt));
		
		XT = F.mult(XT).plus(K.mult(ZT.minus((H.mult(F).mult(XT)))));
		
		ST = predictedState.minus(K.mult(H).mult(predictedState));
	}
	
	private static void begin() throws Exception {
		int iteration = 0;
		while(true) {
			bzrc.updateMyTanks();
			bzrc.updateOtherTanks();
			update();
			
			if(iteration++ % 2 == 0) {
				printMap();
			}
		}
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
