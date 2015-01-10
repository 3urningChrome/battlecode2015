package team1;

import battlecode.common.Clock;
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
	static int swarm_location_channel_x = 1;
	static int swarm_location_channel_y = 2;
	static int defence_location_channel_x = 3;
	static int defence_location_channel_y = 4;	
	static int override_saftey = 5;
	static int troop_count_channel = 7; // (to 25)
	static int next_available_channel = 29;
	static int swarm_trigger = 900;
	static boolean all_out_attack =false;
	
	static double min_building_supply_level = 5;
	static double max_building_supply_level = 1000;
	static double optimal_building_supply_level = 200;
	static double min_mobile_supply_level = 5;
	static double max_mobile_supply_level = 100;
	static double optimal_mobile_supply_level = 20;	
	static double my_max_supply_level = 1000;
	static double my_min_supply_level = 5;
	static double my_optimal_supply_level = 200;	
	
	static double mining_rate = 0;
	static double mining_max = 0;
	static double mining_move_threshold = 10;
	
	static int[] robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] robot_max = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static boolean[] robot_mobile = new boolean[]{false,false,true,true,true,false,true,false,false,false,true,true,false,false,true,false,true,false,false,false,false};
	static int[] spawn_build_ordinals;
	static int[] robot_types_ordinals = new int[RobotType.values().length];
	static RobotType[] robot_types = RobotType.values();
	
    public static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);	
	static int static_broadcast_offset = 100;
	static int mobile_broadcast_offset = static_broadcast_offset + (HASH * HASH);

		
	static MapLocation HQ_location;
	static MapLocation enemy_HQ_Location;
	
	RobotInfo[] sensed_enemy_robots;
	
	
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static int[] directional_looks = new int[]{0,-1,1,-2,2,-3,3,4};
		
	public Arobot(RobotController rc){
		robot_controller = rc;
		my_range = robot_controller.getType().attackRadiusSquared;
		my_team = robot_controller.getTeam();
		enemy_team = my_team.opponent();	
		my_type = robot_controller.getType();
		sensor_range = my_type.sensorRadiusSquared;
		
		HQ_location = robot_controller.senseHQLocation();
		enemy_HQ_Location = robot_controller.senseEnemyHQLocation();
		
		initialise_default_strategy();
		initialise_spawn_build_list();
		
//		if(robot_controller.getID()%2 == 1){
//			directional_looks = new int[]{0,1,-1,2,-2,3,-3,4};
//		}
		
		int num_of_loops = robot_types_ordinals.length;
		RobotType[] all_robots = RobotType.values();
		for(int i=0; i< num_of_loops;i++){
			robot_types_ordinals[i] = all_robots[i].ordinal();
		}
		
		sensed_enemy_robots = robot_controller.senseNearbyRobots(my_range,enemy_team);
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
		robot_max[RobotType.BASHER.ordinal()] = 4;
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.COMMANDER.ordinal()] = 1;
		robot_max[RobotType.COMPUTER.ordinal()] = 0;
		robot_max[RobotType.DRONE.ordinal()] = 0;
		robot_max[RobotType.HANDWASHSTATION.ordinal()] = 0;
		robot_max[RobotType.HELIPAD.ordinal()] = 1;
		robot_max[RobotType.HQ.ordinal()] = 0;
		robot_max[RobotType.LAUNCHER.ordinal()] = 2;
		robot_max[RobotType.MINER.ordinal()] = 5;
		robot_max[RobotType.MINERFACTORY.ordinal()] = 0;
		robot_max[RobotType.MISSILE.ordinal()] = 0;
		robot_max[RobotType.SOLDIER.ordinal()] = 0;
		robot_max[RobotType.SUPPLYDEPOT.ordinal()] = 0;
		robot_max[RobotType.TANK.ordinal()] = 0;
		robot_max[RobotType.TANKFACTORY.ordinal()] = 0;
		robot_max[RobotType.TECHNOLOGYINSTITUTE.ordinal()] = 0;
		robot_max[RobotType.TOWER.ordinal()] = 0;
		robot_max[RobotType.TRAININGFIELD.ordinal()] = 0;	
	}
	
	public void update_strategy(){
		 update_strategy_drone_rush();
	}
	public void update_strategy_drone_rush() {
		if(robot_census[RobotType.BEAVER.ordinal()] == robot_max[RobotType.BEAVER.ordinal()] ){
			robot_max[RobotType.DRONE.ordinal()] = 10;
		}
		if(robot_census[RobotType.HELIPAD.ordinal()] == robot_max[RobotType.HELIPAD.ordinal()] ){
			robot_max[RobotType.BEAVER.ordinal()] = 3;
		}		
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] == robot_max[RobotType.DRONE.ordinal()] ){
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		}
		
		if(robot_census[RobotType.MINER.ordinal()] == robot_max[RobotType.MINER.ordinal()] && robot_census[RobotType.AEROSPACELAB.ordinal()] == robot_max[RobotType.AEROSPACELAB.ordinal()]){
			robot_max[RobotType.HELIPAD.ordinal()] = 2;
			robot_max[RobotType.DRONE.ordinal()] = 900;
		}	
		
		if(robot_controller.getTeamOre() > 600){
			robot_max[RobotType.MINER.ordinal()] +=1;
			robot_max[RobotType.LAUNCHER.ordinal()] +=1;
			robot_max[RobotType.AEROSPACELAB.ordinal()]=1;
		}
		
		if(robot_controller.getTeamOre() > 1000){
			robot_max[RobotType.HELIPAD.ordinal()]+=1;
		}
		if(robot_controller.getTeamOre() > 1300){
			robot_max[RobotType.BEAVER.ordinal()]+=1;
		}
		
		int num_of_towers = robot_controller.senseEnemyTowerLocations().length + 3;
		int swarm_attack = Math.max((num_of_towers * 6),20);
		int swarm_retreat = num_of_towers * 4;
		if(robot_census[RobotType.DRONE.ordinal()] > swarm_attack){
			swarm_trigger = 0;
			all_out_attack = true;
		}
		if(robot_census[RobotType.DRONE.ordinal()] < swarm_retreat){
			swarm_trigger = 900;
			all_out_attack = false;
		}		
		
		if(Clock.getRoundNum()> 1850){
			swarm_trigger = 0;
			all_out_attack = true;
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			
		}
		if(Clock.getRoundNum()> 1880){
			robot_max[RobotType.DRONE.ordinal()] = 0;
		}
			
	}
	public void update_strategy_basher_defence() {
		if(robot_census[RobotType.BEAVER.ordinal()] == robot_max[RobotType.BEAVER.ordinal()] ){
			robot_max[RobotType.DRONE.ordinal()] = 14;
		}
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] == robot_max[RobotType.DRONE.ordinal()] ){
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		}
		
		if(robot_census[RobotType.MINER.ordinal()] == robot_max[RobotType.MINER.ordinal()] ){
			robot_max[RobotType.HELIPAD.ordinal()] = 2;
			robot_max[RobotType.DRONE.ordinal()] = 900;
		}	
		
		int num_of_towers = robot_controller.senseEnemyTowerLocations().length + 3;
		int swarm_attack = Math.max((num_of_towers * 6),20);
		int swarm_retreat = num_of_towers * 4;
		if(robot_census[RobotType.DRONE.ordinal()] > swarm_attack){
			swarm_trigger = 0;
			all_out_attack = true;
		}
		if(robot_census[RobotType.DRONE.ordinal()] < swarm_retreat){
			swarm_trigger = 900;
			all_out_attack = false;
		}		
		
		if(Clock.getRoundNum()> 1850){
			swarm_trigger = 0;
			all_out_attack = true;
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
		}
			
	}
	
	public void update_strategy_missile_attack() {
		if(robot_census[RobotType.BEAVER.ordinal()] == robot_max[RobotType.BEAVER.ordinal()] ){
			robot_max[RobotType.DRONE.ordinal()] = 12;
		}
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] == robot_max[RobotType.DRONE.ordinal()] ){
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		}
		
		if(robot_census[RobotType.MINER.ordinal()] == robot_max[RobotType.MINER.ordinal()] ){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 2;
			robot_max[RobotType.LAUNCHER.ordinal()] = 900;
		}	
		
		int num_of_towers = robot_controller.senseEnemyTowerLocations().length + 3;
		int swarm_attack = 10;
		int swarm_retreat = 5;
		if(robot_census[RobotType.LAUNCHER.ordinal()] > swarm_attack){
			swarm_trigger = 0;
			all_out_attack = true;
		}
		if(robot_census[RobotType.LAUNCHER.ordinal()] < swarm_retreat){
			swarm_trigger = 900;
			all_out_attack = false;
		}		
		
		if(Clock.getRoundNum()> 1850){
			swarm_trigger = 0;
			all_out_attack = true;
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
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
	

	public boolean attack_deadest_enemy_in_range(){
		if(my_type.canAttack()){
			if(robot_controller.isWeaponReady()){
				int attack_radius = my_type.attackRadiusSquared;
				if(my_type.equals(RobotType.HQ)){
					MapLocation[] my_towers = robot_controller.senseTowerLocations();
					boolean hq_splash = false;
					switch(my_towers.length){
					case 6:
					case 5:
						hq_splash = true;	
					case 4:
					case 3:
					case 2:
						attack_radius = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
					default:
						break;		
					}
					int offset = (int) Math.sqrt(attack_radius);
					if(hq_splash){
						offset+=1;
						attack_radius = (offset) * (offset);
					}
				}
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
				if(close_enemies.length > 0){
					int num_of_loops = close_enemies.length;
					double enemy_health = 9999;
					int attack_pos = 0;
					for(int i=0; i< num_of_loops;i++){
						//if(close_enemies[i].health <= enemy_health || close_enemies[i].type == RobotType.MISSILE){
						if(close_enemies[i].health <= enemy_health){
							enemy_health = close_enemies[i].health;
							attack_pos = i;
						}
					}
					try{
							robot_controller.attackLocation(close_enemies[attack_pos].location);
						return true;
					} catch (Exception e){
						 print_exception(e);
					}
				}
			}
		}
		return false;
	}	
	//basic combat ability to go in basic_turn_loop
	public void attack_random_enemy_in_range(){
		if(my_type.canAttack()){
			if(robot_controller.isWeaponReady()){
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(my_type.attackRadiusSquared, enemy_team);
				if(close_enemies.length > 0){
					try{
						robot_controller.attackLocation(close_enemies[0].location);
					} catch (Exception e){
						 print_exception(e);
					}
				}
			}
		}
	}
	
	public void send_supply(int amount, MapLocation location){
		if(!(amount > 0))
			return;
		
		try{		
			robot_controller.transferSupplies(amount, location);
		} catch (Exception e){
			print_exception(e);
		}
	}
		
	public void count_the_troops(){
		
		int num_of_loops = robot_census.length;
		for (int i=0; i<num_of_loops;i++){
			try{
				robot_census[i] = robot_controller.readBroadcast(troop_count_channel + i);
			} catch (Exception e){
				print_exception(e);
			}			
		}
	}		
	
	public MapLocation find_closest(MapLocation starting_point, MapLocation[] possible_locations){
		int closest_distance = 99999;
		int pos_of_closest = 0;
		
		if(possible_locations == null)
			return starting_point;
		
		if(possible_locations.length < 1){
			return starting_point;
		}
		
		for(int i=0; i< possible_locations.length;i++){
			if(starting_point.distanceSquaredTo(possible_locations[i]) < closest_distance){
				closest_distance = starting_point.distanceSquaredTo(possible_locations[i]);
				pos_of_closest = i;
			}
		}
		return possible_locations[pos_of_closest];
	}
	
	public int location_channel(MapLocation encode_this_location){
		MapLocation return_location = new MapLocation(((encode_this_location.x % HASH) + HASH) % HASH,((encode_this_location.y % HASH) + HASH) % HASH);	
		return (return_location.x * HASH) + return_location.y;
	}
	public int encode_location(MapLocation encode_this_location){	
		return (encode_this_location.x * HASH) + encode_this_location.y;
	}
	public MapLocation decode_location(int encoded_location){
		int encoded_location_y = encoded_location % HASH;
		int encoded_location_x = (encoded_location - encoded_location_y) / HASH;

		return new MapLocation(encoded_location_x,encoded_location_y);
	}	
	
	public void send_broadcast(int channel, int data){
		try{
			robot_controller.broadcast(channel, data);
		} catch (Exception e){
			print_exception(e);
		}
	}
	
	public int read_broadcast(int channel){
		try{
			return robot_controller.readBroadcast(channel);
		} catch (Exception e){
			print_exception(e);
		}		
		return 0;
	}
	
	public void dish_out_supply(){	
		int start_turn = Clock.getRoundNum();
		double dish_out_amount;
		if(all_out_attack){
			dish_out_amount = robot_controller.getSupplyLevel();
		}else{
			dish_out_amount = 0;
		}
		RobotInfo[] sensed_friendly_robots = robot_controller.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, my_team);	
		
		if(sensed_friendly_robots.length < 1)
			return;
		
		dish_out_amount /= sensed_friendly_robots.length;
		for (final RobotInfo sensed_friendly_robot: sensed_friendly_robots){
			if(robot_mobile[sensed_friendly_robot.type.ordinal()] || robot_mobile[my_type.ordinal()] ==false){		
				if(!sensed_friendly_robot.type.equals(RobotType.BEAVER) && !sensed_friendly_robot.type.equals(RobotType.MINER) && !sensed_friendly_robot.type.equals(RobotType.HQ)){
					if(Clock.getRoundNum() > start_turn || Clock.getBytecodesLeft() < 550)
						return;					
					send_supply((int)dish_out_amount, sensed_friendly_robot.location);
				}
			}
		}
	}
	
	public void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}
}