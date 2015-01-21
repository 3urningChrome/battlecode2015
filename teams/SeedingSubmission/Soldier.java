package SeedingSubmission;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Soldier extends Mobile{

	public Soldier(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 0; 
		return HQ_location.add(directions[((Clock.getRoundNum()/40)+ robot_controller.getID())%8],(11));
	}
}
