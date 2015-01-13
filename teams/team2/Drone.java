package team2;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class Drone extends Mobile {

	static MapLocation destination; 
	public Drone(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public boolean role_warfare(){
		//try to be harasser
		swarm_attack = false;
		int num_of_bots_in_role = read_broadcast(drone_harass_current);
		if((num_of_bots_in_role < read_broadcast(drone_harass_max)) && (assigned_job == 1 || assigned_job == 0)){
			send_broadcast(drone_harass_current, num_of_bots_in_role+1);
			assigned_job = 1;
			sticky = true; //prefer enemy units over destination;
			location = enemy_HQ_Location;
			int distance = (int)(Math.sqrt(get_attack_radius(RobotType.HQ)) + 1);
			switch(num_of_bots_in_role){
				case 0:
					location = enemy_HQ_Location.add(Direction.NORTH,distance);
					break;
				case 1:
					location = enemy_HQ_Location.add(Direction.SOUTH,distance);
					break;
				case 2:
					location = enemy_HQ_Location.add(Direction.EAST,distance);
					break;
				case 3:
					location = enemy_HQ_Location.add(Direction.WEST,distance);
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
		num_of_bots_in_role = read_broadcast(drone_swarm_current);
		if((num_of_bots_in_role < read_broadcast(drone_swarm_max)) && (assigned_job == 2 || assigned_job == 0)){
			num_of_bots_in_role +=1;
			send_broadcast(drone_swarm_current, num_of_bots_in_role);
			assigned_job = 2;
			sticky = true; //prefer enemy units over destination?;
			
			location = centre_point;
			int y_diff = (centre_point.x - enemy_HQ_Location.x)/2;
			int x_diff = (centre_point.y - enemy_HQ_Location.y)/2;			
				
			switch(num_of_bots_in_role % 3){
			case 0:
				location = centre_point;
				break;
			case 1:
				location = location.add(1-x_diff,y_diff);
				break;
			default:
				location = location.add(x_diff,1-y_diff);
				
			}

			return true;
		}		
		//default
		location = new MapLocation(read_broadcast(location_channel_x),read_broadcast(location_channel_y));
		swarm_attack = true;
//		if((robot_controller.getLocation().add(robot_controller.getLocation().directionTo(location)).distanceSquaredTo(location) <= get_attack_radius(RobotType.HQ))){
//			location = robot_controller.getLocation();
//		}	
		return false;
	}
}
