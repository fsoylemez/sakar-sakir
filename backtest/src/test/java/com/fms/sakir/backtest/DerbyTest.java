package com.fms.sakir.backtest;

import java.sql.*;

public class DerbyTest {

    public DerbyTest() {

        String urlConnection = "jdbc:derby://localhost/MyDbTest;create=false";
        try (Connection con = DriverManager.getConnection(urlConnection)) {
            Statement statement = con.createStatement();
            String sql = "SELECT * FROM derbyDb";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int num = resultSet.getInt("num");
                String addr = resultSet.getString("addr");
                System.out.println("Num: " + num);
                System.out.println("Address: " + addr);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        DerbyTest dao = new DerbyTest();
    }

}