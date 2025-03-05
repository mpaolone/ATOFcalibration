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
        ALERTCalibrationEngine Calib_FB = new ALERTCalibrationEngine();
        H1F meanDist[][][] = new H1F[16][3][5];
        F1D fbFunc = new F1D("fbFunc","[amp]*gaus(x,[mean],[sigma])", -10.0, 10.0);

        for (int sector = 0; sector < 5; sector++) {
            for (int slayer = 0; slayer < 2; slayer++) {
                for (int comp = 0; comp <= 4 - 1; comp++) {

                    meanDist[sector][slayer][comp] = mdist.getItem(sector, slayer, comp).getH1F("fbAlignment");
                    meanDist[sector][slayer][comp].setTitleY("Time Diff");
                    int maxBin = meanDist[sector][slayer][comp].getMaximumBin();
                    double maxPos = meanDist[sector][slayer][comp].getXaxis().getBinCenter(maxBin);
                    double min_test = meanDist[sector][slayer][comp].getMin();

                    // minPos needs to be better defined. minBin doesn't exist
                    double minPos = maxPos - (2*maxPos);
                    fbFunc.setRange(minPos,maxPos);

                    fbFunc.setParameter(0, meanDist[sector][slayer][comp].getBinContent(maxBin));
                    fbFunc.setParLimits(0, meanDist[sector][slayer][comp].getBinContent(maxBin)*0.7, meanDist[sector][slayer][comp].getBinContent(maxBin)*1.2);
                    fbFunc.setParameter(1, maxPos);
                    fbFunc.setParameter(2, 1.0);

                    //H1F hp = pdat.getItem(0,0,1).getH1F("Pedestal");
                    //H1F hp = (H1F) pdat.getH1F("Pedestal");
                    //F1D fp = new F1D("gmFunc", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)",0.0, hvmax);
                    DataFitter.fit(fbFunc, meanDist[sector][slayer][comp], "HQ");
                    double mean = fbFunc.getParameter(1);
                    double mean_error = fbFunc.parameter(1).error();
                    Calib_FB.calib.setDoubleValue(mean,"FBAlign",sector,slayer,comp);
                    Calib_FB.calib.setDoubleValue(mean_error,"FBAlign_err",sector,slayer,comp);
                    //pdat.AssignConsts(fp.getParameter(1));
                }
            }
        }
        Calib_FB.calib.fireTableDataChanged();
        ALERTCalConstants Cal_Passer = new ALERTCalConstants();
        //Cal_Passer.Calib_Passer();
        ALERTCalibGUI DrawFB = new ALERTCalibGUI();
        System.out.println("DRAW HISTO");
        DrawFB.Draw_H1(meanDist[0][1][1], fbFunc);


    }
}
