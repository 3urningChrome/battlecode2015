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
//	static Utilities utils;
	
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
	static MapLocation[] last_processed_enemy_towers;
	
	//arrays
	static int[] robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] robot_max = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static boolean[] robot_mobile = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,false};
	static int[] possible_spawn_and_building_ordinals;
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
	static int troop_count_channel = 1000; // (+ 21)
	static int orders_broadcast_offset = 1050 + (HASH * HASH);
	
	static final int MAX_ROLES = 10;
	static int[] role_channel_start = new int[]{0,MAX_ROLES,2*MAX_ROLES,3*MAX_ROLES,4*MAX_ROLES,5*MAX_ROLES,6*MAX_ROLES,7*MAX_ROLES,8*MAX_ROLES,9*MAX_ROLES,10*MAX_ROLES,11*MAX_ROLES,12*MAX_ROLES,13*MAX_ROLES,14*MAX_ROLES,15*MAX_ROLES,16*MAX_ROLES,17*MAX_ROLES,18*MAX_ROLES,19*MAX_ROLES,20*MAX_ROLES};
	static int role_current_offset = 10;
	static int role_max_offset = 220;
	static int role_x_offset = 430;
	static int role_y_offset = 640;
	static int aggressive_offset = 850;

	//strategy
	static int swarm_trigger = 0; // set in HQ
	static boolean all_out_attack =false;
	static int my_role = -1;
	static int aggressive = 0;
	
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
		
		last_processed_enemy_towers = robot_controller.senseEnemyTowerLocations();
		
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
		
		basic_turn_loop();
	}
	
	//Should override this if you actually want the robot to do something other than exist
	public void basic_turn_loop(){
		while(true){
			System.out.println("You need to overide this method");
			robot_controller.yield();
		}
	}
	
	private void initialise_spawn_build_list() {
		possible_spawn_and_building_ordinals = new int[1];
		switch(my_type){
		case AEROSPACELAB:
			possible_spawn_and_building_ordinals[0] = RobotType.LAUNCHER.ordinal();
			break;
		case BARRACKS:
			possible_spawn_and_building_ordinals = new int[2];
			possible_spawn_and_building_ordinals[0] = RobotType.BASHER.ordinal();
			possible_spawn_and_building_ordinals[1] = RobotType.SOLDIER.ordinal();
			break;
		case BASHER:
			possible_spawn_and_building_ordinals = null;
			break;
		case BEAVER:
			possible_spawn_and_building_ordinals = new int[9];
			possible_spawn_and_building_ordinals[0] = RobotType.AEROSPACELAB.ordinal();
			possible_spawn_and_building_ordinals[1] = RobotType.BARRACKS.ordinal();
			possible_spawn_and_building_ordinals[2] = RobotType.HANDWASHSTATION.ordinal();
			possible_spawn_and_building_ordinals[3] = RobotType.HELIPAD.ordinal();
			possible_spawn_and_building_ordinals[4] = RobotType.MINERFACTORY.ordinal();
			possible_spawn_and_building_ordinals[5] = RobotType.SUPPLYDEPOT.ordinal();
			possible_spawn_and_building_ordinals[6] = RobotType.TANKFACTORY.ordinal();
			possible_spawn_and_building_ordinals[7] = RobotType.TECHNOLOGYINSTITUTE.ordinal();
			possible_spawn_and_building_ordinals[8] = RobotType.TRAININGFIELD.ordinal();
			break;
		case COMMANDER:
			possible_spawn_and_building_ordinals = null;
			break;
		case COMPUTER:
			possible_spawn_and_building_ordinals = null;
			break;
		case DRONE:
			possible_spawn_and_building_ordinals = null;
			break;
		case HANDWASHSTATION:
			possible_spawn_and_building_ordinals = null;
			break;
		case HELIPAD:
			possible_spawn_and_building_ordinals[0] = RobotType.DRONE.ordinal();
			break;
		case HQ:
			possible_spawn_and_building_ordinals[0] = RobotType.BEAVER.ordinal();
			break;
		case LAUNCHER:
			possible_spawn_and_building_ordinals[0] = RobotType.MISSILE.ordinal();
			break;
		case MINER:
			possible_spawn_and_building_ordinals = null;
			break;
		case MINERFACTORY:
			possible_spawn_and_building_ordinals[0] = RobotType.MINER.ordinal();
			break;
		case MISSILE:
			possible_spawn_and_building_ordinals = null;
			break;
		case SOLDIER:
			possible_spawn_and_building_ordinals = null;
			break;
		case SUPPLYDEPOT:
			possible_spawn_and_building_ordinals = null;
			break;
		case TANK:
			possible_spawn_and_building_ordinals = null;
			break;
		case TANKFACTORY:
			possible_spawn_and_building_ordinals[0] = RobotType.TANK.ordinal();
			break;
		case TECHNOLOGYINSTITUTE:
			possible_spawn_and_building_ordinals[0] = RobotType.COMPUTER.ordinal();
			break;
		case TOWER:
			possible_spawn_and_building_ordinals = null;
			break;
		case TRAININGFIELD:
			possible_spawn_and_building_ordinals[0] = RobotType.COMMANDER.ordinal();
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
		robot_max[RobotType.MINER.ordinal()] = 0;
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
		robot_max[RobotType.LAUNCHER.ordinal()] = 500;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		
		//if(robot_census[RobotType.MINER.ordinal()] > 1)
		//	robot_max[RobotType.MINERFACTORY.ordinal()] = 2;
		
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
			robot_max[RobotType.DRONE.ordinal()] = 999;
			robot_max[RobotType.MINER.ordinal()] = 20;
		}
		
		
		if(robot_controller.getTeamOre() > 600){
			robot_max[RobotType.MINER.ordinal()] = robot_census[RobotType.MINER.ordinal()] +=1;
		}
		
		if(robot_controller.getTeamOre() > 825){
			robot_max[RobotType.HELIPAD.ordinal()] = 2;
		}
		
		if(robot_controller.getTeamOre() > 925){
			robot_max[RobotType.BEAVER.ordinal()] =  2;
		}

		if(robot_controller.getTeamOre() > 1025){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 2;
			robot_max[RobotType.HELIPAD.ordinal()] = 3;
		}
		
		if(robot_controller.getTeamOre() > 2000){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 3;
			robot_max[RobotType.HELIPAD.ordinal()] = 5;
		}
		
		if(Clock.getRoundNum()> (TEMPORAL_HASH - 110)){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			robot_max[RobotType.DRONE.ordinal()] = 0;
			robot_max[RobotType.LAUNCHER.ordinal()] = 0;
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
	
	public void send_out_SOS_if_help_is_needed(){		
		if(robot_controller.getHealth() < previous_health ){
				previous_health-=2;
//System.out.println("Wanting help");
				if(read_broadcast(orders_broadcast_offset + location_channel(HQ_location)) == 0){
	//				System.out.println("Requesting help");
					send_broadcast(orders_broadcast_offset + location_channel(HQ_location),robot_controller.getID());
				}
		}
	}	
	

	
	public int get_my_attack_radius(){
		return get_attack_radius(my_type);
	}
	
	public int get_attack_radius(RobotType query_type){
		int attack_radius = query_type.attackRadiusSquared;
		switch(query_type){
		case BASHER:
			return 8;
		case HQ:
			boolean hq_splash = false;
			switch(last_processed_enemy_towers.length){
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
				attack_radius = Utilities.increase_attack_radius(attack_radius, 1);
			}
			return attack_radius;
		case LAUNCHER:
			return 36;
		case MISSILE:
			return 9;
		default:
			return query_type.attackRadiusSquared;
		}


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
						Utilities.print_exception(e);
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
			Utilities.print_exception(e);
		}
	}
		
	public void perform_a_troop_census(){		
		int num_of_loops = robot_census.length;
		for (int i=0; i<num_of_loops;i++){
			try{
				robot_census[i] = read_broadcast(troop_count_channel + i);
			} catch (Exception e){
				Utilities.print_exception(e);
			}			
		}

	}		
	

		
	public int location_channel(MapLocation encode_this_location){
		
		MapLocation return_location = new MapLocation(((encode_this_location.x % HASH) + HASH) % HASH,((encode_this_location.y % HASH) + HASH) % HASH);
		
		return (return_location.x * HASH) + return_location.y;
	}
	
//	public int encode_location(MapLocation encode_this_location){	
//		System.out.println("Location to encode:" + encode_this_location.toString());
//		System.out.println("Encoded Location (x) " + (HQ_location.x - encode_this_location.x) * HASH);
//		System.out.println("Encoded Location (y) " + (HQ_location.y - encode_this_location.y));
//		return ((encode_this_location.x - HQ_location.x) * HASH) + ((encode_this_location.y - HQ_location.y));
//	}
	
//	public MapLocation decode_location(int encoded_location){
//		System.out.println("Location to decode:" + encoded_location);

//		int encoded_location_y = (encoded_location % HASH) + HQ_location.y;
//		int encoded_location_x = ((encoded_location - encoded_location_y) / HASH) + HQ_location.x;

//		System.out.println("decoded Location (x) " + (((encoded_location - encoded_location_y) / HASH) ));
//		System.out.println("decoded Location (y) " + ((encoded_location % HASH) ));
//		return new MapLocation(encoded_location_x,encoded_location_y);
//	}
	
//	public int encode_exclusion_data(int damage, int end_round_num){
//		if(end_round_num > TEMPORAL_HASH)
//			end_round_num = TEMPORAL_HASH;
		
//		return (end_round_num * TEMPORAL_HASH) + damage;
//	}
	
//	public int decode_exclusion_damage(int encoded_data){
//		if(Clock.getRoundNum()> decode_exclusion_end_round(encoded_data))
//			return 0;
//		return encoded_data % TEMPORAL_HASH;
//	}
	
//	public int decode_exclusion_end_round(int encoded_data){
//		return (encoded_data - (encoded_data % TEMPORAL_HASH)) / TEMPORAL_HASH;
//	}
	
	public void send_broadcast(int channel, int data){
		try{
//			System.out.println("Sending on Channel : " + channel  +"  Data: " + data);
			robot_controller.broadcast(channel, data);
		} catch (Exception e){
			Utilities.print_exception(e);
		}
	}
	
	public int read_broadcast(int channel){
		try{
//			System.out.println("Readin Channel : " + channel  +"  Data: " + robot_controller.readBroadcast(channel));			
			return robot_controller.readBroadcast(channel);
		} catch (Exception e){
			Utilities.print_exception(e);
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
	

}