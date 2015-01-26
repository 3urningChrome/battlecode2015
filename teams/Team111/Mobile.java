package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Mobile extends Arobot {
	
	//Navigation
	static MapLocation location;
	static MapLocation previous_location;
	static boolean bugging = false;
	static int bugging_direction = 1;
	static boolean allow_bugging = true;
	static Direction last_heading;
	static int directionalLooksMulti[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	
	//Scanning (and Navigation)
	static MapLocation[] enemy_towers;
	static MapLocation[] positions_of_close_enemies;
	static MapLocation[] positions_of_very_close_friends;
	static RobotInfo[] very_close_friends;
	static int[] attack_ranges_of_close_enemies;
	static MapLocation missile_exclusion_zone = null;
	static boolean enemy_in_range = false;
	
	//Mining
	static int mining_search_range = 10;
	
	public Mobile(RobotController rc){
		super(rc);
	}
	
	public void initialise(){
		//override in class if you need other vars setting
	}
	
	public void basic_turn_loop(){
		initialise();
		enemy_towers = last_processed_enemy_towers;
		previous_location = robot_controller.getLocation();	
		
		while(true){
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(BEYOND_MAX_ATTACK_RANGE),enemy_team);
			enemy_towers = robot_controller.senseEnemyTowerLocations();
			sense_very_close_friends();
			location = get_default_location();
			decide_action();
			dish_out_supply();
			robot_controller.yield();
		}
	}
	
	public void decide_action(){
		if(decided_to_move())
			return; 
		if(shoot())	
			return;
		if(build())
			return;
		mine();
		
		return;
	}
	
	public boolean decided_to_move() {
		process_hostile_contacts();
		
		
		allow_bugging = robot_controller.getLocation().distanceSquaredTo(location) <= BEYOND_MAX_ATTACK_RANGE ? false : true;
		
		if(all_out_attack)
			return move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), location)));
		
		if(missile_exclusion_zone != null && robot_controller.getLocation().distanceSquaredTo(missile_exclusion_zone) < 81)
			return move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), HQ_location)));
		 if(!enemy_in_range)
				return move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), location)));
		 
		return false;
	}
	
	public boolean shoot() {
		if(!robot_controller.isWeaponReady())
			return false;
		try{
			if(robot_controller.canSenseLocation(location)){
				RobotInfo possibleEnemy = robot_controller.senseRobotAtLocation(location);
				if(possibleEnemy != null && possibleEnemy.team.equals(enemy_team))
					if(robot_controller.getLocation().distanceSquaredTo(location) < get_my_attack_radius()){
						robot_controller.attackLocation(location);
						return true;
					}
			}
		} catch(Exception e){
			Utilities.print_exception(e);
		}
		return attack_deadest_enemy_in_range();
	}
	
	private boolean build() {
		if(!my_type.canBuild())
			return false;
		if(!robot_controller.isCoreReady())
			return false;
		perform_a_troop_census();
		update_strategy();
		return try_to_process_builds();
	}
	
	private void mine() {
		if(!my_type.canMine())
			return;
		evaluate_mining_position_and_find_a_new_location_if_needed();
		mine_this_location_if_this_is_my_destination();
		move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), location)));
	}

	public MapLocation get_default_location(){
		location = robot_controller.getLocation();
		if((my_type.equals(RobotType.MINER) || my_type.equals(RobotType.BEAVER)) && Clock.getRoundNum() < 1800)
			return location;
		read_swarm_location(); //sets location
		find_enemy_to_go_after_if_i_am_aggressive(); //sets location
		return location;
	}

	//--------------------------------------------SCANNING--------------------------------------------------------------
	
	public void sense_very_close_friends() {
		very_close_friends = robot_controller.senseNearbyRobots(8,my_team);
		if(very_close_friends == null){
			positions_of_very_close_friends = null;
			return;
		}
			
		positions_of_very_close_friends = new MapLocation[very_close_friends.length];
		for(int i=0; i<very_close_friends.length;i++){
			positions_of_very_close_friends[i] = very_close_friends[i].location;
		}		
	}
	
	public boolean process_hostile_contacts() {
		enemy_in_range = false;
		if(sensed_enemy_robots == null){
			positions_of_close_enemies = null;
			attack_ranges_of_close_enemies = null;
			return false;
		}
		positions_of_close_enemies = new MapLocation[sensed_enemy_robots.length];
		attack_ranges_of_close_enemies = new int[sensed_enemy_robots.length];
		
		boolean I_am_indeed_in_danger = false;
		for(int i=0; i<sensed_enemy_robots.length;i++){
			positions_of_close_enemies[i] = sensed_enemy_robots[i].location;
			attack_ranges_of_close_enemies[i] = get_attack_radius(sensed_enemy_robots[i].type);
			if(sensed_enemy_robots[i].type.equals(RobotType.HQ) || sensed_enemy_robots[i].type.equals(RobotType.TOWER)){
				attack_ranges_of_close_enemies[i] = 0;
			}else{
				if(sensed_enemy_robots[i].type.equals(RobotType.MISSILE)){
					missile_exclusion_zone = sensed_enemy_robots[i].location.add(robot_controller.getLocation().directionTo(sensed_enemy_robots[i].location),2);
					//location = sensed_enemy_robots[i].location.add(robot_controller.getLocation().directionTo(sensed_enemy_robots[i].location),2);
				}
				modify_attack_range_after_considering_relative_delays(i,sensed_enemy_robots[i]);
			}
			if(positions_of_close_enemies[i].distanceSquaredTo(robot_controller.getLocation()) <= attack_ranges_of_close_enemies[i])
				I_am_indeed_in_danger = true;
			if(robot_controller.getLocation().distanceSquaredTo(positions_of_close_enemies[i]) <= get_my_attack_radius()){
				enemy_in_range = true;
			}
		}
		return I_am_indeed_in_danger;
	}
	
	//--------------------------------------------MICRO--------------------------------------------------------------
	public boolean i_do_more_ore_damage(RobotInfo robot_to_examine){
		//TODO should this be DPS as it is now, or should it be shot damage?? (DPS seems to make more sense, but I'm worried properly coded drones
		//can hit and run, keeping their DPS at max, but reducing mine as a result. therefore reducing my_ore_damage to below theirs.

		double my_delay_reduction_rate = (robot_controller.getSupplyLevel() > my_type.supplyUpkeep ? 1 : 0.5);
		double enemy_delay_reduction_rate = (robot_to_examine.supplyLevel > 0 ? 1 : 0.5);
		
		double my_ore_damage = Utilities.get_ore_per_HP_amount(robot_to_examine.type) * ((my_type.attackPower / my_type.attackDelay) * my_delay_reduction_rate);
		double enemy_ore_damage = Utilities.get_ore_per_HP_amount(my_type) * ((robot_to_examine.type.attackPower / robot_to_examine.type.attackDelay)*enemy_delay_reduction_rate);
		if(robot_to_examine.type.equals(RobotType.MINER))
			enemy_ore_damage = 0;
		
		if(my_type.equals(RobotType.MINER))
			my_ore_damage = 0;
		
		if(my_ore_damage > enemy_ore_damage)
			return true;
		return false;
	}
	
	public MapLocation find_enemy_to_go_after_if_i_am_aggressive() {
		if(aggressive == 0)
			return location; // only chase if you are aggressive
		if(sensed_enemy_robots == null)
			return location; // don't calc if there are no enemies
		if(all_out_attack)
			return location; // all out attack, so forget it and get your arse to swarm point
		
		int distance_to_enemy = 9999;
		for(RobotInfo enemy_robot:sensed_enemy_robots)
			if(distance_to_enemy > robot_controller.getLocation().distanceSquaredTo(enemy_robot.location)){
				//if(i_do_more_ore_damage(enemy_robot) || very_close_friends.length > sensed_enemy_robots.length){
					distance_to_enemy = robot_controller.getLocation().distanceSquaredTo(enemy_robot.location);
					location = enemy_robot.location;
			//	}
			}
		if(distance_to_enemy < 9999)
			if(robot_controller.getLocation().distanceSquaredTo(location) <= my_type.attackRadiusSquared)
				 location = robot_controller.getLocation(); // I'm aggressive, and heading for a robot. if it's in range. stop so I can shoot.
		
		return location;
	}
	
	public void modify_attack_range_after_considering_relative_delays(int attack_range_position, RobotInfo robot_to_examine){
		
		if(!robot_to_examine.type.canAttack())
			return;
		double my_delay_reduction_rate = (robot_controller.getSupplyLevel() > my_type.supplyUpkeep ? 1 : 0.5);
		double enemy_delay_reduction_rate = (robot_to_examine.supplyLevel > 0 ? 1 : 0.5);
		
		int my_attack_delay = (int) ((robot_controller.getWeaponDelay() + my_type.loadingDelay)/my_delay_reduction_rate);
		int enemy_move_delay = (int) ((robot_to_examine.coreDelay)/enemy_delay_reduction_rate); //move del
		
		if(i_do_more_ore_damage(robot_to_examine)){
			//if i can move n shoot before they can shoot and run reduce
			if(my_attack_delay < enemy_move_delay || very_close_friends.length > sensed_enemy_robots.length)
				attack_ranges_of_close_enemies[attack_range_position] = Utilities.increase_attack_radius(attack_ranges_of_close_enemies[attack_range_position], -1);
		}else{
//TODO shouldn't this be <= mycurrent range?
			//keep 1 extra square away
			int new_attack_range = Utilities.increase_attack_radius(attack_ranges_of_close_enemies[attack_range_position], 1);
			if (new_attack_range < robot_controller.getLocation().distanceSquaredTo(robot_to_examine.location) ) 
				attack_ranges_of_close_enemies[attack_range_position] =new_attack_range;
		}
	}
	
	//--------------------------------------------ROLE-WARFARE--------------------------------------------------------------
	public MapLocation read_swarm_location(){
		all_out_attack = false;
		int x = read_broadcast(swarm_location_channel_x);
		int y = read_broadcast(swarm_location_channel_y);
		if(x!= 0 && y!= 0){
			location = new MapLocation(x,y);
			if(robot_controller.getLocation().distanceSquaredTo(location) < 81)
				all_out_attack = true;
		}
		return location;
	}
	//--------------------------------------------MINING--------------------------------------------------------------
	public void mine_this_location_if_this_is_my_destination(){		
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				if(location.equals(robot_controller.getLocation())){
					try{
						robot_controller.mine();
						robot_controller.setIndicatorString(2, "mining");
						return;
					}catch(Exception e){
						Utilities.print_exception(e);
					}
				} else{
					robot_controller.setIndicatorString(2, "Heading for mining location");
				}
			}
		}
	}
