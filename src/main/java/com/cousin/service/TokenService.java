package com.cousin.service;

import com.cousin.model.TokenExpiration;
import com.cousin.repository.TokenRepository;
import java.sql.SQLException;
import java.util.List;

public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService() {
        this.tokenRepository = new TokenRepository();
    }

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Vérifie si un token est valide :
     * 1. Le token existe en base ?
     * 2. Le token n'est pas expiré ?
     *
     * @param token la valeur du token à vérifier
     * @return true si le token est valide et non expiré
     */
    public boolean isTokenValid(String token) throws SQLException {
        if (token == null || token.isBlank()) {
            return false;
        }

        TokenExpiration tokenExpiration = tokenRepository.findByToken(token);

        // Token inexistant en base
        if (tokenExpiration == null) {
            return false;
        }

        // Token expiré (expiration < now)
        if (tokenExpiration.isExpired()) {
            return false;
        }

        return true;
    }

    /**
     * Génère un nouveau token avec la durée d'expiration par défaut (8 heures).
     */
    public TokenExpiration generateToken() throws SQLException {
        final int DUREE_EXPIRATION_HEURES = 8;
        return tokenRepository.generateToken(DUREE_EXPIRATION_HEURES);
    }

    /**
     * Génère un nouveau token avec une durée d'expiration en heures.
     */
    public TokenExpiration generateToken(int expirationHours) throws SQLException {
        return tokenRepository.generateToken(expirationHours);
    }

    /**
     * Génère un nouveau token avec une durée d'expiration en minutes.
     */
    public TokenExpiration generateTokenMinutes(int expirationMinutes) throws SQLException {
        return tokenRepository.generateTokenMinutes(expirationMinutes);
    }

    /**
     * Liste tous les tokens.
     */
    public List<TokenExpiration> listTokens() throws SQLException {
        return tokenRepository.findAll();
    }

    /**
     * Supprime les tokens expirés.
     */
    public int cleanExpiredTokens() throws SQLException {
        return tokenRepository.deleteExpired();
    }
}
