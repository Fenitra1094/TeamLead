<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Test Sprint 8</title>
</head>
<body>
    <h1>${titre}</h1>
    
    <form action="test-map" method="POST">
        <h3>Informations personnelles</h3>
        
        Nom: <br>
        <input type="text" name="nom" value="Jean"><br><br>
        
        Âge: <br>
        <input type="number" name="age" value="25"><br><br>
        
        Ville: <br>
        <input type="text" name="ville" value="Paris"><br><br>
        
        Profession: <br>
        <input type="text" name="profession" value="Développeur"><br><br>
        
        <h3>Options (test checkbox simple)</h3>
        Newsletter: 
        <input type="checkbox" name="newsletter" value="oui" checked> Oui<br><br>
        
        <input type="submit" value="Tester Map<String, Object>">
    </form>
    
    <hr>
    <p>Ce formulaire testera :</p>
    <ul>
        <li>L'injection automatique de Map&lt;String, Object&gt; data</li>
        <li>L'extraction automatique des paramètres (nom, age, ville)</li>
        <li>La redirection vers une page JSP avec les données</li>
    </ul>
</body>
</html>