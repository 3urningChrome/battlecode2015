package team111;

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
	static boolean bugging = false;
	static int bugging_direction = 1;
	static Direction last_heading;
	static int directionalLooksMulti[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	
	//Scanning (and Navigation)
	static MapLocation[] enemy_towers;
	static MapLocation[] positions_of_close_enemies;
	static MapLocation[] positions_of_very_close_friends;
	static RobotInfo[] very_close_friends;
	static int[] attack_ranges_of_close_enemies;

	//Role Warfare
	static int assigned_job = 0;
	
	//Mining
	static int mining_search_range = 10;
	
	public Mobile(RobotController rc){
		super(rc);
	}
	
	public void initialise(){
		//override in class if you need other vars setting
		if(false)
			test_run_loop();
	}
	
	//testing loop
	public void test_run_loop(){
		while(true){
			location = HQ_location;
			enemy_towers = last_processed_enemy_towers;
			System.out.println("Heading for:" + get_my_bug_Nav_next_step(robot_controller.getLocation(), location) );
			move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), HQ_location)));
		
			robot_controller.yield();
		}
	}
	
	public void basic_turn_loop(){
		initialise();
		enemy_towers = last_processed_enemy_towers;
					
		while(true){			
			location = robot_controller.getLocation();
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(BEYOND_MAX_ATTACK_RANGE),enemy_team);
			sense_very_close_friends();
			
			send_out_SOS_if_help_is_needed();		
			set_location_for_my_role();			
				
			if(!sense_hostile_activity()){
				perform_a_troop_census();				
				update_strategy();	
				try_to_process_builds();				
				if(i_can_mine()){
					evaluate_mining_position_and_find_a_new_location_if_needed();				
					mine_this_location_if_this_is_my_destination();
				}	
			}
			move(robot_controller.getLocation().directionTo(get_my_bug_Nav_next_step(robot_controller.getLocation(), location)));
			
			attack_deadest_enemy_in_range();
			dish_out_supply();

			robot_controller.setIndicatorString(0, location.toString());
//			Utilities.print_byte_usage("End of basic_turn_loop: ");
			robot_controller.yield();
		}
	}

	//--------------------------------------------SCANNING--------------------------------------------------------------
	
	private void sense_very_close_friends() {
		very_close_friends = robot_controller.senseNearbyRobots(3,my_team);
		if(very_close_friends == null){
			positions_of_very_close_friends = null;
			return;
		}
		positions_of_very_close_friends = new MapLocation[very_close_friends.length];
		for(int i=0; i<very_close_friends.length;i++){
			positions_of_very_close_friends[i] = very_close_friends[i].location;
		}		
	}
	
	private boolean sense_hostile_activity() {
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
			modify_attack_range_after_considering_relative_delays(i,sensed_enemy_robots[i]);
			if(positions_of_close_enemies[i].distanceSquaredTo(robot_controller.getLocation()) <= attack_ranges_of_close_enemies[i])
				I_am_indeed_in_danger = true;
		}
		return I_am_indeed_in_danger;
	}
	
	//--------------------------------------------MICRO--------------------------------------------------------------

	private void modify_attack_range_after_considering_relative_delays(int attack_range_position, RobotInfo robot_to_examine) {
		RobotType enemy_type = robot_to_examine.type;
		
		int my_delay_incurred_by_move_and_shoot = my_type.movementDelay + my_type.loadingDelay + my_type.cooldownDelay;
		
		//can it shoot me if i step into it's outer fire zone.
		int enemy_delay_incurred_by_cool_down = (int)robot_to_examine.weaponDelay; 
		
		//can it follow me and shoot before i can move away again. (assuming i move into it's fire zone this turn
		int enemy_delay_incurred_by_move_and_load = (int)robot_to_examine.coreDelay + enemy_type.loadingDelay;
		
		double my_total_delay = my_delay_incurred_by_move_and_shoot/(robot_controller.getSupplyLevel() > my_type.supplyUpkeep ? 1 : 0.5);
System.out.println("My delay: " + my_total_delay);		
		double enemy_least_total_delay = Math.min(enemy_delay_incurred_by_cool_down/(robot_to_examine.supplyLevel > 0 ? 1 : 0.5),enemy_delay_incurred_by_move_and_load/(robot_to_examine.supplyLevel > 0 ? 1 : 0.5));
System.out.println("Enemy delay: " + enemy_least_total_delay);			
		if(!(enemy_least_total_delay < 1)){
			if(my_total_delay < enemy_least_total_delay){
				//seems we can be cheeky. (or die horribly because this is full of bugs)
				attack_ranges_of_close_enemies[attack_range_position] = Utilities.increase_attack_radius(attack_ranges_of_close_enemies[attack_range_position], -1);
			}
		}
		
		//can't quit wrap my head around this one. so it's probably wrong.
		//and check that the enemy cannot so the same back to me
		int enemy_delay_incurred_by_move_and_shoot = enemy_type.movementDelay + enemy_type.loadingDelay;

		//can I run away if it moves up to me?
		int my_delay_incurred_by_move = (int)robot_controller.getCoreDelay();
		
		double enemy_total_delay = enemy_delay_incurred_by_move_and_shoot/(robot_to_examine.supplyLevel > 0 ? 1 : 0.5);
		double my_least_total_delay = my_delay_incurred_by_move/(robot_controller.getSupplyLevel() > my_type.supplyUpkeep ? 1 : 0.5);
		
		if(!(my_least_total_delay < 1)){
			if(enemy_total_delay < my_least_total_delay){
				//seems they may be able to cheek us back!
				attack_ranges_of_close_enemies[attack_range_position] = Utilities.increase_attack_radius(attack_ranges_of_close_enemies[attack_range_position], 1);
			}
		}		
	}

	//--------------------------------------------ROLE-WARFARE--------------------------------------------------------------
	//TODO implement aggressive roles i.e. activly choose enemies over destination
	public boolean set_location_for_my_role(){
		int i_start=0;
		int loop_max = 	MAX_ROLES;	
		if(my_role != -1){
			i_start = my_role;
			loop_max = my_role + 1;
		}
		for(int i=i_start; i<loop_max;i++){
			int current_drones_in_role = read_broadcast((role_current_offset + role_channel_start[my_type.ordinal()] + i));
			int max_drones_in_role = read_broadcast((role_max_offset + role_channel_start[my_type.ordinal()] + i));
			
			if(max_drones_in_role == -1){
				my_role = -1;
				return false;
			}

			if (current_drones_in_role < max_drones_in_role && (my_role == i || my_role == -1)){
				current_drones_in_role ++;
				send_broadcast((role_current_offset + role_channel_start[my_type.ordinal()] + i), current_drones_in_role);
				int x = read_broadcast(role_x_offset + role_channel_start[my_type.ordinal()] + i);
				int y = read_broadcast(role_y_offset + role_channel_start[my_type.ordinal()] + i);
				aggressive = read_broadcast(aggressive_offset + role_channel_start[my_type.ordinal()] + i);
				my_role = i;
				location = new MapLocation(x,y);
				return true;
			}
		}
		my_role = -1;
		return false;
	}

	//--------------------------------------------MINING--------------------------------------------------------------
	
	private boolean i_can_mine(){
		return robot_controller.canMine();
	}
	
	public void mine_this_location_if_this_is_my_destination(){		
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				if(location.equals(robot_controller.getLocation())){
					try{
						robot_controller.mine();
						robot_controller.setIndicatorString(2, "mining");
					}catch(Exception e){
						Utilities.print_exception(e);
					}
				}
			}
		}
	}
