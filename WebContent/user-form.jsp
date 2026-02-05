<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Test Sprint 8BIS</title>
</head>
<body>
    <h1>${titre}</h1>
    
    <form action="save-user" method="POST">
        <h3>Informations utilisateur</h3>
        
        Nom: <br>
        <input type="text" name="name" value="Jean Dupont"><br><br>
        
        Âge: <br>
        <input type="number" name="age" value="30"><br><br>
        
        Email: <br>
        <input type="email" name="email" value="jean@example.com"><br><br>
        
        Actif: 
        <input type="checkbox" name="active" checked><br><br>
        
        <input type="submit" value="Enregistrer User (Data Binding)">
    </form>
    
    <hr>
    
    <h3>Test avec paramètres mixtes</h3>
    <form action="register" method="POST">
        <input type="text" name="name" value="Marie"><br>
        <input type="number" name="age" value="25"><br>
        <input type="email" name="email" value="marie@example.com"><br>
        <input type="password" name="password" value="secret123"><br>
        <input type="checkbox" name="newsletter" checked> Newsletter<br>
        <input type="submit" value="S'inscrire">
    </form>
</body>
</html>