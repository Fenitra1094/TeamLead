<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Map" %>
<%
    Map<String, Object> sessionData = (Map<String, Object>) request.getAttribute("sessionData");
    Integer sessionSize = (Integer) request.getAttribute("sessionSize");
%>
<html>
<head><title>Debug Session</title></head>
<body>
    <h1>🔍 Debug Session</h1>
    
    <p>Nombre d'attributs : ${sessionSize}</p>
    
    <% if (sessionData != null && !sessionData.isEmpty()) { %>
    <table border="1">
        <tr><th>Clé</th><th>Valeur</th><th>Type</th></tr>
        <% for (Map.Entry<String, Object> entry : sessionData.entrySet()) { %>
        <tr>
            <td><%= entry.getKey() %></td>
            <td><%= entry.getValue() %></td>
            <td><%= entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null" %></td>
        </tr>
        <% } %>
    </table>
    <% } else { %>
    <p>Session vide</p>
    <% } %>
    
    <br>
    <a href="../session-test">← Retour</a>
</body>
</html>