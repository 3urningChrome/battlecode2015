package team111;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Tower extends Building {

	public Tower(RobotController rc) {
		super(rc);
	}	
	
	public boolean attack_deadest_enemy_in_range(){
		if(my_type.canAttack())
			if(robot_controller.isWeaponReady()){
				int attack_radius = get_attack_radius(my_type);
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
				if(close_enemies.length > 0){
					int num_of_loops = close_enemies.length;
					for(int i=0; i< num_of_loops;i++){
						//if(!close_enemies[i].type.equals(RobotType.MISSILE)){
							try{
								if(robot_controller.canAttackLocation(close_enemies[i].location)){
									robot_controller.attackLocation(close_enemies[i].location);
									return true;
								} else{
									MapLocation newLocation = close_enemies[i].location.add(robot_controller.getLocation().directionTo(close_enemies[i].location).opposite());
									if(robot_controller.canAttackLocation(newLocation)){
										robot_controller.attackLocation(newLocation);
										return true;
									}
								}
							} catch (Exception e){
								Utilities.print_exception(e);
							}
						//}
					}
				}
			}
		return false;
	}	
}
