package server;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import network.UserData;

import java.sql.*;

@Slf4j
public class DBService {
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

    public boolean authUser(UserData userData){
        boolean result = false;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT id, userName FROM users WHERE login=? and password=?");
            query.setString(1,userData.getUserLogin());
            query.setString(2,userData.getUserPassword());
            ResultSet resultset = query.executeQuery();
            int userId = -1;
            while (resultset.next()){
                userId=resultset.getInt(1);
            }
            if(userId > 0){
                userData.setUserID(userId);
                result = true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }
}
