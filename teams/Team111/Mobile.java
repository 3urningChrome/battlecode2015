package team111;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Mobile extends Arobot {
	
	MapLocation location;
	
	int[] danger_levels = {0,0,0,0,0,0,0,0};
	int my_danger_level = 0;
	int count_down = 35;
	
	int location_channel_x;
	int location_channel_y;
	
	public int bugging_direction = 1;
	public int turns_bugging = 0;
	private int bugging_threshold = 20;
	public boolean i_am_not_bugging = true;
	public boolean i_have_stopped_bugging = false;	
	public boolean i_have_started_bugging = false;
	
	public MapLocation destination = null;
	
	private Direction current_heading = Direction.NONE;
	private Direction evaluate_heading = Direction.NONE;
	
	static int bugging_directional_looks[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	static int non_bugging_directional_looks[] = new int[]{0,1,-1};
	
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
		while(true){
			while(!robot_controller.isCoreReady() && !robot_controller.isWeaponReady())
				robot_controller.yield();
			
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);
									
			if(robot_controller.isCoreReady()){
				set_my_danger_level();
				
				if(read_broadcast(override_saftey) == 1){
					all_out_attack = true;
				}
				
				//if(Clock.getRoundNum() > 1600){
				if(all_out_attack){
					count_down --;
				}else{
					count_down = 25;
				}
				if(count_down < 1){
					danger_levels = new int[]{0,0,0,0,0,0,0,0};	
					my_danger_level = 0;
				}
				if(my_danger_level == 0){
						count_the_troops();
						update_strategy();	
						check_for_builds();							
				}
				
				if(my_danger_level !=0){
					evasive_move();
				} else if(robot_controller.canMine()){
					evaluate_mining_position();
					go_mining();
				} else{
					if(!attack_deadest_enemy_in_range() && robot_controller.isWeaponReady()){
						int location_x = read_broadcast(location_channel_x);
						int location_y = read_broadcast(location_channel_y);
						location = new MapLocation(location_x,location_y);
						//move_towards_direction(robot_controller.getLocation().directionTo(location));				
						reset_simple_bug(location);
						simpleBug();
						applyMove();
					}
				}
			}
			dish_out_supply();
			attack_deadest_enemy_in_range();
			robot_controller.yield();
		}		
	}
		
	public void evasive_move(){
		reset_simple_bug(HQ_location);
		simpleBug();
		applyMove();
	}
	
	public void set_my_danger_level(){
		my_danger_level = 0;
		MapLocation my_location = robot_controller.getLocation();	
		my_danger_level = read_broadcast(static_broadcast_offset + location_channel(my_location));
		set_danger_levels();	
	}
	
	public void set_danger_levels(){
		MapLocation my_location;
		danger_levels = new int[]{0,0,0,0,0,0,0,0};		
		//add HQ and towers as calculated by HQ
		for(int i=0; i< directions.length; i++){
			my_location = robot_controller.getLocation().add(directions[i]);			
				danger_levels[i] = read_broadcast(static_broadcast_offset + location_channel(my_location));
		}
		
		if(sensed_enemy_robots == null)
			return;
		for(int i=0; i<sensed_enemy_robots.length;i++){
			int attack_range = sensed_enemy_robots[i].type.attackRadiusSquared;
			if(sensed_enemy_robots[i].type.equals(RobotType.LAUNCHER) || sensed_enemy_robots[i].type.equals(RobotType.MISSILE)){
				attack_range = 36;
			}
				
			for(int j=0; j<danger_levels.length;j++){
				if(sensed_enemy_robots[i].location.distanceSquaredTo(robot_controller.getLocation().add(directions[j])) <= attack_range ){
					danger_levels[j] += sensed_enemy_robots[i].type.attackPower;
				}
			}
			if(sensed_enemy_robots[i].location.distanceSquaredTo(robot_controller.getLocation()) <= attack_range ){
				my_danger_level += sensed_enemy_robots[i].type.attackPower;
			}					
		}		
	}

	public void go_mining(){
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				try{
					robot_controller.mine();
				}catch(Exception e){
					 print_exception(e);
				}
			}
		}
	}
	public void evaluate_mining_position(){
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 50; i++)
					for (final Direction direction: directions)
						if(my_mining_rate(robot_controller.getLocation().add(direction,i)) > mining_move_threshold){
							move_towards_direction(direction);
								return;
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
			return Math.min(current_ore, Math.max(1,Math.min(mining_max,(current_ore/mining_rate))));
		}
		return 0;
	}
	
	public boolean move_towards_direction(Direction initial_direction){
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
							return true;
						} catch(Exception e){
							print_exception(e);
						}
		return false;
	}		
	
	public Direction simpleBug(){
		if(i_am_not_bugging){
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
		}
	}
	
	public boolean can_move(Direction move_direction){	
		if(terrain_is_impassable(move_direction))
			return false;
		if(space_is_occupied(move_direction))
			return false;
		if(danger_levels[move_direction.ordinal()] > 0)
			return false;
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
