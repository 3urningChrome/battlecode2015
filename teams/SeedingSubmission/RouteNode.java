package SeedingSubmission;


import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RouteNode {

	MapLocation myLocation;
	MapLocation myDestination;
	MapLocation enemy_HQ_Location;
	RouteNode myLeftNode = null;
	RouteNode myRightNode = null;
	RouteNode myFinalRoute = null;
	int totalLeftDistance = 0;
	int totalRightDistance = 0;
	int distanceToLeftNode = 0;
	int distanceToRightNode = 0;
	int finalDistance = 0;
	RobotController bot;
	MapLocation[] enemy_towers;
	RobotType my_type;
	
	static final int MIN_SAFE_DISTANCE = 49;
	
	
	static final int MAXDEPTH = 3;
	int myDepth = 0;
	
	static int directionalLooksMulti[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public RouteNode(MapLocation theLocation,MapLocation theDestination,RobotController theBot,int depth,MapLocation[] the_towers){
		myLocation = theLocation;
		myDestination = theDestination;
		bot = theBot;
		my_type = bot.getType();

		enemy_towers = the_towers;
		enemy_HQ_Location = bot.senseEnemyHQLocation();
		
		if((bot.senseTerrainTile(theDestination)==TerrainTile.VOID && !(my_type == RobotType.DRONE) )){
			myDestination = myLocation;
			return;
		}

		
		if(myLocation.equals(myDestination))
			return;
		if(depth>MAXDEPTH)
			return;
		myDepth = depth;
//System.out.println("initialise nav: location:" + theLocation.toString() + "  " + Clock.getBytecodeNum());		
//System.out.println("initialise nav: destination:" + theDestination.toString() + "  " + Clock.getBytecodeNum());
		process(myLocation);
//System.out.println("Processed:"  + Clock.getBytecodeNum());		
		choosePath();
//System.out.println("chosen path" + Clock.getBytecodeNum());		
		optimiseMap();
//System.out.println("optinised route" + Clock.getBytecodeNum());		
		
	}
	public void process(MapLocation currentLocation){
		Direction myHeading = currentLocation.directionTo(myDestination);
		Direction nextHeading = myHeading;
		Direction currentHeadingToDestination;
		int straightDistance = 0;
		int bugging = 1;
		MapLocation nextLocation;
		//Loop walking forward
		while(true){
			nextLocation = currentLocation.add(myHeading);
			TerrainTile nextStep = bot.senseTerrainTile(nextLocation);
			
			if((nextStep == TerrainTile.OFF_MAP)){
				//destination is offmap. (who knew?) exit cleanly as if you arrived.
				distanceToLeftNode = straightDistance;
				distanceToRightNode = straightDistance;	
				myDestination = bot.getLocation();
				return;
			}
				
			if ((nextStep==TerrainTile.VOID && !(my_type == RobotType.DRONE) ))
				break;
			
			if(nextLocation.distanceSquaredTo(enemy_HQ_Location) < MIN_SAFE_DISTANCE)	{	
				if(myDestination.distanceSquaredTo(enemy_HQ_Location) < MIN_SAFE_DISTANCE)	{	
					myDestination = currentLocation;
					return;
				}
				break;
			}

			boolean start_bugging = false;
			for(MapLocation tower:enemy_towers){
				if(nextLocation.distanceSquaredTo(tower) < MIN_SAFE_DISTANCE ){
					if(myDestination.distanceSquaredTo(tower) < MIN_SAFE_DISTANCE ){
						myDestination = currentLocation;
						return;
					}					
					start_bugging = true;
					break;
				}
			}

			if(start_bugging)
				break;
			
			currentLocation = nextLocation;
System.out.println("Current NB Location: " + currentLocation);			
			myHeading = currentLocation.directionTo(myDestination);
			straightDistance+=1;
			if(currentLocation.equals(myDestination)){
				distanceToLeftNode = straightDistance;
				distanceToRightNode = straightDistance;	
				return;
			}
		}
		//bugging
		System.out.println("bugging: " + currentLocation);		
		MapLocation buggingStartLocation = currentLocation;
		Direction buggingStartDirection = myHeading; 
		for(int i = 0; i<2;i++){
			
			currentLocation = buggingStartLocation;
			myHeading = buggingStartDirection;
			nextHeading = myHeading;
			
			if(i==1)
				bugging = -1;
					
			int distance = 0;
			myHeading = directions[(myHeading.ordinal()-bugging+8)%8];
			currentHeadingToDestination = currentLocation.directionTo(myDestination);
			while(true){
				for(int directionalOffset:directionalLooksMulti){
					nextHeading = directions[(myHeading.ordinal()+(directionalOffset*bugging)+8)%8];
					nextLocation = currentLocation.add(nextHeading);
					
					if(!(nextLocation.distanceSquaredTo(enemy_HQ_Location) < MIN_SAFE_DISTANCE)){	
						boolean in_exclusion_zone = false;
						for(MapLocation tower:enemy_towers){
							if(nextLocation.distanceSquaredTo(tower) < 24 ){
								in_exclusion_zone = true;
								break;
							}
						}
						if(!in_exclusion_zone){
							TerrainTile nextStep = bot.senseTerrainTile(nextLocation);
							if(nextStep == TerrainTile.OFF_MAP && nextHeading.equals(currentHeadingToDestination)){
								break;
								//destination is offmap.
							}
							if (nextStep != TerrainTile.OFF_MAP && (bot.senseTerrainTile(nextLocation)!=TerrainTile.VOID || (my_type == RobotType.DRONE) )){
								break;
							}
						}						
					}
				}
//get better weighter move!				
				distance +=1; 
				if(nextHeading.equals(currentHeadingToDestination)) {
					//Found a new Node
					if(i==0){
						System.out.println("Creating new right node");
						myRightNode = new RouteNode(currentLocation,myDestination,bot,myDepth+1,enemy_towers);
						distanceToRightNode = distance+straightDistance;
					}else{
						myLeftNode = new RouteNode(currentLocation,myDestination,bot,myDepth+1,enemy_towers);
						distanceToLeftNode = distance+straightDistance;
					}
					break;
				}
				//break out early if this search is already longer than the first was
				if(myRightNode !=null && distance > myRightNode.finalDistance){
//System.out.println("Breaking due to distance: " + currentLocation);					
					distanceToLeftNode = 99999999;
					myLeftNode = myRightNode;
					return;
				}
				currentLocation = nextLocation;
				System.out.println("Current bug Location: " + currentLocation);
				myHeading = nextHeading;
				currentHeadingToDestination = currentLocation.directionTo(myDestination);				
			}
		}
		return;
	}
	public void choosePath(){
		if(myRightNode==null){
			finalDistance = distanceToLeftNode;
			return;
		}

		totalRightDistance = distanceToRightNode + myRightNode.totalRightDistance;
		totalLeftDistance = distanceToLeftNode + myLeftNode.totalLeftDistance;
		
		if(totalLeftDistance > totalRightDistance){
			myFinalRoute = myRightNode;
			finalDistance = totalRightDistance + distanceToRightNode;
		}else{
			myFinalRoute = myLeftNode;
			finalDistance = totalLeftDistance + distanceToLeftNode;	
		}
	}
	public void optimiseMap(){
		//if we are last node, then return.
		if(myFinalRoute == null)
			return;
		//optimise self
		RouteNode optimisedNode = new RouteNode(myLocation,myFinalRoute.myLocation,bot,0,enemy_towers);
		optimisedNode.attachToLastNode(myFinalRoute);
		myFinalRoute = optimisedNode.myFinalRoute;
		return;	
	}
	private void attachToLastNode(RouteNode endOfPath) {
		if(myFinalRoute==null){
			myFinalRoute = endOfPath;
		}else{
			myFinalRoute.attachToLastNode(endOfPath);
		}
	}
	public MapLocation getNextLocation(){
		if(myFinalRoute==null)
			return myDestination;
		if (myFinalRoute.myLocation.equals(bot.getLocation()))
			myFinalRoute = myFinalRoute.myFinalRoute;
		if(myFinalRoute==null)
			return myDestination;				
		return myFinalRoute.myLocation;
	}
	
	public void printMapLocations(){
		if(myFinalRoute!=null)
			myFinalRoute.printMapLocations();
		
//System.out.println("Node Location: " + myLocation.toString());
	}
}
