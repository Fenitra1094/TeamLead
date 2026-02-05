<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.framework.model.UploadedFile" %>
<%
    String username = (String) request.getAttribute("username");
    Integer age = (Integer) request.getAttribute("age");
    UploadedFile avatar = (UploadedFile) request.getAttribute("avatar");
%>
<html>
<head><title>Résultat Test 2</title></head>
<body>
    <h1>Test 2: Upload avec Paramètres</h1>
    
    <p>Username: <%= username %></p>
    <p>Age: <%= age %></p>
    
    <% if (avatar != null && !avatar.isEmpty()) { %>
    <p>Avatar: <%= avatar.getFileName() %> (<%= avatar.getSize() %> bytes)</p>
    <% } else { %>
    <p>Aucun avatar</p>
    <% } %>
    
    <br>
    <a href="upload-test">← Retour tests</a>
</body>
</html>