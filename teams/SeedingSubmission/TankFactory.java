package SeedingSubmission;

import battlecode.common.RobotController;

public class TankFactory extends Building  {

	public TankFactory(RobotController rc) {
		super(rc);
	}
	public void basic_turn_loop(){
		robot_controller.yield();
		while(true){
			//sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(get_my_attack_radius()),enemy_team);			
			//attack_deadest_enemy_in_range();	
			if(robot_controller.getCoreDelay() < 1){
				perform_a_troop_census();
				update_strategy();	
				check_for_spawns();	
			}
			//send_out_SOS_if_help_is_needed();
			dish_out_supply();
			robot_controller.yield();
		}		
	}	
}
