package team111;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public abstract class Utilities {

	public Utilities() {
		// TODO Auto-generated constructor stub
	}
	
	public static MapLocation find_closest_robot(MapLocation starting_point, RobotInfo[] robots){
		//if there are enemies around, and I'm sticky, head for closest one.
		MapLocation[] enemy_positions = new MapLocation[robots.length];
		int num_of_loops = robots.length;
		for(int i=0; i<num_of_loops;i++){
			enemy_positions[i] = robots[i].location;
		}
		return find_closest(starting_point, enemy_positions);
	}
	
	public static MapLocation find_closest(MapLocation starting_point, MapLocation[] possible_locations){
		int closest_distance = 99999;
		int pos_of_closest = 0;
		
		if(possible_locations == null)
			return starting_point;
		
		if(possible_locations.length < 1){
			return starting_point;
		}
		
		for(int i=0; i< possible_locations.length;i++){
			if(starting_point.distanceSquaredTo(possible_locations[i]) < closest_distance){
				closest_distance = starting_point.distanceSquaredTo(possible_locations[i]);
				pos_of_closest = i;
			}
		}
		return possible_locations[pos_of_closest];
	}
	
	public static void print_exception(Exception e){
        System.out.println("Unexpected exception");
        e.printStackTrace();
	}
	public static int increase_attack_radius(int attack_radius, int i) {
		if (i==0)
			return attack_radius;
		
		double root = Math.sqrt(attack_radius);
		root = (int)(root+ 1);
		return (int)Math.pow((root + i),2);
	}
	
	public static FastLocSet process_squares_in_range(MapLocation start_point, int radius, boolean remove, FastLocSet excluded_locations){
		MapLocation[] danger_zones = MapLocation.getAllMapLocationsWithinRadiusSq(start_point, radius);
		for(MapLocation danger_point:danger_zones){
			if(remove){
				excluded_locations.remove(danger_point);			
			} else{
				excluded_locations.add(danger_point);					
			}
		}
		return excluded_locations;
	}
	
	public static boolean inside_radius(MapLocation circle_centre, int radiusSquared, MapLocation test_point){
		if(((test_point.x - circle_centre.x)^2 + (test_point.y - circle_centre.y)^2) < radiusSquared)
			return true;
		return false;
	}
	
	public static void print_byte_usage(String msg){
		boolean print = true;
		if(print){
			System.out.println(msg + Clock.getBytecodeNum());
		}
	}

}
