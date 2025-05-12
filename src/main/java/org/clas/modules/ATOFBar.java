package org.clas.modules;
<<<<<<< Updated upstream

public class ATOFBar {
=======
import org.clas.modules.geom.ALERTGeomAlign;

/**
 *
 * Represents a hit in the atof bar. Is defined
 * by the two hits upstream and downstream composing a full bar hit. 
 *
 * @author mpaolone, npilleux
 */
public class ATOFBar {
    
    //For the ATOF geometry
    ALERTGeomAlign ALERT = new ALERTGeomAlign();
    
    //TO DO: Would like to make them all private with setters and getters
>>>>>>> Stashed changes
    public int component = 0;
    public int sector = 0;
    public int layer = 0;
    private double lbar = 279.7; //40cm paddles

    //TO DO: Would like to replace these with ATOFHit instead
    public double time_up = 0; //upstream
    public double time_down = 0;//downstream
    public double ToT_up = 0;
    public double ToT_down = 0;
    
    public double zhit = 0.0;
    //TO DO: To review, we may want this from the geometry service in the reconstruction
    public int PhiBlock = 0; //an index of (0 to 59) of the azimuthal collection of bar + 10 wedges, clockwise looking downstream, 0 at 12 o'clock
    
    //TO DO: To review, this is in the projection bank from the reconstruction
    public int trackId = -1; //which reconstructed track is associated with the bar hit
    
    public double propTime = 0; //time of track from vertex to the bar
    public boolean hasTrackHit = false; //true when there is an associated track to the hits on the bar

    public void setTrackZhit(double zhit){this.zhit = zhit;}
    public void setTrackId(int trackId){this.trackId = trackId;}
    public void setPropTime(double propTime){this.propTime = propTime;}

    public ATOFBar(int sector, int layer, int component, double time_front, double time_back, double ToT_front, double ToT_back){
        this.component = component;
        this.sector = sector;
        this.layer = layer;
        this.time_up = time_front;
        this.time_down = time_back;
        this.ToT_up = ToT_front;
        this.ToT_down = ToT_back;
        this.PhiBlock = 60 - sector*4 - layer;
    }
    
    public ATOFBar(ATOFHit hitUp, ATOFHit hitDown){
        if(hitUp.component!=10 || hitDown.component!=10){
            throw new IllegalArgumentException("These are not bar hits");
        }
        if(!((hitUp.order==0 && hitDown.order==1)||(hitUp.order==1 && hitDown.order==0))){
            throw new IllegalArgumentException("These are not complementary (order 0 and 1) bar hits");
        }
        this.component = hitUp.component;
        this.sector = hitUp.sector;
        this.layer = hitUp.layer;
        this.time_up = hitUp.time;
        this.time_down = hitDown.time;
        this.ToT_up = hitUp.ToT;
        this.ToT_down = hitDown.ToT;
        this.PhiBlock = 60 - this.sector*4 - this.layer;
    }

    public double getTavg(){return (time_down + time_up)/2.0;}

    public double getRedTavg(double veff){
<<<<<<< Updated upstream
        if(veff == 0) veff = 200; //default;
        return (time_back + time_front - lbar/veff)/2.0;
=======
        if(veff == 0) veff = 200; //default, this should NOT be hardcoded
        return (time_down + time_up - ALERT.getAtof_bar_length()/veff)/2.0;
>>>>>>> Stashed changes
    }

    public double getTdiff(){return (time_down - time_up);}
    //only use tw offsets.
    public double getRedTdiff(double twu, double twd){return time_up - twu - (time_down - twd);}
    //only use veff offset
    public double getRedTdiff(double veff){
<<<<<<< Updated upstream
        if(veff == 0) veff = 200; //default;
        double l_front = lbar/2.0 - zhit;
        double l_back = lbar/2.0 + zhit;
        if(l_front > lbar) l_front = lbar;
=======
        if(veff == 0) veff = 200; //default, this should NOT be hardcoded
        double l_front = ALERT.getAtof_bar_length()/2.0 - zhit;
        double l_back = ALERT.getAtof_bar_length()/2.0 + zhit;
        if(l_front > ALERT.getAtof_bar_length()) l_front = ALERT.getAtof_bar_length();
>>>>>>> Stashed changes
        if(l_front < 0) l_front = 0;
        if(l_back > lbar) l_back = lbar;
        if(l_back < 0) l_back = 0;
        double tdiff = time_up - l_front/veff - (time_down - l_back/veff);
        return (tdiff);
    }

    //use all offsets
    public double getRedTdiff(double veff, double twu, double twd){
<<<<<<< Updated upstream


        if(veff == 0) veff = 200; //default;
        double l_front = lbar/2.0 - zhit;
        double l_back = lbar/2.0 + zhit;
        if(l_front > lbar) l_front = lbar;
=======
        if(veff == 0) veff = 200; //default, this should NOT be hardcoded
        double l_front = ALERT.getAtof_bar_length()/2.0 - zhit;
        double l_back = ALERT.getAtof_bar_length()/2.0 + zhit;
        if(l_front > ALERT.getAtof_bar_length()) l_front = ALERT.getAtof_bar_length();
>>>>>>> Stashed changes
        if(l_front < 0) l_front = 0;
        if(l_back > lbar) l_back = lbar;
        if(l_back < 0) l_back = 0;
        double tdiff = time_up - l_front/veff - twu - (time_down - l_back/veff - twd);
        return (tdiff);
    }
    public double avgBarHitTime(double veff, double twu, double twd){
        double tavg = getRedTavg(veff) - twu + twd;
        return tavg;
    }

    public double getZhit_fromTime(double veff, double twu, double twd){
        System.out.println(veff + "  " + veff/2.0*(time_up - twu - (time_down - twd)));
        return veff/2.0*(time_up - twu - (time_down - twd));
    }
    public double getLogRat(){
        System.out.println(Math.log(ToT_up/ToT_down));
        return Math.log(ToT_up/ToT_down);
    }
    public double getZeroTimeU(double vtime, double veff, double T0u, double twu){
<<<<<<< Updated upstream
        return time_front - propTime - vtime - T0u - twu - veff/(lbar/2.0 - zhit);
    }
    public double getZeroTimeD(double vtime, double veff, double T0d, double twd){
        return time_back - propTime - vtime - T0d - twd - veff/(lbar/2.0 + zhit);
=======
        return time_up - propTime - vtime - T0u - twu - veff/(ALERT.getAtof_bar_length()/2.0 - zhit);
    }
    public double getZeroTimeD(double vtime, double veff, double T0d, double twd){
        return time_down - propTime - vtime - T0d - twd - veff/(ALERT.getAtof_bar_length()/2.0 + zhit);
>>>>>>> Stashed changes
    }
}
