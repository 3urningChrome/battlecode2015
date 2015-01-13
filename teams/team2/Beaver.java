package team2;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Beaver extends Mobile {

	static int miner_factory_helipad_trigger = 1;
	
	public Beaver(RobotController rc) {
		super(rc);
		mining_rate = GameConstants.BEAVER_MINE_RATE;
		mining_max = GameConstants.BEAVER_MINE_MAX;		
		
		basic_turn_loop();
	}
	public void evaluate_mining_position(){
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 1; i++)
					for (final Direction direction: directions){
						MapLocation test_location = robot_controller.getLocation().add(direction,i);
						if(my_mining_rate(test_location) > mining_move_threshold){
							try{
							if(robot_controller.canSenseLocation(test_location) && !robot_controller.isLocationOccupied(test_location)){
//								move_towards_direction(direction);
								location = test_location;
								if(!navigator.myDestination.equals(location) || Clock.getRoundNum() - turn_nav_calculated > 100){
									turn_nav_calculated =Clock.getRoundNum();
									navigator = new RouteNode(robot_controller.getLocation(),location,robot_controller,3);
									navigator.process(robot_controller.getLocation());
								}
								move_towards_direction(robot_controller.getLocation().directionTo(navigator.getNextLocation()));
									return;
							}
							} catch(Exception e){
								print_exception(e);
							}
						}
					}
				mining_move_threshold /=2;
			}
		}
	}
	
}
