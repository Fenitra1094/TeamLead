package com.app.controllers;

import com.framework.annotation.*;
import com.framework.model.*;
import com.app.models.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random; 
import java.util.Arrays; 

@Controller
public class UserController {

    @HandleUrl("/nom")
    public void afficherAccueil() {
        System.out.println("RAKOTO");
    }

    @HandleUrl("/prenom")
    public void afficherProduits() {
        System.out.println("Jean");
    }

    @HandleUrl("/email")
    public void afficherContact() {
        System.out.println("jean@gmail.com");
    }

    @HandleUrl("/hello")
    public String sayHello()
    {
        return "Hello toi!!";
    }

    @HandleUrl("/test")
    public ModelView test()
    {
        ModelView view = new ModelView();
        view.setView("test.jsp");
        view.addAttribute("nom", "RAKOTO");
        view.addAttribute("prenom", "JEAN");
        view.addAttribute("email", "jeanrakoto@gmail.com");
        return view;
    }

    @HandleUrl("/testform")
    public ModelView testform()
    {
        ModelView view = new ModelView();
        view.setView("testform.jsp");
        return view;
    }

    @HandleUrl("/user/{id}")
    public String get(Integer id)
    {
        return "Méthode get() appelée, id = " + id;
    }

    @HandleUrl("/sprint6/{id}")  // localhost:8080/TestApp/sprint6/12?nom=Ericka&age=17&poids=55.23
    public String sprint6Test(String nom, int age, double poids) {
        return "nom = " + nom + ", age = " + age + ", poids = " + poids;
    }

    @HandleUrl("/etudiant/{id}") //http://localhost:8080/TestApp/etudiant/25?var2=hello
    public String get(@RequestParam("id") int var1, String var2, int id) {
        return "var1 = " + var1 + ", var2 = " + var2 + ", id = " + id;
    }

    @HandleUrl("/test1")
    public String test1(@RequestParam("id") int var1, String var2, int id) {
        return String.format("var1=%d, var2=%s, id=%d", var1, var2, id);
    }
    @HandleUrl("/test2")
    public String test2(@RequestParam("user_id") int userId, 
                    @RequestParam("name") String username) {
        return String.format("userId=%d, username=%s", userId, username);
    }
    @HandleUrl("/test3")
    public String test3(@RequestParam("required_param") String required,
                    String optional) {
        return String.format("required=%s, optional=%s", required, optional);
    }
    @HandleUrl("/test4")
    public String test4(@RequestParam("number") int num,
                    @RequestParam("price") double price,
                    @RequestParam("active") boolean isActive) {
        return String.format("num=%d, price=%.2f, active=%b", num, price, isActive);
    }

    // GET: Afficher le formulaire
    @HandleGet("/form")
    public ModelView form() {
        ModelView model = new ModelView();
        model.setView("form.jsp");
        model.addAttribute("titre", "Formulaire d'inscription");
        return model;
    }
    
    // POST: Traiter le formulaire
    @HandlePost("/form")
    public ModelView saveUser(String id, String nom) {
        ModelView model = new ModelView();
        model.setView("result.jsp");
        model.addAttribute("message", "Utilisateur sauvegardé avec succès!");
        model.addAttribute("nom", nom);
        model.addAttribute("id", id);
        return model;
    }
    
    // Exemple avec paramètre d'URL
    @HandleGet("/user/{id}")
    public ModelView getUser(int id) {
        ModelView model = new ModelView();
        model.setView("../user-detail.jsp");
        model.addAttribute("id", id);
        return model;
    }
    
    // Exemple avec @RequestParam
    @HandleGet("/search")
    public ModelView search(@RequestParam("q") String query, 
                        @RequestParam("page") int pageNumber) {
        ModelView model = new ModelView();
        model.setView("search-results.jsp");
        model.addAttribute("query", query);
        model.addAttribute("page", pageNumber);
        return model;
    }

    // Méthode GET pour afficher le formulaire
    @HandleGet("/test-form")
    public ModelView showForm() {
        ModelView mv = new ModelView();
        mv.setView("test-form.jsp");
        mv.addAttribute("titre", "Test Sprint 8 - Map<String, Object>");
        return mv;
    }
    
