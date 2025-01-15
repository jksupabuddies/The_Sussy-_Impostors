package examplefuncsplayer;

import battlecode.common.*;

import java.util.Random;




/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static int transition = 0;
    static Soldier sDl = new Soldier();
    static MapLocation[] targetList = {new MapLocation(19, 16), new MapLocation(19,19)};
    static MapLocation[] pTwLocs = new MapLocation[25];
    int pTwAmount = 0;



    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,//0
        Direction.NORTHEAST,
        Direction.EAST,//2
        Direction.SOUTHEAST,
        Direction.SOUTH,//4
        Direction.SOUTHWEST,
        Direction.WEST,//6
        Direction.NORTHWEST,
    };

    static boolean debug = false;


    static boolean targetSet = false;
    static MapLocation targetloc, startLoc;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        //System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        //rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the UnitType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()){
                    case SOLDIER: runSoldier(rc); break; 
                    case MOPPER: runMopper(rc); break;
                    case SPLASHER: runSplasher(rc);break; // Consider upgrading examplefuncsplayer to use splashers!
                    default: runTower(rc); break;
                    }
                }
             catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();
                rc.disintegrate();
                

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for towers.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException{
        RobotInfo[] rf = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, rc.getTeam().opponent());
        for(int i = rf.length; --i>=0;){
          if(rc.canAttack(rf[i].getLocation())){
                rc.attack(rf[i].getLocation());
            }
        }

        // Pick a direction to build in.
        int robotType;
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        robotType = 0;
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
        else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        }
        else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
            // rc.setIndicatorString("SPLASHER NOT IMPLEMENTED YET");
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // TODO: can we attack other bots?
    }


    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException{
        MapLocation[] mapLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),4);

        if(rc.canSenseLocation(new MapLocation(28, 31))){
            targetloc = new MapLocation(28, 31);
            targetSet = true;
        }
        MapInfo tL;
        boolean lowPaint = rc.getPaint() < 110;
        startLoc = rc.getLocation();
        if(rc.getID() == 11577 && rc.getTeam() == Team.A){
            System.out.println(sDl.printlst());
        }

        // if(lowPaint){
        //     targetloc = sDl.refill(rc);
        //     targetSet = true;
        // }

       if(targetloc == null || !targetSet){
            targetloc = mapLocations[rng.nextInt(mapLocations.length)];
            targetSet = true;
       }
        tL = rc.senseMapInfo(targetloc);
        PaintType pT = tL.getPaint();
        Direction nS = BugNavPathFind.move(rc, targetloc, startLoc);
        if(nS != null && rc.getPaint() > 120 && rc.canAttack(rc.getLocation().add(nS)) && (pT == PaintType.EMPTY || pT.isEnemy())){
            rc.attack(rc.getLocation().add(nS));
        }

        //Running Low on Paint:
        
        if(nS == null){
            targetSet = false;
            if(rc.canTransferPaint(targetloc, -30)){
                rc.transferPaint(targetloc, -30);
            }
        }
        else{
             rc.move(nS);
             if(!lowPaint){
                // if(rc.getID() == 11577 && rc.getTeam() == Team.A){
                //     System.out.println("Added:"+ rc.getRoundNum() + ": " + targetloc);
                // }
                sDl.addPosition(targetloc);
             }
        }
        
        // Sense information about all visible nearby tiles.
        // MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // // Search for a nearby ruin to complete.

        // MapInfo curRuin = null;
        // for (MapInfo tile : nearbyTiles){
        //     if (tile.hasRuin()){
        //         curRuin = tile;
        //     }
        // }
        // if (curRuin != null){
        //     MapLocation targetLoc = curRuin.getMapLocation();
        //     Direction dir = rc.getLocation().directionTo(targetLoc);
        //     if (rc.canMove(dir))
        //         rc.move(dir);
        //     // Mark the pattern we need to draw to build a tower here if we haven't already.
        //     MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
        //     if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
        //         rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
        //         System.out.println("Trying to build a tower at " + targetLoc);
        //     }
        //     // Fill in any spots in the pattern with the appropriate paint.
        //     for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
        //         if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
        //             boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
        //             if (rc.canAttack(patternTile.getMapLocation()))
        //                 rc.attack(patternTile.getMapLocation(), useSecondaryColor);
        //         }
        //     }
        //     // Complete the ruin if we can.
        //     if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
        //         rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
        //         rc.setTimelineMarker("Tower built", 0, 255, 0);
        //         System.out.println("Built a tower at " + targetLoc + "!");
        //     }
        // }

        // // Move and attack randomly if no objective.
        // Direction dir = directions[rng.nextInt(directions.length)];
        // MapLocation nextLoc = rc.getLocation().add(dir);
        // if (rc.canMove(dir)){
        //     rc.move(dir);
        // }
        // // Try to paint beneath us as we walk to avoid paint penalties.
        // // Avoiding wasting paint by re-painting our own tiles.
        // MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        // if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
        //     rc.attack(rc.getLocation());
        // }
    }


    /**
     * Run a single turn for a Mopper.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException{
        // Move and attack randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);                                                                                                                                                                                                                       
        }
        if (rc.canMopSwing(dir)){
            rc.mopSwing(dir);
            System.out.println("Mop Swing! Booyah!");
        }
        else if (rc.canAttack(nextLoc)){
            rc.attack(nextLoc);
        }
        // We can also move our code into different methods or classes to better organize it!
        updateEnemyRobots(rc);
    }

    public static void runSplasher(RobotController rc) throws GameActionException{
        MapInfo[] mapInfos = rc.senseNearbyMapInfos();
        startLoc = rc.getLocation();
        if(rc.canSenseLocation(targetList[transition]) && !targetSet && debug){
            targetloc = targetList[transition];
            if(transition < 1){
                transition++;
            }
            targetSet = true;
        }
        if(!targetSet){
            targetloc = mapInfos[rng.nextInt(mapInfos.length)].getMapLocation();
            targetSet = true;
        }
        rc.setIndicatorLine(startLoc, targetloc, 255, 100, 155);
        Direction nS = BugNavPathFind.move(rc, targetloc, startLoc);
        if(nS == null){
            targetSet = false;
        }
        else{
            rc.move(nS);
        }
        // System.out.println(mapInfos.length);//69 tiles worth of info, 100 bytecodes MapInfo --> wall, paint(color), location, marker(Secondary, Primary or Enemy) of each tile
        // MapLocation startOfGrid = null;
        MapInfo closestTowerSpot = null;
        MapInfo[] adjacientTiles = new MapInfo[2];
        for(int i = 0; i < mapInfos.length; i++){
            if(mapInfos[i].hasRuin()){
                closestTowerSpot = mapInfos[i];
                // startOfGrid = new MapLocation(closestTowerSpot.getMapLocation().x - 3, closestTowerSpot.getMapLocation().y + 2);
                // towerList.put(closestTowerSpot, startOfGrid);
                // int set = 0;
                // for(int j = 0; j < 7; j+=2){
                //     switch(directions[j]){
                //         case Direction.NORTH:
                //             if(rc.canSenseLocation(closestTowerSpot.getMapLocation().add(Direction.NORTH)) && set < 2){
                //                 adjacientTiles[set] = rc.senseMapInfo(closestTowerSpot.getMapLocation().add(Direction.NORTH));
                //                 set+=1;
                //             } break;
                //         case Direction.EAST:
                //             if(rc.canSenseLocation(closestTowerSpot.getMapLocation().add(Direction.SOUTH)) && set < 2){
                //                 adjacientTiles[set] = rc.senseMapInfo(closestTowerSpot.getMapLocation().add(Direction.SOUTH));
                //                 set+=1;
                //             } break;
                //         case Direction.SOUTH:
                //             if(rc.canSenseLocation(closestTowerSpot.getMapLocation().add(Direction.WEST)) && set < 2){
                //                 adjacientTiles[set] = rc.senseMapInfo(closestTowerSpot.getMapLocation().add(Direction.WEST));
                //                 set+=1;
                //             } break;
                //         case Direction.WEST:
                //             if(rc.canSenseLocation(closestTowerSpot.getMapLocation().add(Direction.EAST)) && set < 2){
                //                 adjacientTiles[set] = rc.senseMapInfo(closestTowerSpot.getMapLocation().add(Direction.EAST));
                //                 set+=1;
                //             } break;
                //     }
                // }
                break; //foundRuin
            }  // works currently, but very bad byte code values --> we might need to curb the sensing a bit --> but rn it seems to be fine, we should think about this later though
        }
        //We need to figure out how exactly to sort of access the info about marks for the tiles adjacient to the ruin tile.
        // if(closestTowerSpot != null){
        //     if(adjacientTiles[0].getMark() == PaintType.EMPTY && adjacientTiles[1].getMark() == PaintType.EMPTY){
        //         MapLocation mLocation = closestTowerSpot.getMapLocation();
        //         if(rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, mLocation)){
        //             rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, mLocation);
        //         }
        //     }
        // }
        
        // double minConc = 0;
        // MapInfo[] mp = new MapInfo[3];
        // Direction dr = Direction.NORTH;
        // Direction ranDir = directions[rng.nextInt(directions.length)];
        // for(int i = 1; i < 5; i++){
        //     switch(i%2){
        //         case 0: //North or South East
        //             switch (dr) {
        //                 case Direction.NORTH:
        //                     mp[0] = mapInfos[26];
        //                     mp[1] = mapInfos[25];
        //                     mp[2] = mapInfos[35];  
        //                     if(concGradCalc(mp) < minConc){
        //                         minConc = concGradCalc(mp);
        //                         ranDir = Direction.NORTHEAST;
        //                     }
        //                     dr = Direction.SOUTH;
        //                     break;
                    
        //                 default:
        //                     mp[0] = mapInfos[43];
        //                     mp[1] = mapInfos[44];
        //                     mp[2] = mapInfos[35];   
        //                 if(concGradCalc(mp) < minConc){
        //                     minConc = concGradCalc(mp);
        //                     ranDir = Direction.SOUTHEAST;
        //                 }
        //                 else{
        //                     ranDir = directions[rng.nextInt(directions.length)];
        //                 }
        //                 dr = Direction.NORTH;
        //                     break;
        //             }
        //             break;
        //         default:
        //         switch (dr) {
        //             case Direction.NORTH:
        //                 mp[0] = mapInfos[24];
        //                 mp[1] = mapInfos[25];
        //                 mp[2] = mapInfos[33];  
        //                 if(concGradCalc(mp) < minConc){
        //                     minConc = concGradCalc(mp);
        //                     ranDir = Direction.NORTHWEST;
        //                 }
        //                 dr = Direction.SOUTH;
        //                 break;
                
        //             default:
        //                 mp[0] = mapInfos[33];
        //                 mp[1] = mapInfos[42];
        //                 mp[2] = mapInfos[43];   
        //             if(concGradCalc(mp) < minConc){
        //                 minConc = concGradCalc(mp);
        //                 ranDir = Direction.SOUTHWEST;
        //             }
        //             dr = Direction.NORTH;
        //                 break;
        //         }
        //         break;
                
        //     }
        // }
        // if(rc.canMove(ranDir)){
        //     MapInfo futurePos = rc.senseMapInfo(rc.getLocation().add(ranDir));
        //     if(futurePos.getPaint().isAlly()){
        //         rc.move(ranDir);
        //     }
        //     else{
        //         if(rc.canAttack(rc.getLocation().add(ranDir)) && !futurePos.getPaint().isAlly()){
        //             if(rc.getPaint() > 100){
        //                 rc.attack(futurePos.getMapLocation());
        //                 rc.move(ranDir);
        //             }
        //         }
        //     }
        // }
        //Only markable when there is a ruin in sight...
        //69 tiles --> 100 bytecode.... 5 x 5 grid == 25 tiles ~ 75ish bytecodes
        //HashMap --> (ruinPosition,startofGrid, top-left)
        //Steps:
        // - mark ruin
        /*
         * canMarkTowerPattern() :--> assumes ruin is already here; returns true if there is a ruin in range, and false if not. 
         *  - mark tower pattern(); --> store the first part of the towerPatternsGrid. 
         * pre-existing ruin.. ie --> ruin is already stored in the local HashTable, then don't do any new Marking!
         */

        // MapInfo mi = rc.senseMapInfo(rc.getLocation().add(directions[lastMove]));
        // Direction dir = directions[lastMove];
        // if(rc.canMove(dir)){
        //     rc.move(dir);
        // }
        // else{
        //     if(rc.canMove(directions[2])){
        //         rc.move(directions[2]);
        //         lastMove = 2;
        //     }
        //     else if(rc.canMove(directions[6])){
        //         rc.move(directions[6]);
        //         lastMove = 6;
        //     }
        //     else if(rc.canMove(directions[0])){
        //         rc.move(directions[0]);
        //         lastMove = 0;
        //     }
        //     else{
        //         rc.move(directions[4]);
        //         lastMove = 4;
        //     }
        // }
        // if(rc.canAttack(rc.getLocation().add(directions[lastMove])) && rc.getPaint() > 100){
        //     if(mi.getPaint().equals(PaintType.ENEMY_PRIMARY) || mi.getPaint().equals(PaintType.ENEMY_SECONDARY) || (mi.getPaint().equals(PaintType.EMPTY) && !mi.isWall())){
        //         rc.attack(mi.getMapLocation());
        //     }
        // }
        // MapInfo[] mInfos = rc.senseNearbyMapInfos();
        // for(int i = 0; i < mInfos.length; i++){
        //     MapInfo mis = mInfos[i];
        //     MapLocation markloc = mis.getMapLocation().add(directions[0]);
        //     if(mis.hasRuin()){
        //         if(!mis.getMark().isAlly()){
        //             rc.mark(markloc, false);
        //         }
        //     }
        // }
        
    }
    // public static double concGradCalc(MapInfo[] tiles){
    //     double conc = 0;
    //     for(int i = 0; i < 3; i++){
    //         if(tiles[i].getPaint().isAlly()){
    //             conc++;
    //         }
    //     }
    //     return conc/3;
    // }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
}
