<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="com.cousin.model.Assignation" %>
<%@ page import="com.cousin.model.Reservation" %>
<%@ page import="com.cousin.model.Vehicule" %>

<%!
    private Object invokeGetter(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private List<?> asList(Object value) {
        if (value instanceof List<?>) {
            return (List<?>) value;
        }
        return new ArrayList<Object>();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private LocalDateTime toMinute(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.truncatedTo(ChronoUnit.MINUTES);
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Assignation Vehicules</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 32px; }
        h2, h3 { color: #222; }
        table { border-collapse: collapse; width: 100%; margin-top: 10px; }
        th, td { border: 1px solid #cfcfcf; padding: 8px; text-align: left; }
        th { background-color: #f3f3f3; }
        .error { color: #b00020; font-weight: bold; padding: 10px; background: #fde8ec; border: 1px solid #f3b8c3; margin: 10px 0; }
        .success { color: #0f6b2e; font-weight: bold; padding: 10px; background: #e7f6eb; border: 1px solid #b7e1c2; margin: 10px 0; }
        .warning { color: #8a5a00; font-weight: bold; padding: 10px; background: #fff4df; border: 1px solid #f0d6a2; margin: 10px 0; }
        .card { border: 1px solid #d9d9d9; padding: 12px; margin: 14px 0; background: #fafafa; }
        .meta { margin: 0 0 8px 0; }
        form { margin-bottom: 24px; }
        input[type="date"] { padding: 5px; font-size: 14px; }
        button { padding: 8px 16px; font-size: 14px; cursor: pointer; }
    </style>
</head>
<body>

<h2>Assignation des vehicules aux reservations</h2>

<form method="get" action="<%= request.getContextPath() %>/assignation/assigner">
    <label>Choisir une date :</label><br><br>
    <input type="date" name="date" required>
    <button type="submit">Assigner</button>
</form>

<%
    Object error = request.getAttribute("error");
    Object message = request.getAttribute("message");
    String dateChoisie = (String) request.getAttribute("date");

    List<Assignation> assignations = (List<Assignation>) request.getAttribute("assignations");
    if (assignations == null) {
        assignations = new ArrayList<Assignation>();
    }

    List<Reservation> nonAssignees = (List<Reservation>) request.getAttribute("reservationsNonAssignees");
    if (nonAssignees == null) {
        nonAssignees = new ArrayList<Reservation>();
    }

    List<?> trajets = (List<?>) request.getAttribute("trajets");
    if (trajets == null) {
        trajets = new ArrayList<Object>();
    }

    Set<LocalDateTime> groupesVol = new HashSet<LocalDateTime>();
    Set<Integer> vehiculesUtilises = new HashSet<Integer>();

    for (Assignation assignation : assignations) {
        if (assignation == null) {
            continue;
        }
        Reservation reservation = assignation.getReservation();
        if (reservation != null && reservation.getDateHeureArrive() != null) {
            groupesVol.add(toMinute(reservation.getDateHeureArrive()));
        }
        Vehicule vehicule = assignation.getVehicule();
        if (vehicule != null) {
            vehiculesUtilises.add(vehicule.getIdVehicule());
        }
    }

    for (Reservation reservation : nonAssignees) {
        if (reservation != null && reservation.getDateHeureArrive() != null) {
            groupesVol.add(toMinute(reservation.getDateHeureArrive()));
        }
    }

    int totalReservationsTraitees = assignations.size() + nonAssignees.size();
    int nombreVols = groupesVol.size();
    int nombreVehiculesUtilises = trajets.isEmpty() ? vehiculesUtilises.size() : trajets.size();
%>

<% if (error != null) { %>
    <div class="error"><%= error %></div>
<% } %>

<% if (message != null) { %>
    <div class="success"><%= message %></div>
<% } %>

<% if (dateChoisie != null) { %>
    <h3>Section 1 - Resume</h3>
    <div class="card">
        <p class="meta"><strong>Date choisie:</strong> <%= safe(dateChoisie) %></p>
        <p class="meta"><strong>Nombre total de reservations traitees:</strong> <%= totalReservationsTraitees %></p>
        <p class="meta"><strong>Nombre de vols (groupes):</strong> <%= nombreVols %></p>
        <p class="meta"><strong>Nombre de vehicules utilises:</strong> <%= nombreVehiculesUtilises %></p>
        <p class="meta"><strong>Nombre de reservations non assignees:</strong> <%= nonAssignees.size() %></p>
    </div>
<% } %>

<% if (dateChoisie != null) { %>
    <h3>Section 2 - Tableau par Trajet</h3>

    <% if (!trajets.isEmpty()) { %>
        <% for (Object trajetObj : trajets) {
            Integer idTrajet = toInteger(invokeGetter(trajetObj, "getIdTrajet"));
            Object vehiculeObj = invokeGetter(trajetObj, "getVehicule");
            Object etapesObj = invokeGetter(trajetObj, "getEtapes");
            List<?> etapes = asList(etapesObj);
        %>
            <div class="card">
                <h4>Trajet <%= safe(idTrajet) %></h4>
                <p class="meta">
                    <strong>Vehicule:</strong>
                    <%= safe(invokeGetter(vehiculeObj, "getReference")) %>
                    |
                    <strong>Type:</strong>
                    <%= safe(invokeGetter(vehiculeObj, "getTypeVehicule")) %>
                    |
                    <strong>Capacite:</strong>
                    <%= safe(invokeGetter(vehiculeObj, "getNbPlace")) %>
                </p>
                <p class="meta">
                    <strong>Depart aeroport:</strong> <%= safe(invokeGetter(trajetObj, "getDateHeureDepart")) %>
                    |
                    <strong>Retour aeroport:</strong> <%= safe(invokeGetter(trajetObj, "getDateHeureRetour")) %>
                </p>

                <h4>Sous-tableau Itineraire</h4>
                <table>
                    <thead>
                        <tr>
                            <th>Ordre</th>
                            <th>Etape (Hotel)</th>
                            <th>Distance depuis precedent (km)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (etapes.isEmpty()) { %>
                            <tr>
                                <td colspan="3">Aucune etape detaillee.</td>
                            </tr>
                        <% } else {
                            for (Object etapeObj : etapes) {
                                Object hotelObj = invokeGetter(etapeObj, "getHotel");
                                Object hotelNom = invokeGetter(hotelObj, "getNom");
                                if (hotelNom == null) {
                                    hotelNom = invokeGetter(hotelObj, "getLibelle");
                                }
                        %>
                            <tr>
                                <td><%= safe(invokeGetter(etapeObj, "getOrdre")) %></td>
                                <td><%= safe(hotelNom) %></td>
                                <td><%= safe(invokeGetter(etapeObj, "getDistanceDepuisPrecedent")) %></td>
                            </tr>
                        <%      }
                           }
                        %>
                    </tbody>
                </table>

                <h4>Sous-tableau Reservations assignees</h4>
                <table>
                    <thead>
                        <tr>
                            <th>Reservation</th>
                            <th>Client</th>
                            <th>Nb Passagers</th>
                            <th>Hotel destination</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasAssignation = false;
                            for (Assignation assignation : assignations) {
                                Integer idTrajetAssignation = assignation.getIdTrajet();
                                boolean sameTrajet = (idTrajet == null && idTrajetAssignation == null)
                                        || (idTrajet != null && idTrajet.equals(idTrajetAssignation));
                                if (!sameTrajet) {
                                    continue;
                                }
                                hasAssignation = true;
                                Reservation reservation = assignation.getReservation();
                        %>
                            <tr>
                                <td><%= reservation != null ? reservation.getIdReservation() : "-" %></td>
                                <td><%= reservation != null ? reservation.getIdClient() : "-" %></td>
                                <td><%= reservation != null ? reservation.getNbPassager() : "-" %></td>
                                <td><%= reservation != null && reservation.getHotel() != null ? reservation.getHotel().getNom() : "-" %></td>
                            </tr>
                        <%
                            }
                            if (!hasAssignation) {
                        %>
                            <tr>
                                <td colspan="4">Aucune reservation liee a ce trajet.</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    <% } else { %>
        <div class="warning">Aucun trajet detaille n'a ete retourne pour cette execution.</div>
    <% } %>
<% } %>

<% if (!nonAssignees.isEmpty()) { %>
    <h3>Section 3 - Reservations non assignees</h3>
    <table>
        <thead>
            <tr>
                <th>Reservation</th>
                <th>Client</th>
                <th>Nb Passagers</th>
                <th>Hotel</th>
                <th>Heure Arrivee</th>
            </tr>
        </thead>
        <tbody>
            <% for (Reservation reservation : nonAssignees) { %>
                <tr>
                    <td><%= reservation.getIdReservation() %></td>
                    <td><%= reservation.getIdClient() %></td>
                    <td><%= reservation.getNbPassager() %></td>
                    <td><%= reservation.getHotel() != null ? reservation.getHotel().getNom() : "-" %></td>
                    <td><%= reservation.getDateHeureArrive() %></td>
                </tr>
            <% } %>
        </tbody>
    </table>
<% } %>

<% if (dateChoisie != null && totalReservationsTraitees == 0) { %>
    <div class="warning">Aucune reservation trouvee pour cette date.</div>
<% } %>

</body>
</html>