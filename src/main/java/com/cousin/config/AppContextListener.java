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

/**
 * Initialise la base de données au démarrage de l'application
 * (version simplifiée - pas de dépendance servlet pour Tomcat 11)
 */
public class AppContextListener {
    
    static {
        // Bloc static pour initialiser au chargement de la classe
        try {
            Class.forName("org.postgresql.Driver");
            DbConnection.init(
                "jdbc:postgresql://localhost:5432/reservation_voiture?currentSchema=staging",
                "postgres",
                "postgres"
            );
            System.out.println("[OK] Database initialized for staging schema");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] PostgreSQL JDBC driver not found");
            e.printStackTrace();
        }
    }
}