    // Méthode POST pour recevoir les données
    @HandlePost("/test-map")
    public ModelView testMap(Map<String, Object> data, String nom, int age, String ville) {
        ModelView mv = new ModelView();
        mv.setView("test-result.jsp");
        
        // Ajouter toutes les données à la vue
        mv.addAttribute("mapData", data);
        mv.addAttribute("nom", nom);
        mv.addAttribute("age", age);
        mv.addAttribute("ville", ville);
        
        return mv;
    }

    // Sprint8bis
    // GET: Afficher le formulaire
    @HandleGet("/user-form")
    public ModelView showUserForm() {
        ModelView mv = new ModelView();
        mv.setView("user-form.jsp");
        mv.addAttribute("titre", "Formulaire User - Sprint 8BIS");
        return mv;
    }

    // POST: Recevoir l'objet User automatiquement
    @HandlePost("/save-user")
    public ModelView saveUser(User user) {
        ModelView mv = new ModelView();
        mv.setView("user-result.jsp");
        
        // Passer l'objet à la vue
        mv.addAttribute("user", user);
        mv.addAttribute("message", "Utilisateur enregistré avec succès!");
        
        return mv;
    }

    // Test avec plusieurs paramètres
    @HandlePost("/register")
    public ModelView register(User user, String password, boolean newsletter) {
        ModelView mv = new ModelView();
        mv.setView("register-result.jsp");
        mv.addAttribute("user", user);
        mv.addAttribute("newsletter", newsletter);
        
        return mv;
    }

    // Tester les tableaux
    @HandleGet("/employees-form")
    public ModelView showEmployeesForm() {
        ModelView mv = new ModelView();
        mv.setView("employees-form.jsp");
        mv.addAttribute("titre", "Formulaire Employees - Sprint 8BIS");
        return mv;
    }
    @HandlePost("/save-employees")
    public ModelView saveEmployees(Employee[] employees, Department department, int deptId) {
        if (employees != null) {
            for (int i = 0; i < employees.length; i++) {
                System.out.println("Employee[" + i + "]: " + employees[i]);
            }
        }
        
        ModelView mv = new ModelView();
        mv.setView("employees-result.jsp");
        mv.addAttribute("employees", employees);
        mv.addAttribute("department", department);
        mv.addAttribute("deptId", deptId);
        
        return mv;
    }

    // Sprint 9
    // API 1: Test simple JSON
    @HandleGet("/api/test")
    @JsonResponse
    public Map<String, Object> apiTest() {
        System.out.println("SPRINT 9: /api/test appelé");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API JSON fonctionne !");
        response.put("timestamp", System.currentTimeMillis());
        response.put("framework", "Notre Framework Spring-like");
        response.put("version", "Sprint 9");
        
        return response;
    }
    
    // API 2: Liste d'utilisateurs en JSON
    @HandleGet("/api/users")
    @JsonResponse
    public List<Map<String, Object>> apiUsers() {
        System.out.println("SPRINT 9: /api/users appelé");
        
        List<Map<String, Object>> users = new ArrayList<>();
        
        // Utilisateur 1
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1);
        user1.put("name", "Jean Rakoto");
        user1.put("email", "jean@example.com");
        user1.put("age", 30);
        user1.put("active", true);
        users.add(user1);
        
