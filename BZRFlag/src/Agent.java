import java.io.IOException;
import java.net.SocketException;


public abstract class Agent {

	protected static BZRController bzrc = new BZRController();
	
	protected static void connect(String args[]) throws SocketException, IOException {
		String host = "localhost";
		int port = 50185;
		if(args.length == 3) {
			host = args[1];
			port = Integer.parseInt(args[2]);
		}
		
		//Connect to server
		bzrc.connect(host, port);
		
		//Wait for a response
		if(!bzrc.readLine().startsWith("bzrobots")) {
			System.err.println("Could not connect to server. Exiting");
			System.exit(0);
		}
		
		//handshake
		bzrc.write("agent 1");
	}

}
