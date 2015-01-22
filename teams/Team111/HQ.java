package team111;

import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {
		
	//generic Roles
	static final int ROLE_START_POS = 0;
	static int count_down = 20;
	static int previous_num_of_towers = 0;
	static int swarm_trigger = 0;

	
	public HQ(RobotController rc) {
		super(rc);	
	}
	
	public void basic_turn_loop(){
		update_strategy();				
		check_for_spawns();	
		
		robot_controller.yield();
		
		while(true){
			attack_deadest_enemy_in_range();
			perform_a_troop_census();
			update_strategy();		
			check_for_spawns();	
			broadcast_swarm_point(set_swarm_point(robot_controller.senseEnemyTowerLocations()));
			dish_out_supply();		
			robot_controller.yield();
		}		
	}
	
	public void broadcast_swarm_point(MapLocation swarm_location){
		send_broadcast(swarm_location_channel_x, swarm_location.x);
		send_broadcast(swarm_location_channel_y, swarm_location.y);
	}
	
	public MapLocation set_swarm_point(MapLocation [] the_towers){

		MapLocation return_location = new MapLocation(0,0);
		int total_fighting_robots = robot_census[RobotType.BASHER.ordinal()] + robot_census[RobotType.COMMANDER.ordinal()] + robot_census[RobotType.DRONE.ordinal()] + robot_census[RobotType.SOLDIER.ordinal()] + (robot_census[RobotType.TANK.ordinal()]*3);
		//	total_fighting_robots += robot_census[RobotType.LAUNCHER.ordinal()];
		
		//cry for help, only respond if not swarming
		int my_orders = read_broadcast(orders_broadcast_offset + location_channel(HQ_location));
		if (my_orders !=0 && swarm_trigger == 0){		
			try{
				send_broadcast(orders_broadcast_offset + location_channel(HQ_location),0);
				if(robot_controller.canSenseRobot(my_orders)){
					return robot_controller.senseRobot(my_orders).location;
				}
			} catch(Exception e){
				Utilities.print_exception(e);
			}
		}
				
		int num_of_towers = the_towers.length;
		if(num_of_towers != previous_num_of_towers)
			count_down = 15;
		
		previous_num_of_towers = num_of_towers;
		int swarm_attack = Math.max((num_of_towers * 10),30);
		int swarm_retreat = Math.max(num_of_towers * 4, 15);
		
		//not many bots. lets stop swarming if we are.
		if((total_fighting_robots < swarm_retreat && swarm_trigger != 0)){
			swarm_trigger = 0;
			//location = centre_point;
			return return_location;
		}

		//lots of bots, lets kill (or carry on killing) stuff!
		//if(total_fighting_robots > swarm_attack || swarm_trigger != 0  || Clock.getRoundNum() > 1800 ){
		if(Clock.getRoundNum() > 1750){
			if(the_towers.length < 1)
				the_towers = new MapLocation[] {enemy_HQ_Location};
			return_location = Utilities.find_closest(HQ_location,the_towers);
//			RobotInfo[] my_troops = robot_controller.senseNearbyRobots(return_location, 100,my_team);
//			if(my_troops.length < 8){
//				return_location = return_location.add(return_location.directionTo(HQ_location),1);
//			}
			count_down --;
			swarm_trigger = swarm_attack;
			return return_location;
		}
		return return_location;
	}
	
	public void perform_a_troop_census(){
		robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		RobotInfo[] sensed_friendly_Robots = robot_controller.senseNearbyRobots(999999, my_team);
		
		for (RobotInfo sensed_friendly_Robot : sensed_friendly_Robots) {
				robot_census[sensed_friendly_Robot.type.ordinal()] ++;
		}
		
		robot_census[my_type.ordinal()] ++;
		
		int num_of_loops = robot_census.length;
		for(int i = 0; i < num_of_loops;i++){
			send_broadcast(troop_count_channel + i,robot_census[i]);
		}
	}
	public boolean attack_deadest_enemy_in_range(){
		if(my_type.canAttack())
			if(robot_controller.isWeaponReady()){
				int attack_radius = BEYOND_MAX_ATTACK_RANGE;
				RobotInfo[] close_enemies = robot_controller.senseNearbyRobots(attack_radius, enemy_team);
				if(close_enemies.length > 0){
					int num_of_loops = close_enemies.length;
					for(int i=0; i< num_of_loops;i++){
						//if(!close_enemies[i].type.equals(RobotType.MISSILE)){
							try{
								if(robot_controller.canAttackLocation(close_enemies[i].location)){
									robot_controller.attackLocation(close_enemies[i].location);
									return true;
								} else{
									MapLocation newLocation = close_enemies[i].location.add(robot_controller.getLocation().directionTo(close_enemies[i].location).opposite());
									if(robot_controller.canAttackLocation(newLocation)){
										robot_controller.attackLocation(newLocation);
										return true;
									}
								}
							} catch (Exception e){
								Utilities.print_exception(e);
							}
						//}
					}
				}
			}
		return false;
	}	
}
