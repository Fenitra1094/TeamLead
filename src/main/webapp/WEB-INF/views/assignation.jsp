<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Assignation</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/theme-gray.css">
</head>
<body>
<%
    String ctx = request.getContextPath();
    Integer tempsAttenteAttr = (Integer) request.getAttribute("tempsAttente");
    if (tempsAttenteAttr == null) {
        tempsAttenteAttr = 30;
    }
    Object error = request.getAttribute("error");
%>

<nav class="navbar">
    <div class="layout">
        <span class="brand">BackOffice TeamLead</span>
        <a class="nav-link active" href="<%= ctx %>/assignation/form">Assignation</a>
        <a class="nav-link" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout">
    <h1 class="page-title">Assignation</h1>

    <% if (error != null) { %>
        <div class="alert error"><%= error %></div>
    <% } %>

    <section class="card">
        <h2>Saisie</h2>
        <form method="get" action="<%= ctx %>/assignation/assigner" class="stack">
            <div class="form-row">
                <div>
                    <label for="date">Date</label>
                    <input type="date" id="date" name="date" required />
                </div>
                <div>
                    <label for="tempsAttente">Temps d'attente (minutes)</label>
                    <input type="number" id="tempsAttente" name="tempsAttente" value="<%= tempsAttenteAttr %>" min="1" max="180" />
                </div>
                <div>
                    <button type="submit">Assigner</button>
                </div>
            </div>
        </form>
    </section>
</main>
</body>
</html>