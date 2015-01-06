package Team111;

import battlecode.common.RobotController;

public class Drone extends Mobile {

	public Drone(RobotController rc) {
		super(rc);
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			attack_random_enemy_in_range();
			
			robot_controller.yield();
			
		}
	}

}
