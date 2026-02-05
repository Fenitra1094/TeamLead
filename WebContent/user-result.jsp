<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.app.models.User" %>
<html>
<head>
    <title>Résultat</title>
</head>
<body>
    <h1>✅ Sprint 8BIS Réussi !</h1>
    
    <h2>${message}</h2>
    
    <h3>Données de l'utilisateur :</h3>
    <%
        User user = (User) request.getAttribute("user");
        if (user != null) {
    %>
    <ul>
        <li><strong>Nom :</strong> <%= user.getName() %></li>
        <li><strong>Âge :</strong> <%= user.getAge() %></li>
        <li><strong>Email :</strong> <%= user.getEmail() %></li>
        <li><strong>Actif :</strong> <%= user.isActive() %></li>
    </ul>
    <%
        }
    %>
    
    <br>
    <a href="user-form">← Nouvel utilisateur</a>
</body>
</html>