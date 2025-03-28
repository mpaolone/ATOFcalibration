package org.clas.modules;

import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.clas.modules.utils.ALERTDatabaseConstantProvider;
import org.clas.modules.ALERTCalibrationEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

import java.util.ArrayList;
import java.util.List;

public class ALERTDataStructs implements IDataEventListener{
    public DataGroup SCDG;
    public static IndexedList<DataGroup> dataGroups = new IndexedList<DataGroup>(3);
    //public static H1F Pedestal[][][] = new H1F [16][3][5];
    public static H1F Pedestal[][][][] = new H1F [15][4][11][2]; //component layer order
    public static H1F fbAlign[][] = new H1F [15][4]; //component
    public static H2F TW[][][] = new H2F [15][4][12]; //component layer order
    public static H2F Veff[][][] = new H2F [15][4][11]; //component
    public static H1F wbAlign[][][] = new H1F [15][4][12]; //component
    public static H1F VeffWedge[][][] = new H1F[15][4][11];
    public static H2F AttenLen[][] = new H2F[15][4];
    public static H2F T0;
   // public static ALERTDatabaseConstantProvider dcp = new ALERTDatabaseConstantProvider();
   // public static ALERTDatabaseConstantProvider dcp = null;
    public static CalibrationConstants timeOffConsts = null;
    public static CalibrationConstants twConsts = null;
    public static CalibrationConstants veffConsts = null;
    public static CalibrationConstants attlenConsts = null;
    public double twEval(double x, double p0,double p1,double p2){
        F1D f2 = new F1D("f2", "[a] + [b]*x + [c]*x*x", 0.0, 2000.0);
        f2.setParameter(0,p0);
        f2.setParameter(1,p1);
        f2.setParameter(2,p2);
        return f2.evaluate(x);
    }
    private double veff_default = 200.0; //??
    private double tw_default = 0.0;
    private double attlen_default = 1.0;
    private double t0_default = 0.0;

    private static int numSector = 15;
    private static int numLayer = 4;
    private static int numComp = 11;
    private static int numOrder = 2;

    public int PMTtoIndex(int sector, int layer, int component, int order){
        int indexOrder = component + order;
        int indexN = sector*(numLayer*(numComp+1)) + layer*(numComp + 1) + indexOrder;
        return indexN;
    }

    public ALERTDataStructs(CalibrationConstants timeOffConsts,
                            CalibrationConstants twConsts,
                            CalibrationConstants veffConsts,
                            CalibrationConstants attlenConsts){
        this.timeOffConsts = timeOffConsts;
        this.twConsts = twConsts;
        this.veffConsts = veffConsts;
        this.attlenConsts = attlenConsts;
    }
    //@Override


    public void dataEventAction(DataEvent dataEvent) {
        System.out.println("In Data Struct");
        ALERTCalibrationEngine CalibrationRoutines = new ALERTCalibrationEngine();
        FillData(dataEvent,CalibrationRoutines.PassModule);

    }

    public void timerUpdate() {

    }

    public void resetEventListener() {

    }

    public int XYtoPhiBlock(double x, double y){
        int ret = 0;
        //System.out.println(x + "  " + y + "  " + Math.toDegrees(Math.atan2(x,y)));
        double ang = Math.toDegrees(Math.atan2(x,y));
        if(ang < 0) ang += 360.0;
        ret = (int)Math.ceil((ang + 6.0)*60.0/360.0) - 1;
        if(ret >=60) ret -= 60;
        return ret;
    }

    public ArrayList<ATOFHit> getATOFHits(DataEvent event){
        //System.out.println("in event");
        ArrayList<ATOFHit> hitList = new ArrayList<ATOFHit>();
        DataBank adcBank = event.getBank("ATOF::tdc");
        if (event.hasBank("ATOF::tdc")) {
            for (int i = 0; i < adcBank.rows(); i++) {
                int sector1 = adcBank.getInt("sector", i);
                int layer1 = adcBank.getInt("layer", i);
                int order1 = adcBank.getInt("order", i);
                int component1 = adcBank.getInt("component", i);
                double time1 = adcBank.getInt("TDC", i);
                double ToT1 = adcBank.getInt("ToT", i);
                //double time1 = adcBank.getFloat("time", i);
                //double ToT1 = adcBank.getInt("ADC", i);

                //System.out.println(time1 + "  " + ToT1);

                ATOFHit hit = new ATOFHit(sector1, layer1, component1, order1, time1, ToT1);
                hitList.add(hit);
            }
        }
        return hitList;
    }

