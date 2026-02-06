package com.cousin;

import java.sql.Connection;
import java.sql.SQLException;

import com.cousin.util.DbConnection;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        String url = firstNonBlank(System.getProperty("db.url"), System.getenv("DB_URL"));
        String user = firstNonBlank(System.getProperty("db.user"), System.getenv("DB_USER"));
        String password = firstNonBlank(System.getProperty("db.password"), System.getenv("DB_PASSWORD"));

        if (isBlank(url) || isBlank(user)) {
            System.err.println("Missing database config.");
            System.err.println("Set DB_URL, DB_USER, DB_PASSWORD or pass -Ddb.url/-Ddb.user/-Ddb.password.");
            System.exit(1);
        }

        DbConnection.init(url, user, password);

        try (Connection connection = DbConnection.getConnection()) {
            System.out.println("Database OK: " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (!isBlank(primary)) {
            return primary;
        }
        return isBlank(fallback) ? null : fallback;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
