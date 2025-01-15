package examplefuncsplayer;

import battlecode.common.*;

public class BugNavPathFind {
    static MapLocation targetLoc;
    static MapLocation startLoc; 
    static MapLocation nextStep;
    static MapLocation[] pastLocation = new MapLocation[1];
    static Direction init = Direction.NORTH;
    
    public static Direction move(RobotController rc, MapLocation tL, MapLocation sL) throws GameActionException{
        targetLoc = tL;
        startLoc = sL;
        Direction tD = startLoc.directionTo(targetLoc);
        rc.setIndicatorDot(targetLoc, 255, 100, 100);
        // ArrayList<Integer> debugList = new ArrayList<Integer>();
        rc.setIndicatorString("" + pastLocation[0]);
        MapInfo nS = rc.senseMapInfo(startLoc.add(tD));
        MapInfo tgInfo = rc.senseMapInfo(targetLoc);
        MapInfo[] adjTileInfo = null;
        if(rc.getLocation().equals(targetLoc)){
            return null;
        } 
        else if(rc.getLocation().isAdjacentTo(targetLoc) && !tgInfo.isPassable()){
            //rc.getLocation().directionTo(targetLoc).getDirectionOrderNum()%2 == 1
            return null;
        }
        if(rc.canMove(tD)){
            init = tD;
            pastLocation[0] = startLoc;
            return init;
             
        }
        else if(!nS.isPassable()){
            adjTileInfo = adjacientInfo(rc);
            // System.out.println(adjTileInfo.length);
            int minDist = 1000;
            for(int i = 0; i < adjTileInfo.length; i++){
                MapInfo cI = adjTileInfo[i];
                if(cI != null && cI.isPassable() && (!cI.getMapLocation().equals(pastLocation[0]) || pastLocation == null)){
                    // debugList.add(cI.getMapLocation().distanceSquaredTo(targetLoc));//debugging step
                    if(minDist > costCalc(cI, rc)){
                        minDist = costCalc(cI, rc);
                        nS = cI;
                    }
                }
                // rc.setIndicatorString(debugList.toString());
            }
            //rc.setIndicatorDot(nS.getMapLocation(), 255, 100, 100);
            Direction wallDir = rc.getLocation().directionTo(nS.getMapLocation());
            if(rc.canMove(wallDir)){
                //rc.setIndicatorDot(rc.getLocation().add(wallDir), 150, 120, 100);
                pastLocation[0] = startLoc;
                return wallDir;
               
            }
        }
        return null;
        
    }

    private static MapInfo[] adjacientInfo(RobotController rc) throws GameActionException{
        MapInfo[] adjacientList = new MapInfo[8];
        for(int i = 0; i < adjacientList.length; i++){
            MapLocation tile = rc.getLocation().add(init);
            if(rc.onTheMap(tile)){
                adjacientList[i] = rc.senseMapInfo(tile);
            }
            init = init.rotateRight();
        }
        return adjacientList;
    }

    private static int costCalc(MapInfo mpI, RobotController rc) throws GameActionException{
        int cost = mpI.getMapLocation().distanceSquaredTo(targetLoc);
        switch(mpI.getPaint()){
            case PaintType.EMPTY: 
                cost += 2;
                break;
            case PaintType.ALLY_PRIMARY:
                cost += 1;
                break;
            case PaintType.ALLY_SECONDARY:
                cost += 1;
                break;
            case PaintType.ENEMY_PRIMARY:
                cost += 2;
                break;
            case PaintType.ENEMY_SECONDARY:
                cost += 2;
                break;
        }
        return cost;
    }

    private static int costCalc(MapLocation mp, RobotController rc) throws GameActionException{
        MapInfo mpI = rc.senseMapInfo(mp);
        int cost = mp.distanceSquaredTo(targetLoc);
        switch(mpI.getPaint()){
            case PaintType.EMPTY: 
                cost += 2;
                break;
            case PaintType.ALLY_PRIMARY:
                cost += 1;
                break;
            case PaintType.ALLY_SECONDARY:
                cost += 1;
                break;
            case PaintType.ENEMY_PRIMARY:
                cost += 2;
                break;
            case PaintType.ENEMY_SECONDARY:
                cost += 2;
                break;
        }
        return cost;
    }
    
    private boolean isRobot(MapLocation mp, RobotController rc) throws GameActionException{
        return rc.senseRobotAtLocation(mp) == null;
    }
}
