package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
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
		navigator = new RouteNode(robot_controller.getLocation(),HQ_location,robot_controller,3);
		while(true){
			while(!robot_controller.isCoreReady() && !robot_controller.isWeaponReady()){
				if(!my_type.equals(RobotType.DRONE)){
					request_help();
				}
				robot_controller.yield();
			}
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(BEYOND_MAX_ATTACK_RANGE),enemy_team);
			role_warfare();
			if(robot_controller.isCoreReady()){				
				set_danger_levelz();		
				if(danger_levels[my_danger_level] == 0){		
					count_the_troops();				
					update_strategy();						
					check_for_builds();								
				}
				if(danger_levels[my_danger_level] !=0){
					evasive_move();
					robot_controller.setIndicatorString(2, "Evading");
				} else if(robot_controller.canMine()){
				//	robot_controller.setIndicatorString(2, "tring to mine");
				//	evaluate_mining_position();
				//	go_mining();
				} else{
					robot_controller.setIndicatorString(2, "location");
					if(attack_deadest_enemy_in_range()){
						//If I attacked someone, stay where I am (not currently in danger here)
					} else{
						if(sticky && sensed_enemy_robots.length > 0){
							//if there are enemies around, and I'm sticky, head for closest one.
							MapLocation[] enemy_positions = new MapLocation[sensed_enemy_robots.length];
							int num_of_loops = sensed_enemy_robots.length;
							for(int i=0; i<num_of_loops;i++){
								enemy_positions[i] = sensed_enemy_robots[i].location;
							}
							location = find_closest(robot_controller.getLocation(), enemy_positions);
							robot_controller.setIndicatorString(2, "aggressive");
						}else{
							// if i'm not sticky, or no enemies head for location

						}
				//		reset_simple_bug(location);
				//		simpleBug();
				//		applyMove();	
						if(!navigator.myDestination.equals(location) || Clock.getRoundNum() - turn_nav_calculated > 100){
							turn_nav_calculated =Clock.getRoundNum();
							navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
							navigator.process(robot_controller.getLocation());
						}
						move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
					}

				}
			}
			if(!my_type.equals(RobotType.DRONE)){
				request_help();
			}
				dish_out_supply();
				attack_deadest_enemy_in_range();
				robot_controller.setIndicatorString(0, location.toString());
				robot_controller.setIndicatorString(1, "heading for: " + navigator.getNextLocation().toString());
			robot_controller.yield();
		}		
	}
	public void evaluate_mining_position(){
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 1; i++)
					for (final Direction direction: directions){
						MapLocation test_location = robot_controller.getLocation().add(direction,i);
						if(my_mining_rate(test_location) > mining_move_threshold){
							try{
							if(robot_controller.canSenseLocation(test_location) && !robot_controller.isLocationOccupied(test_location)){
//								move_towards_direction(direction);
								location = test_location;
								if(!navigator.myDestination.equals(location) || Clock.getRoundNum() - turn_nav_calculated > 100){
									turn_nav_calculated =Clock.getRoundNum();
									navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
									navigator.process(robot_controller.getLocation());
								}
								move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
									return;
							}
							} catch(Exception e){
								print_exception(e);
							}
						}
					}
				mining_move_threshold /=2;
			}
		}
	}
	
}
