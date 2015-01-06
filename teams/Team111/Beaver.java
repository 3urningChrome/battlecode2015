package Team111;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Beaver extends Mobile {

	static int beaver_helipad_trigger = 5;
	static int helipad_max = 2;
	
	public Beaver(RobotController rc) {
		super(rc);
		mining_rate = GameConstants.BEAVER_MINE_RATE;
		mining_max = GameConstants.BEAVER_MINE_MAX;		
		count_the_troops();
		if(numBeavers == beaver_helipad_trigger)
			build_helipad();
		basic_turn_loop();
	}

	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			evaluate_mining_position();
			go_mining();
//			System.out.println("MyLocation: " + robot_controller.getLocation().toString() + " Supply: " + robot_controller.getSupplyLevel());
			robot_controller.yield();
		}		
	}
	public void build_helipad(){
		while(true){
			count_the_troops();
			if(numHelipads >= helipad_max)
				return;			
		
			if(robot_controller.isCoreReady()){
				if(robot_controller.hasBuildRequirements(RobotType.HELIPAD)){
					for (final Direction direction: directions){
						if(robot_controller.canBuild(direction, RobotType.HELIPAD)){
							try{			
								robot_controller.build(direction,RobotType.HELIPAD);
							} catch(Exception e){
								print_exception(e);
							}
						}
					}				
				}
			}
			robot_controller.yield();
		}
	}
}
