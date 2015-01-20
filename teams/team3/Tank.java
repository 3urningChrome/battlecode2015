package team3;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Tank extends Mobile {

	public Tank(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 0;
		return centre_point;
	}
}
