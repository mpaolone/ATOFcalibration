package org.clas.modules.scint;

import org.clas.modules.gui.ALERTCalibGUI;
import org.clas.modules.ALERTCalibrationEngine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.utils.groups.IndexedList;

public class ALERTPedestalCalibration {
    private int hvmax = 2000;
    public void findPeakMean(IndexedList<DataGroup> pdat) {
        //H1F hp = (H1F) pdat.SCDG.getH(raw_a);
        ALERTCalibrationEngine Calib_Ped = new ALERTCalibrationEngine();
        H1F hp[][][] = new H1F[16][3][5];
        F1D fp = new F1D("gmFunc", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)",
                0.0, hvmax);
        for (int sector = 0; sector < 16; sector++) {
            for (int slayer = 0; slayer < 2; slayer++) {
                for (int comp = 0; comp <= 4 - 1; comp++) {

                    hp[sector][slayer][comp] = pdat.getItem(sector, slayer, comp).getH1F("Pedestal");
                    hp[sector][slayer][comp].setTitleX("ADC");
                    //H1F hp = pdat.getItem(0,0,1).getH1F("Pedestal");
                    //H1F hp = (H1F) pdat.getH1F("Pedestal");
                    //F1D fp = new F1D("gmFunc", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)",0.0, hvmax);
                    DataFitter.fit(fp, hp[sector][slayer][comp], "HQ");
                    double mean = fp.getParameter(1);
                    double mean_error = fp.parameter(1).error();
                    Calib_Ped.calib.setDoubleValue(mean,"Pedestal",sector,slayer,comp);
                    Calib_Ped.calib.setDoubleValue(mean_error,"Pedestal_err",sector,slayer,comp);

                    //pdat.AssignConsts(fp.getParameter(1));
                }
            }
        }
        Calib_Ped.calib.fireTableDataChanged();
        ALERTCalibGUI DrawHisto = new ALERTCalibGUI();
        //DrawHisto.Draw_H1(hp[1][0][0], fp);
    }


}
