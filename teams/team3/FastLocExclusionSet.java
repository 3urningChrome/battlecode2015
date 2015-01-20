package team3;

import battlecode.common.*;
public class FastLocExclusionSet {
    private static final int HASH = 99;
    public int size = 0;
    private boolean[][] has = new boolean[HASH][HASH];

    public FastLocExclusionSet(int sq_radius_of_exclusion){
    	if(sq_radius_of_exclusion < 1)
    		return;
    	
    	if(sq_radius_of_exclusion > 49){
    		System.out.println("Too big exclusion zone");
    		sq_radius_of_exclusion = 49;
    	}
		MapLocation[] danger_zones = MapLocation.getAllMapLocationsWithinRadiusSq(new MapLocation(49,49), sq_radius_of_exclusion);
		for(MapLocation danger_point:danger_zones){
			add(danger_point);					
		}
    }
    public void add(MapLocation loc) {
        int x = loc.x;
        int y = loc.y;
        if (!has[x][y]){
            size++;
            has[x][y] = true;
        }
    }

    public boolean contains(MapLocation loc) {
        return has[loc.x][loc.y];
    }
    public boolean contains(MapLocation loc,MapLocation owner_of_zone) {
    	int x = (loc.x - owner_of_zone.x)+40;
    	int y = (loc.y - owner_of_zone.y)+40; 
    	if(x < 0 || y < 0 || x > HASH || y > HASH)
    		return false;
        return has[x][y];
    }
}