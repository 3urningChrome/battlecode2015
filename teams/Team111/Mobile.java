package Team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Mobile extends Arobot {
	
	int[] danger_levels = {0,0,0,0,0,0,0,0};
	int my_danger_level = 0;
	RobotInfo[] known_enemies;
	
	public Mobile(RobotController rc){
		super(rc);
	}
	
	public void basic_turn_loop(){
		while(true){
			known_enemies = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);
			attack_random_enemy_in_range();
			if(Clock.getRoundNum()%5 == 0){
				count_the_troops();
				update_strategy();	
			}
			set_my_danger_level();
			
			if(my_danger_level == 0)
				check_for_builds();	
						
			if(robot_controller.isCoreReady()){
				//set_danger_levels();
				if(my_danger_level !=0)
					evasive_move();
			}

			if(robot_controller.canMine() && my_danger_level == 0){
				evaluate_mining_position();
				go_mining();
			}
			robot_controller.yield();
		}		
	}
	
	public void evasive_move(){
		move_towards_direction(robot_controller.getLocation().directionTo(robot_controller.senseHQLocation()));
	}
	
	public void set_my_danger_level(){
		my_danger_level = 0;
		MapLocation my_location = robot_controller.getLocation();
		my_location = new MapLocation(((my_location.x % HASH) + HASH) % HASH,((my_location.y % HASH) + HASH) % HASH);		
		try{
			my_danger_level = robot_controller.readBroadcast(static_broadcast_offset + (my_location.x * GameConstants.MAP_MAX_WIDTH ) + my_location.y);
		} catch (Exception e){
			print_exception(e);
		}				
	}
	
	public void set_danger_levels(){
		MapLocation my_location;
		danger_levels = new int[]{0,0,0,0,0,0,0,0};		
		//add HQ and towers as calculated by HQ
		for(int i=0; i< directions.length; i++){
			my_location = robot_controller.getLocation().add(directions[i]);
			my_location = new MapLocation(((my_location.x % HASH) + HASH) % HASH,((my_location.y % HASH) + HASH) % HASH);				
			try{
				danger_levels[i] = robot_controller.readBroadcast(static_broadcast_offset + (my_location.x * GameConstants.MAP_MAX_WIDTH ) + my_location.y);
			} catch (Exception e){
				print_exception(e);
			}
		}
		
		if(known_enemies == null)
			return;
		for(int i=0; i<known_enemies.length;i++){
			if(!known_enemies[i].type.equals(RobotType.TOWER)){
				for(int j=0; j<danger_levels.length;j++){
					if(known_enemies[i].location.distanceSquaredTo(robot_controller.getLocation().add(directions[j])) <= known_enemies[i].type.attackRadiusSquared){
						danger_levels[j] += known_enemies[i].type.attackPower;
					}
				}
			}		
		}		
	}
	public void set_danger_level(Direction direction){
		MapLocation my_location;
		
		danger_levels[direction.ordinal()] = 0;	
		//add HQ and towers as calculated by HQ
		my_location = robot_controller.getLocation().add(direction);
		my_location = new MapLocation(((my_location.x % HASH) + HASH) % HASH,((my_location.y % HASH) + HASH) % HASH);				
		try{
			danger_levels[direction.ordinal()] = robot_controller.readBroadcast(static_broadcast_offset + (my_location.x * GameConstants.MAP_MAX_WIDTH ) + my_location.y);
		} catch (Exception e){
			print_exception(e);
		}
		
		for(int i=0; i<known_enemies.length;i++){
			if(!known_enemies[i].type.equals(RobotType.TOWER)){
				if(known_enemies[i].location.distanceSquaredTo(robot_controller.getLocation().add(direction)) <= known_enemies[i].type.attackRadiusSquared)
					danger_levels[direction.ordinal()] += known_enemies[i].type.attackPower;
			}		
		}		
	}
	
	public void go_mining(){
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				try{
					robot_controller.mine();
					//System.out.println("Mined");
				}catch(Exception e){
					 print_exception(e);
				}
			}
		}
	}
	public void evaluate_mining_position(){
		//senseOre(mapLocation)
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 50; i++){
					for (final Direction direction: directions){
						if(my_mining_rate(robot_controller.getLocation().add(direction,i)) > mining_move_threshold){
							if (move_towards_direction(direction))
								return;
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
			return Math.min(current_ore, Math.max(1,Math.min(mining_max,(current_ore/mining_rate))));
		}
		return 0;
	}
	
	public boolean move_towards_direction(Direction initial_direction){
		for(int i=0; i<8;i++){
			int test_direction = ((initial_direction.ordinal() + directional_looks[i] + 8)%8);
			if(danger_levels[test_direction] == 0){
				return move(directions[test_direction]);
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
	
}
