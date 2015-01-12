package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Launcher extends Mobile {

	static int last_fired = Clock.getRoundNum();

	
	public Launcher(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}

	public boolean role_warfare(){
		//try to be a defender	
		int num_of_bots_in_role = read_broadcast(launcher_tower_protection_current);
		if((num_of_bots_in_role < read_broadcast(launcher_tower_protection_max)) && (assigned_job == 1 || assigned_job == 0)){
			send_broadcast(launcher_tower_protection_current, num_of_bots_in_role+1);		
			assigned_job = 1;
			sticky = false; //prefer enemy units over destination;
			location = find_closest(enemy_HQ_Location, robot_controller.senseTowerLocations());
			int distance = 1;
			Direction hq_direction = location.directionTo(enemy_HQ_Location);
			switch(num_of_bots_in_role){
				case 0:
					location = location.add(hq_direction,distance);						
					break;
				case 1:
					location = location.add(hq_direction.rotateLeft(),distance);
					break;
				case 2:
					location = location.add(hq_direction.rotateRight(),distance);
					break;
				case 3:
					location = location.add(hq_direction,distance).add(hq_direction.rotateLeft().rotateLeft(),2);
					break;
				case 4:
					location = location.add(hq_direction,distance).add(hq_direction.rotateRight().rotateRight(),2);
					break;
				default:
					location = enemy_HQ_Location;
			}
			if(robot_controller.senseTerrainTile(location) == TerrainTile.OFF_MAP)
				location = enemy_HQ_Location;
//			if((robot_controller.getLocation().add(robot_controller.getLocation().directionTo(location)).distanceSquaredTo(location) <= get_attack_radius(RobotType.HQ))){
//				location = robot_controller.getLocation();
//			}			
			return true;
		}
		
		//try to be a harasser	
		num_of_bots_in_role = read_broadcast(launcher_harass_current);
		if((num_of_bots_in_role < read_broadcast(launcher_harass_max)) && (assigned_job == 2 || assigned_job == 0)){
			send_broadcast(launcher_harass_current, num_of_bots_in_role+1);		
			assigned_job = 2;
			sticky = false; //prefer enemy units over destination;
			MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
			
			if(the_towers.length == 0)
				the_towers = new MapLocation[]{enemy_HQ_Location};
			
			location = find_closest(HQ_location,the_towers);
//			if((robot_controller.getLocation().add(robot_controller.getLocation().directionTo(location)).distanceSquaredTo(location) <= get_attack_radius(RobotType.HQ))){
//				location = robot_controller.getLocation();
//			}			
			return true;
		}
		
		//default
		location = new MapLocation(read_broadcast(location_channel_x),read_broadcast(location_channel_y));
		if((robot_controller.getLocation().add(robot_controller.getLocation().directionTo(location)).distanceSquaredTo(location) <= get_attack_radius(RobotType.HQ))){
			location = robot_controller.getLocation();
		}	
		return false;
	}
	public boolean attack_deadest_enemy_in_range(){
		if(Clock.getRoundNum() - last_fired < 2)
			return false;
		int attack_radius = 64;
		RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
		if(close_enemies.length > 0){
			int num_of_loops = close_enemies.length;
			double enemy_health = 9999;
			int attack_pos = 0;
			for(int i=0; i< num_of_loops;i++){
				if(close_enemies[i].health <= enemy_health || close_enemies[i].type == RobotType.MISSILE){
					if(robot_controller.getLocation().distanceSquaredTo(close_enemies[i].location) > 4){
						enemy_health = close_enemies[i].health;
						attack_pos = i;
					}
				}
			}
			try{
				if(my_type.equals(RobotType.LAUNCHER)){
					Direction initial_direction = robot_controller.getLocation().directionTo(close_enemies[attack_pos].location);
							

					if(robot_controller.canLaunch(initial_direction)){
							robot_controller.launchMissile(initial_direction);
							last_fired = Clock.getRoundNum();
							//turn 1
							MapLocation missile_location = robot_controller.getLocation().add(initial_direction);
							Direction direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
							//turn 2
							missile_location = missile_location.add(direction_to_head);
							direction_to_head = missile_location.directionTo(close_enemies[attack_pos].location);
							if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
								direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
							//turn 3
							missile_location = missile_location.add(direction_to_head);
							direction_to_head = missile_location.directionTo(close_enemies[attack_pos].location);
							if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
								direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
							//turn 4
							missile_location = missile_location.add(direction_to_head);
							direction_to_head = missile_location.directionTo(close_enemies[attack_pos].location);
							if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
								direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
							//turn 5
							missile_location = missile_location.add(direction_to_head);
							direction_to_head = missile_location.directionTo(close_enemies[attack_pos].location);
							if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
								direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
							//turn 6
							missile_location = missile_location.add(direction_to_head);
							direction_to_head = missile_location.directionTo(close_enemies[attack_pos].location);
							if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
								direction_to_head = initial_direction;
							send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());							

							return true;
					}
				}
				return true;
			} catch (Exception e){
				 print_exception(e);
			}
		}
		if(my_type.equals(RobotType.LAUNCHER)){
			if(robot_controller.getMissileCount() == GameConstants.MISSILE_MAX_COUNT && Clock.getRoundNum() % 27 == 0){
			Direction initial_direction = robot_controller.getLocation().directionTo(enemy_HQ_Location);
					

			if(robot_controller.canLaunch(initial_direction)){
					try {
						robot_controller.launchMissile(initial_direction);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					last_fired = Clock.getRoundNum();
					//turn 1
					MapLocation missile_location = robot_controller.getLocation().add(initial_direction);
					Direction direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 2
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(enemy_HQ_Location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 3
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(enemy_HQ_Location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 4
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(enemy_HQ_Location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 5
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(enemy_HQ_Location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 6
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(enemy_HQ_Location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());							
			}
			}
		}
		return false;
	}	
}
