package org.clas.modules;

public class ATOFBar {
    public int component = 0;
    public int sector = 0;
    public int layer = 0;
    private double lbar = 279.7; //40cm paddles

    public double time_front = 0; //front is upstream (name needs to be changed and refactored)
    public double time_back = 0; //back is downstream
    public double ToT_front = 0;
    public double ToT_back = 0;
    public double zhit = 0.0;
    public int PhiBlock = 0; //an index of (0 to 59) of the azimuthal collection of bar + 10 wedges, clockwise looking downstream, 0 at 12 o'clock
    public int trackId = -1; //which reconstructed track is associated with the bar hit
    //propTime is the time the particle spends traveling from the vertex to the bar.
    public double propTime = 0; //time of track from vertex to the bar

    public boolean hasTrackHit = false; //true when there is an associated track to the hits on the bar

    public void setTrackZhit(double zhit){
        this.zhit = zhit;
    }
    public void setTrackId(int trackId){this.trackId = trackId;}
    public void setPropTime(double propTime){this.propTime = propTime;}

    public ATOFBar(int sector, int layer, int component, double time_front, double time_back, double ToT_front, double ToT_back){
        this.component = component;
        this.sector = sector;
        this.layer = layer;
        this.time_front = time_front;
        this.time_back = time_back;
        this.ToT_front = ToT_front;
        this.ToT_back = ToT_back;
        this.PhiBlock = 60 - sector*4 - layer;
    }

    public double getTavg(){
        return (time_back + time_front)/2.0;
    }

    public double getRedTavg(double veff){
        if(veff == 0) veff = 200; //default;
        return (time_back + time_front - lbar/veff)/2.0;
    }

    public double getTdiff(){
        return (time_back - time_front);
    }
    //only use tw offsets.
    public double getRedTdiff(double twu, double twd){
        double tdiff = time_front - twu - (time_back - twd);
        return (tdiff);
    }
    //only use veff offset
    public double getRedTdiff(double veff){
        if(veff == 0) veff = 200; //default;
        double l_front = lbar/2.0 - zhit;
        double l_back = lbar/2.0 + zhit;
        if(l_front > lbar) l_front = lbar;
        if(l_front < 0) l_front = 0;
        if(l_back > lbar) l_back = lbar;
        if(l_back < 0) l_back = 0;

        double tdiff = time_front - l_front/veff - (time_back - l_back/veff);
        return (tdiff);
    }

    //use all offsets
    public double getRedTdiff(double veff, double twu, double twd){


        if(veff == 0) veff = 200; //default;
        double l_front = lbar/2.0 - zhit;
        double l_back = lbar/2.0 + zhit;
        if(l_front > lbar) l_front = lbar;
        if(l_front < 0) l_front = 0;
        if(l_back > lbar) l_back = lbar;
        if(l_back < 0) l_back = 0;

        double tdiff = time_front - l_front/veff - twu - (time_back - l_back/veff - twd);


        //System.out.println(tdiff + "  " + zhit);
        return (tdiff);
    }
    public double avgBarHitTime(double veff, double twu, double twd){
        double tavg = getRedTavg(veff) - twu + twd;
        return tavg;
    }

    public double getZhit_fromTime(double veff, double twu, double twd){
        System.out.println(veff + "  " + veff/2.0*(time_front - twu - (time_back - twd)));
        return veff/2.0*(time_front - twu - (time_back - twd));
    }
    public double getLogRat(){
        System.out.println(Math.log(ToT_front/ToT_back));
        return Math.log(ToT_front/ToT_back);
    }
    public double getZeroTimeU(double vtime, double veff, double T0u, double twu){
        return time_front - propTime - vtime - T0u - twu - veff/(lbar/2.0 - zhit);
    }
    public double getZeroTimeD(double vtime, double veff, double T0d, double twd){
        return time_back - propTime - vtime - T0d - twd - veff/(lbar/2.0 + zhit);
    }

}
