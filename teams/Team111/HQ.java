package Team111;

import battlecode.common.RobotController;

public class HQ extends Building  {

	public HQ(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			robot_controller.yield();
		}		
	}

	//build(Direction dir, RobotType type)
	//spawn(Direction dir, RobotType type)
	//canBuild(Direction dir, RobotType type)
	//canSpawn(Direction dir, RobotType type)
	//hasSpawnRequirements(RobotType type)
	//isLocationOccupied(MapLocation loc)
}
