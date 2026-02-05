<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Résultat de la mise à jour de session</title>
</head>
<body>
    <h2>Session mise à jour</h2>
    <p>Thème choisi : <strong>${theme}</strong></p>
    <p>Langue choisie : <strong>${lang != null ? lang : "Non spécifiée"}</strong></p>
    <a href="session-test">Retour à l'accueil session</a>
</body>
</html>