//TODO reevaluate this, and make sure it's actually doing something useful!
	public void evaluate_mining_position_and_find_a_new_location_if_needed(){
		if(robot_controller.isCoreReady())
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < mining_search_range; i++)
					for (final Direction direction: directions){
						MapLocation test_location = robot_controller.getLocation().add(direction,i);
						if(my_mining_rate(test_location) > mining_move_threshold && terrain_is_navigatable(test_location) && is_free_from_exclusion(test_location))
							try{
								if(robot_controller.canSenseLocation(test_location) && !robot_controller.isLocationOccupied(test_location)){
									location = test_location;
									return;
								}
							} catch(Exception e){
								Utilities.print_exception(e);
							}

					}
				mining_move_threshold /=2;
			}
	}
	
	public double my_mining_rate(MapLocation the_location){
		if(robot_controller.canMine()){
			double current_ore = robot_controller.senseOre(the_location);
			if(current_ore == 0)
				return 0;
			//min(n, max(1, min(mm, n/mr)))
			return Math.min(current_ore, Math.max(GameConstants.MINIMUM_MINE_AMOUNT,Math.min(mining_max,(current_ore/mining_rate))));
		}
		return 0;
	}
	
	//--------------------------------------------MOVEMENT--------------------------------------------------------------
	
	public boolean move(Direction direction){
		if(!robot_controller.isCoreReady())
			return false; // not an invalid move, but cannot move. so return
		try{			
			if(direction.ordinal() > 7)
					return false; // move direction is none or omni etc. return
			if(robot_controller.canMove(direction)){
				robot_controller.move(direction);
				return true; //we moved
			}
		} catch(Exception e){
			Utilities.print_exception(e);
		}		
		return false; //we were asked to move somewhere we couldn't. return false.
	}
	
