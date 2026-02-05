<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile, com.app.models.User, java.util.Map" %>
<%
    String titre = (String) request.getAttribute("titre");
    User user = (User) request.getAttribute("user");
    UploadedFile cv = (UploadedFile) request.getAttribute("cv");
    UploadedFile[] certificats = (UploadedFile[]) request.getAttribute("certificats");
    Map<String, Object> extra = (Map<String, Object>) request.getAttribute("extra");
%>
<html>
<head><title>Résultat Test 6</title></head>
<body>
    <h1>Test 6: Upload Complet</h1>
    
    <p>Titre: <%= titre %></p>
    
    <% if (user != null) { %>
    <p>User: <%= user.getName() %>, Email: <%= user.getEmail() %></p>
    <% } %>
    
    <% if (cv != null && !cv.isEmpty()) { %>
    <p>CV: <%= cv.getFileName() %></p>
    <% } %>
    
    <% if (certificats != null && certificats.length > 0) { 
        int count = 0;
        for (UploadedFile cert : certificats) {
            if (cert != null && !cert.isEmpty()) count++;
        } %>
    <p>Certificats: <%= count %> fichier(s)</p>
    <% } %>
    
    <% if (extra != null && !extra.isEmpty()) { %>
    <p>Extra data: <%= extra.size() %> élément(s)</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>