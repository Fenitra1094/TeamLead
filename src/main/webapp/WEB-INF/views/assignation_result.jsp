<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="com.cousin.model.Assignation" %>
<%@ page import="com.cousin.model.Reservation" %>
<%@ page import="com.cousin.model.Trajet" %>
<%@ page import="com.cousin.model.TrajetEtape" %>
<%@ page import="com.cousin.model.Vehicule" %>
<%@ page import="com.cousin.model.Hotel" %>
<%@ page import="com.cousin.util.GroupeTemps" %>

<%!
    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private LocalDateTime toMinute(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.truncatedTo(ChronoUnit.MINUTES);
    }

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
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Resultat Assignation</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/theme-gray.css">
</head>
<body>
<%
    String ctx = request.getContextPath();
    Object message = request.getAttribute("message");
    Object error = request.getAttribute("error");
    String dateChoisie = (String) request.getAttribute("date");
    Integer tempsAttente = (Integer) request.getAttribute("tempsAttente");
    if (tempsAttente == null) {
        tempsAttente = 30;
    }

    List<GroupeTemps> groupes = (List<GroupeTemps>) request.getAttribute("groupes");
    if (groupes == null) {
        groupes = new ArrayList<GroupeTemps>();
    }

    List<Assignation> assignations = (List<Assignation>) request.getAttribute("assignations");
    if (assignations == null) {
        assignations = new ArrayList<Assignation>();
    }

    List<Reservation> nonAssignees = (List<Reservation>) request.getAttribute("reservationsNonAssignees");
    if (nonAssignees == null) {
        nonAssignees = new ArrayList<Reservation>();
    }

    List<Trajet> trajets = (List<Trajet>) request.getAttribute("trajets");
    if (trajets == null) {
        trajets = new ArrayList<Trajet>();
    }

    Set<LocalDateTime> groupesVol = new HashSet<LocalDateTime>();
    for (Assignation assignation : assignations) {
        if (assignation == null || assignation.getReservation() == null) {
            continue;
        }
        LocalDateTime arrivee = assignation.getReservation().getDateHeureArrive();
        if (arrivee != null) {
            groupesVol.add(toMinute(arrivee));
        }
    }

    int totalReservationsTraitees = assignations.size() + nonAssignees.size();
%>

<nav class="navbar">
    <div class="layout">
        <span class="brand">BackOffice TeamLead</span>
        <a class="nav-link active" href="<%= ctx %>/assignation/form">Assignation</a>
        <a class="nav-link" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout stack">
    <h1 class="page-title">Resultat assignation</h1>

    <div class="card">
        <div class="form-row">
            <span class="badge">Date: <%= safe(dateChoisie) %></span>
            <span class="badge">Fenetre: <%= tempsAttente %> min</span>
            <span class="badge">Total reservations: <%= totalReservationsTraitees %></span>
            <span class="badge">Groupes: <%= groupes.size() %></span>
            <span class="badge">Vehicules utilises: <%= trajets.size() %></span>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert error">${error}</div>
    </c:if>

    <c:if test="${not empty message}">
        <div class="alert success">${message}</div>
    </c:if>

    <section class="card">
        <h2>Groupes de depart (Temps d'attente)</h2>
        <c:if test="${empty groupes}">
            <div class="alert warning">Aucun groupe calcule pour cette date.</div>
        </c:if>
        <c:forEach var="groupe" items="${groupes}">
            <div class="card">
                <h3>Groupe @ ${groupe.heureDepartGroupe}</h3>
                <p>Temps d'attente : ${groupe.tempsAttenteMinutes} min</p>
                <table>
                    <thead>
                    <tr>
                        <th>Reservation</th>
                        <th>Hotel</th>
                        <th>Passagers</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${groupe.reservations}">
                        <tr>
                            <td>${r.idReservation}</td>
                            <td><c:out value="${r.hotel.nom}" default="-" /></td>
                            <td>${r.nbPassager}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:forEach>
    </section>

    <section class="card">
        <h2>Trajets et assignations</h2>
        <% if (trajets.isEmpty()) { %>
            <div class="alert warning">Aucun trajet detaille n'a ete retourne.</div>
        <% } else {
               int numeroTrajet = 0;
               for (Trajet trajet : trajets) {
                   numeroTrajet++;
                   Vehicule vehicule = trajet.getVehicule();
        %>
            <div class="card">
                <h3>Trajet <%= numeroTrajet %></h3>
                <p>
                    Vehicule: <%= vehicule != null ? safe(vehicule.getReference()) : "-" %>
                    | Capacite: <%= vehicule != null ? vehicule.getNbPlace() : "-" %>
                    | Depart: <%= safe(trajet.getDateHeureDepart()) %>
                    | Retour: <%= safe(trajet.getDateHeureRetour()) %>
                </p>
                <table>
                    <thead>
                    <tr>
                        <th>Ordre</th>
                        <th>Etape hotel</th>
                        <th>Distance depuis precedent (km)</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% List<TrajetEtape> etapes = trajet.getEtapes();
                       if (etapes == null || etapes.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="3">Aucune etape detaillee.</td>
                        </tr>
                    <% } else {
                           for (TrajetEtape etape : etapes) {
                               Hotel hotel = etape.getHotel();
                    %>
                        <tr>
                            <td><%= etape.getOrdre() %></td>
                            <td><%= hotel != null ? safe(hotel.getNom()) : "-" %></td>
                            <td><%= etape.getDistanceDepuisPrecedent() %></td>
                        </tr>
                    <%     }
                       }
                    %>
                    </tbody>
                </table>

                <table style="margin-top: 10px;">
                    <thead>
                    <tr>
                        <th>Reservation</th>
                        <th>Client</th>
                        <th>Passagers</th>
                        <th>Hotel</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% boolean hasAssignation = false;
                       for (Assignation a : assignations) {
                           if (a.getIdTrajet() != trajet.getIdTrajet()) {
                               continue;
                           }
                           hasAssignation = true;
                           Reservation r = a.getReservation();
                    %>
                        <tr>
                            <td><%= r != null ? r.getIdReservation() : "-" %></td>
                            <td><%= r != null ? safe(r.getIdClient()) : "-" %></td>
                            <td><%= r != null ? r.getNbPassager() : "-" %></td>
                            <td><%= (r != null && r.getHotel() != null) ? safe(r.getHotel().getNom()) : "-" %></td>
                        </tr>
                    <% }
                       if (!hasAssignation) {
                    %>
                        <tr>
                            <td colspan="4">Aucune reservation liee a ce trajet.</td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        <%   }
           }
        %>
    </section>

    <c:if test="${not empty reservationsNonAssignees}">
        <section class="card">
            <h3>Reservations non assignees (${fn:length(reservationsNonAssignees)})</h3>
            <table>
                <thead>
                <tr>
                    <th>ID Reservation</th>
                    <th>Hotel</th>
                    <th>Nb Passagers</th>
                    <th>Date/Heure Arrivee</th>
                    <th>Raison</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="r" items="${reservationsNonAssignees}">
                    <tr>
                        <td>${r.idReservation}</td>
                        <td><c:out value="${r.hotel.nom}" default="-" /></td>
                        <td>${r.nbPassager}</td>
                        <td>${r.dateHeureArrive}</td>
                        <td>Pas de vehicule disponible</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </c:if>

    <section>
        <a class="nav-link active" href="<%= ctx %>/assignation/form">Nouvelle assignation</a>
    </section>
</main>
</body>
</html>
