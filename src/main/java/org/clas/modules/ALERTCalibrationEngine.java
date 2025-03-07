package org.clas.modules;

import org.clas.modules.utils.ALERTDatabaseConstantProvider;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.group.DataGroup;
import org.jlab.utils.groups.IndexedList;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ALERTCalibrationEngine extends CalibrationEngine {
    private final int npaddles = 4;
    private String                                  moduleName = null;
    private String                                  alertDetector = null;
    private ALERTDetector                           ALERT = null;
    public static ALERTDatabaseConstantProvider dbc = null;
    public static CalibrationConstants timeOffConsts = null;
    public static CalibrationConstants twConsts = null;
    public static CalibrationConstants veffConsts = null;
    public static CalibrationConstants attlenConsts = null;
    ConstantsManager ccdb = null; // What is constants manager and how is it meant to be used?
    private ALERTCalConstants                       constants = new ALERTCalConstants();
    /*
    public static CalibrationConstants timeOffsetCalCons;
    public static CalibrationConstants veffCalCons;
    public static CalibrationConstants twCalCons;
    public static CalibrationConstants attenCalCons;
    */
    //public CalibrationConstants                   calib = null;
    public static CalibrationConstants calib = null;
    public  CalibrationConstants                    calib2 = null;
    private Map<String,CalibrationConstants> globalCalib = null;
    private final IndexedList<DataGroup> dataGroupCal = new IndexedList<DataGroup>(3);

    public static String PassModule;


// Detector should be defined later: WC || SC
    public void ALERTCalibrationModule(String Detector,String ModuleName,int Precision) {
        // System.out.println("Lets Define some properties");
        //System.out.println("And Initialize Histo");
        dbc = new ALERTDatabaseConstantProvider(1,"default","");
        timeOffConsts = dbc.readConstants("/calibration/alert/atof/time_offsets");
        twConsts = dbc.readConstants("/calibration/alert/atof/time_walk");
        veffConsts = dbc.readConstants("/calibration/alert/atof/effective_velocity");
        attlenConsts = dbc.readConstants("/calibration/alert/atof/attenuation");

        ALERTDataStructs hist = new ALERTDataStructs(timeOffConsts, twConsts, veffConsts, attlenConsts);
        hist.Create_Fill_Histo2D(ModuleName);
        //System.out.println("Finished Initialization");
        //this.ALERT    = d;
        this.initModule(Detector,ModuleName, Precision);
        //this.resetEventListener();
        PassModule=ModuleName;
    }

    public ALERTCalConstants getConstants() {

        return constants;
    }

    public String getName() {
        return moduleName;
    }
    public void initModule(String detector, String name, int Precision) {
        this.alertDetector = detector;
        System.out.println("INITMODULE");
        this.moduleName = name;
        //this.calib = new CalibrationConstants(3,Constants);
        //calib = new CalibrationConstants(3, "constant");
        if (name.equals("Veff")) {
            calib = new CalibrationConstants(3, "veff/F:dveff/F");
        }
        if (name.equals("Atten")) {
            calib = new CalibrationConstants(3, "atten/F:datten/F");
        }
        // this corresponds to sector/layer/comp in CalibrationConstants.class
        calib.setName(name);
        calib.setPrecision(3);

        //this.prevCalib = new CalibrationConstants(3,Constants);
        //this.prevCalib.setName(name);
        //this.setCalibrationTablePrecision(Precision);
        //this.ccdb        = ccdb;
        //this.globalCalib = gConstants;
        this.resetEventListener();
    }

    public void CalibHandler(IndexedList<DataGroup> DG, String name) {

        org.clas.modules.scint.ALERTTimeWalkCalibration TW_Calib = new org.clas.modules.scint.ALERTTimeWalkCalibration();
        org.clas.modules.scint.ALERTVeffCalibration Veff_Calib = new org.clas.modules.scint.ALERTVeffCalibration();
        org.clas.modules.scint.ALERTPedestalCalibration Pedestal_Calib = new org.clas.modules.scint.ALERTPedestalCalibration();
        org.clas.modules.scint.ALERTFrontBackCalibration FB_Calib = new org.clas.modules.scint.ALERTFrontBackCalibration();
        org.clas.modules.scint.ALERTAttenLenCalibration Atten_Calib = new org.clas.modules.scint.ALERTAttenLenCalibration();

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
            //System.out.println("Calling Veff");
            Veff_Calib.calcVEFF(DG);
        }

        else if(name.equals("Atten")){
            Atten_Calib.calcATTLEN(DG);
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting");
        //ALERTCalConstants Calib_Const = new ALERTCalConstants();
        //ALERTCalibrationEngine calib_Table = new ALERTCalibrationEngine();
        for (int isec = 0; isec < 15; isec++) {
            for (int suplayer = 0; suplayer < 4; suplayer++) {
                for (int ipad = 0; ipad <= 10; ipad++) {
                    System.out.println("ADDING ENTRY");
                    calib.addEntry(isec, suplayer, ipad);
                   // calib.setDoubleValue(0.0, "constant", isec, suplayer,ipad);
                    calib.setDoubleValue(0.0, "veff", isec, suplayer,ipad);
                    calib.setDoubleValue(0.0, "dveff", isec, suplayer,ipad);
                    System.out.println(calib.getDoubleValue("veff", isec, suplayer,ipad));
                }
            }
        }
        //System.out.println(calib_Table.calib.getColumnName(1));
        calib.fireTableDataChanged();
        System.out.println(calib);

    }

    @Override
    public List<CalibrationConstants> getCalibrationConstants() {
        System.out.println("getCalibConstants!");
        return Arrays.asList(calib);
    }

    @Override
    public IndexedList<DataGroup> getDataGroup() {
        //return dataGroupCal;
        return ALERTDataStructs.dataGroups;
    }

    public static void main(String[] args){

        //establish constants
        /*
        timeOffsetCalCons = new CalibrationConstants(3,"T0/F:upstream_downstream/F:wedge_bar/F");
        veffCalCons = new CalibrationConstants(3,"veff/F:dveff/F");
        twCalCons = new CalibrationConstants(3,"tw0/F:tw1/F:tw2/F:dtw0/F:dtw1/F:dtw2/F");
        attenCalCons = new CalibrationConstants(3,"attenlen/F:dattlen/F");
*/
        Scanner Module_input = new Scanner(System.in);
        String ALERT_Detector = "SC";
        String Module;
        System.out.println("Which Calibration? (Veff, Atten, TW");
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
