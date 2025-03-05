package org.clas.modules;

public class ATOFHit{

    public int sector = 0;
    public int component = 0;
    public int layer = 0;
    public int order = 0;
    public double time = 0;
    public double ToT = 0;
    private double tdc_to_time = 0.015625;

    public ATOFHit(int sector, int layer, int component, int order, double time, double ToT){
        this.sector = sector;
        this.component = component;
        this.layer = layer;
        this.order = order;
        this.time = time*tdc_to_time;
        this.ToT = ToT;
    }
}
