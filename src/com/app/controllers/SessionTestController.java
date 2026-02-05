package com.app.controllers;

import com.framework.annotation.*;
import com.framework.model.ModelView;
import com.framework.model.SessionModelView;
import java.util.*;
import com.app.models.*;

@Controller
public class SessionTestController {
    
    // Page d'accueil des tests session
    @HandleGet("/session-test")
    public ModelView sessionTestHome(@SessionParam(value = "username", required = false) String username) {
        ModelView mv = new ModelView();
        mv.setView("session-home.jsp");
        mv.addAttribute("username", username != null ? username : "Non connecté");
        return mv;
    }

        // Afficher le formulaire de mise à jour de la session
        @HandleGet("/update-form")
        public ModelView showUpdateForm() {
            ModelView mv = new ModelView();
            mv.setView("session-update-form.jsp");
            return mv;
        }

    // Afficher le formulaire de login
    @HandleGet("/login-form")
    public ModelView showLoginForm() {
        ModelView mv = new ModelView();
        mv.setView("session-login-form.jsp");
        return mv;
    }

    // Afficher le formulaire d'ajout au panier
    @HandleGet("/add-to-cart-form")
    public ModelView showAddToCartForm() {
        ModelView mv = new ModelView();
        mv.setView("session-add-to-cart-form.jsp");
        return mv;
    }
    
    // 1. Connexion - mettre en session
    @HandlePost("/login")
    public SessionModelView login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        
        System.out.println("📝 SPRINT 11: Login - username: " + username);
        
        SessionModelView mv = new SessionModelView("session-login-result.jsp");
        mv.setView("session-login-result.jsp");
        mv.addAttribute("username", username);
        
        // Ajouter des attributs à la session
        mv.addSessionAttribute("username", username);
        mv.addSessionAttribute("loggedIn", true);
        mv.addSessionAttribute("loginTime", new Date());
        mv.addSessionAttribute("userRole", "USER");
        
        return mv;
    }
    
    // 2. Page profil - lire la session
    @HandleGet("/profile")
    public ModelView profile(
            @SessionParam("username") String username,
            @SessionParam("loginTime") Date loginTime,
            @SessionParam(value = "userRole", required = false) String role) {
        
        System.out.println("📝 SPRINT 11: Profile - username: " + username);
        
        ModelView mv = new ModelView();
        mv.setView("session-profile.jsp");
        mv.addAttribute("username", username);
        mv.addAttribute("loginTime", loginTime);
        mv.addAttribute("role", role != null ? role : "GUEST");
        mv.addAttribute("sessionId", "Session ID caché par framework");
        
        return mv;
    }
    
    // 3. Panier - session avec objets complexes
    @HandlePost("/add-to-cart")
    public SessionModelView addToCart(
            @SessionParam(value = "cart", required = false) Map<String, Integer> cart,
            @RequestParam("item") String item,
            @RequestParam("quantity") int quantity) {
        
        // Initialiser le panier si vide
        if (cart == null) {
            cart = new HashMap<>();
        }
        
        // Ajouter l'article
        cart.put(item, cart.getOrDefault(item, 0) + quantity);
        
        System.out.println("📝 SPRINT 11: Cart updated - " + cart.size() + " items");
        
        SessionModelView mv = new SessionModelView("session-cart.jsp");
        mv.setView("session-cart.jsp");
        mv.addAttribute("item", item);
        mv.addAttribute("quantity", quantity);
        mv.addAttribute("cartSize", cart.size());
        
        // Sauvegarder le panier en session
        mv.addSessionAttribute("cart", cart);
        
        return mv;
    }
    
    // 4. Voir panier
    @HandleGet("/view-cart")
    public ModelView viewCart(@SessionParam(value = "cart", required = false) Map<String, Integer> cart) {
        ModelView mv = new ModelView();
        mv.setView("session-view-cart.jsp");
        
        if (cart != null && !cart.isEmpty()) {
            mv.addAttribute("cart", cart);
            mv.addAttribute("totalItems", cart.values().stream().mapToInt(Integer::intValue).sum());
        } else {
            mv.addAttribute("message", "Panier vide");
        }
        
        return mv;
    }
    
    // 5. Accéder à toute la session
    @HandleGet("/debug")
    public ModelView sessionDebug(@SessionAttributes Map<String, Object> session) {
        System.out.println("📝 SPRINT 11: Debug session - " + session.size() + " attributs");
        
        ModelView mv = new ModelView();
        mv.setView("session-debug.jsp");
        mv.addAttribute("sessionData", session);
        mv.addAttribute("sessionSize", session.size());
        
        return mv;
    }
    
    // 6. Modifier session
    @HandlePost("/update")
    public SessionModelView updateSession(
            @RequestParam("theme") String theme,
            @RequestParam(value = "lang", required = false) String lang) {
        
        SessionModelView mv = new SessionModelView("session-update-result.jsp");
        mv.setView("session-update-result.jsp");
        mv.addAttribute("theme", theme);
        mv.addAttribute("lang", lang);
        
        // Mettre à jour la session
        mv.addSessionAttribute("theme", theme);
        if (lang != null) {
            mv.addSessionAttribute("language", lang);
        }
        
        return mv;
    }
    
    // 7. Supprimer de la session
    @HandleGet("/remove/{key}")
    public SessionModelView removeFromSession(@RequestParam("key") String key) {
        System.out.println("📝 SPRINT 11: Suppression attribut session: " + key);
        
        SessionModelView mv = new SessionModelView("session-remove-result.jsp");
        mv.addAttribute("removedKey", key);
        
        // Pour supprimer, mettre null (sera retiré par le framework)
        mv.addSessionAttribute(key, null);
        
        return mv;
    }
    
    // 8. Déconnexion - vider la session
    @HandleGet("/logout")
    public SessionModelView logout(@SessionAttributes Map<String, Object> session) {
        System.out.println("📝 SPRINT 11: Logout - suppression de " + session.size() + " attributs");
        
        SessionModelView mv = new SessionModelView("session-logout.jsp");
        mv.setView("session-logout.jsp");
        mv.addAttribute("clearedCount", session.size());
        
        // Vider tous les attributs
        for (String key : session.keySet()) {
            mv.addSessionAttribute(key, null);
        }
        
        return mv;
    }
    
    // 9. Compteur de visites
    @HandleGet("/counter")
    public SessionModelView visitCounter(@SessionParam(value = "visitCount", required = false) Integer count) {
        int newCount = (count != null ? count : 0) + 1;
        
        SessionModelView mv = new SessionModelView("session-counter.jsp");
        mv.addAttribute("count", newCount);
        mv.addSessionAttribute("visitCount", newCount);
        mv.addSessionAttribute("lastVisit", new Date());
        
        return mv;
    }
    
    // 10. Test avec objet complexe en session
    @HandlePost("/save-user")
    public SessionModelView saveUserInSession(User user) {
        SessionModelView mv = new SessionModelView("session-user-saved.jsp");
        mv.addAttribute("user", user);
        mv.addSessionAttribute("currentUser", user);
        
        return mv;
    }
    
    // 11. Récupérer objet complexe
    @HandleGet("/get-user")
    public ModelView getUserFromSession(@SessionParam("currentUser") User user) {
        ModelView mv = new ModelView();
        mv.setView("session-user-display.jsp");
        mv.addAttribute("user", user);
        
        return mv;
    }
}