    public ArrayList<ATOFBarWedgeClust> getClusts(ArrayList<ATOFHit> hitList, ArrayList<ATOFBar> barList){
        ArrayList<ATOFHit> wedgeList = new ArrayList<ATOFHit>();
        ArrayList<ATOFBarWedgeClust> clustList = new ArrayList<ATOFBarWedgeClust>();
        for (ATOFHit hit : hitList) {
            if (hit.component != 10) {
                    wedgeList.add(hit);
            }
        }
        for(ATOFBar bar : barList){
            for(ATOFHit wedge : wedgeList){
                if((wedge.sector == bar.sector) && (wedge.layer == bar.layer)){
                    ATOFBarWedgeClust bwc = new ATOFBarWedgeClust(bar, wedge.sector, wedge.layer, wedge.component, wedge.time, wedge.ToT);
                    clustList.add(bwc);
                }
            }
        }
        return clustList;
    }

    public void setTracksMC(DataEvent event, ArrayList<ATOFBar> barList){
        DataBank trackBank = event.getBank("MC::True");
        if (event.hasBank("MC::True")) {
            //System.out.println("found MC bank");
            for (int i = 0; i < trackBank.rows(); i++) {
                int detector = trackBank.getInt("detector",i);
                double x = trackBank.getFloat("avgX",i);
                double y = trackBank.getFloat("avgY",i);
                double z = trackBank.getFloat("avgZ",i);
                //System.out.println(i + "  " + detector);
                if(detector == 24){
                    int PhiBlock = XYtoPhiBlock(x,y);
                    for (ATOFBar bar : barList) {
                        //System.out.println(PhiBlock + " " + bar.PhiBlock + "  " + bar.sector+ " " + bar.layer);
                        if(PhiBlock == bar.PhiBlock){
                            bar.setTrackZhit(z);
                            bar.setTrackId(0);
                        }
                    }
                }
            }
        }
    }
    public void setTracks(DataEvent event, ArrayList<ATOFBar> barList){
        DataBank projBank = event.getBank("ALERT::Projections");
        DataBank trackBank = event.getBank("AHDC::Track");
        //if (event.hasBank("ALERT::Projections")) System.out.println("has proj banks");
        if (event.hasBank("ALERT::Projections") && event.hasBank("AHDC::Track")) {
            //System.out.println("num of rows in projBank: " + projBank.rows());
            //System.out.println("num of rows in trackBank: " + trackBank.rows());
            for (int i = 0; i < projBank.rows(); i++) {
                double x = projBank.getFloat("x_at_bar",i);
                //int id = projBank.getInt("trackID",i);
                int id = 0;
                //System.out.println("x: " + x);
                double y = projBank.getFloat("y_at_bar",i);
                double z = projBank.getFloat("z_at_bar",i);
                double L = projBank.getFloat("L_at_bar",i);

                double px = trackBank.getFloat("px",i);
                double py = trackBank.getFloat("py",i);
                double pz = trackBank.getFloat("pz",i);
                double p = Math.sqrt(px*px + py*py + pz*pz)/1000.0;
                double M = 0.938272; //set to proton now, but should be set according to PID!!!
                double E = Math.sqrt(p*p + M*M);
                double beta = p/E;
                double v = 2.998e11*beta;
                double ptime = L/v*1.0e9;
                System.out.println("ptime: " + ptime);

                int PhiBlock = XYtoPhiBlock(x,y);
                for (ATOFBar bar : barList) {
                    if(Math.abs(PhiBlock - bar.PhiBlock) == 0){
                        bar.setTrackZhit(z);
                        bar.setTrackId(id);
                        bar.setPropTime(ptime);
                    }else if(Math.abs(PhiBlock - bar.PhiBlock) <= 1){
                        bar.setTrackZhit(z);
                        bar.setTrackId(id);
                        bar.setPropTime(ptime);
                    }
                }
            }

        }
    }

