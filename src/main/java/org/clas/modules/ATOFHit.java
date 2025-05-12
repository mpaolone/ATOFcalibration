package org.clas.modules;
import org.clas.modules.geom.ALERTGeomAlign;

/**
 *
 * Represents a hit in a single atof channel. 
 *
 * @author mpaolone, npilleux
 */
public class ATOFHit{
    //ATOF geometry
    ALERTGeomAlign ALERT = new ALERTGeomAlign();
    
    public int sector = 0;
    public int component = 0;
    public int layer = 0;
    public int order = 0;
    public double time = 0;
    public double ToT = 0;
    public double zeroTime = 0;
    //TO DO: This should be read from CCDB
    private double tdc_to_time = 0.015625;

    public ATOFHit(int sector, int layer, int component, int order, double time, double ToT){
        this.sector = sector;
        this.component = component;
        this.layer = layer;
        this.order = order;
        this.time = time*tdc_to_time;
        this.ToT = ToT;
        this.zeroTime = time*tdc_to_time;
    }
    
    public double getZ(){   
        return this.component * (ALERT.getAtof_wedge_gap() + ALERT.getAtof_wedge_length()) 
                - ALERT.getAtof_bar_length()+ ALERT.getAtof_wedge_length()/2;
    }

    public void setZeroTime(double vertexTime, double propTime){
        this.zeroTime = this.time - vertexTime - propTime;
    }
}
