package Team111;

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
	
	public HQ(RobotController rc) {
		super(rc);
		initialiseTowers();
		basic_turn_loop();
	}
	
	public void basic_turn_loop(){
		while(true){
			count_the_troops();
			attack_random_enemy_in_range();		
			dish_out_supply();
			update_strategy();			
			check_for_spawns();	
			calculate_danger_squares();				
			robot_controller.yield();
		}		
	}	
	
	public void initialiseTowers(){
		original_towers = robot_controller.senseEnemyTowerLocations();
	}
	

	public void calculate_danger_squares(){
		MapLocation[] enemy_towers = robot_controller.senseEnemyTowerLocations();
		
		if(enemy_towers.length == num_of_towers_calculated)
			return;
		
		num_of_towers_calculated = enemy_towers.length;
		
		//clear_hq_tower_data();
		//start with HQ
		int hq_attack_range = RobotType.HQ.attackRadiusSquared;
		double hq_attack_damage = RobotType.HQ.attackPower;
		boolean hq_splash = false;
		
		switch(enemy_towers.length){
		case 2:
			hq_attack_range = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
		case 3:
			hq_attack_damage = RobotType.HQ.attackPower * 1.5;
		case 5:
			hq_splash = true;
		case 6:
			hq_attack_damage = RobotType.HQ.attackPower * 10;
		default:
			break;		
		}
		
		int offset = (int) Math.sqrt(hq_attack_range+1);
		int num_of_loops = 2 * offset;
		for(int x=0; x<num_of_loops; x++){
			for(int y=0; y<num_of_loops;y++){
				MapLocation testLocation = HQ_location.add(x - offset, y - offset);
				if(HQ_location.distanceSquaredTo(testLocation) <= hq_attack_range){
					testLocation = new MapLocation(((testLocation.x % HASH) + HASH) % HASH,((testLocation.y % HASH) + HASH) % HASH);				
					try{
						robot_controller.broadcast(static_broadcast_offset + (testLocation.x * GameConstants.MAP_MAX_WIDTH ) + testLocation.y, (int) hq_attack_damage);
					} catch (Exception e){
						print_exception(e);
					}					
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
						testLocation = new MapLocation(((testLocation.x % HASH) + HASH) % HASH,((testLocation.y % HASH) + HASH) % HASH);				
						try{
							robot_controller.broadcast(static_broadcast_offset + (testLocation.x * GameConstants.MAP_MAX_WIDTH ) + testLocation.y, (int) RobotType.TOWER.attackRadiusSquared);
						} catch (Exception e){
							print_exception(e);
						}					
					}
				}
			}
		}			
	}
	
	public void clear_hq_tower_data(){
		int offset = (int) Math.sqrt(GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED+1);
		int num_of_loops = 2 * offset;
		for(int x=0; x<num_of_loops; x++){
			for(int y=0; y<num_of_loops;y++){
				MapLocation testLocation = HQ_location.add(x - offset, y - offset);
				if(HQ_location.distanceSquaredTo(testLocation) <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED){
					testLocation = new MapLocation(((testLocation.x % HASH) + HASH) % HASH,((testLocation.y % HASH) + HASH) % HASH);				
					try{
						robot_controller.broadcast(static_broadcast_offset + (testLocation.x * GameConstants.MAP_MAX_WIDTH ) + testLocation.y, (int) GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED);
					} catch (Exception e){
						print_exception(e);
					}					
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
						testLocation = new MapLocation(((testLocation.x % HASH) + HASH) % HASH,((testLocation.y % HASH) + HASH) % HASH);				
						try{
							robot_controller.broadcast(static_broadcast_offset + (testLocation.x * GameConstants.MAP_MAX_WIDTH ) + testLocation.y, (int) RobotType.TOWER.attackRadiusSquared);
						} catch (Exception e){
							print_exception(e);
						}					
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