//TODO reevaluate this, and make sure it's actually doing something usefull!
	public void evaluate_mining_position_and_find_a_new_location_if_needed(){
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < mining_search_range; i++)
					for (final Direction direction: directions){
						MapLocation test_location = robot_controller.getLocation().add(direction,i);
						if(my_mining_rate(test_location) > mining_move_threshold){
							try{
								if(robot_controller.canSenseLocation(test_location) && !robot_controller.isLocationOccupied(test_location)){
									location = test_location;
									return;
								}
							} catch(Exception e){
								Utilities.print_exception(e);
							}
						}
					}
				mining_move_threshold /=2;
			}
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
			return true;
		try{			
			//if(direction.equals(Direction.NONE) || direction.equals(Direction.OMNI))
			if(direction.ordinal() > 7)
					return true;
			if(robot_controller.canMove(direction)){
				robot_controller.move(direction);
				return true;
			}
		} catch(Exception e){
			Utilities.print_exception(e);
		}		
		return false;
	}
	
//--------------------------------------------Build Structures--------------------------------------------------------------	
//TODO don't block yourself or others in!
	public void try_to_process_builds() {
		if(robot_controller.isCoreReady())
			if(my_type.canBuild())
				for(int building_ordinal: possible_spawn_and_building_ordinals)
					if(need_more_buildings_of_this_type(building_ordinal))
						if(build_structure(robot_types[building_ordinal]))
							break;
	}
	
	public boolean need_more_buildings_of_this_type(int build_ordinal) {
		if(robot_census[build_ordinal] < robot_max[build_ordinal])
			return true;
		return false;
	}
	
	public boolean build_structure(RobotType required_type){			
			if(robot_controller.hasBuildRequirements(required_type))
				for (final Direction direction: directions)
					if(robot_controller.canBuild(direction, required_type))
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
		return false;
	}	
	
