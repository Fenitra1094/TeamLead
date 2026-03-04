package com.cousin.config;

import com.cousin.service.TokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Filtre de sécurité pour protéger les endpoints API (/api/*).
 * 
 * Processus :
 * 1. Lire le header "Authorization" ou "X-API-TOKEN"
 * 2. Vérifier : token existe ? token présent en base ? expiration > now ?
 * 
 * Résultats :
 * - pas de token       → accès refusé (401)
 * - token inexistant   → accès refusé (401)
 * - token expiré       → accès refusé (401)
 * - token valide       → accès autorisé
 */
public class ApiTokenFilter implements Filter {

    private final TokenService tokenService = new TokenService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à initialiser
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Lire le header Authorization ou X-API-TOKEN
        String token = extractToken(httpRequest);

        // 2. Vérifier le token
        if (token == null || token.isBlank()) {
            sendUnauthorized(httpResponse, "Acces refuse : aucun token fourni");
            return;
        }

        try {
            if (!tokenService.isTokenValid(token)) {
                sendUnauthorized(httpResponse, "Acces refuse : token invalide ou expire");
                return;
            }
        } catch (SQLException e) {
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.setContentType("application/json; charset=UTF-8");
            try (PrintWriter writer = httpResponse.getWriter()) {
                writer.write("{\"error\": \"Erreur interne lors de la verification du token\"}");
            }
            return;
        }

        // 3. Token valide → accès autorisé, on continue la chaîne
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Rien à nettoyer
    }

    /**
     * Extrait le token depuis les headers HTTP ou le query parameter.
     * Priorité : 1) Header X-API-TOKEN, 2) Authorization Bearer, 3) Query param ?token=
     */
    private String extractToken(HttpServletRequest request) {
        // Priorité au header X-API-TOKEN
        String token = request.getHeader("X-API-TOKEN");
        if (token != null && !token.isBlank()) {
            return token.trim();
        }

        // Sinon, chercher dans Authorization (Bearer token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            return authHeader.trim();
        }

        // Sinon, chercher dans le query parameter ?token=
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken.trim();
        }

        return null;
    }

    /**
     * Envoie une réponse 401 Unauthorized en JSON.
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"error\": \"" + message + "\"}");
        }
    }
}
