<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Login</title></head>
<body>
    <h1>Formulaire de Login</h1>
    
    <form action="login" method="POST">
        Username: <input type="text" name="username" value="testuser"><br>
        Password: <input type="password" name="password" value="123"><br>
        <input type="submit" value="Se connecter">
    </form>
    
    <br>
    <a href="../session-test">← Retour</a>
</body>
</html>