//--------------------------------------------NAVIGATION--------------------------------------------------------------
	
//TODO: add breadth first bugging.
//TODO handle is destination is off the map/inaccessable. stop bugging and wait at some point.
	
	public MapLocation get_my_bug_Nav_next_step(MapLocation start_pos, MapLocation end_pos){
		if(!bugging){
			Direction next_direction = start_pos.directionTo(end_pos);
			if(move_straight_toward_location(start_pos,next_direction))
				return start_pos.add(next_direction);
		}
		return move_buggingly_toward_location(start_pos,end_pos);
	}
	
	public boolean move_straight_toward_location(MapLocation start_pos, Direction move_direction){
		MapLocation test_location = start_pos.add(move_direction);
		if(terrain_is_navigatable(test_location) && is_free_from_exclusion(test_location)){
			return true;
		} else{
			bugging = true;
			last_heading = move_direction;
		}
		return false;
	}
	public MapLocation move_buggingly_toward_location(MapLocation start_pos, MapLocation end_pos){
		Direction next_heading;
		MapLocation next_location;
		for(int directionalOffset:directionalLooksMulti){
			next_heading = directions[(last_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
			next_location = start_pos.add(next_heading);
			if(terrain_is_navigatable(next_location) && is_free_from_exclusion(next_location)){
				if(next_heading.equals(start_pos.directionTo(end_pos))){
					bugging = false;
				}
				last_heading = next_heading;
				return next_location;
			}
		}
		return start_pos;
	}
	
	public boolean terrain_is_navigatable(MapLocation test_location){
		TerrainTile nextStep = robot_controller.senseTerrainTile(test_location);
		if((nextStep == TerrainTile.OFF_MAP))
			return false; // or summint else? we are here. destination is of map.		
		if ((nextStep==TerrainTile.VOID && !(my_type == RobotType.DRONE) ))
			return false;
		return true;
	}
	
	public boolean is_free_from_exclusion(MapLocation test_location){
		//HQ
		if(test_location.distanceSquaredTo(enemy_HQ_Location) < 49)
			return false;
		//Towers
		for(MapLocation tower:enemy_towers){
			if(test_location.distanceSquaredTo(tower) < 25)
				return false;				
		}
		//enemies
		if(positions_of_close_enemies != null){
			int num_of_loops = positions_of_close_enemies.length;
			for(int i=0; i<num_of_loops;i++){
				if(test_location.distanceSquaredTo(positions_of_close_enemies[i]) <= attack_ranges_of_close_enemies[i])
					return false;
			}
		}
		//friends
		if(positions_of_very_close_friends != null){
			int num_of_loops = positions_of_very_close_friends.length;
			for(int i=0; i<num_of_loops;i++){
				if(test_location.equals(positions_of_very_close_friends[i]))
					return false;
			}
		}
		return true;
	}
}
