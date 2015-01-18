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

	// Useful variables
	static MapLocation HQ_location;
	static MapLocation enemy_HQ_Location;
	static MapLocation centre_point;
	
	static Team my_team;
	static Team enemy_team;
	static RobotType my_type;

	static int my_range;
	static int sensor_range;
	static double previous_health;
	static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
	static final int TEMPORAL_HASH = GameConstants.ROUND_MAX_LIMIT;	
	static final int BEYOND_MAX_ATTACK_RANGE = 100;
	static final int MAX_ROLES = 10;
	
	//arrays
	static MapLocation[] last_processed_enemy_towers;
	static RobotInfo[] sensed_enemy_robots;
	
	static int[] robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static int[] robot_max = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	static boolean[] robot_mobile = new boolean[]{false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,false};
	static boolean[] non_combatant = new boolean[]{true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false};
	static int[] possible_spawn_and_building_ordinals;
	static int[] robot_types_ordinals = new int[RobotType.values().length];
	static RobotType[] robot_types = RobotType.values();
	
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
	static int[] role_channel_start = new int[]{0,MAX_ROLES,2*MAX_ROLES,3*MAX_ROLES,4*MAX_ROLES,5*MAX_ROLES,6*MAX_ROLES,7*MAX_ROLES,8*MAX_ROLES,9*MAX_ROLES,10*MAX_ROLES,11*MAX_ROLES,12*MAX_ROLES,13*MAX_ROLES,14*MAX_ROLES,15*MAX_ROLES,16*MAX_ROLES,17*MAX_ROLES,18*MAX_ROLES,19*MAX_ROLES,20*MAX_ROLES};
	static int role_current_offset = 10;
	static int role_max_offset = 220;
	static int role_x_offset = 430;
	static int role_y_offset = 640;
	static int aggressive_offset = 850;
	static int troop_count_channel = 2000; // (+ 21)
	static int orders_broadcast_offset = 3050; // + (HASH * HASH);

	//strategy
	static int swarm_trigger = 0; // set in HQ
	static boolean all_out_attack = false;
	static int my_role = -1;
	static int aggressive = 0;
		
	//mining variables
	static double mining_rate = 0;
	static double mining_max = 0;
	static double mining_move_threshold = 1;

	public Arobot(RobotController rc){
		robot_controller = rc;
		my_range = robot_controller.getType().attackRadiusSquared;
		my_team = robot_controller.getTeam();
		enemy_team = my_team.opponent();	
		my_type = robot_controller.getType();
		sensor_range = my_type.sensorRadiusSquared;
		
		HQ_location = robot_controller.senseHQLocation();
		enemy_HQ_Location = robot_controller.senseEnemyHQLocation();
		centre_point = new MapLocation(((HQ_location.x - enemy_HQ_Location.x)/2) + enemy_HQ_Location.x,((HQ_location.y - enemy_HQ_Location.y)/2) + enemy_HQ_Location.y);
		
		last_processed_enemy_towers = robot_controller.senseEnemyTowerLocations();
		
		initialise_default_strategy();
		initialise_spawn_build_list();
			
		int num_of_loops = robot_types_ordinals.length;
		RobotType[] all_robots = RobotType.values();
		for(int i=0; i< num_of_loops;i++){
			robot_types_ordinals[i] = all_robots[i].ordinal();
		}
		
		sensed_enemy_robots = robot_controller.senseNearbyRobots(my_range,enemy_team);
		previous_health = robot_controller.getHealth();
		
		basic_turn_loop();
	}
	
	//Should override this if you actually want the robot to do something other than exist
	public void basic_turn_loop(){
		while(true){
			System.out.println("You need to override this method");
			robot_controller.yield();
		}
	}
	
	//--------------------------------------------GENERAL STRATEGY--------------------------------------------------------------	
	public void perform_a_troop_census(){		
		int num_of_loops = robot_census.length;
		for (int i=0; i<num_of_loops;i++)
			robot_census[i] = read_broadcast(troop_count_channel + i);			
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
		//medium_map_drone_harrass_launcher_swarm();
		//drone_contain();
		soldier_protect_tank_swarm();
	}
	
	private void initialise_spawn_build_list() {
		possible_spawn_and_building_ordinals = null;
		switch(my_type){
		case AEROSPACELAB:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.LAUNCHER.ordinal();
			break;
		case BARRACKS:
			possible_spawn_and_building_ordinals = new int[2];
			possible_spawn_and_building_ordinals[0] = RobotType.BASHER.ordinal();
			possible_spawn_and_building_ordinals[1] = RobotType.SOLDIER.ordinal();
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
		case HELIPAD:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.DRONE.ordinal();
			break;
		case HQ:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.BEAVER.ordinal();
			break;
		case LAUNCHER:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.MISSILE.ordinal();
			break;
		case MINERFACTORY:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.MINER.ordinal();
			break;
		case TANKFACTORY:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.TANK.ordinal();
			break;
		case TECHNOLOGYINSTITUTE:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.COMPUTER.ordinal();
			break;
		case TRAININGFIELD:
			possible_spawn_and_building_ordinals = new int[1];
			possible_spawn_and_building_ordinals[0] = RobotType.COMMANDER.ordinal();
			break;
		default:
			break;
		}
	}
	//--------------------------------------------STRATEGY-(SOLDIER_PROTECT_TANK_SWARM)--------------------------------------------------------------
	public void soldier_protect_tank_swarm(){
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		robot_max[RobotType.MINER.ordinal()] = 12;
		robot_max[RobotType.TANK.ordinal()] = 9999;
		robot_max[RobotType.SOLDIER.ordinal()] = 12;
		
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0)
			robot_max[RobotType.BARRACKS.ordinal()] = 1;

		if(robot_census[RobotType.BARRACKS.ordinal()] > 0)
			robot_max[RobotType.TANKFACTORY.ordinal()] = 1;
		
		if(robot_controller.getTeamOre() > 800 )
			robot_max[RobotType.TANKFACTORY.ordinal()] = 3;

		if(Clock.getRoundNum()> (TEMPORAL_HASH - 110)){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			robot_max[RobotType.DRONE.ordinal()] = 0;
			robot_max[RobotType.LAUNCHER.ordinal()] = 0;
		} 
	}	
	
	
	//--------------------------------------------STRATEGY-(DRONE CONTAIN)--------------------------------------------------------------	

	public void drone_contain(){
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.HELIPAD.ordinal()] = 1;
		robot_max[RobotType.DRONE.ordinal()] = 10;
		robot_max[RobotType.MINER.ordinal()] = 4;
		robot_max[RobotType.LAUNCHER.ordinal()] = 9;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
			
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
			robot_max[RobotType.MINER.ordinal()] = 10;
			robot_max[RobotType.DRONE.ordinal()] = 9999;
		}
		
		if(robot_census[RobotType.AEROSPACELAB.ordinal()] > 0){
			robot_max[RobotType.DRONE.ordinal()] = 9999;
			//robot_max[RobotType.MINER.ordinal()] = 20;
		}
		
		if(robot_census[RobotType.DRONE.ordinal()] > 20){
			robot_max[RobotType.SUPPLYDEPOT.ordinal()] = 2;
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
		}
		
		if(robot_controller.getTeamOre() > 600){
			robot_max[RobotType.MINER.ordinal()] = robot_census[RobotType.MINER.ordinal()] +1;
		}
		
		if(robot_controller.getTeamOre() > 925){
			//robot_max[RobotType.BEAVER.ordinal()] =  2;
		}

		if(robot_controller.getTeamOre() > 1525){
			robot_max[RobotType.HELIPAD.ordinal()] = robot_census[RobotType.HELIPAD.ordinal()] +1;
		}

		if(Clock.getRoundNum()> (TEMPORAL_HASH - 110)){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			robot_max[RobotType.DRONE.ordinal()] = 0;
			robot_max[RobotType.LAUNCHER.ordinal()] = 0;
		} 
		
	}
	//--------------------------------------------STRATEGY-(MEDIUM_MAP_DRONE_HARRASS_LAUNCHER_SWARM)--------------------------------------------------------------
	public void medium_map_drone_harrass_launcher_swarm(){
		robot_max[RobotType.BEAVER.ordinal()] = 1;
		robot_max[RobotType.HELIPAD.ordinal()] = 1;
		robot_max[RobotType.DRONE.ordinal()] = 8;
		robot_max[RobotType.MINER.ordinal()] = 4;
		robot_max[RobotType.LAUNCHER.ordinal()] = 999;
		
		if(robot_census[RobotType.DRONE.ordinal()] > 0 && robot_census[RobotType.DRONE.ordinal()] >= robot_max[RobotType.DRONE.ordinal()])
			robot_max[RobotType.MINERFACTORY.ordinal()] = 1;
		
		
		if(robot_census[RobotType.MINERFACTORY.ordinal()] > 0){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = 1;
			robot_max[RobotType.MINER.ordinal()] = 20;
		}
		
		
		if(robot_controller.getTeamOre() > 600){
			robot_max[RobotType.MINER.ordinal()] = robot_census[RobotType.MINER.ordinal()] +1;
		}
		
		if(robot_controller.getTeamOre() > 925){
			robot_max[RobotType.BEAVER.ordinal()] =  2;
		}

		if(robot_controller.getTeamOre() > 1025){
			robot_max[RobotType.AEROSPACELAB.ordinal()] = robot_census[RobotType.AEROSPACELAB.ordinal()] +1;
		}

		if(Clock.getRoundNum()> (TEMPORAL_HASH - 110)){
			robot_max[RobotType.HANDWASHSTATION.ordinal()] = 100;
			robot_max[RobotType.DRONE.ordinal()] = 0;
			robot_max[RobotType.LAUNCHER.ordinal()] = 0;
		} 
	}
	
