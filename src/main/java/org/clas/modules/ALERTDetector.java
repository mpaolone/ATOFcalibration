package org.clas.modules;


// this

public class ALERTDetector {
    public double Test_ADC = 0.0;
    public double ToT_Front = 0.0;
    public double ToT_Back = 0.0;
    public double ADC_Top = 0.0;
    public double Test_Time = 0.0;
    public double Test_Energy = 0.0;
    public double XPOS = 0.0;
    public double YPOS = 0.0;
    public double ZPOS = 0.0;
    public double timeFront = 0.0;
    public double timeBack =0.0;
    public double timeTop=0.0;
    public double YAmp = 0.0;

    public int component = 0;
    public int order = 0;
    public int sector = 0;
   // public int super_layer = 0;
    public int layer = 0;
    public double RefTime=0.0;

    public boolean isBar = false;
    public boolean isWedge = false;

    public void setGeometry(int sector,int layer, int component){
        this.sector = sector;
        this.layer = layer;
        this.component = component;

    }

    public void set_ToT(double ToT_F,double ToT_B) {
        ToT_Front = ToT_F;
        ToT_Back = ToT_B;
    }

    public void set_Time(double TDC_F,double TDC_B) {
        timeFront = TDC_F;
        timeBack = TDC_B;
    }

    public void setPOS(double XPOS,double YPOS,double ZPOS) {
        this.XPOS = XPOS;
        this.YPOS = YPOS;
        this.ZPOS = ZPOS;
    }

    public double Y_Value(){
        double y = 0.0;

        y = YPOS;

        return y;
    }


    public double ReferenceTime(){
        // ATOF uses RF_Time along with starttime--- Need to update
        return RefTime;
    }


    public double TimeWalkCorL(){
        double tdcTimeF = timeFront  - 0 ;
        return tdcTimeF;
    }
    public double TimeWalkCorR(){
        double tdcTimeB = timeBack - 0  ; //Reference Time?
        return tdcTimeB;
    }

    public double HalfTime(){
        double timeL = TimeWalkCorL();
        double timeR = TimeWalkCorR();
        return ((timeL - timeR)/1);
    }

    public double VeffTestMethod(double xpos, double ypos,double test_ADC){
        YAmp = (ypos - xpos)/2;
        return YAmp;
    }


    public double veffTime(){
        double timeL = TimeWalkCorL();
        double timeR = TimeWalkCorR();
        return ((timeL - timeR )/2.0);
    }

}
