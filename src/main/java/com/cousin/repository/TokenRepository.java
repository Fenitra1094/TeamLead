package com.cousin.repository;

import com.cousin.model.TokenExpiration;
import com.cousin.util.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TokenRepository {

    /**
     * Insère un nouveau token avec sa date d'expiration.
     */
    public void insert(TokenExpiration tokenExpiration) throws SQLException {
        String sql = "INSERT INTO token_expiration(token, expiration) VALUES (?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, tokenExpiration.getToken());
            statement.setTimestamp(2, Timestamp.valueOf(tokenExpiration.getExpiration()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    tokenExpiration.setId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Recherche un token par sa valeur.
     * Retourne null si le token n'existe pas en base.
     */
    public TokenExpiration findByToken(String token) throws SQLException {
        String sql = "SELECT id, token, expiration FROM token_expiration WHERE token = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    TokenExpiration te = new TokenExpiration();
                    te.setId(rs.getInt("id"));
                    te.setToken(rs.getString("token"));
                    Timestamp ts = rs.getTimestamp("expiration");
                    if (ts != null) {
                        te.setExpiration(ts.toLocalDateTime());
                    }
                    return te;
                }
            }
        }
        return null;
    }

    /**
     * Liste tous les tokens.
     */
    public List<TokenExpiration> findAll() throws SQLException {
        String sql = "SELECT id, token, expiration FROM token_expiration ORDER BY id";
        List<TokenExpiration> tokens = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                TokenExpiration te = new TokenExpiration();
                te.setId(rs.getInt("id"));
                te.setToken(rs.getString("token"));
                Timestamp ts = rs.getTimestamp("expiration");
                if (ts != null) {
                    te.setExpiration(ts.toLocalDateTime());
                }
                tokens.add(te);
            }
        }
        return tokens;
    }

    /**
     * Supprime les tokens expirés.
     */
    public int deleteExpired() throws SQLException {
        String sql = "DELETE FROM token_expiration WHERE expiration < NOW()";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Génère un nouveau token GUID et l'insère avec la durée d'expiration spécifiée (en heures).
     */
    public TokenExpiration generateToken(int expirationHours) throws SQLException {
        TokenExpiration tokenExpiration = new TokenExpiration();
        tokenExpiration.setToken(UUID.randomUUID().toString());
        tokenExpiration.setExpiration(java.time.LocalDateTime.now().plusHours(expirationHours));
        insert(tokenExpiration);
        return tokenExpiration;
    }

    /**
     * Génère un nouveau token GUID et l'insère avec la durée d'expiration spécifiée (en minutes).
     */
    public TokenExpiration generateTokenMinutes(int expirationMinutes) throws SQLException {
        TokenExpiration tokenExpiration = new TokenExpiration();
        tokenExpiration.setToken(UUID.randomUUID().toString());
        tokenExpiration.setExpiration(java.time.LocalDateTime.now().plusMinutes(expirationMinutes));
        insert(tokenExpiration);
        return tokenExpiration;
    }
}
