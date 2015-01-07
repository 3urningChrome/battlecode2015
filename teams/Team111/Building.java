package Team111;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Building extends Arobot {
	
	
	public Building(RobotController rc){
		super(rc);
	}
	
	public void basic_turn_loop(){
		while(true){
			update_strategy();
			attack_random_enemy_in_range();
			count_the_troops();
			dish_out_supply();
			check_for_spawns();		
			robot_controller.yield();
		}		
	}	
		
	public void check_for_spawns() {
		if(my_type.canSpawn() && spawn_build_ordinals != null)
//			if(spawn_build_ordinals == null){
//				System.out.println("Robot canSpawn() but nothing to spawn.  Type: " + my_type.toString());
//				return;
//			}
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

	public void dish_out_supply(){	
		if(robot_controller.getSupplyLevel() > minimum_supply){		
			RobotInfo[] sensed_friendly_robots = robot_controller.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, my_team);
			for (final RobotInfo sensed_friendly_robot: sensed_friendly_robots){			
				if(sensed_friendly_robot.supplyLevel < minimum_supply){					
					double amount = Math.min((robot_controller.getSupplyLevel() - minimum_supply), (minimum_supply - sensed_friendly_robot.supplyLevel));
					send_supply((int)amount, sensed_friendly_robot.location);
				}
			}
		}
	}
}
