package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Mobile {

	static MapLocation destination; 
	public Drone(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
}
