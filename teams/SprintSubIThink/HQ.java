package SprintSubIThink;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {
	
	static int recalc_danger_rate = 100;
	static int danger_alternator = 0;
	
	public HQ(RobotController rc) {
		super(rc);
		my_max_supply_level = 0;
		my_min_supply_level = 0;
		my_optimal_supply_level = 0;
		
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		
		while(true){
			attack_deadest_enemy_in_range();
			
			count_the_troops();
						
			update_strategy();	
						
			check_for_spawns();	
			
			broadcast_swarm_location();
			
			refresh_job_roles();
												
			dish_out_supply();
						
			robot_controller.yield();
		}		
	}
	
	public void refresh_job_roles(){
		send_broadcast(drone_harass_current, 0);
		send_broadcast(drone_swarm_current, 0);
		send_broadcast(launcher_harass_current, 0);
		send_broadcast(launcher_tower_protection_current, 0);		
	}
	
	public void broadcast_swarm_location(){
//		send_broadcast(swarm_location_channel_x,HQ_location.x);
//		send_broadcast(swarm_location_channel_y,HQ_location.y);	
//		return;

		MapLocation swarm_location = centre_point;
		int total_fighting_robots = 0;
		total_fighting_robots += robot_census[RobotType.BASHER.ordinal()];
		total_fighting_robots += robot_census[RobotType.COMMANDER.ordinal()];
		total_fighting_robots += robot_census[RobotType.DRONE.ordinal()];
	//	total_fighting_robots += robot_census[RobotType.LAUNCHER.ordinal()];
		total_fighting_robots += robot_census[RobotType.SOLDIER.ordinal()];
		total_fighting_robots += robot_census[RobotType.TANK.ordinal()];
		
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		int num_of_towers = the_towers.length;
		int swarm_attack = Math.max((num_of_towers * 6),20);
		int swarm_retreat = num_of_towers * 4;
		
		if(total_fighting_robots > swarm_attack){
			//Nearest My Tower to enemy HQ until swarm point
			//the_towers = robot_controller.senseTowerLocations();
			MapLocation the_starting_point = enemy_HQ_Location;	
			if(num_of_towers == 0)
				the_towers = new MapLocation[]{enemy_HQ_Location};
			
			swarm_location = find_closest(the_starting_point,the_towers);
			send_broadcast(drone_harass_max, 0);
			send_broadcast(drone_swarm_max, 0);
			send_broadcast(launcher_harass_max, 999);
			swarm_trigger = swarm_attack;
			
		} else{
			int my_orders = read_broadcast(orders_broadcast_offset + location_channel(HQ_location));
			if (my_orders !=0 && swarm_trigger == 0){		
				try{
					send_broadcast(orders_broadcast_offset + location_channel(HQ_location),0);
					if(robot_controller.canSenseRobot(my_orders)){
						swarm_location = robot_controller.senseRobot(my_orders).location;
						send_broadcast(drone_swarm_max, 0);
	//					System.out.println("Sending swarm to " + swarm_location.toString());
					}
				} catch(Exception e){
					print_exception(e);
				}
			} else if(total_fighting_robots < swarm_retreat){
				swarm_trigger = 0;
				send_broadcast(drone_harass_max, 4);
				send_broadcast(drone_swarm_max, 9999);
				send_broadcast(launcher_harass_max, 999);
				send_broadcast(launcher_tower_protection_max, 3);
			} else if(swarm_trigger > 0){
				//Nearest My Tower to enemy HQ until swarm point
				//the_towers = robot_controller.senseTowerLocations();
				MapLocation the_starting_point = enemy_HQ_Location;	
				if(num_of_towers == 0)
					the_towers = new MapLocation[]{enemy_HQ_Location};
				
				swarm_location = find_closest(the_starting_point,the_towers);
				send_broadcast(drone_harass_max, 0);
				send_broadcast(drone_swarm_max, 0);
				send_broadcast(launcher_harass_max, 999);
				swarm_trigger = swarm_attack;
								
			} else{
				send_broadcast(drone_harass_max, 4);
				send_broadcast(drone_swarm_max, 9999);
				send_broadcast(launcher_harass_max, 999);
				send_broadcast(launcher_tower_protection_max, 3);
			}
		}
		send_broadcast(swarm_location_channel_x,swarm_location.x);
		send_broadcast(swarm_location_channel_y,swarm_location.y);
	}
	
	public void count_the_troops(){
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
}
