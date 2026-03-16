<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cousin.model.Vehicule" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste Vehicules</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/theme-gray.css">
</head>
<body>
<%
    String ctx = request.getContextPath();
%>

<nav class="navbar">
    <div class="layout">
        <span class="brand">BackOffice TeamLead</span>
        <a class="nav-link" href="<%= ctx %>/assignation/form">Assignation</a>
        <a class="nav-link" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link active" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout stack">
<h1 class="page-title">Liste des vehicules</h1>

<%
    Object message = request.getAttribute("message");
    if (message != null) {
%>
    <div class="alert success"><%= message %></div>
<%
    }
%>

<p>
    <a class="nav-link active" href="<%= request.getContextPath() %>/vehicule/form">Ajouter un vehicule</a>
</p>

<section class="card">
<table>
    <thead>
    <tr>
        <th>Id</th>
        <th>Reference</th>
        <th>Nb place</th>
        <th>Type vehicule</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <%
        List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
        if (vehicules != null && !vehicules.isEmpty()) {
            for (Vehicule v : vehicules) {
    %>
    <tr>
        <td><%= v.getIdVehicule() %></td>
        <td><%= v.getReference() %></td>
        <td><%= v.getNbPlace() %></td>
        <td><%= v.getTypeVehicule() %></td>
        <td>
            <a href="<%= request.getContextPath() %>/vehicule/list?action=edit&id_vehicule=<%= v.getIdVehicule() %>">Modifier</a>
            |
            <a href="<%= request.getContextPath() %>/vehicule/list?action=delete&id_vehicule=<%= v.getIdVehicule() %>"
               onclick="return confirm('Supprimer ce vehicule ?');">Supprimer</a>
        </td>
    </tr>
    <%
            }
        } else {
    %>
    <tr>
        <td colspan="5">Aucun vehicule</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
</section>
</main>
</body>
</html>
