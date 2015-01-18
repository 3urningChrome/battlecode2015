package team111;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {
		
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
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + ROLE_START_POS, -1);
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + ROLE_START_POS, -1);
		
		initilise_soldier_protect_tank_swarm_job_roles();
	}
	
	private void initialise_job_role_aggression(){
		initialise_soldier_protect_tank_swarm_job_roles();
	}

	public void initialise_job_role_locations(){
		initialise_soldier_protect_tank_swarm_job_role_locations();
	}
	
	public void refresh_job_roles(){
		refresh_soldier_protect_tank_swarm_job_roles();
	}
	
	public void remove_roles(){
		// this will be called to swarm.
		remove_soldier_protect_tank_swarm_roles();
	}

	private void broadcast_role_locations() {
		//send out role locations if they change.
		broadcast_soldier_protect_tank_swarm_role_locations();
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
					System.out.println("Removed Roles - sos");
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
			System.out.println("Removed Roles - charge");
			remove_roles();
			return location;
		}
		initialise_max_job_roles();
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
	
	//-------------------------------------medium_map_drone_harrass_launcher--------------------------------------------------------------
	
	public void initilise_medium_map_drone_harrass_launcher_swarm_job_roles(){
		//drones
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] +0, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 2);
		
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] +5, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, 999);
		
		//launchers
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, 999);
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, 0);	
	}
	private void initialise_medium_map_drone_harass_launcher_swarm_job_roles(){
		//set aggression (if aggressive)
		//drones
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, 1);
		
	}
	public void initialise_medium_map_drone_harrass_launcher_swarm_job_role_locations(){
		//set default Locations
		MapLocation location = enemy_HQ_Location;
		
		//drones
		int distance = (int)(Math.sqrt(get_attack_radius(RobotType.HQ)) + 1);
		location = enemy_HQ_Location.add(Direction.NORTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, location.y);
		
		location = enemy_HQ_Location.add(Direction.SOUTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, location.y);
		
		location = enemy_HQ_Location.add(Direction.EAST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, location.y);
		
		location = enemy_HQ_Location.add(Direction.WEST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, location.y);
		
		location = centre_point; //center
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.y);
			
		int y_diff = (centre_point.x - enemy_HQ_Location.x)/2;
		int x_diff = (centre_point.y - enemy_HQ_Location.y)/2;
		location = location.add(1-x_diff,y_diff);  //off centre
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, location.y);
		
		location = location.add(x_diff,1-y_diff); // off centre
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, location.y);
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, location.y);
		
		location = Utilities.find_closest(enemy_HQ_Location, robot_controller.senseTowerLocations());
		location = location.add(location.directionTo(enemy_HQ_Location),2);
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, location.y);
	}
	public void refresh_medium_map_drone_harrass_launcher_swarm_job_roles(){
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, 0);	
	}
	
	public void remove_medium_map_drone_harrass_launcher_swarm_roles(){
		// this will be called to swarm.
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 0);
		
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 6, 0);
	}
	
	private void broadcast_medium_map_drone_harrass_launcher_role_locations() {
		MapLocation location;
		//Drones: all static locations at the moment
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, location.y);
		
		location = Utilities.find_closest(enemy_HQ_Location, robot_controller.senseTowerLocations());
		location = location.add(location.directionTo(enemy_HQ_Location),2);
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 1, location.y);			
		
		//set "swarm _location" where any non-role bots go.
