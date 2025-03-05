package org.clas.modules.utils;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

//package org.jlab.detector.calib.utils;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.groups.IndexedList.IndexGenerator;
import org.jlab.utils.groups.IndexedTable.IndexedEntry;
import org.jlab.utils.system.ClasUtilsFile;

public class ALERTCalibrationConstants extends IndexedTable {
    String constantsName = "default";

    public ALERTCalibrationConstants(int indexCount) {
        super(indexCount);
    }

    public ALERTCalibrationConstants(int indexCount, String format) {
        super(indexCount, format);
        if (indexCount == 3) {
            this.setIndexName(0, "sector");
            this.setIndexName(1, "layer");
            this.setIndexName(2, "component");
        }

        if (indexCount == 4) {
            this.setIndexName(0, "detector");
            this.setIndexName(1, "sector");
            this.setIndexName(2, "layer");
            this.setIndexName(3, "component");
        }

    }

    public ALERTCalibrationConstants(int indexCount, String[] format) {
        super(indexCount, format);
        if (indexCount == 3) {
            this.setIndexName(0, "sector");
            this.setIndexName(1, "layer");
            this.setIndexName(2, "component");
        }

        if (indexCount == 4) {
            this.setIndexName(0, "detector");
            this.setIndexName(1, "sector");
            this.setIndexName(2, "layer");
            this.setIndexName(3, "component");
        }

    }

    public void setName(String name) {
        this.constantsName = name;
    }

    public String getName() {
        return this.constantsName;
    }

    public void save(String file) {
        List<String> linesFile = new ArrayList();
        Map<Long, IndexedEntry> map = this.getList().getMap();
        int nindex = this.getList().getIndexSize();
        Iterator var5 = map.entrySet().iterator();

        while(var5.hasNext()) {
            Entry<Long, IndexedEntry> entry = (Entry)var5.next();
            StringBuilder str = new StringBuilder();

            int ncolumns;
            for(ncolumns = 0; ncolumns < nindex; ++ncolumns) {
                str.append(String.format("%3d ", IndexGenerator.getIndex((Long)entry.getKey(), ncolumns)));
            }

            ncolumns = ((IndexedEntry)entry.getValue()).getSize();

            for(int i = 0; i < ncolumns; ++i) {
                str.append(String.format("  %e  ", ((IndexedEntry)entry.getValue()).getValue(i)));
            }

            linesFile.add(str.toString());
        }

        ClasUtilsFile.writeFile(file, linesFile);
    }

    public static void main(String[] args) {
        org.jlab.detector.calib.utils.CalibrationConstants gain = new org.jlab.detector.calib.utils.CalibrationConstants(3, "Mean/F:Error/I:Sigma/F:Serror/F");

        for(int i = 0; i < 23; ++i) {
            gain.addEntry(new int[]{1, 1, i + 1});
        }

        gain.setDoubleValue(0.2D, "Mean", new int[]{1, 1, 1});
        gain.setDoubleValue(0.3D, "Mean", new int[]{1, 1, 2});
        gain.setDoubleValue(0.4D, "Mean", new int[]{1, 1, 3});
        gain.setDoubleValue(0.5D, "Mean", new int[]{1, 1, 4});
        gain.setDoubleValue(0.6D, "Mean", new int[]{1, 1, 5});
        gain.setIntValue(4, "Error", new int[]{1, 1, 4});
        gain.save("filename");
    }

    public static class CalibrationConstantsRenderer extends DefaultTableCellRenderer {
        org.jlab.detector.calib.utils.CalibrationConstants calib = null;

        public CalibrationConstantsRenderer(org.jlab.detector.calib.utils.CalibrationConstants cc) {
            this.calib = cc;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!this.calib.isValid(row, column)) {
                if (isSelected) {
                    c.setBackground(Color.RED);
                } else {
                    c.setBackground(new Color(255, 120, 120));
                }

                c.setForeground(Color.YELLOW);
                return c;
            } else if (isSelected) {
                c.setBackground(new Color(20, 20, 255));
                c.setForeground(Color.WHITE);
                return c;
            } else {
                if (row % 2 == 0) {
                    c.setBackground(new Color(220, 255, 220));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(220, 220, 255));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        }
    }
}

