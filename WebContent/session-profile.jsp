<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Profil</title></head>
<body>
    <h1>👤 Profil Utilisateur</h1>
    
    <p><strong>Username :</strong> ${username}</p>
    <p><strong>Connecté depuis :</strong> ${loginTime}</p>
    <p><strong>Rôle :</strong> ${role}</p>
    <p><strong>Session ID :</strong> ${sessionId}</p>
    
    <br>
    <a href="../session-test">← Retour</a>
</body>
</html>