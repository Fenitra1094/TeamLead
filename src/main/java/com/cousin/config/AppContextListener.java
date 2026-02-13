/*package com.cousin.config;

import com.cousin.util.DbConnection;


 * AppContextListener - Initialisation de la base de données
 * Sprint 1 : Version simple sans dépendance Servlet API
 
public class AppContextListener {
    
    /**
     * Initialiser la base de données
     * À appeler depuis web.xml ou une servlet d'initialisation
    
    public static void initDatabase() {
        // Récupérer les paramètres de la BD depuis DbConnection
        // Ces valeurs sont définies dans web.xml
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
 */

package com.cousin.config;

import com.cousin.util.DbConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Initialise la base de données au démarrage de l'application
 * Lit les paramètres depuis web.xml
 */
public class AppContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        
        try {
            // Charger le driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Lire les paramètres depuis web.xml
            String url = ctx.getInitParameter("db.url");
            String user = ctx.getInitParameter("db.user");
            String password = ctx.getInitParameter("db.password");
            String driver = ctx.getInitParameter("db.driver");
            
            System.out.println("[INFO] Loading database config from web.xml:");
            System.out.println("  - URL: " + url);
            System.out.println("  - User: " + user);
            System.out.println("  - Driver: " + driver);
            
            // Initialiser DbConnection avec les paramètres de web.xml
            DbConnection.init(url, user, password);
            System.out.println("[OK] Database initialized successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] PostgreSQL JDBC driver not found");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Database initialization failed");
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[INFO] Application context destroyed");
    }
}
