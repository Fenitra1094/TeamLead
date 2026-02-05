<!DOCTYPE html>
<html>
<head>
    <title>Tests Framework</title>
</head>
<body>
    <h1>Tests Framework GET/POST</h1>
    
    <h2>1. Test GET /form</h2>
    <a href="form" target="_blank">GET /form</a>
    
    <h2>2. Test POST /form</h2>
    <form action="form" method="POST" target="_blank">
        ID: <input type="text" name="id" value="100"><br>
        Nom: <input type="text" name="nom" value="Jean Dupont"><br>
        <input type="submit" value="POST /form">
    </form>
    
    <h2>3. Test GET /user/{id}</h2>
    <a href="user/1" target="_blank">GET /user/1</a><br>
    <a href="user/42" target="_blank">GET /user/42</a><br>
    <a href="user/999" target="_blank">GET /user/999</a>
    
    <h2>4. Test GET /search avec @RequestParam</h2>
    <a href="search?q=java&page=1" target="_blank">search?q=java&page=1</a><br>
    <a href="search?q=spring&page=3" target="_blank">search?q=spring&page=3</a><br>
    
    <h2>5. Test erreur 405 (méthode incorrecte)</h2>
    <a href="form" onclick="alert('Devrait donner erreur 405 car POST requis')">GET sur route POST</a>
</body>
</html>