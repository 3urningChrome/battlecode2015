package SeedingSubmission;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class Tank extends Mobile {

	MapLocation return_location = null;
	public Tank(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 1;
		if(return_location != null)
			return return_location;
		if(robot_controller.senseTerrainTile(centre_point).equals(TerrainTile.UNKNOWN))
			return centre_point;
		MapLocation return_location = centre_point;
		while(true){
			if(robot_controller.senseTerrainTile(return_location).equals(TerrainTile.VOID)){
				return_location = return_location.add(return_location.directionTo(HQ_location),1);
			} else{
				return return_location;
			}
		}
	}
}
