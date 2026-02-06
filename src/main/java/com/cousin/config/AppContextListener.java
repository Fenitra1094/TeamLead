package com.cousin.config;

import com.cousin.util.DbConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String url = ctx.getInitParameter("db.url");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");
        String driver = ctx.getInitParameter("db.driver");

        if (driver != null && !driver.isBlank()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                ctx.log("JDBC driver not found: " + driver, e);
            }
        }

        DbConnection.init(url, user, password);
        ctx.log("Database initialized");
    }
}