//--------------------------------------------ATTACK--------------------------------------------------------------	
	public int get_my_attack_radius(){
		return get_attack_radius(my_type);
	}
	
	public int get_attack_radius(RobotType query_type){
		int attack_radius = query_type.attackRadiusSquared;
		switch(query_type){
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
			return 2; //was 36
		case MISSILE:
			return 15;
		default:
			return query_type.attackRadiusSquared;
		}
	}
	
	public boolean attack_deadest_enemy_in_range(){
		System.out.println("starting attack");
		if(my_type.canAttack()){
			System.out.println("can attack");
			if(robot_controller.isWeaponReady()){
				System.out.println("Weapon is ready");
				int attack_radius = get_my_attack_radius();
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
				if(close_enemies.length > 0){
					System.out.println("enemies in range");
					int num_of_loops = close_enemies.length;
					double enemy_health = 9999;
					int attack_pos = 0;
					for(int i=0; i< num_of_loops;i++){
						if(!close_enemies[i].type.equals(RobotType.MISSILE))
							if(close_enemies[i].health <= enemy_health){
								enemy_health = close_enemies[i].health;
								attack_pos = i;
							}
					}
					System.out.println("atacking robot: " + close_enemies[attack_pos].ID);
					try{
						if(robot_controller.getLocation().distanceSquaredTo(close_enemies[attack_pos].location) > GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED){
							MapLocation newLocation = close_enemies[attack_pos].location.add(robot_controller.getLocation().directionTo(close_enemies[attack_pos].location).opposite());
							if(robot_controller.getLocation().distanceSquaredTo(newLocation) <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED)
								robot_controller.attackLocation(newLocation);
							System.out.println("location too far. adjusting. FIRE!");
						}else{
							robot_controller.attackLocation(close_enemies[attack_pos].location);
							System.out.println("FIRE");
						}
						return true;
					} catch (Exception e){
						Utilities.print_exception(e);
					}
				}
			} else{
				System.out.println("Weapon is not ready");			
			}
		}
		return false;
	}	
		
	//--------------------------------------------BROADCASTING--------------------------------------------------------------	
	public int location_channel(MapLocation encode_this_location){	
		MapLocation return_location = new MapLocation(((encode_this_location.x % HASH) + HASH) % HASH,((encode_this_location.y % HASH) + HASH) % HASH);
		return (return_location.x * HASH) + return_location.y;
	}
		
	public void send_broadcast(int channel, int data){
		try{
			robot_controller.broadcast(channel, data);
		} catch (Exception e){
			Utilities.print_exception(e);
		}
	}
	
	public int read_broadcast(int channel){
		try{		
			return robot_controller.readBroadcast(channel);
		} catch (Exception e){
			Utilities.print_exception(e);
		}		
		return 0;
	}
	
	public void send_out_SOS_if_help_is_needed(){	
		if(non_combatant[my_type.ordinal()]){
			if(robot_controller.getHealth() < previous_health ){
				previous_health-=2;
				if(read_broadcast(orders_broadcast_offset + location_channel(HQ_location)) == 0)
					send_broadcast(orders_broadcast_offset + location_channel(HQ_location),robot_controller.getID());
			}
		}
	}	
	//--------------------------------------------SUPPLY--------------------------------------------------------------	
	public void dish_out_supply(){	
		double dish_out_amount;
		dish_out_amount = robot_controller.getSupplyLevel();

		RobotInfo[] sensed_friendly_robots = robot_controller.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, my_team);	
		
		if(sensed_friendly_robots.length < 1)
			return;
		
		dish_out_amount /= (sensed_friendly_robots.length + 1);
		for (final RobotInfo sensed_friendly_robot: sensed_friendly_robots){
			//always send to mobile robots, or always send to everyone if I am a building.
			if(robot_mobile[sensed_friendly_robot.type.ordinal()] || robot_mobile[my_type.ordinal()] ==false){		
				if(Clock.getBytecodesLeft() < 600)
					return;		
				send_supply((int)(dish_out_amount), sensed_friendly_robot.location);	
			}
		}
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
}