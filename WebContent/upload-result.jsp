<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    String nom = (String) request.getAttribute("nom");
    String email = (String) request.getAttribute("email");
    UploadedFile fichier = (UploadedFile) request.getAttribute("fichier");
    Boolean hasFile = (Boolean) request.getAttribute("hasFile");
%>
<html>
<head><title>Résultat Upload</title></head>
<body>
    <h1>✅ Upload Réussi</h1>
    
    <h2>Informations :</h2>
    <p><strong>Nom :</strong> <%= nom %></p>
    <p><strong>Email :</strong> <%= email %></p>
    
    <h2>Fichier :</h2>
    <% if (hasFile != null && hasFile && fichier != null) { %>
    <p><strong>Nom du fichier :</strong> <%= fichier.getFileName() %></p>
    <p><strong>Type :</strong> <%= fichier.getContentType() %></p>
    <p><strong>Taille :</strong> <%= fichier.getSize() %> bytes</p>
    <% } else { %>
    <p>Aucun fichier uploadé</p>
    <% } %>
    
    <br>
    <a href="upload/form">← Nouvel upload</a>
</body>
</html>