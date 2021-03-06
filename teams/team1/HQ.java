package team1;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HQ extends Building  {

	static int num_of_towers_calculated = 0;
	static MapLocation[] original_towers = null;
	static boolean protect_tower = false;
	MapLocation protect_location;
	
	static int turn_protected_area_last_attacked = 0;
	static double last_health = 999999;
	static int last_tower_id = 9999999;
	
	public HQ(RobotController rc) {
		super(rc);
		initialiseTowers();
		my_max_supply_level = 0;
		my_min_supply_level = 0;
		my_optimal_supply_level = 0;	
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			sensed_enemy_robots = robot_controller.senseNearbyRobots((int)(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED * 1.2),enemy_team);			
			//attack_random_enemy_in_range();
			attack_deadest_enemy_in_range();
			count_the_troops();
			update_strategy();	
			check_for_spawns();	
			calculate_danger_squares();	
			assign_locations();
			dish_out_supply();
			robot_controller.yield();
		}		
	}
	
	public void initialiseTowers(){
		original_towers = robot_controller.senseEnemyTowerLocations();
	}
	
	public void count_the_troops(){
		robot_census = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		RobotInfo[] sensed_friendly_Robots = robot_controller.senseNearbyRobots(999999, my_team);
		
		protect_tower = false;
		boolean protected_tower_still_alive = false;
		for (RobotInfo sensed_friendly_Robot : sensed_friendly_Robots) {
				robot_census[sensed_friendly_Robot.type.ordinal()] ++;
				if(sensed_friendly_Robot.type.equals(RobotType.TOWER)){
					if(sensed_friendly_Robot.ID == last_tower_id)
						protected_tower_still_alive = true;
					
					if(sensed_friendly_Robot.health < RobotType.TOWER.maxHealth){
						protect_tower = true;
						protect_location = sensed_friendly_Robot.location;
						if(sensed_friendly_Robot.health < last_health){
							turn_protected_area_last_attacked = Clock.getRoundNum();
							last_health = sensed_friendly_Robot.health;
							last_tower_id = sensed_friendly_Robot.ID;
						}
					}
				}
		}
		if(!protected_tower_still_alive){
			last_health = 999999;
			last_tower_id = 9999999;			
		}
		
		robot_census[my_type.ordinal()] ++;
		
		int num_of_loops = robot_census.length;
		for(int i = 0; i < num_of_loops;i++){
			send_broadcast(troop_count_channel + i,robot_census[i]);
		}
	}	
	
	public void assign_locations(){
		
		int total_fighting_robots = 0;
		total_fighting_robots += robot_census[RobotType.BASHER.ordinal()];
		total_fighting_robots += robot_census[RobotType.COMMANDER.ordinal()];
		total_fighting_robots += robot_census[RobotType.DRONE.ordinal()];
	//	total_fighting_robots += robot_census[RobotType.LAUNCHER.ordinal()];
		total_fighting_robots += robot_census[RobotType.SOLDIER.ordinal()];
		total_fighting_robots += robot_census[RobotType.TANK.ordinal()];
		
		MapLocation the_starting_point;
		MapLocation[] the_towers;
		MapLocation swarm_location;
		
		if(protect_tower && (Clock.getRoundNum() - turn_protected_area_last_attacked) < 20){
			swarm_location = protect_location;
			//swarm_location = swarm_location.add(swarm_location.directionTo(enemy_HQ_Location), 2);
			send_broadcast(swarm_location_channel_x,swarm_location.x);
			send_broadcast(swarm_location_channel_y,swarm_location.y);	
			send_broadcast(override_saftey, 1);			
		} else{
			if(total_fighting_robots < swarm_trigger){
				//Nearest My Tower to enemy HQ until swarm point
				//the_towers = robot_controller.senseTowerLocations();
				the_starting_point = enemy_HQ_Location;		
				the_towers = new MapLocation[]{enemy_HQ_Location};
				swarm_location = find_closest(the_starting_point,the_towers);
				send_broadcast(swarm_location_channel_x,swarm_location.x);
				send_broadcast(swarm_location_channel_y,swarm_location.y);	
			
				//set defence post
				the_towers = robot_controller.senseTowerLocations();
				the_starting_point = enemy_HQ_Location;
				// if no towers left. head for enemy HQ
				if(the_towers.length < 1){
					the_towers = new MapLocation[]{enemy_HQ_Location};
				}
				swarm_location = find_closest(the_starting_point,the_towers);
				send_broadcast(defence_location_channel_x,swarm_location.x);
				send_broadcast(defence_location_channel_y,swarm_location.y);	
			
			}else{
				//once swarm point, launch against nearest enemy tower.
				the_towers = robot_controller.senseEnemyTowerLocations();
				the_starting_point = HQ_location;
				// if no towers left. head for enemy HQ
				if(the_towers.length < 1)
					the_towers = new MapLocation[]{enemy_HQ_Location};
				
				swarm_location = find_closest(the_starting_point,the_towers);
				send_broadcast(swarm_location_channel_x,swarm_location.x);
				send_broadcast(swarm_location_channel_y,swarm_location.y);	
			}
			send_broadcast(override_saftey, 0);	
		}
	}
	

	public void calculate_danger_squares(){
		MapLocation[] enemy_towers = robot_controller.senseEnemyTowerLocations();
		
		if(enemy_towers.length == num_of_towers_calculated)
			return;
		if(enemy_towers.length < num_of_towers_calculated )
			clear_hq_tower_data();
		
		num_of_towers_calculated = enemy_towers.length;
		
		//clear_hq_tower_data();
		//start with HQ
		int hq_attack_range = RobotType.HQ.attackRadiusSquared;
		double hq_attack_damage = RobotType.HQ.attackPower;
		boolean hq_splash = false;
		
		switch(enemy_towers.length){
		case 6:
			hq_attack_damage = RobotType.HQ.attackPower * 10;
		case 5:
			hq_splash = true;
		case 3:
			hq_attack_damage = RobotType.HQ.attackPower * 1.5;			
		case 2:
			hq_attack_range = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
			
		default:
			break;		
		}
		int offset = (int) Math.sqrt(hq_attack_range+1);
		if(hq_splash){
			offset+=2;
			hq_attack_range = (offset) * (offset);
		}
		int num_of_loops = 2 * offset;
		for(int x=0; x<num_of_loops; x++){
			for(int y=0; y<num_of_loops;y++){
				MapLocation testLocation = enemy_HQ_Location.add(x - offset, y - offset);
				if(enemy_HQ_Location.distanceSquaredTo(testLocation) <= hq_attack_range){			
					send_broadcast(static_broadcast_offset + location_channel(testLocation), (int) hq_attack_damage);			
				}
			}
		}

		//towers
		offset = (int) Math.sqrt(RobotType.TOWER.attackRadiusSquared+1);
		num_of_loops = 2 * offset;
		for(MapLocation enemy_tower : enemy_towers){
			for(int x=0; x<num_of_loops; x++){
				for(int y=0; y<num_of_loops;y++){
					MapLocation testLocation = enemy_tower.add(x - offset, y - offset);
					if(enemy_tower.distanceSquaredTo(testLocation) <= RobotType.TOWER.attackRadiusSquared){	
						send_broadcast(static_broadcast_offset + location_channel(testLocation), (int) RobotType.TOWER.attackPower);						
					}
				}
			}
		}			
	}
	
	public void clear_hq_tower_data(){
		int offset = (int) Math.sqrt(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED+1) + 2;
		int num_of_loops = 2 * offset;
		for(int x=0; x<num_of_loops; x++){
			for(int y=0; y<num_of_loops;y++){
				MapLocation testLocation = enemy_HQ_Location.add(x - offset, y - offset);
				if(HQ_location.distanceSquaredTo(testLocation) <= (offset * offset)){	
					send_broadcast(static_broadcast_offset + location_channel(testLocation),0);				
				}
			}
		}

		//towers
		offset = (int) Math.sqrt(RobotType.TOWER.attackRadiusSquared+1);
		num_of_loops = 2 * offset;
		for(MapLocation enemy_tower : original_towers){
			for(int x=0; x<num_of_loops; x++){
				for(int y=0; y<num_of_loops;y++){
					MapLocation testLocation = enemy_tower.add(x - offset, y - offset);
					if(enemy_tower.distanceSquaredTo(testLocation) <= RobotType.TOWER.attackRadiusSquared){	
						send_broadcast(static_broadcast_offset + location_channel(testLocation),0);					
					}
				}
			}
		}		
	}
	
	public void keep(){
		//then sensedRobots.
		RobotInfo[] sensed_enemy_bots = robot_controller.senseNearbyRobots(99999, enemy_team);
		int counter = 1;
		MapLocation attack_location;
		for(RobotInfo sensed_enemy_bot : sensed_enemy_bots){
			if(!sensed_enemy_bot.type.equals(RobotType.TOWER)){
				while((counter*counter) < sensed_enemy_bot.type.attackRadiusSquared){
					for(Direction direction : directions){
						attack_location = sensed_enemy_bot.location.add(direction,counter);
						attack_location = new MapLocation(((attack_location.x % HASH) + HASH) % HASH,((attack_location.y % HASH) + HASH) % HASH);
						try{
						robot_controller.broadcast(static_broadcast_offset + (attack_location.x * GameConstants.MAP_MAX_WIDTH ) + attack_location.y,(int) sensed_enemy_bot.type.attackPower);
						} catch (Exception e){
							print_exception(e);
						}
					}
					counter +=1;
				}	
			}
		}	
	}
}
