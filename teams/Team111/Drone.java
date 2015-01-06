package Team111;

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
			attack_random_enemy_in_range();
			destination = robot_controller.senseEnemyHQLocation();
			RobotInfo[] known_enemies = get_all_enemy_robots();
			if(known_enemies.length > 0){
				//destination = find_closest_non_tower_enemy(known_enemies);
				//don't move into enemy range. and if in range. move away.
				int closest_sensed_robot = 9999;
				int pos_of_closest_robot = 0;
				int[] danger_levels = {0,0,0,0,0,0,0,0};
				for(int i=0; i<known_enemies.length;i++){
					int distance_to_robot = robot_controller.getLocation().distanceSquaredTo(known_enemies[i].location);
					if (!known_enemies[i].type.equals(RobotType.TOWER) && distance_to_robot < closest_sensed_robot){
						closest_sensed_robot = distance_to_robot;
						pos_of_closest_robot = i;
					}
					if(distance_to_robot <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED)
						for(int j=0; j<danger_levels.length;j++){
							if(known_enemies[i].location.distanceSquaredTo(robot_controller.getLocation().add(directions[j])) <= known_enemies[i].type.attackRadiusSquared){
								danger_levels[j] += known_enemies[i].type.attackPower;
							}
						}
				}
				
				destination = known_enemies[pos_of_closest_robot].location;
				Direction initial_direction = robot_controller.getLocation().directionTo(destination);
				for(int i=0; i<8;i++){
					int test_direction = ((initial_direction.ordinal() + directional_looks[i] + 8)%8);
					if(danger_levels[test_direction] == 0){
						move_towards_direction(directions[test_direction]);
					}
				}
			}
//			if(destination != null){
//				//for now don't move into enemyRange.
//				
//				if(robot_controller.getLocation().distanceSquaredTo(destination) > my_range){
//					move_towards_direction(robot_controller.getLocation().directionTo(destination));
//				} else{
//					if(robot_controller.getLocation().add(robot_controller.getLocation().directionTo(destination).opposite()).distanceSquaredTo(destination) <= my_range){
//						move_towards_direction(robot_controller.getLocation().directionTo(destination).opposite());
//					}
//				}
//			}
			robot_controller.yield();
		}
	}
}