    public float getFDvtime(DataEvent event){
        DataBank recBank = event.getBank("REC::Particle");
        float avgVtime = 0;
        float nelec = 0;
        if (event.hasBank("REC::Particle") ){
            for (int i = 0; i < recBank.rows(); i++) {
                int id = recBank.getInt("pid",i);
                float vt = recBank.getFloat("vt",i);
                if(id == 11){
                    avgVtime += vt;
                    nelec++;
                }
            }
            avgVtime *= 1.0/nelec;
        }
        return avgVtime;
    }

    public ArrayList<ATOFBar> getBars(ArrayList<ATOFHit> hitList){
        ArrayList<ATOFBar> barList = new ArrayList<ATOFBar>();
        ArrayList<ATOFHit> hitList_0 = new ArrayList<ATOFHit>();
        ArrayList<ATOFHit> hitList_1 = new ArrayList<ATOFHit>();
        //System.out.println("in getBars list");
        for (ATOFHit hit : hitList) {
            if(hit.order == 1) System.out.println(hit.component);
            if (hit.component == 10 && hit.order == 0) {
                hitList_1.add(hit);
            }
            if (hit.component == 10 && hit.order == 1) {
                hitList_0.add(hit);
            }
        }
        for(ATOFHit hit0 : hitList_0) {
            for (ATOFHit hit1 : hitList_1) {
                if (hit0.component == hit1.component) {
                    ATOFBar bar = new ATOFBar(hit0.sector, hit0.layer, hit0.component, hit0.time, hit1.time, hit0.ToT, hit1.ToT);
                    barList.add(bar);
                }
            }
        }
        return barList;
    }
/*
    public static List<ALERTDetector> getData(DataEvent event){


        ArrayList<ATOFBarWedgeClust> barList = new ArrayList<ATOFBarWedgeClust>();
        ArrayList<ALERTDetector> ALERTList = new ArrayList<ALERTDetector>();
//        DataBank adcBank = event.getBank("ATOF::adc");
        DataBank tdcBank = event.getBank("ATOF::tdc");
        // NOTE:  This is an MC bank with position information.  This must be changed
        //  to the appropriate hit reconstruction bank
        DataBank trackBank = event.getBank("MC::true");

        //determine bar hits:

        ALERTDetector DataSummary = new ALERTDetector();
        DataSummary.setGeometry(
                tdcBank.getInt("sector",0),
                tdcBank.getInt("superlayer",0),
                tdcBank.getInt("layer",0)
        );

//        DataSummary.set_TDC(tdcBank.getDouble("tdc_front",0),tdcBank.getDouble("tdc_back",0) );
        DataSummary.setPOS(trackBank.getDouble("xpos",0),
                trackBank.getDouble("ypos",0),
                trackBank.getDouble("zpos",0));
        DataSummary.set_ADC(adcBank.getDouble("adc_front",0),
                adcBank.getDouble("adc_back",0));
        DataSummary.Test_Energy = adcBank.getDouble("energy_front",0);
        DataSummary.YAmp = ALERTDataTransformer(DataSummary).YAmp;
        ALERTList.add(DataSummary);
        return ALERTList;
    }
    */

