package org.clas.modules;

public class ATOFBar {
    public int component = 0;
    public int sector = 0;
    public int layer = 0;
    //private double lbar = 300.0; //40cm paddles
    private double lbar = 279.7; //40cm paddles

    public double time_front = 0;
    public double time_back = 0;
    public double ToT_front = 0;
    public double ToT_back = 0;
    public double zhit = 0.0;
    public int PhiBlock = 0;

    public boolean hasTrackHit = false;

    public void setTrackZhit(double zhit){
        this.zhit = zhit;
    }

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
        if(veff == 0) veff = 200; //default;
        double l_front = lbar/2.0 - zhit;
        double l_back = lbar/2.0 + zhit;
        if(l_front > lbar) l_front = lbar;
        if(l_front < 0) l_front = 0;
        if(l_back > lbar) l_back = lbar;
        if(l_back < 0) l_back = 0;
        double tavg = (time_front - l_front/veff - twu)/2.0 + (time_back - l_back/veff - twd)/2.0;
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

}
