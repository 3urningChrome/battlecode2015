package SprintSubIThink;

import battlecode.common.RobotController;

public class Basher extends Mobile  {

	public Basher(RobotController rc) {
		super(rc);
		location_channel_x  = defence_location_channel_x;
		location_channel_y  = defence_location_channel_y;
		basic_turn_loop();
	}

}
