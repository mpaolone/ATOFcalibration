package org.clas.modules.utils;

import org.jlab.ccdb.*;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.groups.IndexedTableViewer;
import org.rcdb.RCDB;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ALERTDatabaseConstantProvider implements ConstantProvider {
    private HashMap<String, String[]> constantContainer = new HashMap();
    private boolean PRINT_ALL = true;
    private String variation = "default";
    private Integer runNumber = 10;
    private Integer loadTimeErrors = 0;
    private Boolean PRINTOUT_FLAG = false;
    private Integer dataYear = 118;
    private Integer dataMonth = 1;
    private Date databaseDate = new Date();
    private JDBCProvider provider;
    private int debugMode = 1;

    public ALERTDatabaseConstantProvider() {
        this.loadTimeErrors = 0;
        this.runNumber = 10;
        this.variation = "default";
       //String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        String address = "sqlite:///DB/latest.sqlite";
        String envAddress = this.getEnvironment();
        if (envAddress != null) {
            address = envAddress;
        }

        this.initialize(address);
    }
//
     /*
    public ALERTDatabaseConstantProvider(int run, String var) {
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
       String address = "sqlite:///DB/latest.sqlite";
      // String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        String envAddress = this.getEnvironment();
        if (envAddress != null) {
            address = envAddress;
        }
        this.initialize(address);
    }
*/
    public ALERTDatabaseConstantProvider(int run, String var, String timestamp) {
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
        String address = "sqlite:///DB/latest.sqlite";
       // String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        String envAddress = this.getEnvironment();
        if (envAddress != null) {
            address = envAddress;
        }

        if (timestamp.length() > 8) {
            this.setTimeStamp(timestamp);
        }

        this.initialize(address);
        System.out.println("here");
    }

    public ALERTDatabaseConstantProvider(String address) {
        this.initialize(address);
    }

    public ALERTDatabaseConstantProvider(String address, String var) {
        this.variation = var;
        this.initialize(address);
    }

    public void setPrintout(Boolean flag) {
        this.PRINTOUT_FLAG = flag;
    }

    public Set<String> getEntrySet() {
        Set<String> entries = new HashSet();
        Iterator var2 = this.constantContainer.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String[]> entry = (Map.Entry)var2.next();
            entries.add((String)entry.getKey());
        }

        return entries;
    }

    private String getEnvironment() {
        String envCCDB = System.getenv("CCDB_DATABASE");
        String envCLAS12 = System.getenv("CLAS12DIR");
        String connection = System.getenv("CCDB_CONNECTION");
        if (connection != null) {
            return connection;
        } else {
            String propCLAS12 = System.getProperty("CLAS12DIR");
            String propCCDB = System.getProperty("CCDB_DATABASE");
            StringBuilder str;
            if (envCCDB != null && envCLAS12 != null) {
                str = new StringBuilder();
                str.append("sqlite:///");
                if (envCLAS12.charAt(0) != '/') {
                    str.append("/");
                }

                str.append(envCLAS12);
                if (envCCDB.charAt(0) != '/' && envCLAS12.charAt(envCLAS12.length() - 1) != '/') {
                    str.append("/");
                }

                str.append(envCCDB);
                return str.toString();
            } else if (propCCDB != null && propCLAS12 != null) {
                str = new StringBuilder();
                str.append("sqlite:///");
                if (propCLAS12.charAt(0) != '/') {
                    str.append("/");
                }

                str.append(propCLAS12);
                if (propCCDB.charAt(0) != '/' && propCLAS12.charAt(propCLAS12.length() - 1) != '/') {
                    str.append("/");
                }

                str.append(propCCDB);
                return str.toString();
            } else {
                return null;
            }
        }
    }

    private void initialize(String address) {
        this.provider = CcdbPackage.createProvider(address);
        System.out.println("[DB] --->  debug mode : " + this.debugMode);

        if (this.debugMode > 0) {
            System.out.println("[DB] --->  open connection with : " + address);
            System.out.println("[DB] --->  database variation   : " + this.variation);
            System.out.println("[DB] --->  database run number  : " + this.runNumber);
            System.out.println("[DB] --->  database time stamp  : " + this.databaseDate);
        }

        this.provider.connect();
        if (this.provider.isConnected()){
            System.out.println("[DB] --->  database connection works yay! : success");
        }else{
            System.out.println("[DB] --->  database connection  : failed");
        }

       /*??? why is not working if (this.provider.isConnected()) {
            if (this.debugMode > 0) {
                System.out.println("[DB] --->  database connection works yay! : success");
            }
        } else {
            System.out.println("[DB] --->  database connection  : failed");
        }*/

        this.provider.setDefaultVariation(this.variation);
        this.provider.setDefaultDate(this.databaseDate);
        this.provider.setDefaultRun(this.runNumber);
    }

    public final void setTimeStamp(String timestamp) {
        String pattern = "MM/dd/yyyy-HH:mm:ss";
        if (!timestamp.contains("-")) {
            pattern = "MM/dd/yyyy";
        }

        SimpleDateFormat format = new SimpleDateFormat(pattern);

        try {
            this.databaseDate = format.parse(timestamp);
        } catch (ParseException var5) {
            System.out.println("\n\n ***** TIMESTAMP ERROR ***** error parsing timestamp : " + timestamp);
            this.databaseDate = new Date();
            System.out.println(" ***** TIMESTAMP WARNING ***** setting date to : " + this.databaseDate);
        }

    }

    public CalibrationConstants readConstants(String table_name) {
        Assignment asgmt = this.provider.getData(table_name);
        int ncolumns = asgmt.getColumnCount();
        Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
        String[] format = new String[ncolumns - 3];

        for(int loop = 3; loop < ncolumns; ++loop) {
            if (((TypeTableColumn)typecolumn.get(loop)).getCellType().name().compareTo("DOUBLE") == 0) {
                format[loop - 3] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/D";
            } else {
                format[loop - 3] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/I";
            }
        }

        CalibrationConstants table = new CalibrationConstants(3, format);

        for(int i = 0; i < 3; ++i) {
            table.setIndexName(i, ((TypeTableColumn)typecolumn.get(i)).getName());
        }

        table.show();
        List<Vector<String>> tableRows = new ArrayList();

        int nrows;
        for(nrows = 0; nrows < ncolumns; ++nrows) {
            String name = ((TypeTableColumn)typecolumn.get(nrows)).getName();
            Vector<String> column = asgmt.getColumnValuesString(name);
            tableRows.add(column);
        }

        nrows = ((Vector)tableRows.get(0)).size();

        for(int nr = 0; nr < nrows; ++nr) {
            String[] values = new String[ncolumns];

            for(int nc = 0; nc < ncolumns; ++nc) {
                values[nc] = (String)((Vector)tableRows.get(nc)).get(nr);
            }

            table.addEntryFromString(values);
        }

        return table;
    }
    public CalibrationConstants readConstants(String table_name, int indexCount) {
        Assignment asgmt = this.provider.getData(table_name);
        int ncolumns = asgmt.getColumnCount();
        Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
        String[] format = new String[ncolumns - 4];

        for(int loop = 4; loop < ncolumns; ++loop) {
            if (((TypeTableColumn)typecolumn.get(loop)).getCellType().name().compareTo("DOUBLE") == 0) {
                format[loop - 4] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/D";
            } else {
                format[loop - 4] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/I";
            }
        }

        CalibrationConstants table = new CalibrationConstants(4, format);

        for(int i = 0; i < 4; ++i) {
            table.setIndexName(i, ((TypeTableColumn)typecolumn.get(i)).getName());
        }

        table.show();
        List<Vector<String>> tableRows = new ArrayList();

        int nrows;
        for(nrows = 0; nrows < ncolumns; ++nrows) {
            String name = ((TypeTableColumn)typecolumn.get(nrows)).getName();
            Vector<String> column = asgmt.getColumnValuesString(name);
            tableRows.add(column);
        }

        nrows = ((Vector)tableRows.get(0)).size();

        for(int nr = 0; nr < nrows; ++nr) {
            String[] values = new String[ncolumns];

            for(int nc = 0; nc < ncolumns; ++nc) {
                values[nc] = (String)((Vector)tableRows.get(nc)).get(nr);
            }

            table.addEntryFromString(values);
        }

        return table;
    }

    public IndexedTable readTable(String table_name) {
        return this.readTable(table_name, 3);
    }

    //Read Table
    public IndexedTable readTable(String table_name, int nindex) {
        Assignment asgmt = this.provider.getData(table_name);
        int ncolumns = asgmt.getColumnCount();
        Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
        String[] format = new String[ncolumns - nindex];

        for(int loop = nindex; loop < ncolumns; ++loop) {
            if (((TypeTableColumn)typecolumn.get(loop)).getCellType().name().compareTo("DOUBLE") == 0) {
                format[loop - nindex] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/D";
            } else {
                format[loop - nindex] = ((TypeTableColumn)typecolumn.get(loop)).getName() + "/I";
            }
        }

        IndexedTable table = new IndexedTable(nindex, format);

        for(int i = 0; i < nindex; ++i) {
            table.setIndexName(i, ((TypeTableColumn)typecolumn.get(i)).getName());
        }

        List<Vector<String>> tableRows = new ArrayList();

        int nrows;
        for(nrows = 0; nrows < ncolumns; ++nrows) {
            String name = ((TypeTableColumn)typecolumn.get(nrows)).getName();
            Vector<String> column = asgmt.getColumnValuesString(name);
            tableRows.add(column);
        }

        nrows = ((Vector)tableRows.get(0)).size();


        //System.out.println("rows:  "+nrows);
        for(int nr = 0; nr < nrows; ++nr) {
            String[] values = new String[ncolumns];

            for(int nc = 0; nc < ncolumns; ++nc) {
                values[nc] = (String)((Vector)tableRows.get(nc)).get(nr);
            }

            table.addEntryFromString(values);
        }

        return table;
    }

    public void loadTable(String table_name) {
        try {
            Assignment asgmt = this.provider.getData(table_name);
            int ncolumns = asgmt.getColumnCount();
            TypeTable table = asgmt.getTypeTable();
            Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
            System.out.println("[DB LOAD] ---> loading data table : " + table_name);
            System.out.println("[DB LOAD] ---> number of columns  : " + typecolumn.size());
            System.out.println();

            for(int loop = 0; loop < ncolumns; ++loop) {
                String name = ((TypeTableColumn)typecolumn.get(loop)).getName();
                Vector<String> row = asgmt.getColumnValuesString(name);
                String[] values = new String[row.size()];

                for(int el = 0; el < row.size(); ++el) {
                    values[el] = (String)row.elementAt(el);
                }

                StringBuilder str = new StringBuilder();
                str.append(table_name);
                str.append("/");
                str.append(((TypeTableColumn)typecolumn.elementAt(loop)).getName());
                this.constantContainer.put(str.toString(), values);
            }
        } catch (Exception var11) {
            System.out.println("[DB LOAD] --->  error loading table : " + table_name);
            Integer var3 = this.loadTimeErrors;
            this.loadTimeErrors = this.loadTimeErrors + 1;
        }

    }

    public void loadTables(String... tbl) {
        String[] var2 = tbl;
        int var3 = tbl.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String var10000 = var2[var4];
        }

    }

    public boolean hasConstant(String string) {
        return this.constantContainer.containsKey(string);
    }

    public int length(String string) {
        return this.hasConstant(string) ? ((String[])this.constantContainer.get(string)).length : 0;
    }

    public double getDouble(String string, int i) {
        return this.hasConstant(string) && i < this.length(string) ? Double.parseDouble(((String[])this.constantContainer.get(string))[i]) : 0.0D;
    }

    public int getInteger(String string, int i) {
        return this.hasConstant(string) && i < this.length(string) ? Integer.parseInt(((String[])this.constantContainer.get(string))[i]) : 0;
    }

    public void disconnect() {
        System.out.println("[DB] --->  database disconnect  : success");
        this.provider.close();
    }

    public void show() {
    }

    public String showString() {
        StringBuilder str = new StringBuilder();
        Iterator var2 = this.constantContainer.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String[]> item = (Map.Entry)var2.next();
            str.append(String.format("\t*  %-52s : %8d   *\n", item.getKey(), ((String[])item.getValue()).length));
        }

        return str.toString();
    }

    public String toString() {
        System.err.println("Database Constat Provider: ");
        StringBuilder str = new StringBuilder();
        Iterator var2;
        Map.Entry entry;
        if (this.PRINT_ALL) {
            var2 = this.constantContainer.entrySet().iterator();

            while(var2.hasNext()) {
                entry = (Map.Entry)var2.next();
                str.append(String.format("%24s : %d\n", entry.getKey(), ((String[])entry.getValue()).length));

                for(int loop = 0; loop < ((String[])entry.getValue()).length; ++loop) {
                    str.append(String.format("%18s ", ((String[])entry.getValue())[loop]));
                }

                str.append("\n");
            }
        } else {
            var2 = this.constantContainer.entrySet().iterator();

            while(var2.hasNext()) {
                entry = (Map.Entry)var2.next();
                str.append(String.format("%24s : %d\n", entry.getKey(), ((String[])entry.getValue()).length));
            }
        }

        return str.toString();
    }

    public void clear() {
        this.constantContainer.clear();
    }

    public int getSize() {
        return this.constantContainer.size();
    }

    public int getSize(String name) {
        if (this.hasConstant(name)) {
            String[] array = (String[])this.constantContainer.get(name);
            return array.length;
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        //ALERTDatabaseConstantProvider provider = new ALERTDatabaseConstantProvider(1, "default","");
        //IndexedTable table = provider.readTable("/test/test_vars/alert_dc_status4");
        //IndexedTable table = provider.readTable("/calibration/dc/time_corrections/T0Corrections");
        //IndexedTable table = provider.readTable("/calibration/dc/tracking/wire_status");
        //provider.disconnect();

        /*

        JFrame frame = new JFrame();
       frame.setSize(600, 600);
        IndexedTableViewer canvas = new IndexedTableViewer(table);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

      //  org.clas.gui.ALERTCalibGUI viewer = new org.clas.gui.ALERTCalibGUI();
        //frame.add(canvas);
        //frame.add(viewer.mainPanel);
      //  frame.setSize(1400, 800);
        //frame.setVisible(true);

        table.show();
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date dateNow = new Date();
        System.out.println(dateNow);

        try {
            Date dateThen = format.parse("01/23/2017");
            System.out.println(dateThen);
        } catch (ParseException var18) {
            Logger.getLogger(org.jlab.detector.calib.utils.DatabaseConstantProvider.class.getName()).log(Level.SEVERE, (String)null, var18);
        }
        */


    }
}

