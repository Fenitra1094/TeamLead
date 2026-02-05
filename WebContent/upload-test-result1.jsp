<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    UploadedFile fichier = (UploadedFile) request.getAttribute("fichier");
%>
<html>
<head><title>Résultat Test 1</title></head>
<body>
    <h1>Test 1: Upload Simple</h1>
    
    <% if (fichier != null && !fichier.isEmpty()) { %>
    <p>Fichier reçu: <%= fichier.getFileName() %></p>
    <p>Taille: <%= fichier.getSize() %> bytes</p>
    <p>Type: <%= fichier.getContentType() %></p>
    <% } else { %>
    <p>Aucun fichier reçu</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>