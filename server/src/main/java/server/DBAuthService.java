package server;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DBAuthService {
    static final String DB_URL = "jdbc:mysql://localhost:3306/cloudstorage?useSSL=false";
    static final String DB_USER = "root";
    static final String DB_PASS = "dikop";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection connection;

    public void start() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            log.info("Connection to database established");
        }
        log.info("Authentication Service has been started");

    }


    public void stop() {

        try {
            connection.close();
            log.info("Disconnected from database");
        } catch (SQLException e) {
            log.error(e.getMessage() + "Closing database connection error");
        }
        log.info("Authentication Service is stopped");
    }

}
