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
	static int sensor_range;
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
	static int numMinerFactories = 0;
	static int numHandwashes = 0; 
	static int numMiners = 0;
	
	static int max_drones = 10;
	static int max_beavers = 4;
	static int max_miner_factories = 2;
	static int helipad_max = 2;
	static int handwash_max = 1;
	static int max_Miners = 10;
	
	static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int directional_looks[] = new int[]{0,-1,1,-2,2,-3,3,4};
		
	public Arobot(RobotController rc){
		robot_controller = rc;
		my_range = robot_controller.getType().attackRadiusSquared;
		my_team = robot_controller.getTeam();
		enemy_team = my_team.opponent();	
		my_type = robot_controller.getType();
		sensor_range = my_type.sensorRadiusSquared;
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
				RobotInfo[] sensed_enemy_robots = get_all_enemies_in_range(my_range);
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
		RobotInfo[] sensed_friendly_Robots = get_all_friendly_robots();
		numSoldiers = 0;
		numBashers = 0;
		numBeavers = 0;
		numBarracks = 0;
		numHelipads = 0;
		numDrones = 0;
		numMinerFactories = 0;
		numHandwashes = 0;
		numMiners = 0;
				
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
			} else if (type == RobotType.DRONE) {
				numDrones++;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.HANDWASHSTATION) {
				numHandwashes++;
			}	else if (type == RobotType.MINER) {
				numMiners++;
			}				
		}
	}		
	
	public RobotInfo[] get_all_enemy_robots(){
		return robot_controller.senseNearbyRobots(999999, enemy_team);
	}
	
	public RobotInfo[] get_all_friendly_robots(){
		return robot_controller.senseNearbyRobots(999999, my_team);
	}
	
	public RobotInfo[] get_all_enemies_in_range(int range){
		return robot_controller.senseNearbyRobots(range, enemy_team);
	}
	
	public RobotInfo[] get_all_friends_in_range(int range){
		return robot_controller.senseNearbyRobots(range, my_team);
	}
	
	public void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}

	public MapLocation find_closest_tower() {
		return find_closest_map_location(robot_controller.senseEnemyTowerLocations());
	}

	public MapLocation find_closest_non_tower_enemy(RobotInfo[] known_enemies) {
		MapLocation[] enemy_locations = new MapLocation[known_enemies.length];
		for(int i=0; i<known_enemies.length;i++){
			if(known_enemies[i].type == RobotType.TOWER){
				enemy_locations[i] = new MapLocation(99999999,99999999);
			} else{
				enemy_locations[i] = known_enemies[i].location;
			}
		}
		return find_closest_map_location(enemy_locations);
	}
	
	public MapLocation find_closest_map_location(MapLocation [] map_locations){
		double closest_distance = 99999;
		int closest_position = 0;
		int number_of_locations = map_locations.length;
		for(int i = 0; i < number_of_locations; i++){
			if(robot_controller.getLocation().distanceSquaredTo(map_locations[i]) < closest_distance){
				closest_position = i;
				closest_distance = robot_controller.getLocation().distanceSquaredTo(map_locations[i]);
			}
		}		
		return map_locations[closest_position];
	}
	

}

//canAttackLocation(MapLocation loc)
//getWeaponDelay()
//isWeaponReady()
//senseNearbyRobots(int radiusSquared, Team team)
//attackLocation(MapLocation loc)