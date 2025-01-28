package examplefuncsplayer;


import java.util.Random;

import battlecode.common.*;
import examplefuncsplayer.Constants.towers;
public class Soldier {
    static MapLocation[] pstL = new MapLocation[Constants.NUM_OF_LOCATIONS_VISITED];
    static Constants.towers towerPattern = Constants.towers.NULL;
    static boolean towerSet = false;
    static boolean[] towerTraversal = new boolean[26];
    static boolean targetSet = false;
    static int head = 0;
    static int senseRange = -1;
    static MapLocation senseCenter;
    static Direction heading;
    static boolean isSecondary = false;
    static MapInfo targetLoc = null;
    static MapInfo attackLoc = null;
    static MapInfo currRuinPos = null;
    static int currRuinPosIdx;
    static MapInfo[] envInfo = null;
    static MapLocation startLoc = null;
    static MapInfo markPos = null;
    static Random rng = new Random();
    static boolean towerCheckSenseRange;



    //Run Sequence:
    /*
     * 1). Sense
     * 2). Respond
     * 3). Act... ie.... move/attack depending on the situation
     */

    /*
     * //TODO
     * Build a micro for this and mopper
     */

    public static void runSoldier(RobotController rc) throws GameActionException{
        startLoc = rc.getLocation();
        senseRange = -1;
        senseCenter  = startLoc;
        towerCheckSenseRange = currRuinPos != null && currRuinPos.hasRuin() && isAdjacentTo(rc, currRuinPos.getMapLocation());
        //rng = new Random(rc.getID());
        if(towerCheckSenseRange){
            senseRange = 8;
            senseCenter = currRuinPos.getMapLocation();
        }
        sense(rc);

        if(!targetSet){
            targetLoc = envInfo[rng.nextInt(envInfo.length)];
            targetSet = true;
        }
        
        if(targetLoc != null){
            if(towerCheckSenseRange){
                towerSequence(targetLoc, rc);
            }
            Direction nS = BugNavPathFind.move(rc, targetLoc.getMapLocation(), startLoc);

            if(nS != null){
                if(rc.canMove(nS)){
                    rc.move(nS);
                }
            }
            else{
                targetSet = false;
            }

            if(attackLoc != null && rc.canAttack(attackLoc.getMapLocation())){
                rc.attack(attackLoc.getMapLocation(), isSecondary);
            }
            
            
        }


        // for(int i = 25; i-- >0;){
        //     rc.setIndicatorDot(envInfo[i].getMapLocation(), i, i, i);
        // }
        //rc.setIndicatorString("" + towerSet + heading + towerPattern);
       if(targetLoc != null){
            rc.setIndicatorDot(targetLoc.getMapLocation(), 0, 200, 200);
       }

       
    }


    public static void sense(RobotController rc) throws GameActionException{
        envInfo = rc.senseNearbyMapInfos(senseCenter, senseRange);
        for(int i = envInfo.length; i-->0;){
            if(envInfo[i].hasRuin() && rc.senseRobotAtLocation(envInfo[i].getMapLocation()) == null){
                currRuinPos = envInfo[i];
                currRuinPosIdx = i;
                //rc.setIndicatorDot(currRuinPos.getMapLocation(), 100, 100, 100);
                targetLoc = currRuinPos;
                targetSet = true;
                return;
            }
        }
    }


