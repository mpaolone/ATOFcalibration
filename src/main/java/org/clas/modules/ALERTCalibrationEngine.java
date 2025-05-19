package org.clas.modules;

import org.clas.modules.utils.ALERTDatabaseConstantProvider;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;
import org.clas.modules.geom.ALERTGeomAlign;
        
import javax.swing.*;
import java.util.*;

public class ALERTCalibrationEngine extends CalibrationEngine {
    ALERTGeomAlign ALERT = new ALERTGeomAlign();
    private String moduleName = null;
    private String alertDetector = null;
    private ALERTChannel ALERTChannel = null;
    public static ALERTDatabaseConstantProvider dbc = null;
    public static CalibrationConstants timeOffConsts = null;
    public static CalibrationConstants twConsts = null;
    public static CalibrationConstants veffConsts = null;
    public static CalibrationConstants attlenConsts = null;
    ConstantsManager ccdb = null; // What is constants manager and how is it meant to be used?
    private ALERTCalConstants                       constants = new ALERTCalConstants();
    public static CalibrationConstants calib = null;
    public  CalibrationConstants                    calib2 = null;
    private Map<String,CalibrationConstants> globalCalib = null;
    private final IndexedList<DataGroup> dataGroupCal = new IndexedList<DataGroup>(3);
    public static String PassModule;


// Detector should be defined later: WC || SC
    public void ALERTCalibrationModule(String Detector,String ModuleName,int Precision) {
        //System.out.println("Initializing Calibration Module");
        dbc = new ALERTDatabaseConstantProvider(1,"default","");
        timeOffConsts = dbc.readConstants("/calibration/alert/atof/time_offsets",4);
        twConsts = dbc.readConstants("/calibration/alert/atof/time_walk");
        veffConsts = dbc.readConstants("/calibration/alert/atof/effective_velocity");
        attlenConsts = dbc.readConstants("/calibration/alert/atof/attenuation");

        ALERTDataStructs hist = new ALERTDataStructs(timeOffConsts, twConsts, veffConsts, attlenConsts);
        hist.Create_Fill_Histo2D(ModuleName);
        this.initModule(Detector,ModuleName, Precision);
        //this.resetEventListener();
        PassModule=ModuleName;
    }

    public ALERTCalConstants getConstants() {

        return constants;
    }
// The following is a reassignment of each PMT to a unique index, and the next few functions return from an index the
// appropriate sector,layer,component, and order
    public int PMTtoIndex(int sector, int layer, int component, int order){
        int indexOrder = component + order;
        int indexN = sector*(ALERT.getNumLayer()*(ALERT.getNumComponent()+1)) 
                + layer*(ALERT.getNumComponent() + 1) + indexOrder;
        return indexN;
    }

    public int IndextoOrder(int index){
        int order = -1;
        int indexOrder = index%(ALERT.getNumComponent() +1);
        if(indexOrder == 11){
            order = 1;
        }else{
            order = 0;
        }
        return order;
    }
    public int IndextoComponent(int index){
        int component = -1;
        int indexOrder = index%(ALERT.getNumComponent() + 1);
        if(indexOrder == 11){
            component = 10;
        }else{
            component = indexOrder;
        }
        return component;
    }
    public int IndextoLayer(int index){
        int layer = index%ALERT.getNumLayer();
        return layer;
    }
    public int IndextoSector(int index){
        int sector = index%ALERT.getNumSector();
        if(sector > 0){
            sector = ALERT.getNumSector() - sector;
        }
        return sector;
    }



    public String getName() {
        return moduleName;
    }
    public void initModule(String detector, String name, int Precision) {
        this.alertDetector = detector;
        System.out.println("INITMODULE");
        this.moduleName = name;
        if (name.equals("Veff")) {
            calib = new CalibrationConstants(3, "veff/F:dveff/F:extra1/F:extra2/F");
        }
        if (name.equals("Atten")) {
            calib = new CalibrationConstants(3, "atten/F:datten/F:extra1/F:extra2/F");
        }
        if (name.equals("T0") || name.equals("T0elas")) {
            calib = new CalibrationConstants(3, "order/I:t0/F:upstream_downstream/F:wedge_bar/F:extra1/F:extra2/F");
        }

        if (name.equals("TW")) {
            calib = new CalibrationConstants(3, "order/I:tw0/F:tw1/F:tw2/F:tw3/F:dtw0/F:dtw1/F:dtw2/F:dtw3/F:chi2ndf/F");
        }
        // this corresponds to sector/layer/comp in CalibrationConstants.class
        calib.setName(name);
        calib.setPrecision(3);
        this.resetEventListener();
    }

