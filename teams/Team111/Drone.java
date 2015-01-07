package Team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Mobile {

	static MapLocation destination; 
	public Drone(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			known_enemies = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);
			attack_random_enemy_in_range();
			if(Clock.getRoundNum()%5 == 0){
				count_the_troops();
				update_strategy();	
			}
			set_my_danger_level();
			
			
			if(robot_controller.isCoreReady()){
				set_danger_levels();
				
				if(my_danger_level !=0){
					evasive_move();
				}else{
					move_towards_direction(robot_controller.getLocation().directionTo(robot_controller.senseEnemyHQLocation()));
				}
			}
						
			robot_controller.yield();	
		}
	}
}
