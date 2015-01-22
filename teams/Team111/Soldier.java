package team111;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Soldier extends Mobile{

	public Soldier(RobotController rc) {
		super(rc);
	}
	public MapLocation get_default_location(){
		if(Clock.getRoundNum() > 800){
			location =  HQ_location.add(directions[((Clock.getRoundNum()/40)+ robot_controller.getID())%8],(20));
			aggressive = 1; 
		} else{	
			location =  HQ_location.add(directions[((Clock.getRoundNum()/40)+ robot_controller.getID())%8],(11));
			aggressive = 0; 
		}
		read_swarm_location(); //sets location
		find_enemy_to_go_after_if_i_am_aggressive(); //sets location
		return location;
	}
}
