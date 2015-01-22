package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Mobile {

	static int last_fired = Clock.getRoundNum();

	public Launcher(RobotController rc) {
		super(rc);
	}
		
	public MapLocation get_default_location(){
		aggressive = 0;
		MapLocation[] the_towers = robot_controller.senseTowerLocations();
		if(the_towers.length < 1)
			the_towers = new MapLocation[] {HQ_location};
		return Utilities.find_closest(enemy_HQ_Location,the_towers).add(HQ_location.directionTo(enemy_HQ_Location),1);
	}

	public boolean attack_deadest_enemy_in_range(){
		if(Clock.getRoundNum()%2 ==0)
			return false;
		int attack_pos = 0;
		int attack_radius = 63;
		RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
		if(close_enemies.length < 1)
			return false;
		if(close_enemies.length > 0){
			int num_of_loops = close_enemies.length;
			double enemy_health = 9999;

			for(int i=0; i< num_of_loops;i++){
			//	if(close_enemies[i].health <= enemy_health || close_enemies[i].type == RobotType.MISSILE){
				if(close_enemies[i].health <= enemy_health && close_enemies[i].type != RobotType.MISSILE){
					if(robot_controller.getLocation().distanceSquaredTo(close_enemies[i].location) > 4){
						enemy_health = close_enemies[i].health;
						attack_pos = i;
					}
				}
			}
		}
		MapLocation target_location = location;
		if(close_enemies.length > 0){
			target_location = close_enemies[attack_pos].location;
		}
		if(robot_controller.getLocation().distanceSquaredTo(target_location) > 63)
			return false;
		try{
			Direction initial_direction = robot_controller.getLocation().directionTo(target_location);
			if(robot_controller.canLaunch(initial_direction)){
				robot_controller.launchMissile(initial_direction);
				last_fired = Clock.getRoundNum();
				//turn 1
				MapLocation missile_location = robot_controller.getLocation().add(initial_direction);
				Direction direction_to_head = missile_location.directionTo(target_location);
		//		System.out.println("Turn1: " + direction_to_head);
				send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
				//turn 2
				missile_location = missile_location.add(direction_to_head);
				direction_to_head = missile_location.directionTo(target_location);
				if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
					direction_to_head = initial_direction;
		//		System.out.println("Turn2: " + direction_to_head);
				send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
				//turn 3
				missile_location = missile_location.add(direction_to_head);
				direction_to_head = missile_location.directionTo(target_location);
				if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
					direction_to_head = initial_direction;
			//	System.out.println("Turn3: " + direction_to_head);
				send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
				//turn 4
				missile_location = missile_location.add(direction_to_head);
				direction_to_head = missile_location.directionTo(target_location);
				if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
					direction_to_head = initial_direction;
			//	System.out.println("Turn4: " + direction_to_head);
				send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
				//turn 5
				missile_location = missile_location.add(direction_to_head);
				direction_to_head = missile_location.directionTo(target_location);
				if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
					direction_to_head = initial_direction;
			//	System.out.println("Turn5: " + direction_to_head);
				send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
			}
				initial_direction = initial_direction.rotateLeft();
				if(robot_controller.canLaunch(initial_direction)){
					robot_controller.launchMissile(initial_direction);
					last_fired = Clock.getRoundNum();
					//turn 1
					MapLocation  missile_location = robot_controller.getLocation().add(initial_direction);
					Direction direction_to_head = missile_location.directionTo(target_location);
			//		System.out.println("Turn1: " + direction_to_head);
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 2
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(target_location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
			//		System.out.println("Turn2: " + direction_to_head);
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 3
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(target_location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
				//	System.out.println("Turn3: " + direction_to_head);
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 4
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(target_location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
				//	System.out.println("Turn4: " + direction_to_head);
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
					//turn 5
					missile_location = missile_location.add(direction_to_head);
					direction_to_head = missile_location.directionTo(target_location);
					if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
						direction_to_head = initial_direction;
				//	System.out.println("Turn5: " + direction_to_head);
					send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());	
				}
					initial_direction= initial_direction.rotateRight().rotateRight();
					if(robot_controller.canLaunch(initial_direction)){
						robot_controller.launchMissile(initial_direction);
						last_fired = Clock.getRoundNum();
						//turn 1
						MapLocation missile_location = robot_controller.getLocation().add(initial_direction);
						Direction direction_to_head = missile_location.directionTo(target_location);
				//		System.out.println("Turn1: " + direction_to_head);
						send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
						//turn 2
						missile_location = missile_location.add(direction_to_head);
						direction_to_head = missile_location.directionTo(target_location);
						if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
							direction_to_head = initial_direction;
				//		System.out.println("Turn2: " + direction_to_head);
						send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
						//turn 3
						missile_location = missile_location.add(direction_to_head);
						direction_to_head = missile_location.directionTo(target_location);
						if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
							direction_to_head = initial_direction;
					//	System.out.println("Turn3: " + direction_to_head);
						send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
						//turn 4
						missile_location = missile_location.add(direction_to_head);
						direction_to_head = missile_location.directionTo(target_location);
						if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
							direction_to_head = initial_direction;
					//	System.out.println("Turn4: " + direction_to_head);
						send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());
						//turn 5
						missile_location = missile_location.add(direction_to_head);
						direction_to_head = missile_location.directionTo(target_location);
						if(direction_to_head.equals(Direction.OMNI) || direction_to_head.equals(Direction.NONE))
							direction_to_head = initial_direction;
					//	System.out.println("Turn5: " + direction_to_head);
						send_broadcast(orders_broadcast_offset + location_channel(missile_location), direction_to_head.ordinal());		
					}
			} catch (Exception e){
				Utilities.print_exception(e);
			}
		return false;
	}	
}