    public static void towerSequence(MapInfo ruinPos, RobotController rc) throws GameActionException{
        //MapLocation startPos = currRuinPos.getMapLocation().translate(-2, 2)
        MapLocation currRuinLoc = currRuinPos.getMapLocation();
        MapInfo paintPos = null;
        MapLocation markPosloc = markPos == null ? null : markPos.getMapLocation();
        boolean isCCW = true;
        UnitType cTwP;
        if(!towerSet(ruinPos,rc)){
            setTower(ruinPos.getMapLocation(),rc);
        }
        switch(towerPattern){
            case Constants.towers.PAINT:
                cTwP = UnitType.LEVEL_ONE_PAINT_TOWER;
                break;
            case Constants.towers.CHIP:
                cTwP = UnitType.LEVEL_ONE_MONEY_TOWER;
                break;
            case Constants.towers.DEFENSE:
                cTwP = UnitType.LEVEL_ONE_DEFENSE_TOWER;
                break;
            default:
                cTwP = null;
                break;
                
        }
        currentHeadingNearRuin(rc);
        int towerNum = getTowerPatternNum();
        for(int i = 4; i-->0;){
            heading = rotate90deg(heading);
            if(isUnpainted(paintCardinals(), isSecondary)){
                isSecondary = false;
                targetLoc = paintCardinals();
                attackLoc = paintCardinals();
                return;
            }
            attackLoc = null;
        }
        currentHeadingNearRuin(rc);
        for(int i = 25; i > 0; i--){
            int dy = (25-i)/5;
            int dx = (25-i)%5;
            paintPos = envInfo[5*dy + dx];//rc.senseMapInfo(startPos.translate(dx, -dy));
            MapLocation paintPosLoc = paintPos.getMapLocation();
            isSecondary = isSecondary(towerNum%2);
            if(isUnpainted(paintPos, isSecondary)){
                if(inAttackRange(rc, paintPosLoc)){
                    rc.setIndicatorDot(paintPos.getMapLocation(), 255, 0, 0);
                    attackLoc = paintPos;
                    return;
                }
                else if(!inAttackRange(rc, paintPosLoc)){
                    rc.setIndicatorDot(paintPos.getMapLocation(), 255, 0, 0);
                    heading =  rotate90deg(heading);
                    if(isOccupied(rc, paintCardinals())){
                        heading = rotate90degCCW(heading);
                        heading = rotate90degCCW(heading);
                    }
                    attackLoc = paintPos;
                    targetLoc = paintCardinals();
                    return;
                }
            }

            if(rc.canCompleteTowerPattern(cTwP, currRuinLoc)){
                rc.completeTowerPattern(cTwP, currRuinLoc);
                if(rc.canRemoveMark(markPosloc)){
                    rc.removeMark(markPosloc);
                }
                return;
            }
            towerNum/=2;
        
        
            
            //paint the four cardinal tiles with paint ==> prioritize this; Start from the top and rotate 90 degrees ==> paint your current location first;
            //then move around on those four tiles
            // if its too occupied --> that is more than two units
                // --> consider it to be occupied and find another place
                // --> 
            
            //check a randomPosition => represent columns via random number(column) * random number(row). 
            /*
             * (digitNumber/rowSize) %(rowSize * (digitNumber/rowSize)) ==> 
             * 
             */
            /*
             * 1). Check for the first false in range; 
             *      1a). if not in range, then move into a key* location that isn't occupied
             *      1b). if all key positions are occupied, stay where you are and break;
             * 2). Divide the pattern to a new value for the next traversal interval
             * 
             * 3). check for updates? 
             */
        } 
        
        
    }

    public static MapInfo paintCardinals() throws GameActionException{
        switch(heading){
            case Direction.EAST:
                return envInfo[currRuinPosIdx - 5];

            case Direction.WEST:
                return envInfo[currRuinPosIdx + 5];

            case Direction.NORTH:
                return envInfo[currRuinPosIdx - 1];

            default:
                return envInfo[currRuinPosIdx + 1];
        }
    }

    public static void currentHeadingNearRuin(RobotController rc) throws GameActionException{
        switch(startLoc.directionTo(currRuinPos.getMapLocation())){
            case Direction.NORTH:
                heading = Direction.WEST;
                break;
            case Direction.SOUTH:
                heading = Direction.EAST;
                break;
            case Direction.WEST:
                heading = Direction.SOUTH;
                break;
            default:
                heading = Direction.NORTH;
                break;
        }
    }

    private static Direction rotate90deg (Direction hd) throws GameActionException{
        return hd.rotateRight().rotateRight();
    }

    private static Direction rotate90degCCW(Direction hd) throws GameActionException{
        return hd.rotateLeft().rotateLeft();
    }

    private static boolean isOccupied(RobotController rc, MapInfo tgL) throws GameActionException{
        return rc.canSenseRobotAtLocation(tgL.getMapLocation());

    }
    private static Direction rotate180deg(Direction hd) throws GameActionException{
        return rotate90deg(rotate90deg(hd));
    }

    

    private static boolean inAttackRange(RobotController rc, MapLocation attackPos){
        return rc.getLocation().distanceSquaredTo(attackPos) <= 9;
    }


    private static boolean isUnpainted(MapInfo paintPos, boolean isSecondary){
        PaintType corrPnT = isSecondary ? PaintType.ALLY_SECONDARY : PaintType.ALLY_PRIMARY;
        return (paintPos.getPaint() == PaintType.EMPTY || paintPos.getPaint() != corrPnT) && !paintPos.hasRuin();
    }

    public static boolean isSecondary(int i){
        switch (i) {
            case 0:
                return false;
        
            default:
                return true;
        }
    }

