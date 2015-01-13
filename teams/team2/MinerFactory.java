package team2;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class MinerFactory extends Building  {

	public MinerFactory(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
}
