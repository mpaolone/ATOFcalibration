package org.clas.modules;
import org.clas.modules.geom.ALERTGeomAlign;
public class ATOFBarWedgeClust {
    ALERTGeomAlign ALERT = new ALERTGeomAlign();
    public ATOFBar bar;
    public int component = 0;
    public int sector = 0;
    public int layer = 0;
    private double barVeff = 200.0;
    private double barUpTW = 0.0;
    private double barDownTW = 0.0;
    private double tcut = 5.0; //time cut for veff calc.  both abr and wedge time must be smaller than this

    public double wedgeTime = 0.0;
    public double wedgeToT = 0.0;
    public double wedgeZ = 0.0;   
    
    public ATOFBarWedgeClust(ATOFBar bar, int sector, int layer, int component, double wedgeTime, double wedgeToT, double wedgeZ){
        this.bar = bar;
        this.sector = sector;
        this.layer = layer;
        this.component = component;
        this.wedgeTime = wedgeTime;
        this.wedgeToT = wedgeToT;
        this.wedgeZ = wedgeZ;
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
            lrat = ALERT.getWedgeThickness()/veff;
        }
        double timeBar = bar.avgBarHitTime(barVeff, barUpTW, barDownTW);
        double tdiff = wedgeTime - lrat - tw - timeBar;
        return (tdiff);
    }
    public double calcVeff(double tw){
        double barTime = bar.avgBarHitTime(barVeff, barUpTW, barDownTW);
        double veff = ALERT.getWedgeThickness()/(wedgeTime - tw - barTime 
                - ALERT.getBarThickness()/barVeff);
        System.out.println("times: " + wedgeTime + "  " + tw 
                + "  " + barTime + "  " + ALERT.getBarThickness()/barVeff 
                + "  " + (wedgeTime - tw - barTime - ALERT.getBarThickness()/barVeff));
        if((barTime > tcut) || (wedgeTime > tcut)){
            return 1.0e10;
        }
        return veff;
    }
    public double getZeroTime(double vtime, double T0, double tw, double veff){
        return wedgeTime - bar.propTime - vtime 
                - T0 - tw - ALERT.getWedgeThickness()/veff;
    }


    public double getTdiff(double veff, double tw){
        return wedgeTime - ALERT.getWedgeThickness()/veff 
                - tw - bar.getRedTavg(veff);
    }
}
