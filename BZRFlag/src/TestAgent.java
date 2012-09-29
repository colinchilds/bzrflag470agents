
public class TestAgent {

	private static BZRController bzrc;
	
	public static void main(String[] args) throws Exception {
		bzrc = new BZRController();
		bzrc.connect("localhost", 50185);
		while(!bzrc.readLine().startsWith("bzrobots")); //wait for response
		bzrc.write("agent 1");
		
		bzrc.shoot(0);
		bzrc.updateTeams();
		bzrc.updateObstacles();
		bzrc.updateBases();
		
		bzrc.disconnect();
	}

}
