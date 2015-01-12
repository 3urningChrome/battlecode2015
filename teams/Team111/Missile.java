package team111;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Missile {

	static final int HASH = Math.max(GameConstants.MAP_MAX_WIDTH,GameConstants.MAP_MAX_HEIGHT);
	static final int orders_broadcast_offset = 200 + (HASH * HASH);
	static RobotController rc;
	
	Missile(RobotController the_rc) {
		rc = the_rc;
		//turn 1
		//read direction from channel
		int direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		rc.yield();
		//turn 2
		//read direction otherwise, carry on
		if(rc.isCoreReady()){
		direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		if(rc.senseNearbyRobots(2, rc.getTeam().opponent()).length > 0){
			if(rc.senseNearbyRobots(2, rc.getTeam()).length < 1){
				explode();
			}
		}
		}
		rc.yield();
		//turn 3
		//read direction otherwise, carry on
		if(rc.isCoreReady()){
		direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		if(rc.senseNearbyRobots(2, rc.getTeam().opponent()).length > 0){
			if(rc.senseNearbyRobots(2, rc.getTeam()).length < 1){
				explode();
			}
		}		
		}
		rc.yield();
		//turn 4
		//read direction otherwise, carry on	
		if(rc.isCoreReady()){
		direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		if(rc.senseNearbyRobots(2, rc.getTeam().opponent()).length > 0){
			if(rc.senseNearbyRobots(2, rc.getTeam()).length < 1){
				explode();
			}
		}		
		}
		rc.yield();
		//turn 5
		//read direction otherwise, carry on
		if(rc.isCoreReady()){
		direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		if(rc.senseNearbyRobots(2, rc.getTeam().opponent()).length > 0){
			if(rc.senseNearbyRobots(2, rc.getTeam()).length < 1){
				explode();
			}
		}		
		}
		rc.yield();
		//turn 6
		//read direction otherwise, carry on		
		direction = read_broadcast(orders_broadcast_offset + location_channel(rc.getLocation()));
		move(Direction.values()[direction]);
		if(rc.senseNearbyRobots(2, rc.getTeam()).length > 0){
			rc.disintegrate();;
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
//	int created_on = Clock.getRoundNum();
//	RobotController robot_controller;
//	
//	public Missile(RobotController rc) {
//		robot_controller = rc;
//		MapLocation enemy_HQ_location = robot_controller.getLocation();
//		
//
//		//move toward closest enemy.
//		//move in direction fired...
//		int attack_distance = 6 - (Clock.getRoundNum() - created_on);
//		attack_distance *= attack_distance;
//		RobotInfo[] nearby_robots = robot_controller.senseNearbyRobots(attack_distance, robot_controller.getTeam().opponent());
//System.out.println("Missile Time1: " + Clock.getBytecodeNum() + " created_on:" + created_on);			
//		int closest_enemy = 9999;
//		int pos_closest_enemy = -1;
//		Direction enemyDirection = robot_controller.getLocation().directionTo(enemy_HQ_location);
//		for(int i=0; i < nearby_robots.length;i++){
//			if(robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location) < closest_enemy && !nearby_robots[i].type.equals(RobotType.MISSILE)){
//				closest_enemy = robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location);
//				pos_closest_enemy = i;
//			}
//		}	
//		System.out.println("Missile Time2: " + Clock.getBytecodeNum() + " created_on:" + created_on );			
//		if(pos_closest_enemy >= 0){
//			enemyDirection = robot_controller.getLocation().directionTo(nearby_robots[pos_closest_enemy].location);
//		}
//		
//
//		if (robot_controller.canMove(enemyDirection)){
//			try{
//				robot_controller.move(enemyDirection);
//			}catch (Exception e){
//				System.out.println("Exception e");
//			}
//		}
//
//		robot_controller.yield();
//			
//		while(true){		
//			//move toward closest enemy.
//			//move in direction fired...
//			attack_distance = 6 - (Clock.getRoundNum() - created_on);
//			attack_distance *= attack_distance;
//			nearby_robots = robot_controller.senseNearbyRobots(attack_distance, robot_controller.getTeam().opponent());
//System.out.println("Missile Time1: " + Clock.getBytecodeNum() + " created_on:" + created_on);			
//			closest_enemy = 9999;
//			pos_closest_enemy = -1;
//			enemyDirection = robot_controller.getLocation().directionTo(enemy_HQ_location);
//			for(int i=0; i < nearby_robots.length;i++){
//				if(robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location) < closest_enemy && !nearby_robots[i].type.equals(RobotType.MISSILE)){
//					closest_enemy = robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location);
//					pos_closest_enemy = i;
//				}
//			}	
//			System.out.println("Missile Time2: " + Clock.getBytecodeNum() + " created_on:" + created_on );			
//			if(pos_closest_enemy >= 0){
//				enemyDirection = robot_controller.getLocation().directionTo(nearby_robots[pos_closest_enemy].location);
//			}
//			
//			RobotInfo[] friendly_fire = robot_controller.senseNearbyRobots(2, robot_controller.getTeam());
//			int friend_count = 0;
//			for(RobotInfo friend : friendly_fire){
//				if(friend.type != RobotType.MISSILE){
//					friend_count +=1;
//				}
//			}
//			System.out.println("Missile Time3: " + Clock.getBytecodeNum() + " created_on:" + created_on );			
//			
//			if(friend_count > 0){
//				if ((Clock.getRoundNum() - created_on) == 5){
//					robot_controller.disintegrate();
//				}
//			}else{
//				if ((Clock.getRoundNum() - created_on)== 5){
//					try{
//					robot_controller.explode();
//					}catch(Exception e){
//						System.out.println("Boom");
//					}
//				} else{
//					if(closest_enemy <= 2){
//						try{
//						robot_controller.explode();
//						}catch(Exception e){
//							System.out.println("Boom");
//						}
//					}
//				}
//			}
//			System.out.println("Missile Time4: " + Clock.getBytecodeNum() + " created_on:" + created_on );			
//			
//			if (robot_controller.canMove(enemyDirection)){
//				try{
//					robot_controller.move(enemyDirection);
//				}catch (Exception e){
//					System.out.println("Exception e");
//				}
//			}
//
//			robot_controller.yield();
//		}		
//	}

