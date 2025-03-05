package org.clas.modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ALERTCalConstants {
    public static Map< String,Double> ALERTCals  = new HashMap< String,Double>();

    public ALERTCalConstants() {


    }

    //public Map< String,Double> getPrevCalib(String )

    //public Map< String,Double> NewCalibConstants()

    public static void Connect_SQL(Map< String,Double> Constants){
        //Class.forName("org.sqlite.JDBC");
        System.out.println("Second: Connect to the database");

        //Connection conn = null;
        Connection conn = null;

        try {
            String url = "jdbc:sqlite:/Users/michaelnycz/Downloads/CLAS_DB.db";
            //conn = (Connection) DriverManager.getConnection(url);
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQlite has been established");

            /*
            Move this out to someplace else if it is connecting
             */
            String veff_left;
            String veff_right;
            String veff_left_err;
            String veff_right_err;

            String query;
            query = "UPDATE ALERT_Veff SET veff_left = ?";
            PreparedStatement myStmt = conn.prepareStatement(query);
            myStmt.setDouble(1,2.2);
            //myStmt.setDouble(2,0.0001);
            myStmt.executeUpdate();
            conn.close();
            System.out.println("Updated SQL and have closed the connection");




        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        finally {
            try {
                if (conn != null){
                    conn.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }



}
