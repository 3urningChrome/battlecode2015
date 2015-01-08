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
	static int swarm_location_channel = 1;
	static double minimum_supply = 5;
	static double mining_rate = 0;
	static double mining_max = 0;
	static double mining_move_threshold = 1;
	
	static int[] robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] robot_max = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] spawn_build_ordinals;
	static int[] robot_types_ordinals = new int[RobotType.values().length];
	static RobotType[] robot_types = RobotType.values();
	
    public static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);	
	static int static_broadcast_offset = 100;
	static int mobile_broadcast_offset = static_broadcast_offset + (HASH * HASH);

		
	static MapLocation HQ_location;
	
	
	static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int directional_looks[] = new int[]{0,-1,1,-2,2,-3,3,4};
		
	public Arobot(RobotController rc){
		robot_controller = rc;
		my_range = robot_controller.getType().attackRadiusSquared;
		my_team = robot_controller.getTeam();
		enemy_team = my_team.opponent();	
		my_type = robot_controller.getType();
		sensor_range = my_type.sensorRadiusSquared;
		
		HQ_location = robot_controller.senseEnemyHQLocation();
		
		initialise_default_strategy();
		initialise_spawn_build_list();
		
		int num_of_loops = robot_types_ordinals.length;
		RobotType[] all_robots = RobotType.values();
		for(int i=0; i< num_of_loops;i++){
			robot_types_ordinals[i] = all_robots[i].ordinal();
		}
	}
	
	private void initialise_spawn_build_list() {
		spawn_build_ordinals = new int[1];
		switch(my_type){
		case AEROSPACELAB:
			spawn_build_ordinals[0] = RobotType.LAUNCHER.ordinal();
			break;
		case BARRACKS:
			spawn_build_ordinals = new int[2];
			spawn_build_ordinals[0] = RobotType.BASHER.ordinal();
			spawn_build_ordinals[1] = RobotType.SOLDIER.ordinal();
			break;
		case BASHER:
			spawn_build_ordinals = null;
			break;
		case BEAVER:
			spawn_build_ordinals = new int[9];
			spawn_build_ordinals[0] = RobotType.AEROSPACELAB.ordinal();
			spawn_build_ordinals[1] = RobotType.BARRACKS.ordinal();
			spawn_build_ordinals[2] = RobotType.HANDWASHSTATION.ordinal();
			spawn_build_ordinals[3] = RobotType.HELIPAD.ordinal();
			spawn_build_ordinals[4] = RobotType.MINERFACTORY.ordinal();
			spawn_build_ordinals[5] = RobotType.SUPPLYDEPOT.ordinal();
			spawn_build_ordinals[6] = RobotType.TANKFACTORY.ordinal();
			spawn_build_ordinals[7] = RobotType.TECHNOLOGYINSTITUTE.ordinal();
			spawn_build_ordinals[8] = RobotType.TRAININGFIELD.ordinal();
			break;
		case COMMANDER:
			spawn_build_ordinals = null;
			break;
		case COMPUTER:
			spawn_build_ordinals = null;
			break;
		case DRONE:
			spawn_build_ordinals = null;
			break;
		case HANDWASHSTATION:
			spawn_build_ordinals = null;
			break;
		case HELIPAD:
			spawn_build_ordinals[0] = RobotType.DRONE.ordinal();
			break;
		case HQ:
			spawn_build_ordinals[0] = RobotType.BEAVER.ordinal();
			break;
		case LAUNCHER:
			spawn_build_ordinals[0] = RobotType.MISSILE.ordinal();
			break;
		case MINER:
			spawn_build_ordinals = null;
			break;
		case MINERFACTORY:
			spawn_build_ordinals[0] = RobotType.MINER.ordinal();
			break;
		case MISSILE:
			spawn_build_ordinals = null;
			break;
		case SOLDIER:
			spawn_build_ordinals = null;
			break;
		case SUPPLYDEPOT:
			spawn_build_ordinals = null;
			break;
		case TANK:
			spawn_build_ordinals = null;
			break;
		case TANKFACTORY:
			spawn_build_ordinals[0] = RobotType.TANK.ordinal();
			break;
		case TECHNOLOGYINSTITUTE:
			spawn_build_ordinals[0] = RobotType.COMPUTER.ordinal();
			break;
		case TOWER:
			spawn_build_ordinals = null;
			break;
		case TRAININGFIELD:
			spawn_build_ordinals[0] = RobotType.COMMANDER.ordinal();
			break;
		default:
			break;
		}
	}

	private void initialise_default_strategy() {
		robot_max[RobotType.AEROSPACELAB.ordinal()] = 0;
		robot_max[RobotType.BARRACKS.ordinal()] = 0;
		robot_max[RobotType.BASHER.ordinal()] = 0;
		robot_max[RobotType.BEAVER.ordinal()] = 4;
		robot_max[RobotType.COMMANDER.ordinal()] = 0;
		robot_max[RobotType.COMPUTER.ordinal()] = 0;
		robot_max[RobotType.DRONE.ordinal()] = 20;
		robot_max[RobotType.HANDWASHSTATION.ordinal()] = 0;
		robot_max[RobotType.HELIPAD.ordinal()] = 0;
		robot_max[RobotType.HQ.ordinal()] = 0;
		robot_max[RobotType.LAUNCHER.ordinal()] = 0;
		robot_max[RobotType.MINER.ordinal()] = 6;
		robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		robot_max[RobotType.MISSILE.ordinal()] = 0;
		robot_max[RobotType.SOLDIER.ordinal()] = 0;
		robot_max[RobotType.SUPPLYDEPOT.ordinal()] = 0;
		robot_max[RobotType.TANK.ordinal()] = 0;
		robot_max[RobotType.TANKFACTORY.ordinal()] = 0;
		robot_max[RobotType.TECHNOLOGYINSTITUTE.ordinal()] = 0;
		robot_max[RobotType.TOWER.ordinal()] = 0;
		robot_max[RobotType.TRAININGFIELD.ordinal()] = 0;	
	}
	
	public void update_strategy() {
		if(robot_census[RobotType.MINERFACTORY.ordinal()] == robot_max[RobotType.MINERFACTORY.ordinal()]){
			robot_max[RobotType.HELIPAD.ordinal()] = 1;
		}
		
		if(robot_census[RobotType.MINER.ordinal()] == robot_max[RobotType.MINER.ordinal()]){
			robot_max[RobotType.DRONE.ordinal()] = 20;
		}else{
			robot_max[RobotType.DRONE.ordinal()] = 0;
		}
		
		if(robot_controller.getTeamOre() > 1000){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 1;
		}
			
	}

	//Should override this if you actually want the robot to do something other than very basic acts.
	//also this will not be very efficient. just simple and guaranteed to work for any RobotType.
	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			update_strategy();
			robot_controller.yield();
		}
	}	
	
	//basic combat ability to go in basic_turn_loop
	public void attack_random_enemy_in_range(){
		if(my_type.canAttack()){
			if(robot_controller.isWeaponReady()){
				RobotInfo[] sensed_enemy_robots = robot_controller.senseNearbyRobots(my_range,enemy_team);
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
		
	public void count_the_troops(){
		robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		RobotInfo[] sensed_friendly_Robots = robot_controller.senseNearbyRobots(999999, my_team);
		
		for (RobotInfo sensed_friendly_Robot : sensed_friendly_Robots) {
				robot_census[sensed_friendly_Robot.type.ordinal()] ++;
		}
		
		robot_census[my_type.ordinal()] ++;
	}		
	
	public void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}
}