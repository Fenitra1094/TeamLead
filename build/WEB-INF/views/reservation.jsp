<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cousin.model.Hotel" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reservation</title>
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
        <a class="nav-link active" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout stack">
<h1 class="page-title">Insertion Reservation</h1>

<%
    Object message = request.getAttribute("message");
    if (message != null) {
%>
    <div class="alert success"><%= message %></div>
<%
    }
%>

<section class="card">
    <form method="post" action="<%= request.getContextPath() %>/reservation/create" class="stack">
        <div class="form-row">
            <div>
                <label>Date et heure d'arrivee</label>
                <input type="datetime-local" name="dateHeureArrive" required>
            </div>

            <div>
                <label>Id client</label>
                <input type="text" name="idClient" required>
            </div>

            <div>
                <label>Nombre de passagers</label>
                <input type="number" name="nbPassager" min="1" required>
            </div>

            <div>
                <label>Hotel</label>
                <select name="hotel.idHotel" required>
                    <option value="">-- Choisir --</option>
                    <%
                        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                        if (hotels != null) {
                            for (Hotel hotel : hotels) {
                    %>
                        <option value="<%= hotel.getIdHotel() %>"><%= hotel.getNom() %></option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
        </div>

        <div>
            <button type="submit">Enregistrer</button>
        </div>
    </form>
</section>
</main>
</body>
</html>
