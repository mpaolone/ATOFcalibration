package org.clas.modules.gui;

import org.clas.modules.ALERTCalibrationEngine;
import org.clas.modules.ALERTDataStructs;
//import org.clas.modules.geom.ALERTGeometry;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.system.ClasUtilsFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;

public class ALERTCalibGUI implements IDataEventListener, ActionListener, CalibrationConstantsListener, DetectorListener{

    private final int npaddles = 4;
    public JPanel mainPanel = null;
    public JTabbedPane motherPane=null;
    public DataSourceProcessorPane processorPane = null;
    public JSplitPane splitPanel = null;
    public JPanel detectorPanel = null;
    public DetectorPane2D detectorView = null;
    public JSplitPane moduleView = null;
    public EmbeddedCanvas canvas = null;
    public CalibrationConstantsView ccview = null;
    public ALERTCalibrationEngine ce = null;
    public static CalibrationConstants timeOffConsts = null;
    public static CalibrationConstants twConsts = null;
    public static CalibrationConstants veffConsts = null;
    public static CalibrationConstants attlenConsts = null;

    //All PMTs have order=0 except one side of the bar, which has order=1.  In the standard calib table, the order is
    // not recognized as a separate indexed value, so there cannot be two items with component=10.  The work around it
    // to define the order=1 as component=11, and fix later.  This is the "fix later" code, that takes the output text
    // and converts it.  Note: NOT THREAD SAFE.
    public void updateFile() throws IOException {
        String fileName = "test.txt"; // Replace with your file name
        String newName = ce.PassModule + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(newName))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+"); // Split line by whitespace
                if (parts.length >= 3) {
                    parts[2] = parts[2].replace("11", "10"); // Replace in third column
                }
                writer.write(" "+String.join("   ", parts));
                writer.newLine();
            }
        }
    }

    private void initializeGui(){

        ccview = new CalibrationConstantsView();
        ccview.addConstants(ce.getCalibrationConstants().get(0));

        motherPane =new JTabbedPane();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());


        // create detector panel
        detectorPanel = new JPanel();
        detectorPanel.setLayout(new BorderLayout());
        detectorView = new DetectorPane2D();
        drawDetector();
        detectorView.getView().addDetectorListener(this);
        detectorPanel.add(detectorView);


        moduleView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        canvas = new EmbeddedCanvas();
        ccview = new CalibrationConstantsView();

        ccview.addConstants(ce.getCalibrationConstants().get(0),this);
        moduleView.setTopComponent(canvas);
        moduleView.setBottomComponent(ccview);
        moduleView.setDividerLocation(0.5);
        moduleView.setResizeWeight(0.6);

        splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(detectorView);
        splitPanel.setRightComponent(moduleView);
        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(10000);
        processorPane.addEventListener(this);
        mainPanel.add(splitPanel);
        mainPanel.add(processorPane, BorderLayout.PAGE_END);
    }

    public ALERTCalibGUI(){
        initializeGui();
    }
    public ALERTCalibGUI(ALERTCalibrationEngine ce){
        this.ce = ce;
        initializeGui();
    }

    public void actionPerformed(ActionEvent e) {

    }

    public void constantsEvent(CalibrationConstants calibrationConstants, int i, int i1) {

        String str_sector    = (String) calibrationConstants.getValueAt(i1, 0);
        String str_layer     = (String) calibrationConstants.getValueAt(i1, 1);
        String str_component = (String) calibrationConstants.getValueAt(i1, 2);
        String str_val = (String) calibrationConstants.getValueAt(i1, 3);
        IndexedList<DataGroup> group = ce.getDataGroup();

        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);

        DataGroup dataGroup = group.getItem(sector,layer,component);
        this.canvas.clear();
        this.canvas.draw(dataGroup);
        this.canvas.update();
        if(ce.PassModule.equals("Veff") || ce.PassModule.equals("Atten")){
            saveFile("test.txt",ALERTCalibrationEngine.calib,3);
        }else saveFile("test.txt",ALERTCalibrationEngine.calib,4);
        try {
            updateFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        resetEventListener();


    }


    public void dataEventAction(DataEvent dataEvent) {
        ALERTCalibrationEngine CalibrationRoutines = new ALERTCalibrationEngine();
        ALERTDataStructs Passing = new ALERTDataStructs(timeOffConsts, twConsts, veffConsts, attlenConsts);
        Passing.FillData(dataEvent,  CalibrationRoutines.PassModule);

    }

    public void timerUpdate() {

    }

    public void resetEventListener() {

    }


    public void drawDetector() {
        //ALERTGeometry BAR_XZ = new ALERTGeometry();
        //System.out.println("Draw Detector called");
        double FTOFSize = 500.0;
        //int[]     widths   = new int[]{6,15,25};
        //int[]     lengths  = new int[]{6,15,25};
        int[] widths = new int[] {4,4,4};
        int[] lengths =new int[] {2,2,2};

        //String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        String[] names = new String[]{"ALERT_ATOF_1","ALERT_ATOF_2","ALERT_ATOF_3"};
        //for(int sector = 1; sector <= 5; sector++){
        for(int sector = 1; sector <= 15; sector++){
            //double rotation = Math.toRadians((sector-1)*(360.0/6)+90.0);
            double rotation = Math.toRadians((sector-1)*(360.0/15)+90.0);
            for(int layer = 1; layer <=2; layer++){
                int width  = widths[layer-1];
                int length = lengths[layer-1];
                //int width  = widths;
                //int length = lengths;
                //for(int paddle = 1; paddle <= npaddles; paddle++){
                for(int paddle = 1; paddle <= 4; paddle++){
  //                  ALERTGeometry BAR_XZ = new ALERTGeometry();
                    DetectorShape2D shape = new DetectorShape2D();
                    //shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setType(DetectorType.UNDEFINED);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    //shape.createBarXY(10 + length*paddle, width);
                    shape.createBarXY(1 + length*2.0, width);
                    shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0.0);
                    //shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0);
                    shape.getShapePath().rotateZ(rotation);

                    /*BAR_XZ.getDescriptor().setType(DetectorType.UNDEFINED);
                    BAR_XZ.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    BAR_XZ.createBarXY(1 + length*2.0, width);
                    BAR_XZ.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0);
                    BAR_XZ.getShapePath().rotateZ(rotation);*/
                    detectorView.getView().addShape(names[layer-1], shape);
                    //detectorView.getView().addShape(names[layer-1], BAR_XZ);
                    //System.out.println("Make it to the end?");
                }
            }
        }
        detectorView.setName("ATOF");
        detectorView.updateBox();
    }




    public void processShape(DetectorShape2D dsd) {
        // show summary
        int sector = dsd.getDescriptor().getSector();
        int layer  =  dsd.getDescriptor().getLayer();
        int paddle = dsd.getDescriptor().getComponent();
        System.out.println("Selected shape " + sector + " " + layer + " " + paddle);
        IndexedList<DataGroup> group = ce.getDataGroup();

        if(group.hasItem(sector,layer,paddle)==true){
            this.canvas.clear();
            this.canvas.draw(this.ce.getDataGroup().getItem(sector,layer,paddle));
            this.canvas.update();
        } else {
            System.out.println(" ERROR: can not find the data group");
        }

    }




    public void Draw(H2F blank, F1D fit) {
// This is drawing an addititonal panel for each Calibration Method. Should be tweaked in order
// to use the same Frame? An additional button to move through the different calibration methods?
        System.out.println("IN main");
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ALERTCalibGUI viewer = new ALERTCalibGUI();
        viewer.canvas.draw(blank);
        viewer.canvas.draw(fit,"same");

        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }
    public void Draw_H1(H1F blank, F1D fit) {
// This is drawing an addititonal panel for each Calibration Method. Should be tweaked in order
// to use the same Frame? An additional button to move through the different calibration methods?
        System.out.println("IN main");
        JFrame frame = new JFrame("Calibration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ALERTCalibGUI viewer = new ALERTCalibGUI();
        viewer.canvas.draw(blank);
        viewer.canvas.draw(fit, "same");

        frame.add(viewer.mainPanel);
        frame.setSize(1400, 800);
        frame.setVisible(true);

    }

    public void setCalibDB(CalibrationConstants timeOffConsts,
                           CalibrationConstants twConsts,
                           CalibrationConstants veffConsts,
                           CalibrationConstants attlenConsts) {
        this.timeOffConsts = timeOffConsts;
        this.veffConsts = veffConsts;
        this.twConsts = twConsts;
        this.attlenConsts = attlenConsts;
    }
    public void saveFile(String filename, CalibrationConstants calib, int indexOrder){
        List<String> linesFile = new ArrayList();
        Map<Long, IndexedTable.IndexedEntry> map = calib.getList().getMap();
        int nindex = calib.getList().getIndexSize();
        for(Map.Entry<Long, IndexedTable.IndexedEntry> entry : map.entrySet()) {
            StringBuilder str = new StringBuilder();

            for(int j = 0; j < nindex; ++j) {
                str.append(String.format("%3d ", IndexedList.IndexGenerator.getIndex((Long)entry.getKey(), j)));
            }

            int ncolumns = ((IndexedTable.IndexedEntry)entry.getValue()).getSize();

            for(int j = 0; j < ncolumns; ++j) {
                if(j == 0 && indexOrder == 4){
                    str.append(String.format("  %d  ", ((IndexedTable.IndexedEntry)entry.getValue()).getValue(j)));
                }else {
                    str.append(String.format("  %e  ", ((IndexedTable.IndexedEntry) entry.getValue()).getValue(j)));
                }
            }

            linesFile.add(str.toString());
        }
        ClasUtilsFile.writeFile(filename, linesFile);
    }
}
