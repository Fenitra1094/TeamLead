<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile, com.app.models.User" %>
<%
    User user = (User) request.getAttribute("user");
    UploadedFile document = (UploadedFile) request.getAttribute("document");
%>
<html>
<head><title>Résultat Test 4</title></head>
<body>
    <h1>Test 4: Upload avec User Object</h1>
    
    <% if (user != null) { %>
    <p>User: <%= user.getName() %></p>
    <p>Email: <%= user.getEmail() %></p>
    <% } %>
    
    <% if (document != null && !document.isEmpty()) { %>
    <p>Document: <%= document.getFileName() %> (<%= document.getSize() %> bytes)</p>
    <% } else { %>
    <p>Aucun document</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>