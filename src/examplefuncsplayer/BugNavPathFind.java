package examplefuncsplayer;
import java.util.ArrayList;

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

    public Direction move(RobotController rc) throws GameActionException{
        Direction ret = null;
        Direction tD = startLoc.directionTo(targetLoc);
        // ArrayList<Integer> debugList = new ArrayList<Integer>();
        //rc.setIndicatorString("" + tD);
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
            ret = tD;
             //rc.move(tD);
             rc.setIndicatorDot(rc.getLocation().add(tD), 150, 120, 100);
        }
        else if(!nS.isPassable()){
            adjTileInfo = adjacientInfo(rc);
            // System.out.println(adjTileInfo.length);
            int minDist = rc.getLocation().distanceSquaredTo(targetLoc);
            for(int i = 0; i < adjTileInfo.length; i++){
                MapInfo cI = adjTileInfo[i];
                if(cI != null && cI.isPassable()){
                    // debugList.add(cI.getMapLocation().distanceSquaredTo(targetLoc));//debugging step
                    if(minDist > cI.getMapLocation().distanceSquaredTo(targetLoc)){
                        minDist = cI.getMapLocation().distanceSquaredTo(targetLoc);
                        nS = cI;
                    }
                }
                // rc.setIndicatorString(debugList.toString());
            }
            //rc.setIndicatorDot(nS.getMapLocation(), 255, 100, 100);
            Direction wallDir = rc.getLocation().directionTo(nS.getMapLocation());
            //rc.setIndicatorString(wallDir.toString() + rc.canMove(wallDir));
            // for(int i = 0; i < 8; i++){
            //     if(rc.canMove(wallDir) && rc.getLocation().add(wallDir).isAdjacentTo(nS.getMapLocation())){
            //         //rc.setIndicatorString("" + false);
            //         break;
            //     }
            //     else{
            //         wallDir = wallDir.rotateRight();
            //     }
            //     //if(rc.onTheMap(rc.getLocation().add(wallDir))){
            //         //rc.setIndicatorDot(rc.getLocation().add(wallDir),255,100,155);
            //    // }
            // }
            if(rc.canMove(wallDir)){
                ret = wallDir;
                // rc.move(wallDir);
                rc.setIndicatorDot(rc.getLocation().add(tD), 150, 120, 100);
            }
        }
        return ret;
        
    }

    private MapInfo[] adjacientInfo(RobotController rc) throws GameActionException{
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
