package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Building extends Arobot {
	
	
	public Building(RobotController rc){
		super(rc);
		my_max_supply_level = max_building_supply_level;
		my_min_supply_level = min_building_supply_level;
		my_optimal_supply_level = optimal_building_supply_level;			
	}
	
	public void basic_turn_loop(){
		robot_controller.yield();
		while(true){
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);			
			//attack_random_enemy_in_range();
			attack_deadest_enemy_in_range();
			if(Clock.getRoundNum()%5 == 0){			
				count_the_troops();
				update_strategy();	
				check_for_spawns();	
			}	
		//	if(all_out_attack)
				dish_out_supply();
			robot_controller.yield();
		}		
	}	
		
	public void check_for_spawns() {
		if(my_type.canSpawn() && spawn_build_ordinals != null)
			for(int spawn_ordinal: spawn_build_ordinals)
				if(need_more_spawns(spawn_ordinal))
					if(spawn_robot(robot_types[spawn_ordinal]))
						break;
		}

	private boolean need_more_spawns(int spawn_ordinal) {
		if(robot_census[spawn_ordinal] < robot_max[spawn_ordinal])
			return true;
		return false;
	}
	
	public boolean spawn_robot(RobotType required_type){
		if(robot_controller.isCoreReady())			
			if(robot_controller.hasSpawnRequirements(required_type))
				for (final Direction direction: directions)
					if(robot_controller.canSpawn(direction, required_type))
						try{
							robot_controller.spawn(direction,required_type);
							return true;
						} catch(Exception e){
							print_exception(e);
						}
		return false;
	}		

}