//--------------------------------------------Build Structures--------------------------------------------------------------	
	public boolean try_to_process_builds() {
		if(robot_controller.isCoreReady())
			if(my_type.canBuild())
				for(int building_ordinal: possible_spawn_and_building_ordinals)
					if(need_more_buildings_of_this_type(building_ordinal))
						if(build_structure(robot_types[building_ordinal]))
							return true;
		return false;
	}
	
	public boolean need_more_buildings_of_this_type(int build_ordinal) {
		if(robot_census[build_ordinal] < robot_max[build_ordinal])
			return true;
		return false;
	}
	
	public boolean build_structure(RobotType required_type){			
			if(robot_controller.hasBuildRequirements(required_type))
				for (final Direction direction: directions){
					MapLocation test_location = robot_controller.getLocation().add(direction);
					if((test_location.x + test_location.y) % 2 == 0 && robot_controller.canBuild(direction, required_type))
						try{
							if(!robot_controller.isCoreReady())
								return true;
							robot_controller.build(direction,required_type);
							send_broadcast(cumulative_ore_spent, read_broadcast(cumulative_ore_spent) + required_type.oreCost);
							send_broadcast(troop_count_channel+required_type.ordinal(), read_broadcast(troop_count_channel+required_type.ordinal()) + 1);
							return true;
						} catch(Exception e){
							Utilities.print_exception(e);
						}
				}
		return false;
	}	
	
