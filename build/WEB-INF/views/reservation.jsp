<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cousin.model.Hotel" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reservation - BackOffice</title>
    <style>
        .error { color: red; font-weight: bold; }
        .success { color: green; font-weight: bold; }
    </style>
</head>
<body>
<h2>Formulaire d'Insertion de Réservation - BackOffice</h2>

<%
    Object message = request.getAttribute("message");
    if (message != null) {
%>
    <p class="success"><%= message %></p>
<%
    }
%>

<%
    Object error = request.getAttribute("error");
    if (error != null) {
%>
    <p class="error">Erreur: <%= error %></p>
<%
    }
%>

<form method="post" action="<%= request.getContextPath() %>/reservation/create">
    <label>Date et heure d'arrivée <span style="color: red;">*</span></label><br>
    <input type="datetime-local" name="dateHeureArrive" required><br><br>

    <label>ID Client <span style="color: red;">*</span> (exactement 4 chiffres)</label><br>
    <input type="text" name="idClient" pattern="\d{4}" maxlength="4" placeholder="ex: 1234" required><br><br>

    <label>Nombre de passagers <span style="color: red;">*</span></label><br>
    <input type="number" name="nbPassager" min="1" required><br><br>

    <label>Hôtel <span style="color: red;">*</span></label><br>
    <select name="hotel.idHotel" required>
        <option value="">-- Sélectionner un hôtel --</option>
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
    <br><br>

    <button type="submit">Enregistrer</button>
    <button type="reset">Réinitialiser</button>
</form>

<hr>
<p><small>BackOffice - Formulaire réservation - Schema: staging</small></p>
</body>
</html>
