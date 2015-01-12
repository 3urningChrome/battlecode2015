package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Mobile extends Arobot {
	
	MapLocation location;
	MapLocation target;
	

	
	int assigned_job = 0;
	
	int[] danger_levels = {0,0,0,0,0,0,0,0,0};
	int my_danger_level = 8;
	int count_down = 35;
	
	int location_channel_x;
	int location_channel_y;
	
	int turn_nav_calculated = 0;
	
	public int bugging_direction = 1;
	public int turns_bugging = 0;
	private int bugging_threshold = 20;
	public boolean i_am_not_bugging = true;
	public boolean i_have_stopped_bugging = false;	
	public boolean i_have_started_bugging = false;
	public boolean sticky = false; //false == destination orientated. true == attack orientated
	public boolean swarm_attack = false;
	
	public MapLocation destination = null;
	
	private Direction current_heading = Direction.NONE;
	private Direction evaluate_heading = Direction.NONE;
	
	static int bugging_directional_looks[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	static int non_bugging_directional_looks[] = new int[]{0,1,-1};
	
	static RouteNode navigator;
	
	public Mobile(RobotController rc){
		super(rc);
		my_max_supply_level = max_mobile_supply_level;
		my_min_supply_level = min_mobile_supply_level;
		my_optimal_supply_level = optimal_mobile_supply_level;		
		location_channel_x  = swarm_location_channel_x;
		location_channel_y  = swarm_location_channel_y;
		destination = HQ_location;

	}
	
	public void basic_turn_loop(){
		navigator = new RouteNode(robot_controller.getLocation(),HQ_location,robot_controller,3);
		while(true){
			while(!robot_controller.isCoreReady() && !robot_controller.isWeaponReady()){
				if(!my_type.equals(RobotType.DRONE)){
					request_help();
				}
				robot_controller.yield();
			}
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(BEYOND_MAX_ATTACK_RANGE),enemy_team);
			role_warfare();
			if(robot_controller.isCoreReady()){				
				set_danger_levelz();		
				if(danger_levels[my_danger_level] == 0){		
					count_the_troops();				
					update_strategy();						
					check_for_builds();								
				}
				if(danger_levels[my_danger_level] !=0){
					evasive_move();
					robot_controller.setIndicatorString(2, "Evading");
				} else if(robot_controller.canMine()){
					robot_controller.setIndicatorString(2, "tring to mine");
					evaluate_mining_position();
					go_mining();
				} else{
					robot_controller.setIndicatorString(2, "location");
					if(attack_deadest_enemy_in_range()){
						//If I attacked someone, stay where I am (not currently in danger here)
					} else{
						if(sticky && sensed_enemy_robots.length > 0){
							//if there are enemies around, and I'm sticky, head for closest one.
							MapLocation[] enemy_positions = new MapLocation[sensed_enemy_robots.length];
							int num_of_loops = sensed_enemy_robots.length;
							for(int i=0; i<num_of_loops;i++){
								enemy_positions[i] = sensed_enemy_robots[i].location;
							}
							location = find_closest(robot_controller.getLocation(), enemy_positions);
							robot_controller.setIndicatorString(2, "aggressive");
						}else{
							// if i'm not sticky, or no enemies head for location

						}
				//		reset_simple_bug(location);
				//		simpleBug();
				//		applyMove();	
						if(!navigator.myDestination.equals(location) || Clock.getRoundNum() - turn_nav_calculated > 100){
							turn_nav_calculated =Clock.getRoundNum();
							navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
							navigator.process(robot_controller.getLocation());
						}
						move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
					}

				}
			}
			if(!my_type.equals(RobotType.DRONE)){
				request_help();
			}
				dish_out_supply();
				attack_deadest_enemy_in_range();
				robot_controller.setIndicatorString(0, location.toString());
				robot_controller.setIndicatorString(1, "heading for: " + navigator.getNextLocation().toString());
			robot_controller.yield();
		}		
	}
	
	public boolean role_warfare(){
		// default, no role. 
		//individual type clases orreride this.
		location = new MapLocation(read_broadcast(location_channel_x),read_broadcast(location_channel_y));
		return false;
	}
		
	public void evasive_move(){
	//	reset_simple_bug(HQ_location);
	//	simpleBug();
	//applyMove();
		location = HQ_location;
//		if(navigator.myDestination.equals(location)){
			turn_nav_calculated =Clock.getRoundNum();
			navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
			navigator.process(robot_controller.getLocation());
//		}
		move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
	}

	public void set_danger_levelz(){
		danger_levels = new int[]{0,0,0,0,0,0,0,0,0};
		
		if(swarm_attack)
			return;
		
		MapLocation my_location;
		
		// if robot can move is lower than my can move.  (assume going to fire if i can)
		//increase range by 1
	
		//if I can move, and move again (assuming fireing if can) before enemy can move or fire, 
		//reduce range by 1	
		double core_amount = robot_controller.getCoreDelay();
		double weapon_amount = robot_controller.getWeaponDelay();
		double reduction_per_turn = 0.5;
		if(robot_controller.getSupplyLevel()> (my_type.supplyUpkeep * my_type.movementDelay + my_type.loadingDelay)){
			reduction_per_turn = 1;
		}

		int attack_radius;
		if(!(robot_controller.getLocation().distanceSquaredTo(enemy_HQ_Location) > BEYOND_MAX_ATTACK_RANGE)){
		attack_radius = get_attack_radius(RobotType.HQ);		
		for(int j=0; j<danger_levels.length;j++){
			if(enemy_HQ_Location.distanceSquaredTo(robot_controller.getLocation().add(danger_directions[j])) <= attack_radius ){
				danger_levels[j] += RobotType.HQ.attackPower;
			}
		}
		}
		attack_radius = get_attack_radius(RobotType.TOWER);		
		MapLocation[] sensed_enemy_towers = robot_controller.senseEnemyTowerLocations();
		for(int i=0; i<sensed_enemy_towers.length;i++){
			if(!(robot_controller.getLocation().distanceSquaredTo(sensed_enemy_towers[i]) > BEYOND_MAX_ATTACK_RANGE)){
				for(int j=0; j<danger_levels.length;j++){
					if(sensed_enemy_towers[i].distanceSquaredTo(robot_controller.getLocation().add(danger_directions[j])) <= attack_radius ){
						danger_levels[j] += RobotType.TOWER.attackPower;
					}
				}		
			}
		}
		if(sensed_enemy_robots == null)
			return;
		for(int i=0; i<sensed_enemy_robots.length;i++){
			attack_radius = get_attack_radius(sensed_enemy_robots[i].type);
			double enemy_reduction = 0.5;
			if(sensed_enemy_robots[i].supplyLevel > 0)
				enemy_reduction = 1;
			if((my_type.loadingDelay/reduction_per_turn) >= ((sensed_enemy_robots[i].coreDelay-1) / enemy_reduction) && sensed_enemy_robots[i].type.canMove())
				attack_radius = increase_attack_radius(attack_radius,1);
			
			if(((my_type.loadingDelay + my_type.movementDelay)/reduction_per_turn) < (Math.min(((sensed_enemy_robots[i].coreDelay-1) / enemy_reduction),((sensed_enemy_robots[i].weaponDelay-1) / enemy_reduction)))){
				attack_radius = increase_attack_radius(attack_radius,-1);
			}

			for(int j=0; j<danger_levels.length;j++){
				if(sensed_enemy_robots[i].location.distanceSquaredTo(robot_controller.getLocation().add(danger_directions[j])) <= attack_radius ){
					danger_levels[j] += sensed_enemy_robots[i].type.attackPower;
				}
			}				
		}
	}
	
	public void go_mining(){
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				try{
					robot_controller.mine();
					robot_controller.setIndicatorString(2, "mining");
				}catch(Exception e){
					 print_exception(e);
				}
			}
		}
	}
	
	public void evaluate_mining_position(){
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 10; i++)
					for (final Direction direction: directions){
						MapLocation test_location = robot_controller.getLocation().add(direction,i);
						if(my_mining_rate(test_location) > mining_move_threshold){
							try{
							if(robot_controller.canSenseLocation(test_location) && !robot_controller.isLocationOccupied(test_location)){
//								move_towards_direction(direction);
								location = test_location;
								if(!navigator.myDestination.equals(location) || Clock.getRoundNum() - turn_nav_calculated > 100){
									turn_nav_calculated =Clock.getRoundNum();
									navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
									navigator.process(robot_controller.getLocation());
								}
								move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
									return;
							}
							} catch(Exception e){
								print_exception(e);
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
	
	public boolean move_towards_direction(Direction initial_direction){
		if(initial_direction.equals(Direction.OMNI))
			return false;
//		System.out.println("Move towards: " + initial_direction);
		for(int i=0; i<8;i++){
			int test_direction = ((initial_direction.ordinal() + directional_looks[i] + 8)%8);
			if(danger_levels[test_direction] == 0){
				if(move(directions[test_direction]))
					return true;
			}
		}	
		return false;
	}
	public boolean move(Direction direction){
		if(!robot_controller.isCoreReady())
			return false;
		try{	
			if(direction.equals(Direction.NONE) || direction.equals(Direction.OMNI))
					return true;
			if (robot_controller.canMove(direction)){
				robot_controller.move(direction);
				return true;
			}
		} catch(Exception e){
			print_exception(e);
		}		
		return false;
	}
	
	public void check_for_builds() {
		if(my_type.canBuild())
			for(int build_ordinal: spawn_build_ordinals)
				if(need_more_build(build_ordinal))
					if(build_structure(robot_types[build_ordinal]))
						break;
	}
	public boolean need_more_build(int build_ordinal) {
		if(robot_census[build_ordinal] < robot_max[build_ordinal])
			return true;
		return false;
	}
	
	public boolean build_structure(RobotType required_type){
		if(robot_controller.isCoreReady())			
			if(robot_controller.hasBuildRequirements(required_type))
				for (final Direction direction: directions)
					if(robot_controller.canBuild(direction, required_type))
						try{
							robot_controller.build(direction,required_type);
							send_broadcast(cumulative_ore_spent, read_broadcast(cumulative_ore_spent) + required_type.oreCost);
//							send_broadcast(troop_count_channel+required_type.ordinal(), read_broadcast(troop_count_channel+required_type.ordinal()) + 1);
							return true;
						} catch(Exception e){
							print_exception(e);
						}
		return false;
	}		
	
	public Direction simpleBug(){
		if(i_am_not_bugging){
			current_heading = robot_controller.getLocation().directionTo(destination);
			if(current_heading.equals(Direction.OMNI)){
				evaluate_heading = Direction.NONE;
				return Direction.NONE;
			}
				
			for(int directionalOffset:non_bugging_directional_looks){
				evaluate_heading = directions[(current_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
				if(can_move(evaluate_heading))
					return evaluate_heading;
			}
			i_have_started_bugging = true;	
		}
		i_am_not_bugging = false;
		turns_bugging++;
		for(int directionalOffset:bugging_directional_looks){
//System.out.println("I am bugging");			
			evaluate_heading = directions[(current_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
			if(can_move(evaluate_heading))
				return evaluate_heading;
		}		
		evaluate_heading = Direction.NONE;
		return evaluate_heading;
	}	

	public void applyMove(){
		current_heading = evaluate_heading;
		move(current_heading);
			
		i_have_started_bugging = false;
		i_have_stopped_bugging = false;
		
		if(i_am_not_bugging){
			current_heading = robot_controller.getLocation().directionTo(destination);
		} else{
			if (current_heading.equals(robot_controller.getLocation().directionTo(destination))){
				i_am_not_bugging = true;
				i_have_stopped_bugging = true;				
				turns_bugging = 0;
			}
			if(turns_bugging > bugging_threshold)
				bugging_direction *= -1;
				i_am_not_bugging = true;
				i_have_stopped_bugging = true;				
				turns_bugging = 0;
		}
	}
	
	public boolean can_move(Direction move_direction){	
//		System.out.println("direction: " + move_direction);
		if(terrain_is_impassable(move_direction)){
//			System.out.println("Terrain is impassable");
			return false;
		}
		if(space_is_occupied(move_direction)){
//			System.out.println("space is occupied");
			return false;
		}
		if(danger_levels[move_direction.ordinal()] > 0){
//			System.out.println("dangerous");
			return false;
		}
		return true;
	}

	public boolean terrain_is_impassable(Direction move_direction){
		MapLocation next_move = robot_controller.getLocation().add(move_direction);
		TerrainTile test_terrain = robot_controller.senseTerrainTile(next_move);
		if(test_terrain.equals(TerrainTile.NORMAL))
			return false;
		if(test_terrain.equals(TerrainTile.OFF_MAP))
			return true;
		if(test_terrain.equals(TerrainTile.VOID) && (robot_controller.getType().equals(RobotType.DRONE) || robot_controller.getType().equals(RobotType.MISSILE)))
			return false;
		if(test_terrain.equals(TerrainTile.VOID))
			return true;
		
		return false;
	}
	
	//Not even sure if this should be checked here....
	public boolean space_is_occupied(Direction move_direction){
		try{
			return robot_controller.isLocationOccupied(robot_controller.getLocation().add(move_direction));
		}catch(Exception e){
			System.out.println("Exception in bug space_is_occupied");
		}
		return false;	
	}
	
	public void reset_simple_bug(MapLocation the_destination){
		if(destination != null && destination.equals(the_destination))
				return;
		
		bugging_direction = 1;
		turns_bugging = 0;
		bugging_threshold = 20;
		i_am_not_bugging = true;
		i_have_stopped_bugging = false;	
		i_have_started_bugging = false;

		current_heading = Direction.NONE;
		evaluate_heading = Direction.NONE;
		
		destination = the_destination;
	}
}
