<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    UploadedFile[] photos = (UploadedFile[]) request.getAttribute("photos");
    Integer photoCount = (Integer) request.getAttribute("photoCount");
%>
<html>
<head><title>Résultat Test 3</title></head>
<body>
    <h1>Test 3: Upload Multiple</h1>
    
    <p>Nombre de photos: <%= photoCount != null ? photoCount : 0 %></p>
    
    <% if (photos != null && photos.length > 0) { %>
    <ul>
    <% for (int i = 0; i < photos.length; i++) { 
        UploadedFile photo = photos[i];
        if (photo != null && !photo.isEmpty()) { %>
        <li>Photo <%= i+1 %>: <%= photo.getFileName() %> (<%= photo.getSize() %> bytes)</li>
    <%   }
       } %>
    </ul>
    <% } else { %>
    <p>Aucune photo</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>