package examplefuncsplayer;
import battlecode.common.*;
public class Soldier {
    MapLocation[] pstL = new MapLocation[Constants.NUM_OF_LOCATIONS_VISITED];
    int head = 0;
   

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
