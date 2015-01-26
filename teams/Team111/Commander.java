package team111;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Commander extends Mobile {

	public Commander(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		aggressive = 1; 
		if(Clock.getRoundNum() < 1000)
			return enemy_HQ_Location.add(directions[(Clock.getRoundNum()/20)%8],(9));
		return enemy_HQ_Location.add(directions[(Clock.getRoundNum()/20)%8],(16));
	}
	public void decide_action(){
		decided_to_move();
		shoot();
		return;
	}

}
