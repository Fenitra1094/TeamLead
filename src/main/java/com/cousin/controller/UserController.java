package com.cousin.controller;

import com.framework.annotation.Controller;
import com.framework.model.ModelView;
import com.framework.annotation.GetMapping;
import com.framework.annotation.PostMapping;
import com.framework.annotation.Param;
import com.framework.annotation.RequestMapping;

@Controller
@RequestMapping("/user") // Préfixe pour toutes les routes user
public class UserController {

    @GetMapping("/list")
    public String list() {
        return "Voici la liste des utilisateurs (GET)";
    }

    @PostMapping("/add")
    public String addUser() {
        return "Page d'ajout d'utilisateur (POST)";
    }

    @GetMapping("/profile")
    public ModelView userProfile() {
        ModelView mv = new ModelView("/WEB-INF/views/user/profil.jsp");
        mv.addAttribute("username", "John Doe");
        mv.addAttribute("email", "john@example.com");
        return mv;
    }
    
    @GetMapping("/{id}")
    public String getUserById(@Param("id") int userId) {
        return "<h2>GET /user/" + userId + "</h2>" +
               "<p>Utilisateur ID: " + userId + "</p>";
    }
}