    public static boolean towerSet(MapInfo ruin, RobotController rc) throws GameActionException{
        for(int i = 3; i-->0;){
            switch (i) {
                case 1:
                    markPos = rc.senseMapInfo(ruin.getMapLocation().add(Direction.NORTH));
                    towerSet = markPos.getMark().equals(PaintType.EMPTY) && !towerSet ? false : true;
                    towerPattern = towerSet && towerPattern == Constants.towers.NULL ? Constants.towers.PAINT : towerPattern;
                
                case 2:
                    markPos = rc.senseMapInfo(ruin.getMapLocation().add(Direction.WEST));
                    towerSet = markPos.getMark().equals(PaintType.EMPTY) && !towerSet ? false : true;
                    towerPattern = towerSet && towerPattern == Constants.towers.NULL ? Constants.towers.DEFENSE : towerPattern;

                case 3:
                    markPos = rc.senseMapInfo(ruin.getMapLocation().add(Direction.SOUTH));
                    towerSet = markPos.getMark().equals(PaintType.EMPTY) && !towerSet ? false : true;
                    towerPattern = towerSet && towerPattern == Constants.towers.NULL ? Constants.towers.CHIP : towerPattern;
            }
        }

        return towerSet;
    }


    public static void setTower(MapLocation ruinPos, RobotController rc) throws GameActionException {
        switch (rng.nextInt(rc.getID())) {
            case 0:
                towerPattern = Constants.towers.PAINT;
                if(rc.canMark(ruinPos.add(Direction.NORTH))){
                    rc.mark(ruinPos.add(Direction.NORTH), true);
                }
                break;
        
            default:
                towerPattern = Constants.towers.CHIP;
                if(rc.canMark(ruinPos.add(Direction.SOUTH))){
                    rc.mark(ruinPos.add(Direction.SOUTH), true);
                }
                break;
        }
    }


    public static int getTowerPatternNum() {
        switch(towerPattern){
            case Constants.towers.PAINT:
                return GameConstants.PAINT_TOWER_PATTERN;
            case Constants.towers.CHIP:
                return GameConstants.MONEY_TOWER_PATTERN;
            default:
                return 0;
        }
    }

    public static boolean isAdjacentTo(RobotController rc, MapLocation tgL) throws GameActionException{
        return rc.getLocation().distanceSquaredTo(tgL) == 1;
        //return rc.getLocation().translate(0, 1).equals(tgL) || rc.getLocation().translate(0, -1).equals(tgL) || rc.getLocation().translate(-1, 0).equals(tgL) || rc.getLocation().translate(1, 0).equals(tgL);
    }

    public MapLocation refill(RobotController rc) throws GameActionException{
        MapInfo rf[] = rc.senseNearbyMapInfos(2);
        for(int i = 0; i < rf.length; i++){
            RobotInfo lc = rc.senseRobotAtLocation(rf[i].getMapLocation());
            if(lc != null && lc.type == UnitType.MOPPER){
                MapLocation markLoc = rc.getLocation().add(Direction.EAST);
                if(rc.canMark(markLoc)){
                    rc.mark(markLoc, false);
                }
                return rc.getLocation();
            }

            if(rf[i].hasRuin() && lc != null){
                return rf[i].getMapLocation();
            }
            
        }
        switch(retraceSteps(rc)){
            case null: 
                return null;
            default:
                return retraceSteps(rc);
        }
    }


    public  MapLocation retraceSteps(RobotController rc) throws GameActionException{
        MapLocation way = getLastPosition();
        switch(way){
            case null:
                return null;
            default:
                return way;
        }
    }

    //Limited Array STACK Methods
    public  void addPosition(MapLocation mp){
        if(this.head > Constants.NUM_OF_LOCATIONS_VISITED - 1){
            pstL[this.head % Constants.NUM_OF_LOCATIONS_VISITED] = pstL[(this.head-1) % Constants.NUM_OF_LOCATIONS_VISITED];
            this.head = 1;
        }
        pstL[this.head % Constants.NUM_OF_LOCATIONS_VISITED] = mp;
        this.head+=1;
    }

    private  MapLocation getLastPosition(){
        if(isEmpty()){
            head = 0;
            return null;
        }
        head-=1;
        return pstL[head];
    }

    public  boolean isEmpty(){
        return head == 0;
    }
    
    public String  printlst(){
        String s = "[";
        for(int i = 0; i < head; i++){
            if(pstL[i] != null){
                s = s + pstL[i].toString() + ", ";
            }
        }
        s += "]" + head % (Constants.NUM_OF_LOCATIONS_VISITED);
        return s;
    }
}
