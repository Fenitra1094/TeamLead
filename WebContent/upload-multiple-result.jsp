<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    String titre = (String) request.getAttribute("titre");
    UploadedFile[] documents = (UploadedFile[]) request.getAttribute("documents");
    Integer fileCount = (Integer) request.getAttribute("fileCount");
%>
<html>
<head><title>Résultat Upload Multiple</title></head>
<body>
    <h1>✅ Upload Multiple Réussi</h1>
    
    <h2>Informations :</h2>
    <p><strong>Titre :</strong> <%= titre %></p>
    <p><strong>Nombre de fichiers :</strong> <%= fileCount != null ? fileCount : 0 %></p>
    
    <h2>Fichiers uploadés :</h2>
    <% if (documents != null && documents.length > 0) { %>
    <table border="1">
        <tr>
            <th>#</th>
            <th>Nom</th>
            <th>Type</th>
            <th>Taille</th>
        </tr>
        <% for (int i = 0; i < documents.length; i++) { 
            UploadedFile doc = documents[i];
            if (doc != null && !doc.isEmpty()) {
        %>
        <tr>
            <td><%= i+1 %></td>
            <td><%= doc.getFileName() %></td>
            <td><%= doc.getContentType() %></td>
            <td><%= doc.getSize() %> bytes</td>
        </tr>
        <%   }
           } %>
    </table>
    <% } else { %>
    <p>Aucun fichier uploadé</p>
    <% } %>
    
    <br>
    <a href="upload/form">← Nouvel upload</a>
</body>
</html>