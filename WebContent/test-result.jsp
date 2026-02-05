<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Map" %>
<html>
<head>
    <title>Résultat Test Sprint 8</title>
</head>
<body>
    <h1>✅ Test Sprint 8 Réussi !</h1>
    
    <h2>Données extraites automatiquement :</h2>
    <ul>
        <li><strong>Nom :</strong> ${nom}</li>
        <li><strong>Âge :</strong> ${age}</li>
        <li><strong>Ville :</strong> ${ville}</li>
    </ul>
    
    <h2>Map complète (Map&lt;String, Object&gt;) :</h2>
    <table border="1" cellpadding="5">
        <tr>
            <th>Clé</th>
            <th>Valeur</th>
            <th>Type Java</th>
        </tr>
        <%
            Map<String, Object> map = (Map<String, Object>) request.getAttribute("mapData");
            if (map != null) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
        %>
        <tr>
            <td><%= entry.getKey() %></td>
            <td><%= entry.getValue() %></td>
            <td><%= (entry.getValue() != null) ? entry.getValue().getClass().getSimpleName() : "null" %></td>
        </tr>
        <%
                }
            }
        %>
    </table>
    
    <hr>
    <h3>Debug Console :</h3>
    <p>Regardez la console de votre serveur pour voir les logs :</p>
    <pre>
=== SPRINT 8 - RÉSULTAT ===
Map complète: {nom=Jean, age=25, ville=Paris, profession=Développeur, newsletter=oui}
Nom extrait: Jean
Âge extrait: 25
Ville extraite: Paris
    </pre>
    
    <br>
    <a href="test-form">← Retour au formulaire</a>
</body>
</html>