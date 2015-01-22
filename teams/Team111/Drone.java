package team111;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Drone extends Mobile {

	public Drone(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 0; 
		if(Clock.getRoundNum() < 1000)
		return enemy_HQ_Location.add(directions[(Clock.getRoundNum()/20)%8],(9));
		
		return enemy_HQ_Location.add(directions[(Clock.getRoundNum()/20)%8],(16));
	}

}
