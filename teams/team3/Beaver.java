package team3;

import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Beaver extends Mobile {

	static int miner_factory_helipad_trigger = 1;
	
	public Beaver(RobotController rc) {
		super(rc);
	}
	public void initialise(){
		mining_rate = GameConstants.BEAVER_MINE_RATE;
		mining_max = GameConstants.BEAVER_MINE_MAX;	
		mining_search_range = 2;
		mining_move_threshold = 0.2;
	}
}
