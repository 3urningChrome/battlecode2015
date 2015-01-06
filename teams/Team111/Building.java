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
	
	public void basic_building_tasks(){
		attack_random_enemy_in_range();
		dish_out_supply();
	}
	
	public void dish_out_supply(){	
		if(robot_controller.getSupplyLevel() > minimum_supply){		
			RobotInfo[] sensed_friendly_robots = get_all_friends_in_range(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED);
			for (final RobotInfo sensed_friendly_robot: sensed_friendly_robots){			
				if(sensed_friendly_robot.supplyLevel < minimum_supply){					
					double amount = Math.min((robot_controller.getSupplyLevel() - minimum_supply), (minimum_supply - sensed_friendly_robot.supplyLevel));
					send_supply((int)amount, sensed_friendly_robot.location);
				}
			}
		}
	}
	
	public void spawn_robot(RobotType required_type){
System.out.println("Building Copters");	
		if(robot_controller.isCoreReady()){
			System.out.println("Building Copters - Core ready");				
			if(robot_controller.hasSpawnRequirements(required_type)){	
				System.out.println("Building Copters - has spawn requirements");	
				for (final Direction direction: directions){
					if(robot_controller.canSpawn(direction, required_type)){
						System.out.println("Building Copters - Can Spawn: " + direction.toString());	
						try{
							robot_controller.spawn(direction,required_type);
							System.out.println("Building Copters - spawned");
							return;
						} catch(Exception e){
							print_exception(e);
						}
					}
				}
			}
		}
	}	

	
}
