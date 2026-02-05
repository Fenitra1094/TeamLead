<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Formulaire Test</title>
</head>
<body>
    <h1>${titre}</h1>
    
    <h2>1. Formulaire POST vers /form</h2>
    <form action="form" method="POST">
        ID: <input type="text" name="id"><br>
        Nom: <input type="text" name="nom"><br>
        <input type="submit" value="Sauvegarder (POST)">
    </form>
    
    <hr>
    
    <h2>2. Liens GET vers /user/{id}</h2>
    <a href="user/1">User 1</a><br>
    <a href="user/42">User 42</a><br>
    <a href="user/100">User 100</a>
    
    <hr>
    
    <h2>3. Recherche GET vers /search</h2>
    <form action="search" method="GET">
        Recherche (q): <input type="text" name="q"><br>
        Page: <input type="number" name="page" value="1"><br>
        <input type="submit" value="Rechercher (GET)">
    </form>
    
    <hr>
</body>
</html>