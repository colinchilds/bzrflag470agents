import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import com.oroinc.net.telnet.TelnetClient;

public class BZRController {

	private TelnetClient telnet = new TelnetClient();
	private PrintWriter out;
	private BufferedReader in;
	
	//Game values
	HashMap<String, Team> teams = new HashMap<String, Team>();
	ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
	ArrayList<Flag> flags = new ArrayList<Flag>();
	ArrayList<Shot> shots = new ArrayList<Shot>();
	HashMap<String, MyTank> myTanks = new HashMap<String, MyTank>();
	HashMap<String, String> constants = new HashMap<String, String>();
	ArrayList<OtherTank> otherTanks = new ArrayList<OtherTank>();
	
	//Connects the agent to the host and given port
	public void connect(String host, int port) throws SocketException, IOException {
		telnet.connect(host, port);
		out = new PrintWriter(telnet.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
	}
	
	//Cleans up the connection and buffers
	public void disconnect() throws IOException {
		telnet.disconnect();
		out = null;
		in = null;
	}
	
	//Reads a line of input from the telnet client
	//This parses everything up until the first newline character
	public String readLine() {
		String result = "";
		try {
			//Wait for a response.
			int i = 0;
			while(!in.ready()) {
				//terrible hack for continuing in case of read error thats was freezing agent
				if(i++ > 10000000) {
					break;
				}
			}
			return in.readLine();
		} catch(IOException e) {
			System.err.println("Problem reading from buffer:");
			System.err.println(e.getStackTrace());
		}
		return result;
	}
	
	//Reads and acknowledgment statement from the server
	private void readAck() throws Exception {
		expect("ack");
	}
	
	//Looks for the success response
	private boolean readBool() {
		String response = readLine();
		if(response.startsWith("ok")) {
			return true;
		} else {
			return false;
		}
	}
	
	//Reads responses formatted as lists
	//Starts with a 'begin' and goes until it finds 'end'
	private ArrayList<String> readList() {
		ArrayList<String> result = new ArrayList<String>();
		String response = readLine();
		if("fail".equals(response)) {
			System.err.println("Command returned error");
			return result;
		}
		if(!"begin".equals(response)) {
			System.err.println("Trying to read list that did not return with a 'begin'");
			return result;
		}
		while(!response.equals("end")) {
			response = readLine();
			if(!response.equals("end")) {
				result.add(response);
			}
		}
		return result;
	}
	
	//Reads a line from the server and checks against the expected result
	//If the result is not what was expected, die with an error
	private void expect(String expected) throws Exception {
		String response = readLine();
		if(!response.startsWith(expected)) {
			throw new Exception("Expected: " + expected + "\t Received: " + response);
		}
	}
	
	//Sends a command to the server
	public void write(String s) {
		out.println(s);
	}
	
	
	//********************************************************************
	//BOT COMMANDS
	//********************************************************************
	public boolean shoot(String id) throws Exception {
		write("shoot " + id);
		readAck();
		return readBool();
	}
	
	public boolean speed(String id, float amount) throws Exception {
		write("speed " + id + " " + amount);
		readAck();
		return readBool();
	}
	
	public boolean speed(String id) throws Exception {
		return speed(id, 1);
	}
	
	public boolean angvel(String id, float amount) throws Exception {
		write("angvel " + id + " " + amount);
		readAck();
		return readBool();
	}
	
	//Bulk commands - helps with speed since waiting on the server response
	//before issuing a command to a tank is time consuming
	public boolean doBulkCommands(ArrayList<Command> commands) throws Exception {
		boolean ret = true;
		
		for(int i = 0; i < commands.size(); i++) {
			Command c = commands.get(i);
			switch (c.getType()) {
				case Command.SPEED:
					write("speed " + c.getTank() + " " + c.getArg1());
					break;
				case Command.ANGVEL:
					write("angvel " + c.getTank() + " " + c.getArg1());
					break;
				case Command.SHOOT:
					write("shoot " + c.getTank());
					break;
			}
		}
		
		//now read the responses in bulk
		for(int i = 0; i < commands.size(); i++) {
			readAck();
			ret &= readBool();
		}
		
		return ret;
	}
	
	
	//********************************************************************
	//Queries
	//********************************************************************
	public void updateTeams() throws Exception {
		write("teams");
		readAck();
		ArrayList<String> list = readList();
		teams.clear();
		for(String team : list) {
			String[] arr = team.split(" ");
			teams.put(arr[1], new Team(arr[1], Integer.parseInt(arr[2])));
		}
	}
	
	public void updateObstacles() throws Exception {
		write("obstacles");
		readAck();
		ArrayList<String> list = readList();
		obstacles.clear();
		for(String obs : list) {
			String[] arr = obs.split(" ");
			Obstacle o = new Obstacle();
			for(int i = 1; i < arr.length; i += 2) {
				Point2D.Float p = new Point2D.Float(Float.parseFloat(arr[i]),
						Float.parseFloat(arr[i + 1]));
				o.addCorner(p);
			}
			obstacles.add(o);
		}
	}
	
	public void updateBases() throws Exception {
		if(teams.size() == 0) {
			updateTeams();
		}
		
		write("bases");
		readAck();
		ArrayList<String> list = readList();
		for(String obs : list) {
			String[] arr = obs.split(" ");
			Team team = teams.get(arr[1]);
			if(team == null) {
				System.err.println("Team not found in team list: " + arr[0]);
				continue;
			}
			
			for(int i = 2; i < arr.length; i += 2) {
				Point2D.Float p = new Point2D.Float(Float.parseFloat(arr[i]),
						Float.parseFloat(arr[i + 1]));
				team.addCorner(p);
			}
		}
	}
	
	public void updateFlags() throws Exception {
		write("flags");
		readAck();
		ArrayList<String> list = readList();
		flags.clear();
		for(String s : list) {
			String[] arr = s.split(" ");
			String controllingTeam = arr[2];
			if("none".equalsIgnoreCase(controllingTeam)) {
				controllingTeam = null;
			}
			Flag f = new Flag(arr[1], controllingTeam,
					new Point2D.Float(Float.parseFloat(arr[3]), Float.parseFloat(arr[4])));
			flags.add(f);
		}
	}
	
	public void updateShots() throws Exception {
		write("shots");
		readAck();
		ArrayList<String> list = readList();
		shots.clear();
		for(String s : list) {
			String[] arr = s.split(" ");
			Shot shot = new Shot(Float.parseFloat(arr[1]), Float.parseFloat(arr[2]),
					Float.parseFloat(arr[3]), Float.parseFloat(arr[4]));
			shots.add(shot);
		}
	}
	
	public void updateMyTanks() throws Exception {
		write("mytanks");
		readAck();
		ArrayList<String> list = readList();
		myTanks.clear();
		for(String s : list) {
			String[] arr = s.split("\\s+");
			MyTank t = new MyTank(arr[1], arr[2]);
			t.setStatus(arr[3]);
			t.setShotsAvailable(Integer.parseInt(arr[4]));
			t.setTimeToReload(Float.parseFloat(arr[5]));
			if(!"-".equals(arr[6])) {
				t.setFlag(arr[6]);
			}
			t.setX(Float.parseFloat(arr[7]));
			t.setY(Float.parseFloat(arr[8]));
			t.setAngle(Float.parseFloat(arr[9]));
			t.setVx(Float.parseFloat(arr[10]));
			t.setVy(Float.parseFloat(arr[11]));
			t.setAngvel(Float.parseFloat(arr[12]));
			myTanks.put(t.getId(), t);
		}
	}
	
	public void updateOtherTanks() throws Exception {
		write("othertanks");
		readAck();
		ArrayList<String> list = readList();
		otherTanks.clear();
		for(String s : list) {
			String[] arr = s.split("\\s+");
			OtherTank t = new OtherTank();
			t.setCallsign(arr[1]);
			t.setColor(arr[2]);
			t.setStatus(arr[3]);
			if(!"-".equals(arr[4])) {
				t.setFlag(arr[4]);
			}
			t.setX(Float.parseFloat(arr[5]));
			t.setY(Float.parseFloat(arr[6]));
			t.setAngle(Float.parseFloat(arr[7]));
			otherTanks.add(t);
		}
	}
	
	public void updateConstants() throws Exception {
		write("constants");
		readAck();
		ArrayList<String> list = readList();
		constants.clear();
		for(String s : list) {
			String[] arr = s.split("\\s+");
			constants.put(arr[1], arr[2]);
		}
	}
	
	public void updateAll() throws Exception {
		updateTeams();
		updateObstacles();
		updateBases();
		updateFlags();
		updateShots();
		updateMyTanks();
		updateOtherTanks();
	}
	
	public Occgrid getOccgrid(String id) throws Exception {
		write("occgrid " + id);
		readAck();
		ArrayList<String> list = readList();
		
		//get coordinates
		String at = list.remove(0);
		at = at.replace("at ", "");
		int x = Integer.parseInt(at.split(",")[0]);
		int y = Integer.parseInt(at.split(",")[1]);
		
		//get size of grid
		String size = list.remove(0);
		size = size.replace("size ", "");
		int height = Integer.parseInt(size.split("x")[0]);
		int width = Integer.parseInt(size.split("x")[1]);
		
		int[][] grid = new int[height][width];
		for(int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			for(int j = 0; j < s.length(); j++) {
				grid[i][j] = Integer.parseInt(s.charAt(j)+"");
			}
		}
		
		return new Occgrid(x, y, width, height, grid);
	}
	
}
