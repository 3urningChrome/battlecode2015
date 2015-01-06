package Team111;

import battlecode.common.Direction;
import battlecode.common.RobotController;

public class Mobile extends Arobot {
	
	public Mobile(RobotController rc){
		super(rc);
	}
	
	public void go_mining(){
		if(robot_controller.canMine()){
			if(robot_controller.isCoreReady()){
				try{
					robot_controller.mine();
					//System.out.println("Mined");
				}catch(Exception e){
					 print_exception(e);
				}
			}
		}
	}
	public void evaluate_mining_position(){
		//senseOre(mapLocation)
		if(robot_controller.isCoreReady()){
			if(my_mining_rate(robot_controller.getLocation()) < mining_move_threshold){
				for(int i = 1; i < 50; i++){
					for (final Direction direction: directions){
						if(my_mining_rate(robot_controller.getLocation().add(direction,i)) > mining_move_threshold && robot_controller.canMove(direction)){
							try{
								robot_controller.move(direction);
								return;
							} catch(Exception e){
								print_exception(e);
							}
						}
					}
				}
				mining_move_threshold /=2;
			}
		}
	}
	
}
