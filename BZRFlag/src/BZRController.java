import java.awt.Point;
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
			while(!in.ready());
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
			System.err.println("FAILURE: " + response);
			return false;
		}
	}
	
	//Reads responses formatted as lists
	//Starts with a 'begin' and goes until it finds 'end'
	private ArrayList<String> readList() {
		ArrayList<String> result = new ArrayList<String>();
		String response = readLine();
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
	public boolean shoot(int bot) throws Exception {
		write("shoot " + bot);
		readAck();
		return readBool();
	}
	
	public boolean speed(int bot, float amount) throws Exception {
		write("speed " + bot + " " + amount);
		readAck();
		return readBool();
	}
	
	public boolean speed(int bot) throws Exception {
		return speed(bot, 1);
	}
	
	public boolean angvel(int bot, float amount) throws Exception {
		write("angvel " + bot + " " + amount);
		readAck();
		return readBool();
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
	
}
