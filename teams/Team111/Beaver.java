package Team111;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Beaver extends Mobile {

	static int miner_factory_helipad_trigger = 1;
	
	public Beaver(RobotController rc) {
		super(rc);
		mining_rate = GameConstants.BEAVER_MINE_RATE;
		mining_max = GameConstants.BEAVER_MINE_MAX;		
		
		basic_turn_loop();
	}
}
