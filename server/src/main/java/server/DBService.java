package server;

import filesystem.FileNavigator;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import network.UserData;

import java.sql.*;

@Slf4j
public class DBService {
    static final String DB_URL = "jdbc:mysql://localhost:3306/cloudstorage?useSSL=false";
    static final String DB_USER = "root";
    static final String DB_PASS = "12345";

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

    public boolean authUser(UserData userData) {
        boolean result = false;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT id, userName FROM users WHERE login=? and password=?");
            query.setString(1, userData.getUserLogin());
            query.setString(2, userData.getUserPassword());
            ResultSet resultset = query.executeQuery();
            int userId = -1;
            while (resultset.next()) {
                userId = resultset.getInt(1);
            }
            if (userId > 0) {
                userData.setUserID(userId);
                result = true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public ResultSet getUsersList() {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT id, userName FROM users;");
            resultSet = query.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    public ResultSet getFriendsList(int userId) {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT distinct u.id, u.username FROM shared_folder_to sh_to\n" +
                    "                        JOIN users_shared_folder uf\n" +
                    "                          ON sh_to.folderid = uf.id\n" +
                    "                        JOIN users u on u.id = uf.userid\n" +
                    "                       WHERE sh_to.userid = ?;");
            query.setInt(1, userId);
            resultSet = query.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    public void addSharedFolder(int userId, int toUserId, String folderName, String folderPath) {
        CallableStatement call;
        try {
            call = connection.prepareCall("call add_shared_folder(?,?,?,?,?,?);");
            call.setInt(1, userId);
            call.setString(2, folderName);
            call.setString(3, folderPath);
            call.setInt(4, toUserId);
            call.registerOutParameter(5, Types.NVARCHAR);
            call.registerOutParameter(6, Types.NVARCHAR);


            call.execute();
            String retval = call.getString(5);
            String message = call.getString(6);
            if (!retval.equals("00000")) {
                log.error(message);
                return;
            }

        } catch (
                SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeSharedFolder(int userId, int toUserId, String folderName) {
        CallableStatement call;
        try {
            call = connection.prepareCall("call remove_shared_folder(?,?,?,?,?);");
            call.setInt(1, userId);
            call.setString(2, folderName);
            call.setInt(3, toUserId);
            call.registerOutParameter(4, Types.NVARCHAR);
            call.registerOutParameter(5, Types.NVARCHAR);


            call.execute();
            String retval = call.getString(4);
            String message = call.getString(5);
            if (!retval.equals("00000")) {
                log.error(message);
                return;
            }

        } catch (
                SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ResultSet getMyFriends(FileNavigator fn) {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT distinct u.id, u.username FROM users_shared_folder uf\n" +
                    "                        JOIN shared_folder_to sh_to\n" +
                    "                          ON sh_to.folderid = uf.id\n" +
                    "                        JOIN users u on u.id = sh_to.userid\n" +
                    "                       WHERE uf.userid = ?;");
            query.setInt(1, fn.getUserid());
            resultSet = query.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    public boolean isFolderShared(int userid, String foldername) {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT count(1) as count from users_shared_folder where userid = ? and folder = ?");
            query.setInt(1, userid);
            query.setString(2,foldername);
            resultSet = query.executeQuery();
            while (resultSet.next()){
                return resultSet.getInt(1)>0;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public ResultSet getUserSharedFolders(int fromUserId, int toUserId) {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("select distinct usf.folder, usf.id  from shared_folder_to sh_to \n" +
                    "         join users_shared_folder usf \n" +
                    "           on usf.id = sh_to.folderid \n" +
                    "        where sh_to.userid = ?\n" +
                    "          and usf.userid = ?");
            query.setInt(1, toUserId);
            query.setInt(2, fromUserId);
            resultSet = query.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }

    public ResultSet getFolderPath(int folderID) {
        ResultSet resultSet = null;
        PreparedStatement query;
        try {
            query = connection.prepareStatement("SELECT folderpath, folder from users_shared_folder where id = ?;");
            query.setInt(1, folderID);

            resultSet = query.executeQuery();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return resultSet;
    }
}
