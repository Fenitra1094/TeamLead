<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Map" %>
<%
    Map<String, Integer> cart = (Map<String, Integer>) request.getAttribute("cart");
    Integer totalItems = (Integer) request.getAttribute("totalItems");
    String message = (String) request.getAttribute("message");
%>
<html>
<head><title>Panier</title></head>
<body>
    <h1>🛒 Votre Panier</h1>
    
    <% if (message != null) { %>
    <p>${message}</p>
    <% } else if (cart != null && !cart.isEmpty()) { %>
    <p>Total articles : ${totalItems}</p>
    
    <table border="1">
        <tr><th>Article</th><th>Quantité</th></tr>
        <% for (Map.Entry<String, Integer> entry : cart.entrySet()) { %>
        <tr>
            <td><%= entry.getKey() %></td>
            <td><%= entry.getValue() %></td>
        </tr>
        <% } %>
    </table>
    <% } else { %>
    <p>Votre panier est vide</p>
    <% } %>
    
    <br>
    <a href="../session-test">← Retour</a>
</body>
</html>