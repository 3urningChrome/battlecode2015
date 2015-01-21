package SeedingSubmission;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {
		
	//generic Roles
	static final int ROLE_START_POS = 0;
	static int count_down = 20;
	static int previous_num_of_towers = 0;

	
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

		MapLocation location = new MapLocation(0,0);
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
			count_down = 20;
		
		previous_num_of_towers = num_of_towers;
		int swarm_attack = Math.max((num_of_towers * 10),30);
		int swarm_retreat = Math.max(num_of_towers * 4, 15);
		
		//not many bots. lets stop swarming if we are.
		if((total_fighting_robots < swarm_retreat && swarm_trigger != 0)){
			swarm_trigger = 0;
			//location = centre_point;
			return location;
		}

		//lots of bots, lets kill (or carry on killing) stuff!
		if(total_fighting_robots > swarm_attack || swarm_trigger != 0  || Clock.getRoundNum() > 1800 ){
			if(the_towers.length < 1)
				the_towers = new MapLocation[] {enemy_HQ_Location};
			location = Utilities.find_closest(HQ_location,the_towers);
			RobotInfo[] my_troops = robot_controller.senseNearbyRobots(location, 100,my_team);
			if(my_troops.length < 6){
				location = location.add(location.directionTo(HQ_location),6);
			}
			count_down --;
			swarm_trigger = swarm_attack;
			return location;
		}
		return location;
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
}
