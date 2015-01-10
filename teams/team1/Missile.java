package team1;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Missile {

	static int created_on = Clock.getRoundNum();
	static RobotController robot_controller;
	
	public Missile(RobotController rc) {
		robot_controller = rc;
		MapLocation enemy_HQ_location = robot_controller.getLocation();
		
		while(true){		
			//move toward closest enemy.
			//move in direction fired...
			int attack_distance = 5 - (Clock.getRoundNum() - created_on);
			attack_distance *= attack_distance;
			RobotInfo[] nearby_robots = robot_controller.senseNearbyRobots(attack_distance, robot_controller.getTeam().opponent());
			
			int closest_enemy = 9999;
			int pos_closest_enemy = -1;
			Direction enemyDirection = robot_controller.getLocation().directionTo(enemy_HQ_location);
			for(int i=0; i < nearby_robots.length;i++){
				if(robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location) < closest_enemy && !nearby_robots[i].type.equals(RobotType.MISSILE)){
					closest_enemy = robot_controller.getLocation().distanceSquaredTo(nearby_robots[i].location);
					pos_closest_enemy = i;
				}
			}	
			
			if(pos_closest_enemy >= 0){
				enemyDirection = robot_controller.getLocation().directionTo(nearby_robots[pos_closest_enemy].location);
			}
			
			RobotInfo[] friendly_fire = robot_controller.senseNearbyRobots(2, robot_controller.getTeam());
			int friend_count = 0;
			for(RobotInfo friend : friendly_fire){
				if(friend.type != RobotType.MISSILE){
					friend_count +=1;
				}
			}
			
			if(friend_count > 0){
				if ((Clock.getRoundNum() - created_on) == 5){
					robot_controller.disintegrate();
				}
			}else{
				if ((Clock.getRoundNum() - created_on)== 5){
					try{
					robot_controller.explode();
					}catch(Exception e){
						System.out.println("Boom");
					}
				} else{
					if(closest_enemy <= 2){
						try{
						robot_controller.explode();
						}catch(Exception e){
							System.out.println("Boom");
						}
					}
				}
			}
			
			if (robot_controller.canMove(enemyDirection)){
				try{
					robot_controller.move(enemyDirection);
				}catch (Exception e){
					System.out.println("Exception e");
				}
			}

			robot_controller.yield();
		}		
	}
}
