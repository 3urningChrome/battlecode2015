package team111;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Tank extends Mobile {

	MapLocation return_location = null;
	public Tank(RobotController rc) {
		super(rc);
	}

	public MapLocation get_default_location(){
		aggressive = 0;
		
		MapLocation[] the_towers = robot_controller.senseTowerLocations();
		if(the_towers.length < 1)
			the_towers = new MapLocation[] {HQ_location};
		location =  Utilities.find_closest(enemy_HQ_Location,the_towers).add(HQ_location.directionTo(enemy_HQ_Location),1);
		
//		if(return_location == null){
//			return_location = HQ_location.add(HQ_location.directionTo(enemy_HQ_Location),7);
//			if(robot_controller.senseTerrainTile(return_location).equals(TerrainTile.UNKNOWN)){
//				//return_location =  centre_point;
//			} else{
//			//return_location = centre_point;
//				while(true){
//					if(robot_controller.senseTerrainTile(return_location).equals(TerrainTile.VOID)){
//						return_location = return_location.add(return_location.directionTo(HQ_location),1);
//					} else{
//						break;
//					}
//				}	
//			}
//		}
//		location = return_location;
		read_swarm_location(); //sets location
		find_enemy_to_go_after_if_i_am_aggressive(); //sets location
		return location;
	}
}