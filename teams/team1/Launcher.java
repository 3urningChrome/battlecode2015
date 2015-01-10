package team1;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Mobile {

	static int last_fired = Clock.getRoundNum();
	public Launcher(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	public void basic_turn_loop(){
		while(true){
			while(!robot_controller.isCoreReady() && !robot_controller.isWeaponReady())
				robot_controller.yield();
			
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);
									
			if(robot_controller.isCoreReady()){
				set_my_danger_level();
				
				//if(Clock.getRoundNum() > 1600){
				if(all_out_attack){
					count_down --;
				}else{
					count_down = 50;
				}
				if(count_down < 1){
					danger_levels = new int[]{0,0,0,0,0,0,0,0};	
					my_danger_level = 0;
				}
				if(my_danger_level == 0){
					//if(Clock.getRoundNum()%5 == 0){
						count_the_troops();
						update_strategy();	
						check_for_builds();							
					//}	
				}
				
				if(my_danger_level !=0){
					evasive_move();
					attack_deadest_enemy_in_range();
				}else{
					if(!attack_deadest_enemy_in_range() && robot_controller.isWeaponReady()){
						int destination_x;
						int destination_y;
						
//						if(all_out_attack){
//							destination_x= read_broadcast(swarm_location_channel_x);
//							destination_y = read_broadcast(swarm_location_channel_y);
//						} else{
							destination_x= read_broadcast(defence_location_channel_x);
							destination_y = read_broadcast(defence_location_channel_y);
//						}

						MapLocation destination = new MapLocation(destination_x,destination_y);
						move_towards_direction(robot_controller.getLocation().directionTo(destination));
					}
				}
			}
			//attack_random_enemy_in_range();
			//if(all_out_attack)
				dish_out_supply();
			robot_controller.yield();
		}		
	}
	
	public boolean attack_deadest_enemy_in_range(){
		if(Clock.getRoundNum() - last_fired < 2)
			return false;
		int attack_radius = 36;
		RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
		if(close_enemies.length > 0){
			int num_of_loops = close_enemies.length;
			double enemy_health = 9999;
			int attack_pos = 0;
			for(int i=0; i< num_of_loops;i++){
				if(close_enemies[i].health <= enemy_health || close_enemies[i].type == RobotType.MISSILE){
					enemy_health = close_enemies[i].health;
					attack_pos = i;
				}
			}
			try{
				if(my_type.equals(RobotType.LAUNCHER)){
					Direction initial_direction = robot_controller.getLocation().directionTo(close_enemies[attack_pos].location);
							
					//for(int i=0; i<8;i++){
					//	int test_direction = ((initial_direction.ordinal() + directional_looks[i] + 8)%8);
					//	if(robot_controller.canLaunch(directions[test_direction])){
					//		robot_controller.launchMissile(directions[test_direction]);
					//		return true;
					//	}
					//}	
					if(robot_controller.canLaunch(initial_direction)){
							robot_controller.launchMissile(initial_direction);
							last_fired = Clock.getRoundNum();
							return true;
					}
				}
				return true;
			} catch (Exception e){
				 print_exception(e);
			}
		}
		return false;
	}	
}
