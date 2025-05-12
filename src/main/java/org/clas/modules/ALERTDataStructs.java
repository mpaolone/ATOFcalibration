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
import org.clas.modules.geom.ALERTGeomAlign;
import org.jlab.clas.physics.LorentzVector;


import java.util.ArrayList;
import java.util.List;

public class ALERTDataStructs implements IDataEventListener{
    ALERTGeomAlign ALERT;
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

    private double ToTthresh = 1000; //threshold for ToT

    public int PMTtoIndex(int sector, int layer, int component, int order){
        int indexOrder = component + order;
        int indexN = sector*(ALERT.getNumLayer()*(ALERT.getNumComponent()+1))
                + layer*(ALERT.getNumComponent() + 1) + indexOrder;
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
        DataBank tdcBank = event.getBank("ATOF::tdc");
        //tdcBank.show();
        if (event.hasBank("ATOF::tdc")) {
            for (int i = 0; i < tdcBank.rows(); i++) {
                int sector1 = tdcBank.getInt("sector", i);
                int layer1 = tdcBank.getInt("layer", i);
                int order1 = tdcBank.getInt("order", i);
                int component1 = tdcBank.getInt("component", i);
                double time1 = tdcBank.getInt("TDC", i);
                double ToT1 = tdcBank.getInt("ToT", i);

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
                    ATOFBarWedgeClust bwc = new ATOFBarWedgeClust(bar, wedge.sector, wedge.layer, wedge.component, wedge.time, wedge.ToT, wedge.getZ());
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
    /*
    public void setTracks(DataEvent event, ArrayList<ATOFBar> barList){
        DataBank projBank = event.getBank("ALERT::Projections");
        DataBank trackBank = event.getBank("AHDC::Track");
        if (event.hasBank("ALERT::Projections") && event.hasBank("AHDC::Track")) {
            for (int i = 0; i < projBank.rows(); i++) {
                double x = projBank.getFloat("x_at_bar",i);
                //int id = projBank.getInt("trackID",i);
                int id = 0;
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
                        System.out.print("First case \n");
                        bar.setTrackZhit(z);
                        bar.setTrackId(id);
                        bar.setPropTime(ptime);
                        bar.hasTrackHit = true;
                    }else if(Math.abs(PhiBlock - bar.PhiBlock) <= 1){
                        System.out.print("Second case \n");
                        bar.setTrackZhit(z);
                        bar.setTrackId(id);
                        bar.setPropTime(ptime);
                        bar.hasTrackHit = true;
                    }
                    else System.out.print("No case \n");
                }
            }
        }
    }
*/
    //begin of elastic
    
public void setTracks(DataEvent event, ArrayList<ATOFBar> barList) {
    if (!event.hasBank("REC::Particle") || !event.hasBank("REC::Event")) return;

    DataBank partBank = event.getBank("REC::Particle");
    DataBank eventBank = event.getBank("REC::Event");

    double E_beam = 2.2;
    double mp = 0.938272;
    double c = 29.9792458;
    double barrelRadius_cm = 78.5;

    double vz = 0;
    LorentzVector beam = new LorentzVector(0, 0, E_beam, E_beam);
    LorentzVector electron = new LorentzVector();
    boolean foundElectron = false;

    for (int i = 0; i < partBank.rows(); i++) {
        if (partBank.getInt("pid", i) == 11) {
            double px = partBank.getFloat("px", i) / 1000.0;
            double py = partBank.getFloat("py", i) / 1000.0;
            double pz = partBank.getFloat("pz", i) / 1000.0;
            double E = Math.sqrt(px * px + py * py + pz * pz);
            vz = partBank.getFloat("vz", i);
            electron.setPxPyPzE(px, py, pz, E);
            foundElectron = true;
            break;
        }
    }

    if (!foundElectron) return;

    LorentzVector q = new LorentzVector();
    q.copy(beam);
    q.sub(electron);

    double p = q.p();
    double E = Math.sqrt(p * p + mp * mp);
    double beta = p / E;
    double v = beta * c;

    double ux = q.px() / p;
    double uy = q.py() / p;
    double uz = q.pz() / p;

    double pathLength_cm = barrelRadius_cm / Math.sqrt(ux * ux + uy * uy);
    double dz = pathLength_cm * uz;
    double z_at_bar = vz + dz;

    double ptime = pathLength_cm / v;

    double phi = Math.atan2(q.py(), q.px());
    if (phi < 0) phi += 2 * Math.PI;
    int phiBlock = XYtoPhiBlock(Math.cos(phi), Math.sin(phi));

    for (ATOFBar bar : barList) {
        if (Math.abs(phiBlock - bar.PhiBlock) == 0 || Math.abs(phiBlock - bar.PhiBlock) == 1) {
            bar.setTrackZhit(z_at_bar);
            bar.setTrackId(0);
            bar.setPropTime(ptime);
            bar.hasTrackHit = true;
        }
    }
}

    //end of elastic 


    








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
        for (ATOFHit hit : hitList) {
            if (hit.component == 10 && hit.order == 0) {
                hitList_1.add(hit);
            }
            if (hit.component == 10 && hit.order == 1) {
                hitList_0.add(hit);
            }
        }
        for(ATOFHit hit0 : hitList_0) {
            for (ATOFHit hit1 : hitList_1) {
                if (hit0.sector == hit1.sector && hit0.layer == hit1.layer) {
                    ATOFBar bar = new ATOFBar(hit0.sector, hit0.layer, hit0.component, hit0.time, hit1.time, hit0.ToT, hit1.ToT);
                    barList.add(bar);
                }
            }
        }
        //System.out.print("Size of bar list " + barList.size() + "\n");
        return barList;
    }
/*
    public static List<ALERTDetector> getData(DataEvent event){


        ArrayList<ATOFBarWedgeClust> barList = new ArrayList<ATOFBarWedgeClust>();
        ArrayList<ALERTDetector> ALERTList = new ArrayList<ALERTDetector>();
//        DataBank tdcBank = event.getBank("ATOF::adc");
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
        DataSummary.set_ADC(tdcBank.getDouble("adc_front",0),
                tdcBank.getDouble("adc_back",0));
        DataSummary.Test_Energy = tdcBank.getDouble("energy_front",0);
        DataSummary.YAmp = ALERTDataTransformer(DataSummary).YAmp;
        ALERTList.add(DataSummary);
        return ALERTList;
    }
    */
    
    public double GetBarVeff(ATOFBar bar){
        double veff = veffConsts.getDoubleValue("veff",bar.sector,bar.layer,bar.component);
        if(veff == 0){ veff = veff_default;}
        double twu = twEval(bar.ToT_up,
                        twConsts.getDoubleValue("tw0",bar.sector,bar.layer,bar.component,0),
                        twConsts.getDoubleValue("tw1",bar.sector,bar.layer,bar.component,0),
                        twConsts.getDoubleValue("tw2",bar.sector,bar.layer,bar.component,0));

        double twd = twEval(bar.ToT_down,
                        twConsts.getDoubleValue("tw0",bar.sector,bar.layer,bar.component,1),
                        twConsts.getDoubleValue("tw1",bar.sector,bar.layer,bar.component,1),
                        twConsts.getDoubleValue("tw2",bar.sector,bar.layer,bar.component,1));
        return bar.getRedTdiff(twu,twd);
    }

    public void FillData(DataEvent event, String name) {
        //System.out.print("An event------------\n");
        //first get list of bars and clusters:
        ArrayList<ATOFHit> hits = getATOFHits(event);
        ArrayList<ATOFBar> bars = getBars(hits);
        
        //System.out.print("Wedges " + hits.size() + "\n");
        //System.out.print("Bars " + bars.size() + "\n");
        setTracks(event,bars);
        //setTracksMC(event,bars);
        double vtime = getFDvtime(event);
        ArrayList<ATOFBarWedgeClust> clusts = getClusts(hits, bars);
        //if(clusts.size()>0) System.out.print("Clusters " + clusts.size() + "\n"); 

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
                double veff_new = GetBarVeff(bar);
                if(bar.zhit != 0.0) {
                    System.out.print("Hist rempli \n");
                    Veff[sector][layer][component].fill(bar.zhit, veff_new);
                }
            }
            for (ATOFBarWedgeClust clust : clusts) {
                if(!clust.bar.hasTrackHit){
                    //System.out.print("Using wedge z \n");
                    //When the bar doesn't have a track to match
                    //Use the z position from the wedges instead
                    clust.bar.zhit = clust.wedgeZ;
                    double veff_new = GetBarVeff(clust.bar);
                    Veff[sector][layer][component].fill(clust.bar.zhit, veff_new);
                }
                sector = clust.sector;
                layer = clust.layer;
                component = clust.component;
                double twu = twEval(clust.bar.ToT_up,
                        twConsts.getDoubleValue("tw0",sector,layer,10,0),
                        twConsts.getDoubleValue("tw1",sector,layer,10,0),
                        twConsts.getDoubleValue("tw2",sector,layer,10,0));

                double twd = twEval(clust.bar.ToT_down,
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
                double twu = twEval(bar.ToT_up,
                        twConsts.getDoubleValue("tw0",sector,layer,10,0),
                        twConsts.getDoubleValue("tw1",sector,layer,10,0),
                        twConsts.getDoubleValue("tw2",sector,layer,10,0));

                double twd = twEval(bar.ToT_down,
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
            for (ATOFBar bar : bars) {
                System.out.println("in bars");
                if(bar.ToT_up > ToTthresh && bar.ToT_down > ToTthresh) {
                    System.out.println("passed cutsT");
                    T0.fill(PMTtoIndex(bar.sector, bar.layer, 10, 0), bar.time_up - bar.propTime);
                    T0.fill(PMTtoIndex(bar.sector, bar.layer, 10, 1), bar.time_down - bar.propTime);
                    fbAlign[bar.sector][bar.layer].fill(bar.getTdiff());
                }
            }

            for(ATOFBarWedgeClust clust : clusts){
                double ToTmax = 0;
                double timeMax = 0;
                double tdiffMax = 0;
                if(clust.wedgeToT > ToTmax) {
                    timeMax = clust.wedgeTime;
                    ToTmax = clust.wedgeToT;
                    tdiffMax = clust.getTdiff(veff_default, 0.0);
                }
                /*
                if(clust.bar.ToT_front > ToTthresh && clust.bar.ToT_back > ToTthresh && clust.wedgeToT > ToTthresh) {
                    T0.fill(PMTtoIndex(clust.sector, clust.layer, clust.component, 0), clust.wedgeTime - clust.bar.propTime);
                    wbAlign[clust.sector][clust.layer][clust.component].fill(clust.getTdiff(veff_default, 0.0));
                }
                 */
                T0.fill(PMTtoIndex(clust.sector, clust.layer, clust.component, 0), timeMax - clust.bar.propTime);
                wbAlign[clust.sector][clust.layer][clust.component].fill(tdiffMax);
            }


            if (event.getType() == DataEventType.EVENT_STOP) {
                //System.out.println("at Event stop");
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
                TW[sector][layer][component].fill(bar.ToT_up, bar.getZeroTimeU(vtime,veff_default,0,0));
                TW[sector][layer][component + 1].fill(bar.ToT_down, bar.getZeroTimeD(vtime,veff_default,0,0));
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
                            Veff[i][j][k] = new H2F("Veff", Hist_Name, 20, -150, 150, 100, -5, 5);
                        //}else{
                            VeffWedge[i][j][k] = new H1F("VeffWedge", Hist_Name, 100, -1000, 1000);
                        //}
                    }
                }
            }
        }
        else if(Module.equals("T0")){
            T0 = new H2F("T0",660,1,660,100,150,250);
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
