package Team111;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {


	
	public HQ(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			basic_building_tasks();
			spawn_beaver();
			robot_controller.yield();
		}		
	}

	public void spawn_beaver(){
		count_the_troops();
		if(numBeavers >= max_beavers)
			return;
		spawn_robot(RobotType.BEAVER);
	}
}
