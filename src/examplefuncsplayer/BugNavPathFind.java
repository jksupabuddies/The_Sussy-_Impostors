package examplefuncsplayer;
import battlecode.common.*;

public class BugNavPathFind {
    MapLocation targetLoc;
    MapLocation startLoc; 
    MapLocation nextStep;

    public BugNavPathFind(MapLocation tL, MapLocation sL){
        this.targetLoc = tL;
        this.startLoc = sL;
        this.nextStep = null;
    }

    public MapLocation move(RobotController rc) throws GameActionException{
        Direction tD = startLoc.directionTo(targetLoc);
        MapInfo nS = rc.senseMapInfo(startLoc.add(tD));
        MapInfo tgInfo = rc.senseMapInfo(targetLoc);
        MapInfo[] adjTileInfo = null;
        nextStep = nS.getMapLocation();
        if(rc.getLocation().equals(targetLoc)){
            return null;
        }
        else if(rc.getLocation().isAdjacentTo(targetLoc) && !tgInfo.isPassable() && rc.getLocation().directionTo(targetLoc).getDirectionOrderNum()%2 == 1){
            return null;
        }
        if(rc.canMove(tD)){
             rc.move(tD);
        }
        else if(nS.isWall() || nS.hasRuin()){
            adjTileInfo = rc.senseNearbyMapInfos(1); 
            int minDist = rc.getLocation().distanceSquaredTo(targetLoc);
            for(int i = 0; i < adjTileInfo.length; i++){
                if(!adjTileInfo[i].isPassable()){
                    if(minDist >= targetLoc.distanceSquaredTo(adjTileInfo[i].getMapLocation())){
                        minDist = targetLoc.distanceSquaredTo(adjTileInfo[i].getMapLocation());
                        nS = adjTileInfo[i];
                    }
                }
            }
            Direction wallDir = rc.getLocation().directionTo(nS.getMapLocation());
            rc.setIndicatorString(wallDir.toString());
            for(int i = 0; i < 7; i++){
                if(rc.canMove(wallDir) && rc.getLocation().add(wallDir).isAdjacentTo(nS.getMapLocation())){
                    break;
                }
                else{
                    wallDir = wallDir.rotateRight();
                }
                rc.setIndicatorDot(rc.getLocation().add(wallDir),255,100,155);
            }
            if(rc.canMove(wallDir)){
                rc.move(wallDir);
            }
        }
        return nextStep;
        
    }
    //Random commented out code:
    // Direction lD;
            // switch (rc.getLocation().directionTo(nextStep)) {
            //     case Direction.NORTH:
            //         if(rc.canMove(Direction.EAST)){
            //             lD =(Direction.EAST);
            //         }
            //         else{
            //             lD =(Direction.WEST);
            //         }
            //         break;
            //     case Direction.SOUTH:
            //         if(rc.canMove(Direction.EAST)){
            //             lD =(Direction.EAST);
            //         }
            //         else{
            //             lD =(Direction.WEST);
            //         }
            //         break;
            //     case Direction.EAST:
            //         if(rc.canMove(Direction.NORTH)){
            //             lD =(Direction.NORTH);
            //         }
            //         else{
            //             lD =(Direction.SOUTH);
            //         }
            //         break;
            //     case Direction.WEST:
            //         if(rc.canMove(Direction.NORTH)){
            //             lD =(Direction.NORTH);
            //         }
            //         else{
            //             lD =(Direction.SOUTH);
            //         }
            //         break;
            //     case Direction.NORTHEAST:
            //         if(rc.canMove(Direction.NORTH)){
            //             lD =(Direction.NORTH);
            //         }
            //         else{
            //             lD =(Direction.EAST);
            //         }
            //         break;
            //     case Direction.NORTHWEST:
            //         if(rc.canMove(Direction.NORTH)){
            //             lD =(Direction.NORTH);
            //         }
            //         else{
            //             lD =(Direction.WEST);
            //         }
            //         break;
            //     case Direction.SOUTHWEST:
            //         if(rc.canMove(Direction.SOUTH)){
            //             lD =(Direction.SOUTH);
            //         }
            //         else{
            //             lD =(Direction.WEST);
            //         }
            //         break;
            //     default:
            //         if(rc.canMove(Direction.SOUTH)){
            //             lD =(Direction.SOUTH);
            //         }
            //         else{
            //             lD =(Direction.EAST);
            //         }
            //         break;
            // }
            // nextStep = rc.getLocation().add(lD);
            // if(rc.canMove(lD)){
            //     rc.move(lD);
            // }
}
