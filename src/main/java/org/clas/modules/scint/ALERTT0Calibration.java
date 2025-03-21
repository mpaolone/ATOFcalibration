package org.clas.modules.scint;

import org.clas.modules.ALERTCalConstants;
import org.clas.modules.ALERTCalibrationEngine;
import org.clas.modules.gui.ALERTCalibGUI;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

public class ALERTT0Calibration {

    private String fitMode = "RN"; // is fitMode a String -- ParallelSliceFitter line 48?
    private int fitMinEvents = 50;
    private int backgroundSF = -1; // ParallelSliceFitter lines [61-66]
    private float fitSliceMaxError = -1;
    public void calcT0(IndexedList<DataGroup> ds) {

        System.out.println("Start in T0");
        ALERTCalibrationEngine Calib_1 = new ALERTCalibrationEngine();
        H2F T0Histo;// = new H2F();
        T0Histo = ds.getItem(0,0,1).getH2F("T0");
        ParallelSliceFitter psf = new ParallelSliceFitter(T0Histo);
        GraphErrors gr;// = new GraphErrors("gr");
        psf.setFitMode(fitMode);
        psf.setMinEvents(fitMinEvents);
        psf.setBackgroundOrder(backgroundSF); //setBackgroundOrder(int mode) {return this.mode = mode +1} ----  so this would be -1+1 = 0 ---->  P0_BG (Flat background)
        psf.setNthreads(1);
        psf.fitSlicesX();
        gr = psf.getMeanSlices();

        //ALERTCalConstants CalibConstant = new ALERTCalConstants();
        //String CalibName = "T0";

        H1F meanDist[][] = new H1F[15][4];
        F1D fbFunc = new F1D("fbFunc","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);


        for (int sector = 0; sector < 15; sector++) {
            for (int slayer = 0; slayer < 4; slayer++) {
                double mean = -1.0;
                meanDist[sector][slayer] = ds.getItem(sector, slayer, 0).getH1F("fbAlignment");
                meanDist[sector][slayer].setTitleY("Time Diff");
                int maxBin = meanDist[sector][slayer].getMaximumBin();
                double maxPos = meanDist[sector][slayer].getXaxis().getBinCenter(maxBin);
                double min_test = meanDist[sector][slayer].getMin();

                // minPos needs to be better defined. minBin doesn't exist
                double minPos = maxPos - (2*maxPos);
                fbFunc.setRange(minPos,maxPos);

                fbFunc.setParameter(0, meanDist[sector][slayer].getBinContent(maxBin));
                fbFunc.setParLimits(0, meanDist[sector][slayer].getBinContent(maxBin)*0.7, meanDist[sector][slayer].getBinContent(maxBin)*1.2);
                fbFunc.setParameter(1, 0.0);
                fbFunc.setParameter(2, 1.0);

                //H1F hp = pdat.getItem(0,0,1).getH1F("Pedestal");
                //H1F hp = (H1F) pdat.getH1F("Pedestal");
                //F1D fp = new F1D("gmFunc", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)",0.0, hvmax);
                DataFitter.fit(fbFunc, meanDist[sector][slayer], "HQ");
                mean = fbFunc.getParameter(1);
                System.out.println("upstream_downstream mean: " + mean);

                for (int comp = 0; comp <= 10; comp++) {
                    int index = Calib_1.PMTtoIndex(sector, slayer, comp, 0);
                    ALERTCalibrationEngine.calib.setDoubleValue(gr.getDataY(index), "t0", sector, slayer, comp);
                    ALERTCalibrationEngine.calib.setDoubleValue(mean, "upstream_downstream", sector, slayer, comp);
                    if (comp == 10) {
                        index = Calib_1.PMTtoIndex(sector, slayer, comp, 1);
                        ALERTCalibrationEngine.calib.setDoubleValue(gr.getDataY(index), "t0", sector, slayer, 11);
                        ALERTCalibrationEngine.calib.setDoubleValue(mean, "upstream_downstream", sector, slayer, 11);
                    }
                }
            }
        }
        ALERTCalibrationEngine.calib.fireTableDataChanged();
    }
}
