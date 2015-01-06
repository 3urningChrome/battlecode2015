package Team111;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class MinerFactory extends Building  {

	public MinerFactory(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			basic_building_tasks();
			spawn_miner();
			robot_controller.yield();
		}		
	}
	
	public void spawn_miner(){
		count_the_troops();
		if(numMiners >= max_Miners)
			return;
		spawn_robot(RobotType.MINER);
	}

}
