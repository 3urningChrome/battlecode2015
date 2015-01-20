package team3;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Drone extends Mobile {

	public Drone(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 1; 
		return enemy_HQ_Location.add(directions[(Clock.getRoundNum()/20)%8],(9));
	}

}
