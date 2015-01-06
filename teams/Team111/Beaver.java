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

	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			
			count_the_troops();
			build_helipad();
			build_handwash();
			build_miner_factory();
			
			evaluate_mining_position();
			go_mining();
			
			robot_controller.yield();
		}		
	}
	public void build_helipad(){
			if(numHelipads >= helipad_max || numBeavers+1 < max_beavers)
				return;			
		
			build_structure(RobotType.HELIPAD);
	}
	public void build_handwash(){
			if(robot_controller.getTeamOre() < 600 || numHandwashes >= handwash_max)
				return;
		
			build_structure(RobotType.HANDWASHSTATION);
	}	
	public void build_miner_factory(){
			if((numMinerFactories >= max_miner_factories) || numBeavers+1 < max_beavers)
				return;			
		
			build_structure(RobotType.MINERFACTORY);
	}	
	public void build_structure(RobotType build_type){
		if(robot_controller.isCoreReady()){
			if(robot_controller.hasBuildRequirements(build_type)){
				for (final Direction direction: directions){
					if(robot_controller.canBuild(direction, build_type)){
						try{			
							robot_controller.build(direction,build_type);
							break;
						} catch(Exception e){
							print_exception(e);
						}
					}
				}				
			}
		}		
	}
}
