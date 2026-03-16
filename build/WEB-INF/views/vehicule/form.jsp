<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.cousin.model.Vehicule" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vehicule</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/theme-gray.css">
</head>
<body>
<%
    String ctx = request.getContextPath();
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

<nav class="navbar">
    <div class="layout">
        <span class="brand">BackOffice TeamLead</span>
        <a class="nav-link" href="<%= ctx %>/assignation/form">Assignation</a>
        <a class="nav-link" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link active" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout stack">
<h1 class="page-title"><%= isEdit ? "Modification Vehicule" : "Insertion Vehicule" %></h1>

<% if (message != null) { %>
    <div class="alert success"><%= message %></div>
<% } %>

<section class="card">
    <form method="post" action="<%= request.getContextPath() %>/vehicule/form?action=<%= action %>" class="stack">
        <% if (isEdit) { %>
            <input type="hidden" name="id_vehicule" value="<%= vehicule.getIdVehicule() %>">
        <% } %>

        <div class="form-row">
            <div>
                <label>Reference</label>
                <input type="text" name="reference" value="<%= reference %>" required>
            </div>

            <div>
                <label>Nombre de places</label>
                <input type="number" name="nbPlace" min="1" value="<%= nbPlace %>" required>
            </div>

            <div>
                <label>Type vehicule</label>
                <input type="text" name="typeVehicule" value="<%= typeVehicule %>">
            </div>
        </div>

        <div>
            <button type="submit">Enregistrer</button>
        </div>
    </form>

    <p>
        <a class="nav-link active" href="<%= request.getContextPath() %>/vehicule/list">Voir la liste</a>
    </p>
</section>
</main>
</body>
</html>
