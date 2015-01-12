package team111;

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
	static MapLocation HQ_location;
	static MapLocation enemy_HQ_Location;
	static int my_range;
	static int sensor_range;
	static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
	static final int TEMPORAL_HASH = GameConstants.ROUND_MAX_LIMIT;	
	static final int BEYOND_MAX_ATTACK_RANGE = 64;
	
	//arrays
	static int[] robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] robot_max = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static boolean[] robot_mobile = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,false};
	static int[] spawn_build_ordinals;
	static int[] robot_types_ordinals = new int[RobotType.values().length];
	static RobotType[] robot_types = RobotType.values();
	static RobotInfo[] sensed_enemy_robots;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static Direction[] danger_directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST,Direction.NONE};
	static int[] directional_looks = new int[]{0,-1,1,-2,2,-3,3,4};
	
	//broadcast channels
	static int swarm_location_channel_x = 1;
	static int swarm_location_channel_y = 2;
	static int defence_location_channel_x = 3;
	static int defence_location_channel_y = 4;	
	static int override_saftey = 5;
	static int cumulative_ore_spent = 6;
	static int drone_harass_max = 7;
	static int drone_harass_current = 8;
	static int drone_swarm_max = 9;
	static int drone_swarm_current = 10;
	static int launcher_harass_max = 11;
	static int launcher_harass_current = 12;
	static int launcher_tower_protection_max = 13;
	static int launcher_tower_protection_current = 14;
	static int troop_count_channel = 100; // (+ 21)
	static int orders_broadcast_offset = 200 + (HASH * HASH);

	//strategy
	static int swarm_trigger = 30; // set in HQ
	static boolean all_out_attack =false;
	
	//supply values
	static double min_building_supply_level = 5;
	static double max_building_supply_level = 1000;
	static double optimal_building_supply_level = 200;
	static double min_mobile_supply_level = 5;
	static double max_mobile_supply_level = 100;
	static double optimal_mobile_supply_level = 20;	
	static double my_max_supply_level = 1000;
	static double my_min_supply_level = 5;
	static double my_optimal_supply_level = 200;	
	static MapLocation centre_point;
	
	//mining variables
	static double mining_rate = 0;
	static double mining_max = 0;
	static double mining_move_threshold = 1;
	
	double previous_health;

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
			
		int num_of_loops = robot_types_ordinals.length;
		RobotType[] all_robots = RobotType.values();
		for(int i=0; i< num_of_loops;i++){
			robot_types_ordinals[i] = all_robots[i].ordinal();
		}
		
		sensed_enemy_robots = robot_controller.senseNearbyRobots(my_range,enemy_team);
		centre_point = new MapLocation(((HQ_location.x - enemy_HQ_Location.x)/2) + enemy_HQ_Location.x,((HQ_location.y - enemy_HQ_Location.y)/2) + enemy_HQ_Location.y);
		previous_health = robot_controller.getHealth();
		
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
		robot_max[RobotType.BEAVER.ordinal()] = 0;
		robot_max[RobotType.COMMANDER.ordinal()] = 0;
		robot_max[RobotType.COMPUTER.ordinal()] = 0;
		robot_max[RobotType.DRONE.ordinal()] = 0;
		robot_max[RobotType.HANDWASHSTATION.ordinal()] = 0;
		robot_max[RobotType.HELIPAD.ordinal()] = 0;
		robot_max[RobotType.HQ.ordinal()] = 0;
		robot_max[RobotType.LAUNCHER.ordinal()] = 0;
		robot_max[RobotType.MINER.ordinal()] = 4;
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
		 //update_strategy_drone_rush();
		small_harass_swarm_drone();
	}
	public void small_harass_swarm_drone(){
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.HELIPAD.ordinal()] = 1;
		robot_max[RobotType.DRONE.ordinal()] = 1;
		robot_max[RobotType.MINER.ordinal()] = 4;
		robot_max[RobotType.LAUNCHER.ordinal()] = 50;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		
		//if(robot_census[RobotType.MINER.ordinal()] > 1)
		//	robot_max[RobotType.MINERFACTORY.ordinal()] = 2;
		
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
			robot_max[RobotType.DRONE.ordinal()] = 999;
		}
		
		
		if(robot_controller.getTeamOre() > 600){
			robot_max[RobotType.MINER.ordinal()] +=1;
		}
		
		if(robot_controller.getTeamOre() > 825){
			robot_max[RobotType.HELIPAD.ordinal()] += 1;
		}
		
