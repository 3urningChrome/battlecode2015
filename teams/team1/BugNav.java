package team1;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class BugNav{
	public MapLocation destination;
	public MapLocation my_location;
	
	private Direction current_heading = Direction.NONE;
	private Direction evaluate_heading = Direction.NONE;
	
	static int bugging_directional_looks[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	static int non_bugging_directional_looks[] = new int[]{0,1,-1};
	private final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public boolean i_am_not_bugging = true;
	public boolean i_have_stopped_bugging = false;	
	public boolean i_have_started_bugging = false;
	public boolean complete = false;
	public int bugging_direction = 1;
	public int turns_bugging = 0;
	private int bugging_threshold = 20;
	public int navigator_state = 0; // 0= pre-bugging, 1 = bugging, 2 = post bugging, 3 = reached destination
		
	RobotController robot_controller;
	
	public BugNav(MapLocation the_location,MapLocation the_destination,RobotController theRC){
		my_location = the_location;
		destination = the_destination;
		current_heading = my_location.directionTo(destination);
		robot_controller = theRC;
	}
	
	public Direction get_next_move(){
//in-efficient overlap here if not bugging, then bugging in same 'turn'. tests 0,-1,1 twice	
		if(i_am_not_bugging){
			for(int directionalOffset:non_bugging_directional_looks){
				evaluate_heading = directions[(current_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
				if(can_move(evaluate_heading))
					return evaluate_heading;
			}
			i_have_started_bugging = true;	
			navigator_state = 1;
		}
		i_am_not_bugging = false;
		turns_bugging++;
		for(int directionalOffset:bugging_directional_looks){
			evaluate_heading = directions[(current_heading.ordinal()+(directionalOffset*bugging_direction)+8)%8];
			if(can_move(evaluate_heading))
				return evaluate_heading;
		}		
		evaluate_heading = Direction.NONE;
		return evaluate_heading;
	}	

	public void applyMove(){
		current_heading = evaluate_heading;
		my_location = my_location.add(current_heading);
		if(my_location.equals(destination)){
			complete = true;
		}
			
		i_have_started_bugging = false;
		i_have_stopped_bugging = false;
		
		if(i_am_not_bugging){
			current_heading = my_location.directionTo(destination);
		} else{
			if (current_heading.equals(my_location.directionTo(destination))){
				i_am_not_bugging = true;
				i_have_stopped_bugging = true;				
				navigator_state = 2;
				turns_bugging = 0;
			}
			if(turns_bugging > bugging_threshold)
				bugging_direction *= -1;
		}
		if(complete)
			navigator_state = 3;
	}
	
	public boolean can_move(Direction move_direction){	
		if(terrain_is_impassable(move_direction))
			return false;
		if(space_is_occupied(move_direction))
			return false;
		return true;
	}

	public boolean terrain_is_impassable(Direction move_direction){
		MapLocation next_move = my_location.add(move_direction);
		TerrainTile test_terrain = robot_controller.senseTerrainTile(next_move);
		if(test_terrain.equals(TerrainTile.NORMAL))
			return false;
		if(test_terrain.equals(TerrainTile.OFF_MAP))
			return true;
		if(test_terrain.equals(TerrainTile.VOID) && (robot_controller.getType().equals(RobotType.DRONE) || robot_controller.getType().equals(RobotType.MISSILE)))
			return false;
		if(test_terrain.equals(TerrainTile.VOID))
			return true;
		
		return false;
	}
	
	//Not even sure if this should be checked here....
	public boolean space_is_occupied(Direction move_direction){
		try{
			return robot_controller.isLocationOccupied(my_location.add(move_direction));
		}catch(Exception e){
			System.out.println("Exception in bug space_is_occupied");
		}
		return false;
	}	
	
	public void set_bugging_threshold(int new_threshold){
		bugging_threshold = new_threshold;
	}
	
	public void bug_clockwise(){
		bugging_direction = 1;	
	}
	public void bug_anti_clockwise(){
		bugging_direction = -1;
	}
	public void flip_bugging_wall(){
		bugging_direction *= -1;
	}
	
	public boolean has_arrived_at_destination(){
		if(my_location.equals(destination))
			return true;
		return false;
	}
	
	public MapLocation current_evaluation_location(){
		return my_location;
	}
}