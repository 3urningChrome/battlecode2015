package Team111;

import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.RobotType;

public class Arobot {

	static RobotController robot_controller;
	static Team my_team;
	static Team enemy_team;
	static int my_range;
	static RobotType my_type;
	
	public Arobot(RobotController rc){
		robot_controller = rc;
		my_range = robot_controller.getType().attackRadiusSquared;
		my_team = robot_controller.getTeam();
		enemy_team = my_team.opponent();	
		my_type = robot_controller.getType();
	}
	
	//Should override this if you actually want the robot to do something other than very basic acts.
	//also this will not be very efficient. just simple and guaranteed to work for any RobotType.
	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			robot_controller.yield();
		}
	}	
	
	//basic combat ability to go in basic_turn_loop
	public void attack_random_enemy_in_range(){
		if(my_type.canAttack()){
			if(robot_controller.isWeaponReady()){
				RobotInfo[] sensed_enemy_robots = robot_controller.senseNearbyRobots(my_range, enemy_team);
				if(sensed_enemy_robots.length > 0){
					try{
					robot_controller.attackLocation(sensed_enemy_robots[0].location);
					} catch (Exception e){
		                System.out.println("Unexpected exception");
		                e.printStackTrace();
					}
				}
			}
		}
	}
}


//canAttackLocation(MapLocation loc)
//getWeaponDelay()
//isWeaponReady()
//senseNearbyRobots(int radiusSquared, Team team)
//attackLocation(MapLocation loc)