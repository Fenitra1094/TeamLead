<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Login Réussi</title></head>
<body>
    <h1>✅ Login Réussi !</h1>
    
    <p>Bienvenue <strong>${username}</strong> !</p>
    <p>Session créée avec succès.</p>
    
    <h2>Actions :</h2>
    <ul>
        <li><a href="profile">Voir mon profil</a></li>
        <li><a href="debug">Debug session</a></li>
        <li><a href="add-to-cart-form">Ajouter au panier</a></li>
    </ul>
    
    <br>
    <a href="../session-test">← Retour accueil</a>
</body>
</html>