//		if(robot_controller.getTeamOre() > 925){
//			robot_max[RobotType.BEAVER.ordinal()] += 1;
//		}
		
		if(robot_controller.getTeamOre() > 1025){
			robot_max[RobotType.MINERFACTORY.ordinal()] += 1;
		}
		
		
	}
	
	public void update_strategy_drone_rush() {
//build beaver
//build Helipad
//build 4 drones
//build miners base (beaver has found secluded place to avoid counter rush
//build 4 miners
//build 1 beaver
//build aerospace
//build 4 drones (miner patrol)
//build launchers (tower defence)
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.HELIPAD.ordinal()] = 1;
		robot_max[RobotType.DRONE.ordinal()] = 4;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0 && robot_census[RobotType.MINERFACTORY.ordinal()] == robot_max[RobotType.MINERFACTORY.ordinal()])
			robot_max[RobotType.MINER.ordinal()] = 4;
		
		if(robot_census[RobotType.MINER.ordinal()] > 0 && robot_census[RobotType.MINER.ordinal()] >= robot_max[RobotType.MINER.ordinal()])
			robot_max[RobotType.BEAVER.ordinal()] = 2;

		if(robot_census[RobotType.BEAVER.ordinal()] > 1 && robot_census[RobotType.BEAVER.ordinal()] >= robot_max[RobotType.BEAVER.ordinal()])
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
	
		if(robot_census[RobotType.AEROSPACELAB.ordinal()] > 0 && robot_census[RobotType.AEROSPACELAB.ordinal()] == robot_max[RobotType.AEROSPACELAB.ordinal()])
			robot_max[RobotType.DRONE.ordinal()] = 8;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.DRONE.ordinal()] = 8;
		
		if(robot_census[RobotType.BEAVER.ordinal()] > 0 && robot_census[RobotType.BEAVER.ordinal()] >= robot_max[RobotType.BEAVER.ordinal()] )
			robot_max[RobotType.LAUNCHER.ordinal()] = 6;

		if(robot_census[RobotType.LAUNCHER.ordinal()] > 0 && robot_census[RobotType.LAUNCHER.ordinal()] >= robot_max[RobotType.LAUNCHER.ordinal()] )
			robot_max[RobotType.DRONE.ordinal()] = 999;
		
		if(Clock.getRoundNum()> (TEMPORAL_HASH - 110)){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			robot_max[RobotType.DRONE.ordinal()] = 0;
		} 
		
