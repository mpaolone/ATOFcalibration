package org.clas.modules;

public class ATOFBarWedgeClust {
    public ATOFBar bar;
    public int component = 0;
    public int sector = 0;
    public int layer = 0;
    private double lwedge = 20.0; //length of bar front to pmt on back of wedge in cm
    private double barVeff = 200.0;
    private double barUpTW = 0.0;
    private double barDownTW = 0.0;
    private double barThickness = 3.0;
    private double tcut = 5.0; //time cut for veff calc.  both abr and wedge time must be smaller than this

    public double wedgeTime = 0.0;
    public double wedgeToT = 0.0;

    public ATOFBarWedgeClust(ATOFBar bar, int sector, int layer, int component, double wedgeTime, double wedgeToT){
        this.bar = bar;
        this.sector = sector;
        this.layer = layer;
        this.component = component;
        this.wedgeTime = wedgeTime;
        this.wedgeToT = wedgeToT;
    }
    public void setBarParams(double veff, double twu, double twd){
        this.barVeff = veff;
        this.barUpTW = twu;
        this.barDownTW = twd;
    }

    //use all offsets
    public double getRedTdiff(double veff, double tw){
        double lrat = 0;
        if(veff != 0){
            lrat = lwedge/veff;
        }
        double timeBar = bar.avgBarHitTime(barVeff, barUpTW, barDownTW);
        double tdiff = wedgeTime - lrat - tw - timeBar;
        return (tdiff);
    }
    public double calcVeff(double tw){
        double barTime = bar.avgBarHitTime(barVeff, barUpTW, barDownTW);
        double veff = lwedge/(wedgeTime - tw - barTime - barThickness/barVeff);
        System.out.println("times: " + wedgeTime + "  " + tw + "  " + barTime + "  " + barThickness/barVeff + "  " + (wedgeTime - tw - barTime - barThickness/barVeff));
        if((barTime > tcut) || (wedgeTime > tcut)){
            return 1.0e10;
        }
        return veff;
    }
    public double getZeroTime(double vtime, double T0, double tw, double veff){
        return wedgeTime - bar.propTime - vtime - T0 - tw - lwedge/veff;
    }


    public double getTdiff(double veff, double tw){
        return wedgeTime - lwedge/veff - tw - bar.getRedTavg(veff);
    }
}
