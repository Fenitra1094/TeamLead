<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Test Sessions</title></head>
<body>
    <h1>Test Sessions - Sprint 11</h1>
    
    <p>Status: ${username}</p>
    
    <h2>Tests :</h2>
    <ul>
        <li><a href="login-form">Connexion (formulaire)</a></li>
        <li><a href="profile">Profil</a></li>
        <li><a href="counter">Compteur visites</a></li>
        <li><a href="debug">Debug Session</a></li>
        <li><a href="view-cart">Panier</a></li>
        <li><a href="logout">Déconnexion</a></li>
    </ul>
    
    <h3>Tests Rapides :</h3>
    <ul>
        <li><a href="counter">🎯 Compteur</a></li>
        <li><a href="remove/theme">🗑️ Supprimer 'theme'</a></li>
        <li><a href="get-user">👤 Récupérer User</a></li>
    </ul>
</body>
</html>