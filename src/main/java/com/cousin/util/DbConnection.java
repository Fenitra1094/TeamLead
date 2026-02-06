package com.cousin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnection {
    private static String url;
    private static String user;
    private static String password;

    private DbConnection() {
    }

    public static void init(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Database is not initialized. Check web.xml params.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
