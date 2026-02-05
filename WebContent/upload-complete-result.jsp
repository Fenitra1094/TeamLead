<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    String projet = (String) request.getAttribute("projet");
    String description = (String) request.getAttribute("description");
    UploadedFile rapport = (UploadedFile) request.getAttribute("rapport");
    UploadedFile[] images = (UploadedFile[]) request.getAttribute("images");
    UploadedFile autres = (UploadedFile) request.getAttribute("autres");
%>
<html>
<head><title>Résultat Upload Complet</title></head>
<body>
    <h1>✅ Upload Complet Réussi</h1>
    
    <h2>Projet :</h2>
    <p><strong>Nom :</strong> <%= projet %></p>
    <p><strong>Description :</strong> <%= description %></p>
    
    <h2>Rapport :</h2>
    <% if (rapport != null && !rapport.isEmpty()) { %>
    <p><strong>Fichier :</strong> <%= rapport.getFileName() %> (<%= rapport.getSize() %> bytes)</p>
    <% } else { %>
    <p>Aucun rapport uploadé</p>
    <% } %>
    
    <h2>Images :</h2>
    <% if (images != null && images.length > 0) { 
        int imageCount = 0;
        for (UploadedFile img : images) {
            if (img != null && !img.isEmpty()) imageCount++;
        }
    %>
    <p><strong>Nombre d'images :</strong> <%= imageCount %></p>
    <% } else { %>
    <p>Aucune image uploadée</p>
    <% } %>
    
    <h2>Autres fichiers :</h2>
    <% if (autres != null && !autres.isEmpty()) { %>
    <p><strong>Fichier :</strong> <%= autres.getFileName() %> (<%= autres.getSize() %> bytes)</p>
    <% } else { %>
    <p>Aucun autre fichier</p>
    <% } %>
    
    <br>
    <a href="upload/form">← Nouvel upload</a>
</body>
</html>