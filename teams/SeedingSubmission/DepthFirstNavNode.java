package SeedingSubmission;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DepthFirstNavNode{
  MapLocation start_location;
  MapLocation end_location;
  MapLocation bugging_start_location;
  
  BugNav navigate;
  
  DepthFirstNavNode leftNode = null;
  DepthFirstNavNode rightNode = null;
  DepthFirstNavNode finalNode = null;
  
  Direction next_move_direction;
  
  int total_left_distance = 0;
  int total_right_distance = 0;
  int distance_to_left_node = 0;
  int distance_to_right_node = 0;
  int final_distance = 0;  
  int distance_to_node = 0;
  
  boolean complete = false;
  
  RobotController robot_controller;
  
  public DepthFirstNavNode(MapLocation new_start, MapLocation new_end, RobotController new_rc){
	robot_controller = new_rc;
	start_location = new_start;
	end_location = new_end;
	navigate = new BugNav(start_location, end_location,robot_controller);
  }
  
  public boolean evaluate_next_move(){
    if(leftNode != null && !leftNode.complete && leftNode.finalNode != null)
		return leftNode.evaluate_next_move();
		
	if(rightNode != null && !rightNode.complete && rightNode.finalNode != null)
		return rightNode.evaluate_next_move();
		
	switch(navigate.navigator_state){
		case 0: //Pre-Bugging
			return pre_bugging_move();
		case 1: //Bugging
			return bugging_move();
		case 3: //Post Bugging
			return post_bugging_move();
		case 4: //Complete
			return navigation_complete();
		default:
			break;
	}
	return false;
  }
  
  public boolean pre_bugging_move(){
  	next_move_direction = navigate.get_next_move();
	if(navigate.i_am_not_bugging){
		navigate.applyMove();
		distance_to_node+=1;
	} else{
		bugging_start_location = navigate.current_evaluation_location();			
	}
	return false;
  }
  
  public boolean bugging_move(){
	next_move_direction = navigate.get_next_move();
	navigate.applyMove();
	distance_to_node+=1;
	return false;
  }
  
  public boolean post_bugging_move(){
	DepthFirstNavNode new_node = new DepthFirstNavNode(navigate.current_evaluation_location(), end_location, robot_controller);
	if(finalNode != null){
		new_node.finalNode = finalNode;
//		int buggingDir = navigate.bugging_direction;
		navigate = new BugNav(start_location, finalNode.start_location, robot_controller);  		 					
	}
	if(leftNode == null){
		leftNode = new_node;
		distance_to_left_node = distance_to_node;
		distance_to_node = 0;
		navigate = new BugNav(bugging_start_location, end_location, robot_controller);  		 				
		navigate.bug_anti_clockwise();
	}else if (rightNode == null){
		rightNode = new_node;
		distance_to_right_node = distance_to_node;
		distance_to_node = 0;		
		set_final_node();
	} else{
		//should this really happen?
	}
	return false;
  }
  
  public boolean navigation_complete(){
	final_distance = navigate.turns_bugging;
	return true;
  }
  
  public void set_final_node(){
	if(leftNode == null)
		finalNode = rightNode;
	
	if(rightNode == null)
		finalNode = leftNode;
		
	total_left_distance = distance_to_left_node + leftNode.total_left_distance;
	total_right_distance = distance_to_right_node + rightNode.total_right_distance;
  	
	if(total_left_distance < total_right_distance){
		finalNode = leftNode;
		navigate = new BugNav(start_location, finalNode.start_location, robot_controller);  		 				
	} else{
		finalNode = rightNode;
		navigate = new BugNav(start_location, finalNode.start_location, robot_controller);  		 				
		navigate.bug_anti_clockwise();
	}
  }
}
  
  //set start and end.
  //set off bug nav.
  //if bugnav starts bugging
	//create left and right bugnav instead from point of bugging start
  //if bugnav(l/r) is at destination, 
		//run the other one.
		// if both are done, this node is complete
  //if bugnav(L/R) stops bugging
		//create child node. 
		//set my destination to child node start
  
  
 