//		System.out.println("Build X Minerfactories:"  + robot_max[RobotType.MINERFACTORY.ordinal()]);
//		System.out.println("Max Array:"  + robot_max.toString());
	}
	
	public void request_help(){		
		if(robot_controller.getHealth() < previous_health ){
				previous_health-=2;
System.out.println("Wanting help");
				if(read_broadcast(orders_broadcast_offset + location_channel(HQ_location)) == 0){
					System.out.println("Requesting help");
					send_broadcast(orders_broadcast_offset + location_channel(HQ_location),robot_controller.getID());
				}
		}
	}
	//Should override this if you actually want the robot to do something other than very basic acts.
	//also this will not be very efficient. just simple and guaranteed to work for any RobotType.
	public void basic_turn_loop(){
		while(true){
			attack_deadest_enemy_in_range();
			update_strategy();
			robot_controller.yield();
		}
	}	
	
	public int increase_attack_radius(int attack_radius, int i) {
		// TODO Auto-generated method stub
		if (i==0)
			return attack_radius;
		
		double root = Math.sqrt(attack_radius);
		root = (int)(root+ 1);
		return (int)Math.pow((root + i),2);
	}
	
	public int get_my_attack_radius(){
		return get_attack_radius(my_type);
	}
	
	public int get_attack_radius(RobotType query_type){
		int attack_radius = query_type.attackRadiusSquared;
		if(query_type.equals(RobotType.HQ)){
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
			if(hq_splash){
				attack_radius = increase_attack_radius(attack_radius, 1);
			}
			return attack_radius;
		}
		if(query_type.equals(RobotType.LAUNCHER)){
			return 36;
		}
		if(query_type.equals(RobotType.MISSILE)){
			return 36;
		}	
		if(query_type.equals(RobotType.BASHER)){
			return 8;
		}	
		return query_type.attackRadiusSquared;
	}
	
	public boolean attack_deadest_enemy_in_range(){
		//Overrid because HQ distances are screwy.
		//i.e. sense within s+splah, but if using splash, need to shoot closer.
		if(my_type.canAttack()){
			if(robot_controller.isWeaponReady()){
				int attack_radius = get_my_attack_radius();
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
				if(close_enemies.length > 0){
					int num_of_loops = close_enemies.length;
					double enemy_health = 9999;
					int attack_pos = 0;
					for(int i=0; i< num_of_loops;i++){
						if(close_enemies[i].health <= enemy_health){
							enemy_health = close_enemies[i].health;
							attack_pos = i;
						}
					}
					try{
						if(robot_controller.getLocation().distanceSquaredTo(close_enemies[attack_pos].location) > GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED){
							robot_controller.attackLocation(close_enemies[attack_pos].location.add(robot_controller.getLocation().directionTo(close_enemies[attack_pos].location).opposite()));
						}else{
							robot_controller.attackLocation(close_enemies[attack_pos].location);
						}
						return true;
					} catch (Exception e){
						 print_exception(e);
					}
				}
			}
		}
		return false;
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
				robot_census[i] = read_broadcast(troop_count_channel + i);
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
		System.out.println("Location to encode:" + encode_this_location.toString());
		System.out.println("Encoded Location (x) " + (HQ_location.x - encode_this_location.x) * HASH);
		System.out.println("Encoded Location (y) " + (HQ_location.y - encode_this_location.y));
		return ((encode_this_location.x - HQ_location.x) * HASH) + ((encode_this_location.y - HQ_location.y));
	}
	
	public MapLocation decode_location(int encoded_location){
		System.out.println("Location to decode:" + encoded_location);

		int encoded_location_y = (encoded_location % HASH) + HQ_location.y;
		int encoded_location_x = ((encoded_location - encoded_location_y) / HASH) + HQ_location.x;

		System.out.println("decoded Location (x) " + (((encoded_location - encoded_location_y) / HASH) ));
		System.out.println("decoded Location (y) " + ((encoded_location % HASH) ));
		return new MapLocation(encoded_location_x,encoded_location_y);
	}
	
	public int encode_exclusion_data(int damage, int end_round_num){
		if(end_round_num > TEMPORAL_HASH)
			end_round_num = TEMPORAL_HASH;
		
		return (end_round_num * TEMPORAL_HASH) + damage;
	}
	
	public int decode_exclusion_damage(int encoded_data){
		if(Clock.getRoundNum()> decode_exclusion_end_round(encoded_data))
			return 0;
		return encoded_data % TEMPORAL_HASH;
	}
	
	public int decode_exclusion_end_round(int encoded_data){
		return (encoded_data - (encoded_data % TEMPORAL_HASH)) / TEMPORAL_HASH;
	}
	
	public void send_broadcast(int channel, int data){
		try{
//			System.out.println("Sending on Channel : " + channel  +"  Data: " + data);
			robot_controller.broadcast(channel, data);
		} catch (Exception e){
			print_exception(e);
		}
	}
	
	public int read_broadcast(int channel){
		try{
//			System.out.println("Readin Channel : " + channel  +"  Data: " + robot_controller.readBroadcast(channel));			
			return robot_controller.readBroadcast(channel);
		} catch (Exception e){
			print_exception(e);
		}		
		return 0;
	}
	
	public void dish_out_supply(){	
		int start_turn = Clock.getRoundNum();
		double dish_out_amount;
//		if(all_out_attack){
			dish_out_amount = robot_controller.getSupplyLevel();
//		}else{
//			dish_out_amount = 0;
//		}
		RobotInfo[] sensed_friendly_robots = robot_controller.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, my_team);	
		
		if(sensed_friendly_robots.length < 1)
			return;
		
		dish_out_amount /= (sensed_friendly_robots.length + 1);
		for (final RobotInfo sensed_friendly_robot: sensed_friendly_robots){
			if(robot_mobile[sensed_friendly_robot.type.ordinal()] || robot_mobile[my_type.ordinal()] ==false){		
			//	if(!sensed_friendly_robot.type.equals(RobotType.BEAVER) && !sensed_friendly_robot.type.equals(RobotType.MINER) && !sensed_friendly_robot.type.equals(RobotType.HQ)){
				if(Clock.getRoundNum() > start_turn || Clock.getBytecodesLeft() < 550)
					return;		
				//dishout_supply + 
				send_supply((int)(dish_out_amount - sensed_friendly_robot.supplyLevel), sensed_friendly_robot.location);	

			}
		}
	}
	
	public void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}
}