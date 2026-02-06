package com.cousin.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.PostMapping;
import com.framework.annotation.SessionAttributes;
import com.framework.annotation.SessionParam;
import com.framework.annotation.Param;
import com.framework.model.SessionModelView;
import java.util.Map;

@Controller
public class SessionTestController {

    // Test 1: Stocker des valeurs dans la session
    @PostMapping("/session/login")
    public SessionModelView login(@Param("username") String username, @Param("role") String role) {
        SessionModelView smv = new SessionModelView("/WEB-INF/views/session/dashboard.jsp");
        
        // Ajouter des attributs de session
        smv.addSessionAttribute("username", username);
        smv.addSessionAttribute("role", role);
        smv.addSessionAttribute("loginTime", System.currentTimeMillis());
        
        // Ajouter des attributs de requête
        smv.addAttribute("message", "Connexion réussie !");
        
        return smv;
    }

    // Test 2: Récupérer un attribut spécifique de session avec @SessionParam
    @GetMapping("/session/profile")
    public String showProfile(@SessionParam("username") String username, 
                             @SessionParam(value = "role", required = false) String role) {
        return """
            <h1>Profil Utilisateur</h1>
            <p>Nom: %s</p>
            <p>Rôle: %s</p>
            <a href="/test/session/all">Voir toute la session</a>
            """.formatted(
                username != null ? username : "Non connecté",
                role != null ? role : "Aucun"
            );
    }

    // Test 3: Récupérer toute la session avec @SessionAttributes
    @GetMapping("/session/all")
    public String showAllSession(@SessionAttributes Map<String, Object> sessionData) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Contenu de la Session</h1>");
        sb.append("<ul>");
        
        if (sessionData.isEmpty()) {
            sb.append("<li>Session vide</li>");
        } else {
            sessionData.forEach((key, value) -> {
                sb.append("<li><strong>").append(key).append(":</strong> ")
                  .append(value).append("</li>");
            });
        }
        
        sb.append("</ul>");
        sb.append("<a href='/test/session/form'>Retour au formulaire</a>");
        return sb.toString();
    }

    // Test 4: Formulaire de login
    @GetMapping("/session/form")
    public String showLoginForm() {
        return """
            <h1>Test Session Management</h1>
            <form method="POST" action="/test/session/login">
                <label>Username: <input type="text" name="username" value="john"></label><br>
                <label>Role: <input type="text" name="role" value="admin"></label><br>
                <button>Se connecter</button>
            </form>
            <hr>
            <a href="/test/session/profile">Voir profil</a><br>
            <a href="/test/session/all">Voir toute la session</a>
            """;
    }

    // Test 5: Modifier la session
    @PostMapping("/session/update")
    public SessionModelView updateSession(@SessionParam("username") String currentUser,
                                         @Param("newRole") String newRole) {
        SessionModelView smv = new SessionModelView("/WEB-INF/views/session/updated.jsp");
        
        // Mettre à jour le rôle dans la session
        smv.addSessionAttribute("role", newRole);
        smv.addSessionAttribute("lastUpdate", System.currentTimeMillis());
        
        smv.addAttribute("username", currentUser);
        smv.addAttribute("newRole", newRole);
        
        return smv;
    }
}
