<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Mettre à jour la session</title>
</head>
<body>
    <h2>Mettre à jour les préférences de session</h2>
    <form action="update" method="post">
        <label for="theme">Thème :</label>
        <input type="text" id="theme" name="theme" required><br><br>
        <label for="lang">Langue (optionnel) :</label>
        <input type="text" id="lang" name="lang"><br><br>
        <button type="submit">Mettre à jour</button>
    </form>
    <br>
    <a href="session-test">Retour à l'accueil session</a>
</body>
</html>
