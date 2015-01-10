package team1;

import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Miner extends Mobile {

	public Miner(RobotController rc) {
		super(rc);
		mining_rate = GameConstants.MINER_MINE_RATE;
		mining_max = GameConstants.MINER_MINE_MAX;				
		basic_turn_loop();
	}

	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			evaluate_mining_position();
			go_mining();
			robot_controller.yield();
		}		
	}	
}
