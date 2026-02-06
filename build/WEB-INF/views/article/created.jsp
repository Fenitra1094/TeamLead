<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Article Créé</title>
    <style>
        body { font-family: Arial; margin: 40px; }
        .article { border: 2px solid #4CAF50; padding: 20px; margin: 20px 0; }
        .success { color: #4CAF50; font-weight: bold; }
    </style>
</head>
<body>
    <h1 class="success">✅ Article Créé avec Succès!</h1>
    
    <div class="article">
        <h2>${title}</h2>
        <p>${content}</p>
        <hr>
        <p><small>Créé le: ${createdAt}</small></p>
    </div>
    
    <a href="/test/test_http_methods.html">Retour aux tests</a>
</body>
</html>