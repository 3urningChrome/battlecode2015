package team111;

import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Tower extends Building {

	double previous_health;
	public Tower(RobotController rc) {
		super(rc);
		previous_health = robot_controller.getHealth();
		basic_turn_loop();
	}
	public void basic_turn_loop(){
		robot_controller.yield();
		while(true){
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);			
			attack_deadest_enemy_in_range();
			if(Clock.getRoundNum()%5 == 0){			
				count_the_troops();
				update_strategy();	
				check_for_spawns();	
			}	
				dish_out_supply();
				
			robot_controller.yield();
		}		
	}	

}
