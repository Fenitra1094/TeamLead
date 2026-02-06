package com.cousin.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.PostMapping;
import com.framework.annotation.RequestMapping;
import com.cousin.model.Emp;

@Controller
@RequestMapping("/test-simple")
public class SimpleTestController {
    
    @GetMapping("/form")
    public String showForm() {
        return """
            <h1>Test Simple Objet</h1>
            <form method="POST" action="/test/test-simple/save">
                <input type="text" name="nom" value="TestNom"><br>
                <input type="text" name="prenom" value="TestPrenom"><br>
                <input type="number" name="age" value="25"><br>
                <button>Envoyer</button>
            </form>
            """;
    }
    
    @PostMapping("/save")
    public String save(Emp emp) {
        return "<h1>Résultat:</h1>" +
               "<p>Nom: " + emp.getNom() + "</p>" +
               "<p>Prénom: " + emp.getPrenom() + "</p>" +
               "<p>Âge: " + emp.getAge() + "</p>" +
               "<p>Objet complet: " + emp.toString() + "</p>";
    }
}