<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.cousin.model.Vehicule" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vehicule</title>
</head>
<body>
<%
    Object message = request.getAttribute("message");
    Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
    String action = (String) request.getAttribute("action");
    if (action == null || action.isBlank()) {
        action = "insert";
    }
    boolean isEdit = "edit".equalsIgnoreCase(action) && vehicule != null;
    String reference = vehicule != null && vehicule.getReference() != null ? vehicule.getReference() : "";
    String nbPlace = vehicule != null ? String.valueOf(vehicule.getNbPlace()) : "";
    String typeVehicule = vehicule != null && vehicule.getTypeVehicule() != null ? vehicule.getTypeVehicule() : "";
%>

<h2><%= isEdit ? "Modification Vehicule" : "Insertion Vehicule" %></h2>

<% if (message != null) { %>
    <p><strong><%= message %></strong></p>
<% } %>

<form method="post" action="<%= request.getContextPath() %>/vehicule/form?action=<%= action %>">
    <% if (isEdit) { %>
        <input type="hidden" name="id_vehicule" value="<%= vehicule.getIdVehicule() %>">
    <% } %>

    <label>Reference</label><br>
    <input type="text" name="reference" value="<%= reference %>" required><br><br>

    <label>Nombre de places</label><br>
    <input type="number" name="nbPlace" min="1" value="<%= nbPlace %>" required><br><br>

    <label>Type vehicule</label><br>
    <input type="text" name="typeVehicule" value="<%= typeVehicule %>"><br><br>

    <button type="submit">Enregistrer</button>
</form>

<p>
    <a href="<%= request.getContextPath() %>/vehicule/list">Voir la liste</a>
</p>
</body>
</html>
