package org.clas.modules.scint;

import org.clas.modules.ALERTCalConstants;
import org.clas.modules.ALERTCalibrationEngine;
import org.clas.modules.gui.ALERTCalibGUI;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

public class ALERTAttenLenCalibration {
    private String fitMode = "RN"; // is fitMode a String -- ParallelSliceFitter line 48?
    private int fitMinEvents = 50;
    private float fitSliceMaxError = -1;
    private int backgroundSF = -1; // ParallelSliceFitter lines [61-66]
    private GraphErrors fixGraph(GraphErrors graphIn, String graphName) {
        //System.out.println("seems to make it here!");
        int n = graphIn.getDataSize(0);
        int m = 0;
        for (int i = 0; i < n; i++) {
            if (graphIn.getDataEY(i) < fitSliceMaxError) {
                m++;
            }
        }
        double[] x = new double[m];
        double[] xerr = new double[m];
        double[] y = new double[m];
        double[] yerr = new double[m];
        int j = 0;

        for (int i = 0; i < n; i++) {
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
    public void calcATTLEN(IndexedList<DataGroup> ds) {
        ALERTCalibrationEngine Calib_1 = new ALERTCalibrationEngine();
        H2F AttLenHisto[][] = new H2F[15][4];
        F1D f2 = new F1D("f2", "[a]+[b]*x", -150, 150);
        for (int sector = 0; sector < 15; sector++) {
            for (int layer = 0; layer < 4; layer++) {
                AttLenHisto[sector][layer] = ds.getItem(sector, layer, 10).getH2F("Atten");
                AttLenHisto[sector][layer].setTitleX("Hit Position");
                AttLenHisto[sector][layer].setTitleY("log(totu/totd");
                System.out.println("Histo entries: " + AttLenHisto[sector][layer].getEntries());
                ParallelSliceFitter psf = new ParallelSliceFitter(AttLenHisto[sector][layer]);
                GraphErrors gr = new GraphErrors("gr");
                psf.setFitMode(fitMode);
                psf.setMinEvents(fitMinEvents);
                psf.setBackgroundOrder(backgroundSF); //setBackgroundOrder(int mode) {return this.mode = mode +1} ----  so this would be -1+1 = 0 ---->  P0_BG (Flat background)
                psf.setNthreads(1);
                psf.fitSlicesX();
                //gr.copy(fixGraph(psf.getMeanSlices(), "gr")); //fixGraph(GraphErrors graphIN, String graphName)
                gr = psf.getMeanSlices();
                f2.setParameter(0, 0.0);
                f2.setParameter(1, 2.0 / 200.0);
                System.out.println("data size: " + gr.getDataSize(0));
                try {
                    //Added Option "V"
                    DataFitter.fit(f2, gr, "V");
                    //DataFitter.fit(f2,gr,"RQ");
                } catch (Exception e) {
                    System.out.println("Fit error ");
                    e.printStackTrace();
                }
                double gradient = f2.getParameter(1);
                double gradient_error = f2.parameter(1).error();
                System.out.println("fit results: " + sector + " " + layer + ": " + gradient + " +/- " + gradient_error);
                ALERTCalConstants CalibConstant = new ALERTCalConstants();
                String CalibName = String.format("Atten_%d", sector);

                double attlen = 2.0 / gradient;
                double dattlen = 2.0 * gradient_error / gradient / gradient;

                //ALERTCalibrationEngine Calib_1 = new ALERTCalibrationEngine();
                Calib_1.calib.setDoubleValue(attlen, "attlen", sector, layer, 10);
                Calib_1.calib.setDoubleValue(dattlen, "dattlen", sector, layer, 10);
                //CalibConstant.NewCalConstants(CalibName, gradient);
            }
        }
        Calib_1.calib.fireTableDataChanged();
        //ALERTCalConstants Cal_Passer = new ALERTCalConstants();
        //Cal_Passer.Calib_Passer();
        //ALERTCalibGUI DrawHisto = new ALERTCalibGUI();
        //DrawHisto.Draw(AttLenHisto[0][1], f2);
    }
}
