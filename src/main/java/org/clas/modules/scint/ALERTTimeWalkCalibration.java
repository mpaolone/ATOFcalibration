package org.clas.modules.scint;

import org.clas.modules.gui.ALERTCalibGUI;
import org.clas.modules.ALERTCalConstants;
import org.clas.modules.ALERTCalibrationEngine;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

public class ALERTTimeWalkCalibration {

    private String fitMode = "RN"; // I believe it should be a string
    private int fitMinEvents = 100;
    private int backgroundSF = -1;
    private float fitSliceMaxError = -1;
    private GraphErrors fixGraph(GraphErrors graphIn, String graphName) {

        int n = graphIn.getDataSize(0);
        int m = 0;
        for (int i=0; i<n; i++) {
            if (graphIn.getDataEY(i) < fitSliceMaxError) {
                m++;
            }
        }

        double[] x = new double[m];
        double[] xerr = new double[m];
        double[] y = new double[m];
        double[] yerr = new double[m];
        int j = 0;

        for (int i=0; i<n; i++) {
            if (graphIn.getDataEY(i) < fitSliceMaxError) {
                x[j] = graphIn.getDataX(i);
                xerr[j] = graphIn.getDataEX(i);
                y[j] = graphIn.getDataY(i);
                yerr[j] = graphIn.getDataEY(i);
                j++;
            }
        }

        GraphErrors fixGraph = new GraphErrors(graphName, x, y, xerr, yerr);
        fixGraph.setName(graphName);

        return fixGraph;

    }

    //private void calcTW(ALERTDataStructs ds){
    // Changed to public. May want these to be private
    //public void calcTW(DataGroup ds){
    public void calcTW(IndexedList<DataGroup> ds) {
        System.out.println("Start in TimeWalk");
        ALERTCalibrationEngine Calib_TW = new ALERTCalibrationEngine();
        F1D f2 = new F1D("f2", "[a]*x*x + [b]*x + [c]", 0.0, 2000.0);
        f2.setLineWidth(3);
        f2.setLineColor(1);
        H2F TimeWalk[][][] = new H2F [15][3][5];

        for (int sector = 0; sector < 15; sector++) {
            for (int slayer=0; slayer<2;slayer++) {
                for (int comp = 0; comp <= 4 - 1; comp++) {

                    //H2F h2 = (H2F) ds.SCDG.getH2(raw_a, raw_t);

                    TimeWalk[sector][slayer][comp] = ds.getItem(sector, slayer, comp).getH2F("TW");
                    TimeWalk[sector][slayer][comp].setTitleX("ToT");
                    TimeWalk[sector][slayer][comp].setTitleY("time");
                    //H2F TimeWalk = ds.getH2F("TW");
                    //F1D f2 = new F1D("f2", "[a]*x*x + [b]*x + [c]", -250.0, 250.0);
                    ParallelSliceFitter psf = new ParallelSliceFitter(TimeWalk[sector][slayer][comp]);
                    //ParallelSliceFitter psf = new ParallelSliceFitter(ds.getH2F("TW"));
                    GraphErrors gr = new GraphErrors("gr");
                    psf.setFitMode(fitMode);
                    psf.setMinEvents(fitMinEvents);
                    psf.setBackgroundOrder(backgroundSF);
                    psf.setNthreads(1);
                    /* setOutput(false); */
                    psf.fitSlicesX();
        /* setOutput(true);
        if (showSlices) {
            psfL.inspectFits();
            showSlices = false;
        }

        fitSliceMaxError = 2.0;
        */
                    gr.copy(fixGraph(psf.getMeanSlices(), "gr"));
                    f2.setParameter(0, 0.0);
                    f2.setParameter(1, 0.0);
                    f2.setParameter(2, 0.0);

                    try {
                        DataFitter.fit(f2, gr, "V"); // fit method needs an "option"

                    } catch (Exception e) {
                        System.out.println("Fit error ");
                        e.printStackTrace();
                    }
                    //ds.AssignConsts(f2.getParameter(0));

                    double Param0 = f2.getParameter(0);
                    double Param1 = f2.getParameter(1);

                    System.out.println("Fit Parameter 0 "+ Param0);

                    ALERTCalibrationEngine.calib.setDoubleValue(Param0,
                            "tw0", sector, slayer, comp);

                    //ALERTCalConstants CalibConstant = new ALERTCalConstants();
                    //CalibConstant.NewCalConstants("TimeWalk", (float) Param0);

                    //ALERTCalibGUI DrawHisto = new ALERTCalibGUI();
                    //System.out.println("Drawing Histo");
                    //DrawHisto.Draw(TimeWalk[sector], f2);


                    /* Update ALERTDetector methods so one can then process Veff*/
                }
            }
        }
        ALERTCalibrationEngine.calib.fireTableDataChanged();
    }
}
