package examplefuncsplayer;

import battlecode.common.*;


public class BugNavPathFind {
    static MapLocation targetLoc = null;
    static MapLocation startLoc; 
    static MapLocation nextStep;
    static MapLocation currObs = null;
    static int heuristicDist = Integer.MAX_VALUE;
    static Direction tD;
    static Direction traceDir;
    static boolean obsOnright = false;
    public static Direction move(RobotController rc, MapLocation tL, MapLocation sL) throws GameActionException{
        //reset if target changes
        if(!tL.equals(targetLoc)){
            rc.setIndicatorString("ASSIGNED NEW TARGET" );
            currObs = null;
            heuristicDist = Integer.MAX_VALUE;
            obsOnright = false;
        }

        targetLoc = tL;
        startLoc = sL;
        Direction tD = sL.directionTo(tL);
        MapInfo tgInfo = null;
        rc.setIndicatorDot(targetLoc, 255, 0, 0);
        // ArrayList<Integer> debugList = new ArrayList<Integer>();
        rc.setIndicatorString("" + heuristicDist + currObs + tD);
        MapInfo nS = rc.senseMapInfo(startLoc.add(tD));

        if(rc.canSenseLocation(targetLoc)){
           tgInfo = rc.senseMapInfo(targetLoc);
        }

        

        MapInfo[] adjTileInfo = null;
        if(startLoc.equals(targetLoc) || (startLoc.distanceSquaredTo(targetLoc) == 1 && !tgInfo.isPassable())){
            currObs = null;
            heuristicDist = Integer.MAX_VALUE;
            obsOnright = false;
            rc.setIndicatorString("REACHED" );
            return null;
        } 
        // else if(rc.getLocation().isAdjacentTo(targetLoc)){
        //     //rc.getLocation().directionTo(targetLoc).getDirectionOrderNum()%2 == 1
        //     currObs = null;
        //     heuristicDist = Integer.MAX_VALUE;
        //     obsOnright = false;
        //     return null;
        // }

        if(currObs == null && rc.canMove(tD)){
            currObs = null;
            rc.setIndicatorDot(startLoc.add(tD), 0, 0 , 255);
            rc.setIndicatorString("Moving to target directly");
            heuristicDist = Integer.MAX_VALUE;
            obsOnright = false;
            return tD;
             
        }
        else{
            //rc.setIndicatorDot(nS.getMapLocation(),100 , 125 , 150 );
            if(!nS.isPassable()){
                currObs = nS.getMapLocation();
            }
            adjTileInfo = adjacientInfo(rc);
            // System.out.println(adjTileInfo.length);
            int minDist = Integer.MAX_VALUE - 1;
            for(int i = 0; i < adjTileInfo.length; i++){
                MapInfo cI = adjTileInfo[i];
                if(cI != null && cI.isPassable()){
                    // debugList.add(cI.getMapLocation().distanceSquaredTo(targetLoc));//debugging step
                    if(minDist > costCalc(cI, rc)){
                        minDist = costCalc(cI, rc);
                        if(heuristicDist > minDist){
                            heuristicDist = minDist;
                            currObs = null;
                            nS = cI;
                        }
                    }
                }
                // rc.setIndicatorString(debugList.toString());
            }
            //Did the Heuristic Distance Increase?
            //rc.setIndicatorDot(nS.getMapLocation(), 255, 100, 100);
            Direction wallDir = rc.getLocation().directionTo(nS.getMapLocation());
            if(rc.canMove(wallDir)){
                rc.setIndicatorDot(rc.getLocation().add(wallDir), 0, 255, 0);
                //rc.setIndicatorString("" + rc.getLocation().directionTo(nS.getMapLocation()));
                rc.setIndicatorString("closer!");
                return wallDir;
            }
            checkDirection(wallDir, rc.getLocation().directionTo(nS.getMapLocation()), rc);
        }
        rc.setIndicatorString("Later: " + heuristicDist + tD);
        traceDir = followWall(rc);
        //rc.setIndicatorDot(currObs, 255, 100, 100);
        checkDirection(traceDir, startLoc.directionTo(currObs), rc);
        return traceDir;
        // rc.setIndicatorString("" + traceWall(rc,traceDir));
        // traceDir = traceWall(rc, traceDir);
    }

    private static MapInfo[] adjacientInfo(RobotController rc) throws GameActionException{
        MapInfo[] adjacientList = new MapInfo[8];
        Direction init = Direction.NORTH;
        for(int i = 0; i < adjacientList.length; i++){
            MapLocation tile = rc.getLocation().add(init);
            if(rc.onTheMap(tile)){
                adjacientList[i] = rc.senseMapInfo(tile);
            }
            init = init.rotateRight();
        }
        return adjacientList;
    }

   private static void checkDirection(Direction Moving, Direction target, RobotController rc) throws GameActionException{
        Direction left = target.rotateLeft();
        for(int i = 8; i-- >0;){
            if(rc.canMove(left) && rc.canSenseLocation(startLoc.add(left))){
                break;
            }
            left = left.rotateLeft();
        }

        Direction right = target.rotateRight();
        for(int i = 8; i-- > 0;){
            if(rc.canMove(right) && rc.canSenseLocation(startLoc.add(right))){
                break;
            }
            right = right.rotateRight();
        }

        if(right.equals(Moving)){
            obsOnright = false;
            currObs = rc.adjacentLocation(right.rotateLeft());
            //rc.setIndicatorString("On right");
        }
        else{
            obsOnright = true;
            currObs = rc.adjacentLocation(left.rotateRight());
            //rc.setIndicatorString("On Left");
        }
   }


   private static Direction followWall(RobotController rc) throws GameActionException{
        Direction dir = rc.getLocation().directionTo(currObs);
        for(int i = 8; i-- > 0;){
            dir = obsOnright ? dir.rotateLeft() : dir.rotateRight();
            if(rc.canMove(dir) && rc.canSenseLocation(startLoc.add(dir))){
                break;
            }

            if(!rc.canSenseLocation(currObs)){
                obsOnright = !obsOnright;
            }
        }
        return dir;
   }

   private static int costCalc(MapInfo mpI, RobotController rc) throws GameActionException{
        int cost = mpI.getMapLocation().distanceSquaredTo(targetLoc);
        return cost;
    }
}
