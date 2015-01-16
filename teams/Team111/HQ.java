package team111;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {
	
	//Drone roles:
	static final int DRONE_HARASS1 = 0;
	static final int DRONE_HARASS2 = 1;
	static final int DRONE_HARASS3 = 2;
	static final int DRONE_HARASS4 = 3;
	static final int DRONE_SWARM1 = 4;
	static final int DRONE_SWARM2 = 5;
	static final int DRONE_SWARM3 = 6;
	
	//Launcher Roles:
	static final int LAUNCHER_DEFEND = 0;
	static final int LAUNCHER_HARASS = 1;
	
	//generic Roles
	static final int ROLE_START_POS = 0;

	
	public HQ(RobotController rc) {
		super(rc);	
	}
	
	public void basic_turn_loop(){
		update_strategy();				
		check_for_spawns();	
		
		initialise_max_job_roles();
		initialise_job_role_aggression();
		initialise_job_role_locations();
		
		robot_controller.yield();
		
		while(true){
			attack_deadest_enemy_in_range();
			perform_a_troop_census();
			update_strategy();		
			check_for_spawns();	
			refresh_job_roles();
			broadcast_role_locations();
			dish_out_supply();		
			robot_controller.yield();
		}		
	}
	
	private void initialise_max_job_roles() {
		//currently buildings don't check for roles, so only apply to mobile bots.
		//initialise all non-role bots to -1 (Speeds up role check for them)
		send_broadcast(role_max_offset + role_channel_start[RobotType.BASHER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.BEAVER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.COMMANDER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.COMPUTER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.MINER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.TANK.ordinal()] + ROLE_START_POS, -1);
		
		//drones
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, 1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, 1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, 1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, 1);
		
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, 999);
		
		//launchers
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS, 999);
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, 3);	
	}
	private void initialise_job_role_aggression(){
		//set aggression (if aggressive)
		//drones
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, 1);
		
	}
	public void initialise_job_role_locations(){
		//set default Locations
		MapLocation location = enemy_HQ_Location;
		
		//drones
		int distance = (int)(Math.sqrt(get_attack_radius(RobotType.HQ)) + 1);
		location = enemy_HQ_Location.add(Direction.NORTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, location.y);
		
		location = enemy_HQ_Location.add(Direction.SOUTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, location.y);
		
		location = enemy_HQ_Location.add(Direction.EAST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, location.y);
		
		location = enemy_HQ_Location.add(Direction.WEST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, location.y);
		
		location = centre_point; //center
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, location.y);
			
		int y_diff = (centre_point.x - enemy_HQ_Location.x)/2;
		int x_diff = (centre_point.y - enemy_HQ_Location.y)/2;
		location = location.add(1-x_diff,y_diff);  //off centre
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, location.y);
		
		location = location.add(x_diff,1-y_diff); // off centre
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, location.y);
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS, location.y);
		
		location = Utilities.find_closest(enemy_HQ_Location, robot_controller.senseTowerLocations());
		location = location.add(location.directionTo(enemy_HQ_Location),2);
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, location.y);
	}
	
	public void refresh_job_roles(){
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, 0);	
	}
	
	public void remove_roles(){
		System.out.println("Removed Roles");
		// this will be called to swarm.
		//only ignore people here if they are not to swarm
		//drones
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS1, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS2, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS3, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_HARASS4, 0);
		
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM1, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM2, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + DRONE_SWARM3, 0);
	}

	private void broadcast_role_locations() {
		//send out role locations if they change.
		MapLocation location;
		//Drones: all static locations at the moment
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_HARASS, location.y);
		
		location = Utilities.find_closest(enemy_HQ_Location, robot_controller.senseTowerLocations());
		location = location.add(location.directionTo(enemy_HQ_Location),2);
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + LAUNCHER_DEFEND, location.y);			
		
		//set "swarm _location" where any non-role bots go.
		location = set_swarm_point(the_towers);
		send_broadcast(swarm_location_channel_x,location.x);
		send_broadcast(swarm_location_channel_y,location.y);
	}
	
	public MapLocation set_swarm_point(MapLocation [] the_towers){

		MapLocation location = centre_point;
		int total_fighting_robots = robot_census[RobotType.BASHER.ordinal()] + robot_census[RobotType.COMMANDER.ordinal()] + robot_census[RobotType.DRONE.ordinal()] + robot_census[RobotType.SOLDIER.ordinal()] + robot_census[RobotType.TANK.ordinal()];
		//	total_fighting_robots += robot_census[RobotType.LAUNCHER.ordinal()];
		
		//cry for help, only respond if not swarming
		int my_orders = read_broadcast(orders_broadcast_offset + location_channel(HQ_location));
		if (my_orders !=0 && swarm_trigger == 0){		
			try{
				send_broadcast(orders_broadcast_offset + location_channel(HQ_location),0);
				if(robot_controller.canSenseRobot(my_orders)){
					remove_roles();
					return robot_controller.senseRobot(my_orders).location;
				}
			} catch(Exception e){
				Utilities.print_exception(e);
			}
		}
				
		int num_of_towers = the_towers.length;
		int swarm_attack = Math.max((num_of_towers * 6),20);
		int swarm_retreat = num_of_towers * 4;
		
		//not many bots. lets stop swarming if we are.
		if(total_fighting_robots < swarm_retreat && swarm_trigger != 0){
			swarm_trigger = 0;
			location = centre_point;
			initialise_max_job_roles();
			return location;
		}

		//lots of bots, lets kill (or carry on killing) stuff!
		if(total_fighting_robots > swarm_attack || swarm_trigger != 0 ){
			location = Utilities.find_closest(HQ_location,the_towers);
			swarm_trigger = swarm_attack;
			remove_roles();
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
