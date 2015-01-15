package team111;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Building extends Arobot {
	
	
	public Building(RobotController rc){
		super(rc);			
	}
	
	//Building based run loop
	public void basic_turn_loop(){
		robot_controller.yield();
		while(true){
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(get_my_attack_radius()),enemy_team);			
			attack_deadest_enemy_in_range();		
			perform_a_troop_census();
			update_strategy();	
			check_for_spawns();	
			send_out_SOS_if_help_is_needed();
			dish_out_supply();
			robot_controller.yield();
		}		
	}	
		
	public void check_for_spawns() {
		if(robot_controller.isCoreReady())
			if(my_type.canSpawn() && possible_spawn_and_building_ordinals != null)
				for(int spawn_ordinal: possible_spawn_and_building_ordinals)
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
			if(robot_controller.hasSpawnRequirements(required_type))
				for (final Direction direction: directions)
					if(robot_controller.canSpawn(direction, required_type))
						try{
							if(!robot_controller.isCoreReady())
								return true; //quit building as we now have core delay. must have gone over byte boundary
							robot_controller.spawn(direction,required_type);
							send_broadcast(cumulative_ore_spent, read_broadcast(cumulative_ore_spent) + required_type.oreCost);
							send_broadcast(troop_count_channel+required_type.ordinal(), robot_census[required_type.ordinal()]+ 1);
							return true;
						} catch(Exception e){
							Utilities.print_exception(e);
						}
		return false;
	}		
}