//		location = set_swarm_point(the_towers);
//		send_broadcast(swarm_location_channel_x,location.x);
//		send_broadcast(swarm_location_channel_y,location.y);
	}
	
	//-------------------------------------soldier_protect_tank_swarm--------------------------------------------------------------
	
	public void initilise_soldier_protect_tank_swarm_job_roles(){
		//Soldiers
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, 3);
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, 6);
		
		//Tanks
		send_broadcast(role_max_offset + role_channel_start[RobotType.TANK.ordinal()] + 0, 999);	
	}
	
	private void initialise_soldier_protect_tank_swarm_job_roles(){
		//set aggression (if aggressive)
		send_broadcast(aggressive_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, 1);
	}

	public void initialise_soldier_protect_tank_swarm_job_role_locations(){
		//set default Locations
		MapLocation location = enemy_HQ_Location;
		
		//Soldier
		int distance = 7;
		location = HQ_location.add(HQ_location.directionTo(enemy_HQ_Location).rotateLeft(),distance);
		location = enemy_HQ_Location.add(Direction.NORTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, location.y);
		
		location = HQ_location.add(HQ_location.directionTo(enemy_HQ_Location).rotateRight(),distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, location.y);
		
		location = HQ_location.add(HQ_location.directionTo(enemy_HQ_Location),distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, location.y);
		

		//tanks
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		//location = Utilities.find_closest(HQ_location,the_towers);		
	//	location = location.add(location.directionTo(HQ_location),10);
		location = centre_point;
		send_broadcast(role_x_offset + role_channel_start[RobotType.TANK.ordinal()] + 0,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.TANK.ordinal()] + 0, location.y);
	}
	
	public void refresh_soldier_protect_tank_swarm_job_roles(){
		send_broadcast(role_current_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.TANK.ordinal()] + 0, 0);
	}
	
	public void remove_soldier_protect_tank_swarm_roles(){
		// this will be called to swarm.
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 0, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 1, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.SOLDIER.ordinal()] + 2, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.TANK.ordinal()] + 0, 0);
	}
	
	private void broadcast_soldier_protect_tank_swarm_role_locations() {
		MapLocation location;
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
//		location = Utilities.find_closest(HQ_location,the_towers);		
//		send_broadcast(role_x_offset + role_channel_start[RobotType.TANK.ordinal()] + 0,location.x);
//		send_broadcast(role_y_offset + role_channel_start[RobotType.TANK.ordinal()] + 0, location.y);
		initialise_soldier_protect_tank_swarm_job_role_locations();
		
		//set "swarm _location" where any non-role bots go.
		location = set_swarm_point(the_towers);
		send_broadcast(swarm_location_channel_x,location.x);
		send_broadcast(swarm_location_channel_y,location.y);
	}

	//-------------------------------------drone_contain--------------------------------------------------------------
	
	public void initilise_drone_contain_job_roles(){
		//drones
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] +0, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 2);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 99999);
		
		//launchers
		send_broadcast(role_max_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, 999);	
	}
	
	private void initialise_drone_contain_job_roles(){
		//set aggression (if aggressive)
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 1);
		send_broadcast(aggressive_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 1);
	}
	public void initialise_drone_contain_job_role_locations(){
		//set default Locations
		MapLocation location = enemy_HQ_Location;
		
		//drones
		int distance = 7;
		location = enemy_HQ_Location.add(Direction.NORTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, location.y);
		
		location = enemy_HQ_Location.add(Direction.SOUTH,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, location.y);
		
		location = enemy_HQ_Location.add(Direction.EAST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, location.y);
		
		location = enemy_HQ_Location.add(Direction.WEST,distance);
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, location.y);
		
		location = enemy_HQ_Location;
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.y);
		
		location = enemy_HQ_Location;
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, location.y);

		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, location.y);
	}
	
	public void refresh_drone_contain_job_roles(){
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 0);
		send_broadcast(role_current_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 0);
	}
	
	public void remove_drone_contain_roles(){
		// this will be called to swarm.
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 0, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 1, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 2, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 3, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, 0);
		send_broadcast(role_max_offset + role_channel_start[RobotType.DRONE.ordinal()] + 5, 0);
	}
	
	private void broadcast_drone_contain_role_locations() {
		MapLocation location;
		//Drones: all static locations at the moment
		location = enemy_HQ_Location.add(directions[(Clock.getRoundNum()/100)%8],(20));
		send_broadcast(role_x_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.DRONE.ordinal()] + 4, location.y);
		
		//launchers
		MapLocation[] the_towers = robot_controller.senseEnemyTowerLocations();
		if(the_towers.length == 0)
			the_towers = new MapLocation[]{enemy_HQ_Location};
		location = Utilities.find_closest(HQ_location,the_towers);		
		send_broadcast(role_x_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0,location.x);
		send_broadcast(role_y_offset + role_channel_start[RobotType.LAUNCHER.ordinal()] + 0, location.y);	
		
		//set "swarm _location" where any non-role bots go.
//		location = set_swarm_point(the_towers);
//		send_broadcast(swarm_location_channel_x,location.x);
//		send_broadcast(swarm_location_channel_y,location.y);
	}
}