    public void FillData(DataEvent event, String name) {

        //first get list of bars and clusters:

        ArrayList<ATOFHit> hits = getATOFHits(event);
        ArrayList<ATOFBar> bars = getBars(hits);
        setTracks(event,bars);
        //setTracksMC(event,bars);
        double vtime = getFDvtime(event);
        ArrayList<ATOFBarWedgeClust> clusts = getClusts(hits, bars);


        int sector = 0;
        int component = 0;
        int layer = 0;
        int order = 0;

//        List<ALERTDetector> paddle = new ArrayList<ALERTDetector>();

        ALERTDataStructs Pass = new ALERTDataStructs(timeOffConsts, twConsts, veffConsts, attlenConsts);
//        paddle = getData(event);


        if(name.equals("Pedestal")){
            for (ATOFHit hit : hits) {
                sector = hit.sector;
                component = hit.component;
                layer = hit.layer;
                order = hit.order;
                Pedestal[sector][layer][component][order].fill(hit.ToT);

            }

            if (event.getType() == DataEventType.EVENT_STOP) {
                for (int i =0;i<15;i++) {
                    for (int j = 0; j < 4; j++) {
                        for (int k = 0; k < 11; k++) {
                            for(int l =0; l < 1; l++) {
                                DataGroup TempGroup2 = new DataGroup(1, 1);
                                TempGroup2.addDataSet(Pedestal[i][j][k][l], 0);
                                //dataGroups.add(TempGroup2, i, j, k, l);
                                dataGroups.add(TempGroup2, i, j, k);
                            }
                        }
                    }
                }
                System.out.println("Passing to DG_Passer");
                Pass.DG_Passer(dataGroups,name);
            }
        }



        else if(name.equals("FBAlign")){
            for (ATOFBar bar : bars) {
                sector = bar.sector;
                layer = bar.layer;
                // ** This is being filled with a junk place holder!
                //fbAlign[sector][super_layer][layer].fill(dr.ADC_Front);
                fbAlign[sector][layer].fill(bar.getTdiff());
                //Pass.DG_Passer(dataGroups,name);
            }

            if (event.getType() == DataEventType.EVENT_STOP) {
                for (int i =0;i<15;i++) {
                    for (int j = 0; j < 4; j++) {
                            DataGroup TempGroup2 = new DataGroup(1, 1);
                            TempGroup2.addDataSet(fbAlign[i][j], 0);
                            dataGroups.add(TempGroup2, i, j,10);
                    }
                }
                System.out.println("Passing to DG_Passer");
                Pass.DG_Passer(dataGroups,name);
            }
        }

        else if (name.equals("Veff")) {
            for (ATOFBar bar : bars) {
                sector = bar.sector;
                layer = bar.layer;
                component = bar.component;
                double veff = veffConsts.getDoubleValue("veff",sector,layer,component);
                if(veff == 0){ veff = veff_default;}
                double twu = twEval(bar.ToT_front,
                        twConsts.getDoubleValue("tw0",sector,layer,component,0),
                        twConsts.getDoubleValue("tw1",sector,layer,component,0),
                        twConsts.getDoubleValue("tw2",sector,layer,component,0));

                double twd = twEval(bar.ToT_back,
                        twConsts.getDoubleValue("tw0",sector,layer,component,1),
                        twConsts.getDoubleValue("tw1",sector,layer,component,1),
                        twConsts.getDoubleValue("tw2",sector,layer,component,1));
                double veff_new = bar.getRedTdiff(twu,twd);
                //Veff[sector][layer][component].fill(bar.zhit, bar.getRedTdiff(veff,twu,twd));
                if(bar.zhit != 0.0) {
                    Veff[sector][layer][component].fill(bar.zhit, veff_new);
                }
                //System.out.println(event.getType());
            }
            for (ATOFBarWedgeClust clust : clusts) {
                sector = clust.sector;
                layer = clust.layer;
                component = clust.component;
                double twu = twEval(clust.bar.ToT_front,
                        twConsts.getDoubleValue("tw0",sector,layer,10,0),
                        twConsts.getDoubleValue("tw1",sector,layer,10,0),
                        twConsts.getDoubleValue("tw2",sector,layer,10,0));

                double twd = twEval(clust.bar.ToT_back,
                        twConsts.getDoubleValue("tw0",sector,layer,10,1),
                        twConsts.getDoubleValue("tw1",sector,layer,10,1),
                        twConsts.getDoubleValue("tw2",sector,layer,10,1));
                double veff_bar = veffConsts.getDoubleValue("veff",sector,layer,10);

                if(veff_bar < 100 || veff_bar > 300) {
                    clust.setBarParams(200.0, twu, twd);
                }else{
                    clust.setBarParams(veff_bar, twu, twd);
                }
                double tww = twEval(clust.wedgeToT,
                        twConsts.getDoubleValue("tw0",sector,layer,component,0),
                        twConsts.getDoubleValue("tw1",sector,layer,component,0),
                        twConsts.getDoubleValue("tw2",sector,layer,component,0));

                double veff_wedge = clust.calcVeff(tww);
                VeffWedge[sector][layer][component].fill(veff_wedge);
            }
            if (event.getType() == DataEventType.EVENT_STOP) {
                //System.out.println("the last event");
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 4; j++) {
                        for (int k = 0; k <= 10; k++) {
                            DataGroup TempGroup2 = new DataGroup(1, 1);
                            if(k <= 9){
                                TempGroup2.addDataSet(VeffWedge[i][j][k], 0);
                            }else {
                                TempGroup2.addDataSet(Veff[i][j][k], 0);
                            }
                            dataGroups.add(TempGroup2, i, j, k);
                        }
                    }
                }
                Pass.DG_Passer(dataGroups, name);
            }
        }

        else if (name.equals("Atten")){
            for (ATOFBar bar : bars){
                layer = bar.layer;
                sector = bar.sector;
                double twu = twEval(bar.ToT_front,
                        twConsts.getDoubleValue("tw0",sector,layer,10,0),
                        twConsts.getDoubleValue("tw1",sector,layer,10,0),
                        twConsts.getDoubleValue("tw2",sector,layer,10,0));

                double twd = twEval(bar.ToT_back,
                        twConsts.getDoubleValue("tw0",sector,layer,10,1),
                        twConsts.getDoubleValue("tw1",sector,layer,10,1),
                        twConsts.getDoubleValue("tw2",sector,layer,10,1));
                double veff_bar = veffConsts.getDoubleValue("veff",sector,layer,10);
                if(veff_bar == 0) veff_bar = veff_default;
                AttenLen[sector][layer].fill(bar.getZhit_fromTime(veff_bar, twu, twd),bar.getLogRat());
            }
            if (event.getType() == DataEventType.EVENT_STOP) {
                for (int i =0;i<15;i++){
                    for (int j=0;j<4;j++){
                        for (int k=0;k<=11;k++) {
                            DataGroup TempGroup2 = new DataGroup(1, 1);
                            TempGroup2.addDataSet(AttenLen[i][j], 0);
                            dataGroups.add(TempGroup2, i, j, k);
                        }
                    }
                }
                Pass.DG_Passer(dataGroups,name);
            }
        }

        else if( name.equals("T0")){
            System.out.println("T0");
            /*
            for (ATOFHit hit : hits) {
                sector = hit.sector;
                component = hit.component;
                layer = hit.layer;
                order = hit.order;
                T0.fill(PMTtoIndex(sector, layer, component, order), hit.time);
            }
            */

            for (ATOFBar bar : bars) {
                T0.fill(PMTtoIndex(bar.sector, bar.layer, 10, 0), bar.time_front - bar.propTime);
                T0.fill(PMTtoIndex(bar.sector, bar.layer, 10, 1), bar.time_back - bar.propTime);
                fbAlign[bar.sector][bar.layer].fill(bar.getTdiff());
            }

            for(ATOFBarWedgeClust clust : clusts){
                T0.fill(PMTtoIndex(clust.sector,clust.layer,clust.component, 0), clust.wedgeTime - clust.bar.propTime);
                wbAlign[clust.sector][clust.layer][clust.component].fill(clust.getTdiff(veff_default,0.0));
            }


            if (event.getType() == DataEventType.EVENT_STOP) {
                System.out.println("at Event stop");
                for (int i =0;i<15;i++){
                    for (int j=0;j<4;j++){
                        for (int k=0;k<=11;k++) {
                            DataGroup TempGroup2 = new DataGroup(3, 1);
                            TempGroup2.addDataSet(T0, 0);
                            TempGroup2.addDataSet(fbAlign[i][j], 1);
                            TempGroup2.addDataSet(wbAlign[i][j][k], 2);
                            dataGroups.add(TempGroup2, i, j, k);
                        }
                    }
                }
                System.out.println("at end of adding datagroup");
                dataGroups.show();
                Pass.DG_Passer(dataGroups,name);
            }
        }

        else if (name.equals("TW")) {
            for (ATOFBar bar : bars){
                component = bar.component;
                sector = bar.sector;
                layer = bar.layer;
                //TW[sector][super_layer][layer].fill(dr.ADC_Front,dr.timeFront);
                //no T0 offset and default veff is used.
                TW[sector][layer][component].fill(bar.ToT_front, bar.getZeroTimeU(vtime,veff_default,0,0));
                TW[sector][layer][component + 1].fill(bar.ToT_back, bar.getZeroTimeD(vtime,veff_default,0,0));
            }
            for (ATOFBarWedgeClust clust : clusts){
                component = clust.component;
                sector = clust.sector;
                layer = clust.layer;
                //TW[sector][super_layer][layer].fill(dr.ADC_Front,dr.timeFront);
                TW[sector][layer][component].fill(clust.wedgeToT, clust.getZeroTime(vtime,0,0,veff_default));
            }


            if (event.getType() == DataEventType.EVENT_STOP) {
                for (int i =0;i<15;i++){
                    for (int j=0;j<4;j++){
                        for (int k=0;k<=11;k++) {
                            DataGroup TempGroup2 = new DataGroup(1, 1);
                            TempGroup2.addDataSet(TW[i][j][k], 0);
                            dataGroups.add(TempGroup2, i, j, k);

                        }
                    }
                }
                Pass.DG_Passer(dataGroups,name);
            }
        }
    }

        public void DG_Passer(IndexedList<DataGroup>  ILdg, String name){
            //SCDG = ILdg;
            ALERTCalibrationEngine Send_To_Engine = new ALERTCalibrationEngine();
            Send_To_Engine.CalibHandler(ILdg,name);

            //return SCDG;
        }
    /*
    public static ALERTDetector ALERTDataTransformer(ALERTDetector DataSum){
        // apply method in ALERTDetector to get the final form of the data (apply corrections)
        double test_Veff = 0.0;

        ALERTDetector Transformer = new ALERTDetector();
        Transformer.YAmp= Transformer.VeffTestMethod(DataSum.XPOS,DataSum.YPOS,DataSum.Test_ADC);
        //Transformer.VeffTestMethod(ls.XPOS, ls.YPOS));
        return Transformer;
    }
    */




    public void Create_Fill_Histo2D(String Module){
        if (Module.equals("Pedestal")) {
            System.out.println("Initialize Pedestal Histograms");
            for (int i=0; i<15;i++){
                for (int j = 0; j < 4; j++){
                    for (int k=0;k<=10;k++){
                        for (int l=0; l<1;l++) {
                            String Hist_Name = String.format("Pedestal_%d_%d_%d_%d", i, j, k, l);
                            Pedestal[i][j][k][l] = new H1F("Pedestal", Hist_Name, 1000, 0, 10000);
                        }
                    }
                }
            }
        }


       //else if (Module.equals("FBAlign")) {
        else if (Module.equals("FBAlign")) {
            System.out.println("Initialize FBAlign Histograms");
            for (int i=0; i<15;i++){
                for (int j = 0; j < 4; j++){
                    String Hist_Name = String.format("FrontBack_%d_%d", i, j);
                    fbAlign[i][j] = new H1F("fbAlignment",Hist_Name,20,-10,10);
                }
            }
        }
        else if (Module.equals("TW")) {
            System.out.println("Initialize Timewalk Histograms");
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 4; j++) {
                    for (int k=0;k<=11;k++) {
                        String Hist_Name = String.format("TW_%d_%d_%d", i, j, k);
                        TW[i][j][k] = new H2F("TW", Hist_Name, 20, 0, 2000, 20, -2, 2);
                    }
                }
            }
        }
        else if (Module.equals("Atten")) {
            System.out.println("Initialize Atten Histograms");
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 4; j++) {
                    String Hist_Name = String.format("Atten_%d_%d", i, j);
                    AttenLen[i][j] = new H2F("Atten", Hist_Name, 40, -150, 150, 40, -1, 1);
                }
            }
        }
        else if (Module.equals("Veff")) {
            System.out.println("Initialize Veff Histograms");
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 4; j++) {
                    for (int k=0;k<=10;k++) {
                        String Hist_Name = String.format("Veff_%d_%d_%d", i, j,k);
                        //System.out.println(Hist_Name);
                        //Veff[i][j][k] = new H2F("Veff", Hist_Name, 80, -30, 60, 80, -20, 50);
                        //if(k == 10) {
                            Veff[i][j][k] = new H2F("Veff", Hist_Name, 40, -150, 150, 40, -5, 5);
                        //}else{
                            VeffWedge[i][j][k] = new H1F("VeffWedge", Hist_Name, 100, -1000, 1000);
                        //}
                    }
                }
            }
        }
        else if(Module.equals("T0")){
            T0 = new H2F("T0",660,1,660,100,0,5);
            for (int i=0; i<15;i++){
                for (int j = 0; j < 4; j++){
                    String Hist_Name = String.format("FrontBack_%d_%d", i, j);
                    fbAlign[i][j] = new H1F("fbAlignment",Hist_Name,50,-10,10);
                    for(int k = 0; k < 12; k++){
                        Hist_Name = String.format("WedgeBar_%d_%d_%d", i, j, k);
                        wbAlign[i][j][k] = new H1F("wbAlignment",Hist_Name,50,-10,10);
                    }
                }
            }
        }
    }



}
