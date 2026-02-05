<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.app.models.User" %>
<% 
    User user = (User) request.getAttribute("user");
    Boolean newsletter = (Boolean) request.getAttribute("newsletter");
%>
<html>
<head><title>Inscription</title></head>
<body>
    <h1>Confirmation</h1>
    
    <% if (user != null) { %>
        <p>Merci <strong><%= user.getName() %></strong> !</p>
        
        <h3>Récapitulatif :</h3>
        <ul>
            <li>Âge : <%= user.getAge() %></li>
            <li>Email : <%= user.getEmail() %></li>
            <li>Compte : <%= user.isActive() ? "Actif" : "Inactif" %></li>
            <li>Newsletter : <%= (newsletter != null && newsletter) ? "Oui" : "Non" %></li>
        </ul>
    <% } %>
    
    <br>
    <a href="user-form">Retour</a>
</body>
</html>