        // Utilisateur 2
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2);
        user2.put("name", "Marie Rasoa");
        user2.put("email", "marie@example.com");
        user2.put("age", 25);
        user2.put("active", true);
        users.add(user2);
        
        // Utilisateur 3
        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", 3);
        user3.put("name", "Paul Randria");
        user3.put("email", "paul@example.com");
        user3.put("age", 35);
        user3.put("active", false);
        users.add(user3);
        
        return users;
    }
    
    // API 3: User par ID (avec paramètre d'URL)
    @HandleGet("/api/users/{id}")
    @JsonResponse
    public Map<String, Object> apiUserById(@RequestParam("id") int id) {
        System.out.println("SPRINT 9: /api/users/" + id + " appelé");
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", "Utilisateur " + id);
        user.put("email", "user" + id + "@example.com");
        user.put("age", 20 + id);
        user.put("createdAt", "2024-01-15");
        user.put("roles", new String[]{"USER", "MEMBER"});
        
        return user;
    }
    
    // API 4: Recherche avec paramètres GET
    @HandleGet("/api/search")
    @JsonResponse
    public Map<String, Object> apiSearch(@RequestParam("q") String query, 
                                        @RequestParam(value = "page", required = false) Integer page) {
        System.out.println("SPRINT 9: /api/search?q=" + query + "&page=" + page);
        
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("page", page != null ? page : 1);
        result.put("totalResults", 42);
        result.put("results", Arrays.asList(
            "Résultat 1 pour: " + query,
            "Résultat 2 pour: " + query,
            "Résultat 3 pour: " + query
        ));
        
        return result;
    }
    
    // API 5: Création d'utilisateur (POST avec données JSON attendues)
    @HandleGet("/api/users/create")
    @JsonResponse
    public Map<String, Object> apiCreateUser(Map<String, Object> data) {
        System.out.println("SPRINT 9: /api/users/create appelé");
        System.out.println("Données reçues: " + data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "created");
        response.put("id", 100 + new Random().nextInt(900));
        response.put("message", "Utilisateur créé avec succès");
        response.put("data", data);
        response.put("timestamp", new Date());
        
        return response;
    }
    
    // API 6: Test avec objet Java (combinaison Sprint 8bis + 9)
    @HandleGet("/api/employee")
    @JsonResponse
    public Employee apiEmployee() {
        System.out.println("SPRINT 9: /api/employee appelé");
        
        Employee emp = new Employee();
        emp.setName("John API");
        emp.setPosition("Développeur");
        emp.setSalary(50000.0);
        
        return emp;
    }
    
    // API 7: Liste d'objets Java
    @HandleGet("/api/employees")
    @JsonResponse
    public List<Employee> apiEmployees() {
        System.out.println("SPRINT 9: /api/employees appelé");
        
        List<Employee> employees = new ArrayList<>();
        
        Employee emp1 = new Employee();
        emp1.setName("Alice");
        emp1.setPosition("Manager");
        emp1.setSalary(75000.0);
        employees.add(emp1);
        
        Employee emp2 = new Employee();
        emp2.setName("Bob");
        emp2.setPosition("Développeur");
        emp2.setSalary(60000.0);
        employees.add(emp2);
        
        Employee emp3 = new Employee();
        emp3.setName("Charlie");
        emp3.setPosition("Designer");
        emp3.setSalary(55000.0);
        employees.add(emp3);
        
        return employees;
    }
    
    // API 8: Test d'erreur
    @HandleGet("/api/error")
    @JsonResponse
    public Map<String, Object> apiError() {
        System.out.println("SPRINT 9: /api/error appelé");
        
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("code", 500);
        error.put("message", "Erreur simulée pour le test");
        error.put("details", "Ceci est une erreur de test pour vérifier le format JSON");
        
        return error;
    }
    
    // API 9: Données complexes (mixte)
    @HandleGet("/api/complex")
    @JsonResponse
    public Map<String, Object> apiComplex() {
        System.out.println("SPRINT 9: /api/complex appelé");
        
        Map<String, Object> response = new HashMap<>();
        
        // Types simples
        response.put("string", "Hello JSON");
        response.put("number", 123.45);
        response.put("boolean", true);
        response.put("nullValue", null);
        
        // Tableau
        response.put("array", new int[]{1, 2, 3, 4, 5});
        
        // Liste
        List<String> fruits = Arrays.asList("Pomme", "Banane", "Orange");
        response.put("fruits", fruits);
        
        // Map imbriquée
        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "nestedValue");
        nested.put("count", 99);
        response.put("nested", nested);
        
        // Objet Java
        User user = new User();
        user.setName("Test User");
        user.setAge(28);
        user.setEmail("test@json.com");
        response.put("userObject", user);
        
        return response;
    }
}