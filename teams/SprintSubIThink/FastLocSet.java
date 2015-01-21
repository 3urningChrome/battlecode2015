package SprintSubIThink;

import battlecode.common.*;
public class FastLocSet {
    private static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
    public int size = 0;
    private boolean[][] has = new boolean[HASH][HASH];

    public void add(MapLocation loc) {
        int x = loc.x % HASH;
        int y = loc.y % HASH;
        if (!has[x][y]){
            size++;
            has[x][y] = true;
        }
    }
    public void add(String locStr) {
        int x = (int)locStr.charAt(0) % HASH;
        int y = (int)locStr.charAt(1) % HASH;
        if (!has[x][y]){
            size++;
            has[x][y] = true;
        }
    }
    public void remove(MapLocation loc) {
        int x = loc.x % HASH;
        int y = loc.y % HASH;
        if (has[x][y]){
            size--;
            has[x][y] = false;
        }
    }

    public boolean contains(MapLocation loc) {
        return has[loc.x % HASH][loc.y % HASH];
    }

    public void clear() {
        has = new boolean[HASH][HASH];
        size = 0;
    }
    public void appendNewData(String newData){
        int appendSize = (int)(newData.length()/2);
        for(int i=0; i<appendSize;i++){
            String theSubString = newData.substring(i*2,i*2+2);
            if(theSubString == "^^")
                break;
            add(theSubString);
        }
    }     
    public String extractData(){
        StringBuilder returnString = new StringBuilder("");
        for(int x=0;x<HASH;x++){
            for(int y=0;y<HASH;y++){
                if(has[x][y])
                    returnString.append("" +(char) x + (char) y);
            }
        }
        return returnString.toString();
    }
}