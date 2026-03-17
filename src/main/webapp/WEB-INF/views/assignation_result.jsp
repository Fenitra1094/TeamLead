<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.time.Duration" %>
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

    Set<Vehicule> vehiculesUtilises = (Set<Vehicule>) request.getAttribute("vehiculesUtilises");
    if (vehiculesUtilises == null) {
        vehiculesUtilises = new LinkedHashSet<Vehicule>();
    }

    List<Vehicule> vehiculesNonUtilises = (List<Vehicule>) request.getAttribute("vehiculesNonUtilises");
    if (vehiculesNonUtilises == null) {
        vehiculesNonUtilises = new ArrayList<Vehicule>();
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
    int kmTotaux = 0;
    Map<Integer, Integer> nbTrajetsParVehicule = new LinkedHashMap<Integer, Integer>();
    for (Trajet trajet : trajets) {
        if (trajet == null) {
            continue;
        }
        int vehiculeId = trajet.getIdVehicule();
        if (vehiculeId <= 0 && trajet.getVehicule() != null) {
            vehiculeId = trajet.getVehicule().getIdVehicule();
        }
        if (vehiculeId > 0) {
            Integer current = nbTrajetsParVehicule.get(vehiculeId);
            nbTrajetsParVehicule.put(vehiculeId, current == null ? 1 : current + 1);
        }

        List<TrajetEtape> etapesTrajet = trajet.getEtapes();
        if (etapesTrajet != null) {
            for (TrajetEtape etape : etapesTrajet) {
                if (etape != null) {
                    kmTotaux += etape.getDistanceDepuisPrecedent();
                }
            }
        }
    }
%>

<nav class="navbar">
    <div class="layout">
        <span class="brand">BackOffice TeamLead : 3265 - 3273 - 3371</span>
        <a class="nav-link active" href="<%= ctx %>/assignation/form">Assignation</a>
        <a class="nav-link" href="<%= ctx %>/reservation/form">Reservation</a>
        <a class="nav-link" href="<%= ctx %>/vehicule/list">Vehicule</a>
    </div>
</nav>

<main class="layout stack">
    <h1 class="page-title">Resultat assignation</h1>

    <section class="card">
        <div class="stats-grid">
            <div class="stat-pill"><strong>Groupes</strong><span><%= groupes.size() %></span></div>
            <div class="stat-pill"><strong>Assignees</strong><span><%= assignations.size() %></span></div>
            <div class="stat-pill"><strong>Vehicules utilises</strong><span><%= vehiculesUtilises.size() %></span></div>
            <div class="stat-pill"><strong>Non assignees</strong><span><%= nonAssignees.size() %></span></div>
            <div class="stat-pill"><strong>Km totaux</strong><span><%= kmTotaux %> km</span></div>
        </div>
        <p class="meta-line">Date: <%= safe(dateChoisie) %> | Fenetre: <%= tempsAttente %> min | Total reservations traitees: <%= totalReservationsTraitees %></p>
    </section>

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
                   int distanceTotaleTrajet = 0;
                   List<TrajetEtape> etapes = trajet.getEtapes();
                   if (etapes != null) {
                       for (TrajetEtape etape : etapes) {
                           if (etape != null) {
                               distanceTotaleTrajet += etape.getDistanceDepuisPrecedent();
                           }
                       }
                   }
                   long dureeMinutes = 0;
                   if (trajet.getDateHeureDepart() != null && trajet.getDateHeureRetour() != null) {
                       dureeMinutes = Duration.between(trajet.getDateHeureDepart(), trajet.getDateHeureRetour()).toMinutes();
                   }
        %>
            <div class="card trajet-block">
                <h3>[Trajet #<%= numeroTrajet %> : <%= vehicule != null ? safe(vehicule.getReference()) : "V-?" %> (<%= vehicule != null ? vehicule.getNbPlace() : "?" %>p, <%= vehicule != null ? safe(vehicule.getTypeVehicule()) : "-" %>)]</h3>
                <p>Depart <%= safe(trajet.getDateHeureDepart()) %> -> Retour <%= safe(trajet.getDateHeureRetour()) %></p>
                <p>Distance : <%= distanceTotaleTrajet %> km | Duree : <%= dureeMinutes %> min</p>

                <div class="route-line">
                    <strong>Itineraire :</strong>
                    <span>AER</span>
                    <% if (etapes == null || etapes.isEmpty()) { %>
                        <span> -> AER</span>
                    <% } else {
                           for (TrajetEtape etape : etapes) {
                               Hotel hotel = etape.getHotel();
                    %>
                        <span> --<%= etape.getDistanceDepuisPrecedent() %>km--> <%= hotel != null ? safe(hotel.getNom()) : "HOTEL" %></span>
                    <%     } %>
                        <span> --> AER</span>
                    <% } %>
                </div>

                <p class="meta-line">
                    <strong>Reservations :</strong>
                    <% boolean hasAssignation = false;
                       for (Assignation a : assignations) {
                           if (a.getIdTrajet() != trajet.getIdTrajet()) {
                               continue;
                           }
                           hasAssignation = true;
                           Reservation r = a.getReservation();
                           String hotelNom = (r != null && r.getHotel() != null) ? r.getHotel().getNom() : "-";
                    %>
                        <span class="trip-chip">R<%= r != null ? r.getIdReservation() : "-" %> (<%= r != null ? r.getNbPassager() : "-" %>p, <%= safe(hotelNom) %>)</span>
                    <% }
                       if (!hasAssignation) {
                    %>
                        <span>Aucune reservation liee a ce trajet.</span>
                    <% } %>
                </p>
            </div>
        <%   }
           }
        %>
    </section>

    <section class="card">
        <h2>Vehicules utilises</h2>
        <c:if test="${empty vehiculesUtilises}">
            <div class="alert warning">Aucun vehicule utilise pour cette execution.</div>
        </c:if>
        <c:if test="${not empty vehiculesUtilises}">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Reference</th>
                    <th>Type</th>
                    <th>Capacite</th>
                    <th>Nb trajets effectues</th>
                </tr>
                </thead>
                <tbody>
                <% for (Vehicule v : vehiculesUtilises) {
                       int idVeh = v != null ? v.getIdVehicule() : 0;
                       Integer nbTrajets = nbTrajetsParVehicule.get(idVeh);
                %>
                    <tr>
                        <td><%= idVeh > 0 ? idVeh : -1 %></td>
                        <td><%= v != null ? safe(v.getReference()) : "-" %></td>
                        <td><%= v != null ? safe(v.getTypeVehicule()) : "-" %></td>
                        <td><%= v != null ? v.getNbPlace() : 0 %></td>
                        <td><%= nbTrajets != null ? nbTrajets : 0 %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </c:if>
    </section>

    <section class="card table-muted">
        <h2>Vehicules non utilises</h2>
        <c:if test="${empty vehiculesNonUtilises}">
            <div class="alert warning">Tous les vehicules ont ete utilises.</div>
        </c:if>
        <c:if test="${not empty vehiculesNonUtilises}">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Reference</th>
                    <th>Type</th>
                    <th>Capacite</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="v" items="${vehiculesNonUtilises}">
                    <tr>
                        <td>${v.idVehicule}</td>
                        <td>${v.reference}</td>
                        <td>${v.typeVehicule}</td>
                        <td>${v.nbPlace}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:if>
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
