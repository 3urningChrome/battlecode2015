package SeedingSubmission;

import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Miner extends Mobile {

	public Miner(RobotController rc) {
		super(rc);			
	}
	
	public void initialise(){
		mining_rate = GameConstants.MINER_MINE_RATE;
		mining_max = GameConstants.MINER_MINE_MAX;	
	}
}
