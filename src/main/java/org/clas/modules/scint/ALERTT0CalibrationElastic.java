package org.clas.modules.scint;

import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.data.SchemaFactory;

import org.jlab.clas.physics.LorentzVector;

import org.jlab.groot.data.H1F;
import org.jlab.groot.math.F1D;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;

import javax.swing.*;
import java.util.*;

public class ALERTT0CalibrationElastic {

    private static final double E_beam = 2.2;
    private static final double mp_GeV = 0.938272;
    private static final double c_cm_ns = 29.9792458;
    private static final double barLength_mm = 279.79;
    private static final double v_eff_cm_ns = 20.0;
    private static final double tdcLSB_ns = 0.015625;
    private static final double defaultPathLength_cm = 78.5;

    private final Map<String, H1F> deltaTHistos = new HashMap<>();
    private final Map<String, List<Double>> deltaTValues = new HashMap<>();
    private final Map<String, Double> t0OffsetMap = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar ALERTT0CalibrationElastic.jar <input.hipo>");
            return;
        }
        new ALERTT0CalibrationElastic().runCalibration(args[0]);
    }

    public void runCalibration(String inputFile) {
        HipoReader reader = new HipoReader();
        reader.open(inputFile);
        Event event = new Event();
        SchemaFactory sf = reader.getSchemaFactory();

        while (reader.hasNext()) {
            reader.nextEvent(event);
            setTracksElastic(event, sf);
        }

        reader.close();
        fitAndPlotT0Offsets();
    }

    public void setTracksElastic(Event event, SchemaFactory sf) {
        Bank bR = new Bank(sf.getSchema("REC::Particle"));
        Bank bE = new Bank(sf.getSchema("REC::Event"));
        Bank bD = new Bank(sf.getSchema("ATOF::tdc"));
        event.read(bR);
        event.read(bE);
        event.read(bD);

        if (bR.getRows() == 0 || bE.getRows() == 0 || bD.getRows() == 0) return;

        float timeRef = bE.getFloat("startTime", 0);

        LorentzVector beam = new LorentzVector(0, 0, E_beam, E_beam);
        LorentzVector electron = new LorentzVector();
        boolean foundElectron = false;
        double vz = 0;

        for (int i = 0; i < bR.getRows(); i++) {
            if (bR.getInt("pid", i) == 11) {
                double ex = bR.getFloat("px", i) / 1000.0;
                double ey = bR.getFloat("py", i) / 1000.0;
                double ez = bR.getFloat("pz", i) / 1000.0;
                double Ee = Math.sqrt(ex * ex + ey * ey + ez * ez);
                vz = bR.getFloat("vz", i);
                electron.setPxPyPzE(ex, ey, ez, Ee);
                foundElectron = true;
                break;
            }
        }

        if (!foundElectron) return;

        LorentzVector q = new LorentzVector();
        q.copy(beam);
        q.sub(electron);

        double p = q.p();
        double E = Math.sqrt(p * p + mp_GeV * mp_GeV);
        double beta = p / E;
        double tProp = defaultPathLength_cm / (beta * c_cm_ns);

        for (int j = 0; j < bD.getRows(); j++) {
            int sector = bD.getInt("sector", j);
            int layer = bD.getInt("layer", j);
            int component = bD.getInt("component", j);
            int order = bD.getInt("order", j);
            double tPMT = bD.getInt("TDC", j) * tdcLSB_ns;

            String key = String.format("s%d_l%d_c%d_o%d", sector, layer, component, order);

            double tLight;
            if (component >= 10) {
                double zhit = vz / 10.0;
                double Llight = (barLength_mm / 2.0) + (order == 0 ? zhit : -zhit);
                tLight = Llight / v_eff_cm_ns;
            } else {
                tLight = (3.0 / 10.0) / v_eff_cm_ns;
            }

            double ptime = tProp + tLight;
            double deltaT = tPMT + timeRef - ptime;

            deltaTHistos.computeIfAbsent(key, s -> new H1F("hDT_" + s, 100, 200, 400)).fill((float) deltaT);
            deltaTValues.computeIfAbsent(key, s -> new ArrayList<>()).add(deltaT);
        }
    }

    private void fitAndPlotT0Offsets() {
        for (String key : deltaTHistos.keySet()) {
            H1F hRaw = deltaTHistos.get(key);
            if (hRaw.getEntries() < 30) continue;

            double mean = hRaw.getMean();
            double rms = hRaw.getRMS();
            double fitMin = mean - 1.5 * rms;
            double fitMax = mean + 1.5 * rms;

            F1D gaus = new F1D("gaus", "[amp]*exp(-0.5*((x-[mean])/[sigma])^2)", fitMin, fitMax);
            gaus.setParameter(0, hRaw.getMax());
            gaus.setParameter(1, mean);
            gaus.setParameter(2, rms);
            DataFitter.fit(gaus, hRaw, "Q");

            double sigma = gaus.getParameter(2);
            if (sigma > 20 || sigma <= 0) continue;

            double t0 = gaus.getParameter(1);
            t0OffsetMap.put(key, t0);
            System.out.printf("PMT %s: T0 offset = %.3f ns\n", key, t0);

            H1F hCorr = new H1F("hCorr_" + key, 100, -30, 30);
            for (double dt : deltaTValues.get(key)) {
                hCorr.fill((float)(dt - t0));
            }

            JFrame frame = new JFrame("T0 Calibration - " + key);
            frame.setSize(1000, 500);
            EmbeddedCanvas canvas = new EmbeddedCanvas();
            canvas.divide(2, 1);

            canvas.cd(0);
            hRaw.setTitleX("ΔT (ns)");
            canvas.draw(hRaw);
            canvas.draw(gaus, "same");

            canvas.cd(1);
            hCorr.setTitleX("ΔT - T₀ (ns)");
            canvas.draw(hCorr);

            frame.add(canvas);
            frame.setVisible(true);
        }
    }
}


