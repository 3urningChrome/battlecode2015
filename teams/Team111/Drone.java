package Team111;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Drone extends Mobile {

	static MapLocation destination; 
	public Drone(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			destination = null;
			RobotInfo[] known_enemies = get_all_enemy_robots();
			if(known_enemies.length > 0){
				destination = find_closest_enemy(known_enemies);
			} else{
				destination = find_closest_tower();
			}
			if(destination != null){
				if(robot_controller.getLocation().distanceSquaredTo(destination) > my_range){
					move_towards_direction(robot_controller.getLocation().directionTo(destination));
				} else{
					if(robot_controller.getLocation().add(robot_controller.getLocation().directionTo(destination).opposite()).distanceSquaredTo(destination) <= my_range){
						move_towards_direction(robot_controller.getLocation().directionTo(destination).opposite());
					}
				}
			}
			robot_controller.yield();
		}
	}
}
