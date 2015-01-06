package Team111;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.RobotType;
import battlecode.common.GameConstants;

public class Arobot {

	static RobotController robot_controller;
	static Team my_team;
	static Team enemy_team;
	static RobotType my_type;
	
	static int my_range;
	static double minimum_supply = 5;
	static double mining_rate = 0;
	static double mining_max = 0;
	static double mining_move_threshold = 1;
	
	static int numSoldiers = 0;
	static int numBashers = 0;
	static int numBeavers = 0;
	static int numBarracks = 0;	
	static int numHelipads = 0;
	static int numDrones = 0;
	
	static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		
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
						 print_exception(e);
					}
				}
			}
		}
	}
	
	public void send_supply(int amount, MapLocation location){
		try{		
			robot_controller.transferSupplies(amount, location);
//			System.out.println("Amount:" + amount + " Location:" + location.toString());			
		} catch (Exception e){
			 print_exception(e);
		}
	}
	
	public double my_mining_rate(MapLocation the_location){
		if(robot_controller.canMine()){
			double current_ore = robot_controller.senseOre(the_location);
			if(current_ore == 0)
				return 0;
			
			//min(n, max(1, min(mm, n/mr)))
			return Math.min(current_ore, Math.max(1,Math.min(mining_max,(current_ore/mining_rate))));
		}
		return 0;
	}
	
	public void count_the_troops(){
		RobotInfo[] sensed_friendly_Robots = robot_controller.senseNearbyRobots(999999, my_team);
		numSoldiers = 0;
		numBashers = 0;
		numBeavers = 0;
		numBarracks = 0;
		numHelipads = 0;
		numDrones = 0;
				
		for (RobotInfo sensed_friendly_Robot : sensed_friendly_Robots) {
			RobotType type = sensed_friendly_Robot.type;
			if (type == RobotType.SOLDIER) {
				numSoldiers++;
			} else if (type == RobotType.BASHER) {
				numBashers++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
			} else if (type == RobotType.BARRACKS) {
				numBarracks++;
			} else if (type == RobotType.HELIPAD) {
				numHelipads++;
			}	else if (type == RobotType.DRONE) {
				numDrones++;
			}		
		}
	}		
	
	public void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}
}


//canAttackLocation(MapLocation loc)
//getWeaponDelay()
//isWeaponReady()
//senseNearbyRobots(int radiusSquared, Team team)
//attackLocation(MapLocation loc)