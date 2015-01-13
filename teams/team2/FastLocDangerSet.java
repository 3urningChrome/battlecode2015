package team2;

import battlecode.common.*;
public class FastLocDangerSet {
    private static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
    private static final int MAX_DANGER = 999;
    public int size = 0;
    private boolean[][] has = new boolean[HASH][HASH];
    private int[][] danger_level = new int[HASH][HASH];

    public void add(MapLocation loc, int danger_amount) {
        int x = ((loc.x % HASH) + HASH) % HASH;
        int y = ((loc.y % HASH) + HASH) % HASH;

        int dl = danger_amount;
        if (dl > MAX_DANGER)
        	dl = MAX_DANGER;
        
        if (!has[x][y]){
            size++;
            has[x][y] = true;
        }
        danger_level[x][y] += dl;
    }

    public void remove(MapLocation loc) {
        int x = ((loc.x % HASH) + HASH) % HASH;
        int y = ((loc.y % HASH) + HASH) % HASH;
        if (has[x][y]){
            size--;
            has[x][y] = false;
        }
    }

    public boolean contains(MapLocation loc) {
        return has[((loc.x % HASH) + HASH) % HASH][((loc.y % HASH) + HASH) % HASH];
    }
    public int get_danger_level(MapLocation loc) {
        if(has[((loc.x % HASH) + HASH) % HASH][((loc.y % HASH) + HASH) % HASH])
        	return danger_level[((loc.x % HASH) + HASH) % HASH][((loc.y % HASH) + HASH) % HASH];
        
        return 0;
    }
    public boolean contains(int x, int y) {
        return has[x][y];
    }
    public int get_danger_level(int x, int y) {
        if(has[x][y])
        	return danger_level[x][y];
        
        return 0;
    }

    public void clear() {
        has = new boolean[HASH][HASH];
        danger_level = new int[HASH][HASH];
        size = 0;
    }   
}