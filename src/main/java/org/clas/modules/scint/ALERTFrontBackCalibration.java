package org.clas.modules.scint;

import org.clas.modules.gui.ALERTCalibGUI;
import org.clas.modules.ALERTCalConstants;
import org.clas.modules.ALERTCalibrationEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

public class ALERTFrontBackCalibration {

    public void getMeanDifference(IndexedList<DataGroup> mdist){
        System.out.println("Start in getMean");
        ALERTCalibrationEngine Calib_FB = new ALERTCalibrationEngine();
        H1F meanDist[][] = new H1F[15][4];
        F1D fbFunc = new F1D("fbFunc","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);

        for (int sector = 0; sector < 15; sector++) {
            for (int slayer = 0; slayer < 4; slayer++) {

                meanDist[sector][slayer] = mdist.getItem(sector, slayer, 10).getH1F("fbAlignment");
                meanDist[sector][slayer].setTitleY("Time Diff");
                int maxBin = meanDist[sector][slayer].getMaximumBin();
                double maxPos = meanDist[sector][slayer].getXaxis().getBinCenter(maxBin);
                double min_test = meanDist[sector][slayer].getMin();

                // minPos needs to be better defined. minBin doesn't exist
                double minPos = maxPos - (2*maxPos);
                fbFunc.setRange(minPos,maxPos);

                fbFunc.setParameter(0, meanDist[sector][slayer].getBinContent(maxBin));
                fbFunc.setParLimits(0, meanDist[sector][slayer].getBinContent(maxBin)*0.7, meanDist[sector][slayer].getBinContent(maxBin)*1.2);
                fbFunc.setParameter(1, maxPos);
                fbFunc.setParameter(2, 1.0);

                //H1F hp = pdat.getItem(0,0,1).getH1F("Pedestal");
                //H1F hp = (H1F) pdat.getH1F("Pedestal");
                //F1D fp = new F1D("gmFunc", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)",0.0, hvmax);
                DataFitter.fit(fbFunc, meanDist[sector][slayer], "HQ");
                double mean = fbFunc.getParameter(1);
                System.out.println("upstream_downstream mean: " + mean);

                   // double mean_error = fbFunc.parameter(1).error();
                for (int comp = 0; comp <= 10; comp++) {
                    ALERTCalibrationEngine.calib.setDoubleValue(mean,"upstream_downstream",sector,slayer,comp);
                   // Calib_FB.calib.setDoubleValue(mean_error,"FBAlign_err",sector,slayer,comp);
                    //pdat.AssignConsts(fp.getParameter(1));
                    if(comp==10){
                        ALERTCalibrationEngine.calib.setDoubleValue(mean,"upstream_downstream",sector,slayer,11);
                    }
                }
            }
        }
        ALERTCalibrationEngine.calib.fireTableDataChanged();


    }
}
