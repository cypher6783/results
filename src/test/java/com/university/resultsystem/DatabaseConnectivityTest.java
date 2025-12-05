package com.university.resultsystem;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectivityTest {

    @Test
    public void listTables() {
        String url = "jdbc:h2:file:./data/resultdb;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE";
        String user = "postgres";
        String password = "asphalt6";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement()) {

            System.out.println("Connected to database successfully.");

            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Tables in database:");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
