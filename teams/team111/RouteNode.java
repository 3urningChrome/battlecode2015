package team111;


import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RouteNode {

	MapLocation myLocation;
	MapLocation myDestination;
	RouteNode myLeftNode = null;
	RouteNode myRightNode = null;
	RouteNode myFinalRoute = null;
	int totalLeftDistance = 0;
	int totalRightDistance = 0;
	int distanceToLeftNode = 0;
	int distanceToRightNode = 0;
	int finalDistance = 0;
	RobotController bot;
	
	static final int MAXDEPTH = 3;
	int myDepth = 0;
	
	static int directionalLooksMulti[] = new int[]{1,0,-1,-2,-3,-4,-5,-6,-7};
	static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public RouteNode(MapLocation theLocation,MapLocation theDestination,RobotController theBot,int depth){
		myLocation = theLocation;
		myDestination = theDestination;
		bot = theBot;
		
		if(bot.senseTerrainTile(theDestination)==TerrainTile.OFF_MAP || (bot.senseTerrainTile(theDestination)==TerrainTile.VOID && !(bot.getType() == RobotType.DRONE) ))
			myDestination = myLocation;
		if(myLocation.equals(myDestination))
			return;
		if(depth>MAXDEPTH)
			return;
		myDepth = depth;
		process(myLocation);
		choosePath();
		optimiseMap();
		
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
			if (nextStep == TerrainTile.OFF_MAP || (bot.senseTerrainTile(nextLocation)==TerrainTile.VOID && !(bot.getType() == RobotType.DRONE) ))
				break;
			
			currentLocation = nextLocation;
			myHeading = currentLocation.directionTo(myDestination);
			straightDistance+=1;
			if(currentLocation.equals(myDestination)){
				distanceToLeftNode = straightDistance;
				distanceToRightNode = straightDistance;	
				return;
			}
		}
		//bugging
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
					TerrainTile nextStep = bot.senseTerrainTile(nextLocation);
					if (nextStep == TerrainTile.OFF_MAP || (bot.senseTerrainTile(nextLocation)==TerrainTile.VOID && !(bot.getType() == RobotType.DRONE) ))
						break;
				}
//get better weighter move!				
				distance +=1; 
				if(nextHeading.equals(currentHeadingToDestination)) {
					//Found a new Node
					if(i==0){
						myRightNode = new RouteNode(currentLocation,myDestination,bot,myDepth+1);
						distanceToRightNode = distance+straightDistance;
					}else{
						myLeftNode = new RouteNode(currentLocation,myDestination,bot,myDepth+1);
						distanceToLeftNode = distance+straightDistance;
					}
					break;
				}
				//break out early if this search is already longer than the first was
				if(myRightNode !=null && distance > myRightNode.finalDistance){
					distanceToLeftNode = 99999999;
					myLeftNode = myRightNode;
					return;
				}
				currentLocation = nextLocation;
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
		RouteNode optimisedNode = new RouteNode(myLocation,myFinalRoute.myLocation,bot,0);
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
}
