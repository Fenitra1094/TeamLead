package com.cousin;

import com.cousin.model.TokenExpiration;
import com.cousin.service.TokenService;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

        // --- Gestion des tokens ---
        String action = args.length > 0 ? args[0] : "generate";

        TokenService tokenService = new TokenService();

        try {
            switch (action) {
                case "generate":
                    int hours = 8; // durée par défaut : 8h
                    if (args.length > 1) {
                        hours = Integer.parseInt(args[1]);
                    }
                    TokenExpiration newToken = tokenService.generateToken(hours);
                    System.out.println("=== Nouveau token genere (expire dans " + hours + "h) ===");
                    System.out.println("Token  : " + newToken.getToken());
                    System.out.println("Expire : " + newToken.getExpiration());
                    System.out.println("ID     : " + newToken.getId());
                    break;

                case "generate-min":
                    int minutes = 10; // durée par défaut : 10 min
                    if (args.length > 1) {
                        minutes = Integer.parseInt(args[1]);
                    }
                    TokenExpiration newTokenMin = tokenService.generateTokenMinutes(minutes);
                    System.out.println("=== Nouveau token genere (expire dans " + minutes + " min) ===");
                    System.out.println("Token  : " + newTokenMin.getToken());
                    System.out.println("Expire : " + newTokenMin.getExpiration());
                    System.out.println("ID     : " + newTokenMin.getId());
                    break;

                case "list":
                    List<TokenExpiration> tokens = tokenService.listTokens();
                    System.out.println("=== Liste des tokens ===");
                    if (tokens.isEmpty()) {
                        System.out.println("Aucun token en base.");
                    } else {
                        for (TokenExpiration t : tokens) {
                            String status = t.isExpired() ? "EXPIRE" : "VALIDE";
                            System.out.printf("  [%d] %s | expire: %s | %s%n",
                                    t.getId(), t.getToken(), t.getExpiration(), status);
                        }
                    }
                    break;

                case "clean":
                    int deleted = tokenService.cleanExpiredTokens();
                    System.out.println("Tokens expires supprimes : " + deleted);
                    break;

                default:
                    System.out.println("Usage: Main <action> [options]");
                    System.out.println("  generate [heures]      - Generer un token (defaut: 8h)");
                    System.out.println("  generate-min [minutes] - Generer un token (defaut: 10min)");
                    System.out.println("  list                   - Lister tous les tokens");
                    System.out.println("  clean                  - Supprimer les tokens expires");
                    break;
            }
        } catch (SQLException e) {
            System.err.println("Erreur token : " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
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
