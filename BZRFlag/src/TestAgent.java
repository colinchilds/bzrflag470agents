
public class TestAgent extends Agent {

	public static void main(String[] args) throws Exception {
		connect(args);
		begin();
	}

	private static void begin() throws Exception {
		bzrc.shoot(0);
		bzrc.updateTeams();
		bzrc.updateObstacles();
		bzrc.updateBases();
		bzrc.updateFlags();
		bzrc.updateShots();
		bzrc.updateMyTanks();
		bzrc.updateOtherTanks();
		
		bzrc.disconnect();
	}

}
