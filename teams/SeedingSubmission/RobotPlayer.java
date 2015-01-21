package SeedingSubmission;

import battlecode.common.RobotController;
public class RobotPlayer {
	
	static Object myRobot;

	public static void run(RobotController rc) {
		switch (rc.getType()){
		case AEROSPACELAB:
			myRobot = new AerospaceLab(rc);
			break;
		case BARRACKS:
			myRobot = new Barracks(rc);
			break;
		case BASHER:
			myRobot = new Basher(rc);
			break;
		case BEAVER:
			myRobot = new Beaver(rc);
			break;
		case COMMANDER:
			myRobot = new Commander(rc);
			break;
		case COMPUTER:
			myRobot = new Computer(rc);
			break;
		case DRONE:
			myRobot = new Drone(rc);
			break;
		case HANDWASHSTATION:
			myRobot = new HandwashStation(rc);
			break;
		case HELIPAD:
			myRobot = new Helipad(rc);
			break;
		case HQ:
			myRobot = new HQ(rc);
			break;
		case LAUNCHER:
			myRobot = new Launcher(rc);
			break;
		case MINER:
			myRobot = new Miner(rc);
			break;
		case MINERFACTORY:
			myRobot = new MinerFactory(rc);
			break;
		case MISSILE:
			myRobot = new Missile(rc);
			break;
		case SOLDIER:
			myRobot = new Soldier(rc);
			break;
		case SUPPLYDEPOT:
			myRobot = new SupplyDepot(rc);
			break;
		case TANK:
			myRobot = new Tank(rc);
			break;
		case TANKFACTORY:
			myRobot = new TankFactory(rc);
			break;
		case TECHNOLOGYINSTITUTE:
			myRobot = new TechnologyInstitute(rc);
			break;
		case TOWER:
			myRobot = new Tower(rc);
			break;
		case TRAININGFIELD:
			myRobot = new TrainingField(rc);
			break;
		default:
			System.out.println("Unknown robot spawned");
			break;
		}
	}
}
