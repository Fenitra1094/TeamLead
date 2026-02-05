<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile, java.util.Map" %>
<%
    Map<String, Object> data = (Map<String, Object>) request.getAttribute("data");
    UploadedFile fichier = (UploadedFile) request.getAttribute("fichier");
%>
<html>
<head><title>Résultat Test 5</title></head>
<body>
    <h1>Test 5: Upload avec Map</h1>
    
    <h2>Données Map:</h2>
    <% if (data != null) { %>
    <ul>
    <% for (Map.Entry<String, Object> entry : data.entrySet()) { %>
        <li><%= entry.getKey() %>: <%= entry.getValue() %></li>
    <% } %>
    </ul>
    <% } %>
    
    <h2>Fichier:</h2>
    <% if (fichier != null && !fichier.isEmpty()) { %>
    <p><%= fichier.getFileName() %> (<%= fichier.getSize() %> bytes)</p>
    <% } else { %>
    <p>Aucun fichier</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>