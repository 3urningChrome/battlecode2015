package Team111;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad extends Building  {
	
	public Helipad(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			basic_building_tasks();			
			spawn_drone();
			robot_controller.yield();
		}		
	}	
	
	public void spawn_drone(){
		count_the_troops();
		if(numDrones >= max_drones)
			return;
		
		spawn_robot(RobotType.DRONE);
	}

}