//--------------------------------------------NAVIGATION--------------------------------------------------------------
	
//TODO: add predictive nav
	
	public MapLocation get_my_bug_Nav_next_step(MapLocation start_pos, MapLocation end_pos){
		if(!previous_location.equals(end_pos))
			bugging = false;

		previous_location = end_pos;
		if(!bugging)		
			return move_straight_toward_location(start_pos,end_pos);
		if(allow_bugging)
			return move_buggingly_toward_location(start_pos,end_pos);
		return robot_controller.getLocation();
	}
	
	public MapLocation move_straight_toward_location(MapLocation start_pos, MapLocation end_pos){
		Direction next_direction = start_pos.directionTo(end_pos);
		MapLocation test_location = start_pos.add(next_direction);
		if(terrain_is_navigatable(test_location) && is_free_from_exclusion(test_location) && available_location(test_location)){
			return test_location; // head straight for destination.
		} else{
			if(terrain_is_off_map(test_location)){
				return test_location; // no better direction to head to get to (off_map) destination.
			} else{
				if(!available_location(test_location)){
					//friend on it. don't bug around them. move around them
					last_heading = next_direction;
				} else{
					bugging = true;
					last_heading = next_direction;
				}
			}
		}
		return move_buggingly_toward_location(start_pos,end_pos);
	}
	
	public MapLocation move_buggingly_toward_location(MapLocation start_pos, MapLocation end_pos){
		Direction next_heading;
		Direction direction_to_end_pos = start_pos.directionTo(end_pos);
		MapLocation next_location;
		for(int directionalOffset:directionalLooksMulti){
			next_heading = directions[(last_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
			next_location = start_pos.add(next_heading);
			if(terrain_is_navigatable(next_location) && is_free_from_exclusion(next_location) && available_location(next_location)){
				if(next_heading.equals(direction_to_end_pos))
					bugging = false;
				last_heading = next_heading;
				return next_location;
			} else{
				if(terrain_is_off_map(next_location)){
					bugging = false; //stop bugging around outer edge of map. no point what so ever!
					bugging_direction *= (-1); // reverse bugging direction so that next time, we go the other way.
					return start_pos;
				}
			}
		}
		return start_pos;
	}
	
	public boolean terrain_is_navigatable(MapLocation test_location){
		TerrainTile nextStep = robot_controller.senseTerrainTile(test_location);
		if((nextStep.equals(TerrainTile.OFF_MAP)))
			return false;
		if (nextStep.equals(TerrainTile.VOID) && !(my_type.equals(RobotType.DRONE)))
			return false; //Missiles use their own Nav as this is too big.
		return true;
	}
	
	public boolean terrain_is_off_map(MapLocation test_location){
		TerrainTile nextStep = robot_controller.senseTerrainTile(test_location);
		if((nextStep.equals(TerrainTile.OFF_MAP)))
			return true;
		return false;
	}
	
	public boolean is_free_from_exclusion(MapLocation test_location){
		//HQ
		if(!location.equals(enemy_HQ_Location) && test_location.distanceSquaredTo(enemy_HQ_Location) < get_attack_radius(RobotType.HQ))
			return false;
		//Towers
		for(MapLocation tower:enemy_towers){
			if(!location.equals(tower) && test_location.distanceSquaredTo(tower) < 25)
				return false;
			if(location.equals(tower))
				return true;
		}
		//enemies
		if(positions_of_close_enemies != null){
			int num_of_loops = positions_of_close_enemies.length;
			for(int i=0; i<num_of_loops;i++)
				if(!location.equals(positions_of_close_enemies[i]) && test_location.distanceSquaredTo(positions_of_close_enemies[i]) <= attack_ranges_of_close_enemies[i])
					return false;
		}
		//missiles
		if(!all_out_attack)
			if(missile_exclusion_zone != null && test_location.distanceSquaredTo(missile_exclusion_zone) < 81)
				return false;
		return true;
	}
	
	public boolean available_location(MapLocation test_location){
		//friends
		if(positions_of_very_close_friends != null){
			int num_of_loops = positions_of_very_close_friends.length;
			for(int i=0; i<num_of_loops;i++)
				if(!location.equals(positions_of_very_close_friends[i]) && test_location.equals(positions_of_very_close_friends[i]))
					return false;
		}	
		return true;
	}
}