    public void CalibHandler(IndexedList<DataGroup> DG, String name) {

        org.clas.modules.scint.ALERTTimeWalkCalibration TW_Calib = new org.clas.modules.scint.ALERTTimeWalkCalibration();
        org.clas.modules.scint.ALERTVeffCalibration Veff_Calib = new org.clas.modules.scint.ALERTVeffCalibration();
        org.clas.modules.scint.ALERTPedestalCalibration Pedestal_Calib = new org.clas.modules.scint.ALERTPedestalCalibration();
        org.clas.modules.scint.ALERTFrontBackCalibration FB_Calib = new org.clas.modules.scint.ALERTFrontBackCalibration();
        org.clas.modules.scint.ALERTAttenLenCalibration Atten_Calib = new org.clas.modules.scint.ALERTAttenLenCalibration();
        org.clas.modules.scint.ALERTT0Calibration T0_Calib = new org.clas.modules.scint.ALERTT0Calibration();

        if (name.equals("Pedestal")){
            System.out.println("Calling Pedestal");
            Pedestal_Calib.findPeakMean(DG);
        }
        else if (name.equals("FBAlign")){
            System.out.println("Calling FBAlign");
            FB_Calib.getMeanDifference(DG);
        }

        else if (name.equals("TW")){
            System.out.println("Cal TW");
            TW_Calib.calcTW(DG);
        }
        else if (name.equals("Veff")){
            Veff_Calib.calcVEFF(DG);
        }

        else if(name.equals("Atten")){
            Atten_Calib.calcATTLEN(DG);
        }
        else if(name.equals("T0") || name.equals("T0elas")){
            T0_Calib.calcT0(DG);
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting");
        for (int isec = 0; isec < 15; isec++) {
            for (int suplayer = 0; suplayer < 4; suplayer++) {
                for (int ipad = 0; ipad <= 10; ipad++) {
                    if(moduleName.equals("Veff")){
                        calib.addEntry(isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "veff", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dveff", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra1", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra2", isec, suplayer, ipad);
                    }
                    if(moduleName.equals("Atten")){
                        calib.addEntry(isec, suplayer, ipad);
                        calib.setIntValue(0, "order", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "atten", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "datten", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra1", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra2", isec, suplayer, ipad);
                        if(ipad == 10){
                            calib.addEntry(isec, suplayer, ipad+1);
                            calib.setIntValue(1, "order", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "atten", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "datten", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "extra1", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "extra2", isec, suplayer, ipad+1);
                        }
                    }
                    if(moduleName.equals("TW")){
                        calib.addEntry(isec, suplayer, ipad);
                        calib.setIntValue(0, "order", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "tw0", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "tw1", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "tw2", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "tw3", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dtw0", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dtw1", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dtw2", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dtw3", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "dtw3", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "chi2ndf", isec, suplayer, ipad);
                        if(ipad == 10){
                            calib.addEntry(isec, suplayer, ipad+1);
                            calib.setIntValue(1, "order", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "tw0", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "tw1", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "tw2", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "tw3", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "dtw0", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "dtw1", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "dtw2", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "dtw3", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "dtw3", isec, suplayer, ipad+1);
                            calib.setDoubleValue(0.0, "chi2ndf", isec, suplayer, ipad+1);
                        }
                    }


                    else if(moduleName.equals("T0") || moduleName.equals("T0elas")){
                        calib.addEntry(isec, suplayer, ipad);
                        calib.setIntValue(0, "order", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "t0", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "upstream_downstream", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "wedge_bar", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra1", isec, suplayer, ipad);
                        calib.setDoubleValue(0.0, "extra2", isec, suplayer, ipad);
                        if(ipad == 10){
                            calib.addEntry(isec, suplayer, ipad +1);
                            calib.setIntValue(1, "order", isec, suplayer, ipad +1);
                            calib.setDoubleValue(0.0, "t0", isec, suplayer, ipad +1);
                            calib.setDoubleValue(0.0, "upstream_downstream", isec, suplayer, ipad +1);
                            calib.setDoubleValue(0.0, "wedge_bar", isec, suplayer, ipad +1);
                            calib.setDoubleValue(0.0, "extra1", isec, suplayer, ipad +1);
                            calib.setDoubleValue(0.0, "extra2", isec, suplayer, ipad +1);
                        }
                    }


                }
            }
        }
        calib.fireTableDataChanged();

    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        System.out.println(Arrays.asList(calib));
        return Arrays.asList(calib);
    }

    @Override
    public IndexedList<DataGroup> getDataGroup() {
        return ALERTDataStructs.dataGroups;
    }

    /*
    public static void main(String[] args){
        Scanner Module_input = new Scanner(System.in);
        String ALERT_Detector = "SC";
        String Module;
        System.out.println("Which Calibration? (T0, Veff, Atten, TW)");
        ALERTCalibrationEngine Module_Setter = new ALERTCalibrationEngine();
        Module = Module_input.nextLine();
        System.out.println("Calibration will be done for:" + Module);
        Module_Setter.ALERTCalibrationModule(ALERT_Detector,Module,1);
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        org.clas.modules.gui.ALERTCalibGUI viewer = new org.clas.modules.gui.ALERTCalibGUI(Module_Setter);
        viewer.setCalibDB(timeOffConsts, twConsts, veffConsts, attlenConsts);
        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }
     */

    public static void runGUI(){
        Scanner Module_input = new Scanner(System.in);
        String ALERT_Detector = "SC";
        String Module;
        System.out.println("Which Calibration? (T0, Veff, Atten, TW)");
        ALERTCalibrationEngine Module_Setter = new ALERTCalibrationEngine();
        Module = Module_input.nextLine();
        System.out.println("Calibration will be done for:" + Module);
        Module_Setter.ALERTCalibrationModule(ALERT_Detector,Module,1);
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        org.clas.modules.gui.ALERTCalibGUI viewer = new org.clas.modules.gui.ALERTCalibGUI(Module_Setter);
        viewer.setCalibDB(timeOffConsts, twConsts, veffConsts, attlenConsts);
        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }
}
