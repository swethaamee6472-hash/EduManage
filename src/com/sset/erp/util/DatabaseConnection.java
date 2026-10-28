package com.sset.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection manager for the EduManage MySQL database.
 * Uses MySQL Connector/J (lib/mysql-connector-j-8.4.0.jar) to talk to the
 * local database server on this device.
 */
public final class DatabaseConnection {
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String DATABASE = "edumanage";
    private static final String USER = "root";
    private static final String PASSWORD = "swetha-123";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect();
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] MySQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Initial MySQL connect failed: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns a live connection, reconnecting transparently if the previous one dropped.
     */
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    public boolean isAvailable() {
        try {
            return getConnection().isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void connect() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
                + "&characterEncoding=UTF-8&autoReconnect=true";
        connection = DriverManager.getConnection(url, USER, PASSWORD);
        connection.setAutoCommit(true);
    }
}
