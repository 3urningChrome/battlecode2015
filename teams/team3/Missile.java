package team3;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Missile {

	static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
	static final int orders_broadcast_offset = 3050; 
	static RobotController rc;
	
	Missile(RobotController the_rc) {
		rc = the_rc;
		//turn 1
		//read direction from channel
		int direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
	//	System.out.println("Read Direction: " + Direction.values()[direction]);
		move(Direction.values()[direction]);
		rc.yield();
		//turn 2 - 5
		for(int i =0; i < 4;i++){
			if(rc.isCoreReady()){
				direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
				move(Direction.values()[direction]);
			}
			if(rc.senseNearbyRobots(2, rc.getTeam().opponent()).length > 0){
	//			if(rc.senseNearbyRobots(2, rc.getTeam()).length < 1){
				explode();
				rc.yield();
		//		}
			}
			if(i == 3)
				rc.disintegrate();
			rc.yield();
		}
		explode();
		rc.yield();
	}
	
	public int location_channel(MapLocation encode_this_location){
		
		MapLocation return_location = new MapLocation(((encode_this_location.x % HASH) + HASH) % HASH,((encode_this_location.y % HASH) + HASH) % HASH);
		
		return (return_location.x * HASH) + return_location.y;
	}
	
	public int read_broadcast(int channel){
		try{			
			return rc.readBroadcast(channel);
		} catch (Exception e){
			System.out.println("Exception missile!");
		}		
		return 0;
	}
	
	public void move(Direction direction){
		if (rc.canMove(direction)){
			try{
				rc.move(direction);
			}catch (Exception e){
				 e.printStackTrace();
			}
		}
	}
	
	public void explode(){
		try{
			rc.explode();
		}catch (Exception e){
			System.out.println("Exceptional missiles");
		}
	}
}