<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Collections" %>
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

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDateTime toMinute(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.truncatedTo(ChronoUnit.MINUTES);
    }

    private boolean isInInterval(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        return !value.isBefore(start) && value.isBefore(end);
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

    Map<Integer, List<Map<String, Object>>> splitDetailsParReservation = (Map<Integer, List<Map<String, Object>>>) request.getAttribute("splitDetailsParReservation");
    if (splitDetailsParReservation == null) {
        splitDetailsParReservation = new LinkedHashMap<Integer, List<Map<String, Object>>>();
    }

    Map<Integer, Integer> passagersDemandesParReservation = (Map<Integer, Integer>) request.getAttribute("passagersDemandesParReservation");
    if (passagersDemandesParReservation == null) {
        passagersDemandesParReservation = new LinkedHashMap<Integer, Integer>();
    }

    Map<Integer, Integer> passagersAssignesParReservation = (Map<Integer, Integer>) request.getAttribute("passagersAssignesParReservation");
    if (passagersAssignesParReservation == null) {
        passagersAssignesParReservation = new LinkedHashMap<Integer, Integer>();
    }

    Map<Integer, Integer> passagersRestantsParReservation = (Map<Integer, Integer>) request.getAttribute("passagersRestantsParReservation");
    if (passagersRestantsParReservation == null) {
        passagersRestantsParReservation = new LinkedHashMap<Integer, Integer>();
    }

    Map<Integer, Reservation> reservationsParId = new LinkedHashMap<Integer, Reservation>();
    for (Assignation assignation : assignations) {
        if (assignation != null && assignation.getReservation() != null) {
            Reservation reservation = assignation.getReservation();
            reservationsParId.putIfAbsent(reservation.getIdReservation(), reservation);
        }
    }
    for (GroupeTemps groupe : groupes) {
        if (groupe == null || groupe.getReservations() == null) {
            continue;
        }
        for (Reservation reservation : groupe.getReservations()) {
            if (reservation != null) {
                reservationsParId.putIfAbsent(reservation.getIdReservation(), reservation);
            }
        }
    }
    for (Reservation reservation : nonAssignees) {
        if (reservation != null) {
            reservationsParId.putIfAbsent(reservation.getIdReservation(), reservation);
        }
    }

    Set<Integer> reservationsSplitIds = new LinkedHashSet<Integer>();
    reservationsSplitIds.addAll(passagersDemandesParReservation.keySet());
    reservationsSplitIds.addAll(passagersAssignesParReservation.keySet());
    reservationsSplitIds.addAll(passagersRestantsParReservation.keySet());
    reservationsSplitIds.addAll(splitDetailsParReservation.keySet());
    for (Reservation reservation : nonAssignees) {
        if (reservation != null) {
            reservationsSplitIds.add(reservation.getIdReservation());
        }
    }

    List<Integer> reservationsSplitTriees = new ArrayList<Integer>(reservationsSplitIds);
    Collections.sort(reservationsSplitTriees);

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
        <h2>Suivi split par reservation</h2>
        <% if (reservationsSplitTriees.isEmpty()) { %>
            <div class="alert warning">Aucune reservation a afficher pour le split.</div>
        <% } else { %>
            <table class="split-table">
                <thead>
                <tr>
                    <th>Reservation</th>
                    <th>Hotel</th>
                    <th>Demande</th>
                    <th>Assignations partielles (vehicule - passagers)</th>
                    <th>Restant</th>
                    <th>Statut</th>
                </tr>
                </thead>
                <tbody>
                <% for (Integer idReservation : reservationsSplitTriees) {
                       if (idReservation == null) {
                           continue;
                       }

                       Reservation reservationInfo = reservationsParId.get(idReservation);
                       int demandes = passagersDemandesParReservation.containsKey(idReservation)
                           ? Math.max(0, passagersDemandesParReservation.get(idReservation))
                           : (reservationInfo != null ? Math.max(0, reservationInfo.getNbPassager()) : 0);
                       int assignes = passagersAssignesParReservation.containsKey(idReservation)
                           ? Math.max(0, passagersAssignesParReservation.get(idReservation))
                           : 0;
                       int restants = passagersRestantsParReservation.containsKey(idReservation)
                           ? Math.max(0, passagersRestantsParReservation.get(idReservation))
                           : Math.max(0, demandes - assignes);

                       List<Map<String, Object>> details = splitDetailsParReservation.get(idReservation);
                       int nbFragments = details == null ? 0 : details.size();

                       String statusClass = "split-badge split-full";
                       String statusLabel = "Assigne complet (1 vehicule)";
                       if (assignes <= 0) {
                           statusClass = "split-badge split-none";
                           statusLabel = "Non assigne";
                       } else if (restants > 0) {
                           statusClass = "split-badge split-partial";
                           statusLabel = "Split partiel";
                       } else if (nbFragments > 1) {
                           statusClass = "split-badge split-complete";
                           statusLabel = "Split complet";
                       }
                %>
                    <tr>
                        <td>R<%= idReservation %></td>
                        <td>
                            <%= reservationInfo != null && reservationInfo.getHotel() != null
                                    ? safe(reservationInfo.getHotel().getNom())
                                    : "-" %>
                        </td>
                        <td><%= demandes %>p</td>
                        <td>
                            <% if (details == null || details.isEmpty()) { %>
                                <span class="meta-line">Aucune assignation enregistree</span>
                            <% } else { %>
                                <ul class="split-list">
                                    <% for (Map<String, Object> detail : details) {
                                           if (detail == null) {
                                               continue;
                                           }
                                           String refVehicule = detail.get("vehiculeReference") == null
                                               ? ""
                                               : String.valueOf(detail.get("vehiculeReference"));
                                           int idVehicule = asInt(detail.get("idVehicule"));
                                           String labelVehicule = refVehicule.trim().isEmpty()
                                               ? "V" + safe(idVehicule)
                                               : refVehicule;
                                           int passagersDetail = Math.max(0, asInt(detail.get("passagersAssignes")));
                                    %>
                                        <li><strong><%= safe(labelVehicule) %></strong> : <%= passagersDetail %>p embarques</li>
                                    <% } %>
                                </ul>
                            <% } %>
                        </td>
                        <td>
                            <% if (restants > 0) { %>
                                <span class="split-remaining"><%= restants %>p reportes / non assignes</span>
                            <% } else { %>
                                0p
                            <% } %>
                        </td>
                        <td><span class="<%= statusClass %>"><%= statusLabel %></span></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </section>

    <section class="card">
        <h2>Groupes de depart (Temps d'attente)</h2>
        <% if (groupes.isEmpty()) { %>
            <div class="alert warning">Aucun groupe calcule pour cette date.</div>
        <% } else {
               Map<Integer, Trajet> trajetsParId = new LinkedHashMap<Integer, Trajet>();
               for (Trajet trajet : trajets) {
                   if (trajet != null) {
                       trajetsParId.put(trajet.getIdTrajet(), trajet);
                   }
               }

               Set<Integer> reservationsDejaRattachees = new HashSet<Integer>();
               Set<Integer> reportsEnAttente = new LinkedHashSet<Integer>();
               for (int i = 0; i < groupes.size(); i++) {
                   GroupeTemps groupe = groupes.get(i);
                   int numeroGroupe = i + 1;
                   boolean dernierGroupe = (i == groupes.size() - 1);
                   List<Reservation> reservationsGroupe = groupe != null && groupe.getReservations() != null
                       ? groupe.getReservations()
                       : new ArrayList<Reservation>();

                   LocalDateTime debutIntervalle = groupe != null ? toMinute(groupe.getHeureDepartGroupe()) : null;
                   int attenteMinutes = groupe != null ? groupe.getTempsAttenteMinutes() : 0;
                   LocalDateTime finIntervalle = debutIntervalle != null ? debutIntervalle.plusMinutes(attenteMinutes) : null;

                   Set<Integer> reservationsGroupeIds = new LinkedHashSet<Integer>();
                   for (Reservation reservation : reservationsGroupe) {
                       if (reservation != null) {
                           reservationsGroupeIds.add(reservation.getIdReservation());
                       }
                   }

                   Set<Integer> lotIds = new LinkedHashSet<Integer>(reportsEnAttente);
                   lotIds.addAll(reservationsGroupeIds);

                   List<Assignation> assignationsGroupe = new ArrayList<Assignation>();
                   Set<Integer> reservationsAssigneesLot = new LinkedHashSet<Integer>();
                   for (Assignation assignation : assignations) {
                       if (assignation == null || assignation.getReservation() == null) {
                           continue;
                       }

                       int idReservation = assignation.getReservation().getIdReservation();
                       if (!lotIds.contains(idReservation) || reservationsDejaRattachees.contains(idReservation)) {
                           continue;
                       }

                       LocalDateTime dateTraitement = toMinute(assignation.getDateHeureDepart());
                       boolean traiteDansCeGroupe = isInInterval(dateTraitement, debutIntervalle, finIntervalle);
                       if (!traiteDansCeGroupe && !dernierGroupe) {
                           continue;
                       }

                       assignationsGroupe.add(assignation);
                       reservationsAssigneesLot.add(idReservation);
                       reservationsDejaRattachees.add(idReservation);
                   }

                   Set<Integer> nouveauReport = new LinkedHashSet<Integer>(lotIds);
                   nouveauReport.removeAll(reservationsAssigneesLot);
                   reportsEnAttente = nouveauReport;

                   Map<Integer, List<Assignation>> assignationsParTrajet = new LinkedHashMap<Integer, List<Assignation>>();
                   for (Assignation assignation : assignationsGroupe) {
                       Integer idTrajet = assignation.getIdTrajet();
                       if (idTrajet == null || idTrajet <= 0) {
                           continue;
                       }
                       List<Assignation> liste = assignationsParTrajet.get(idTrajet);
                       if (liste == null) {
                           liste = new ArrayList<Assignation>();
                           assignationsParTrajet.put(idTrajet, liste);
                       }
                       liste.add(assignation);
                   }

                   int nbReservations = reservationsGroupeIds.size();
                   int nbAssignees = 0;
                   for (Integer idReservation : reservationsGroupeIds) {
                       if (reservationsAssigneesLot.contains(idReservation)) {
                           nbAssignees++;
                       }
                   }
                   int nbNonAssignees = Math.max(0, nbReservations - nbAssignees);
        %>
            <div class="card">
                <h3>Groupe #<%= numeroGroupe %> @ <%= safe(debutIntervalle) %></h3>
                <p class="meta-line">
                    Intervalle: <%= safe(debutIntervalle) %> -> <%= safe(finIntervalle) %>
                    | Duree: <%= attenteMinutes %> min
                    | Reservations: <%= nbReservations %>
                    | Assignees: <%= nbAssignees %>
                    | Non assignees: <%= nbNonAssignees %>
                </p>
                <p>Reportees en entree : <%= lotIds.size() - reservationsGroupeIds.size() %></p>

                <% if (reservationsGroupe.isEmpty()) { %>
                    <div class="alert warning">Aucune reservation dans ce groupe.</div>
                <% } else { %>
                    <table>
                        <thead>
                        <tr>
                            <th>Reservation</th>
                            <th>Hotel</th>
                            <th>Passagers</th>
                            <th>Etat</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (Reservation reservation : reservationsGroupe) {
                               if (reservation == null) {
                                   continue;
                               }
                               boolean assignee = reservationsAssigneesLot.contains(reservation.getIdReservation());
                        %>
                            <tr>
                                <td><%= reservation.getIdReservation() %></td>
                                <td><%= reservation.getHotel() != null ? safe(reservation.getHotel().getNom()) : "-" %></td>
                                <td><%= reservation.getNbPassager() %></td>
                                <td><%= assignee ? "Assignee" : "Reportee / Non assignee" %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                <% } %>

                <div class="stack" style="margin-top:12px;">
                    <h4>Trajets et assignations du groupe</h4>
                    <% if (assignationsParTrajet.isEmpty()) { %>
                        <div class="alert warning">Aucun trajet associe a ce groupe (y compris les reservations reportees).</div>
                    <% } else {
                           int numeroTrajetGroupe = 0;
                           for (Map.Entry<Integer, List<Assignation>> entry : assignationsParTrajet.entrySet()) {
                               Integer idTrajet = entry.getKey();
                               List<Assignation> assignationsTrajet = entry.getValue();
                               Trajet trajet = trajetsParId.get(idTrajet);
                               numeroTrajetGroupe++;

                               Vehicule vehicule = trajet != null ? trajet.getVehicule() : null;
                               int distanceTotaleTrajet = 0;
                               long dureeMinutes = 0;
                               List<TrajetEtape> etapes = trajet != null ? trajet.getEtapes() : null;
                               if (etapes != null) {
                                   for (TrajetEtape etape : etapes) {
                                       if (etape != null) {
                                           distanceTotaleTrajet += etape.getDistanceDepuisPrecedent();
                                       }
                                   }
                               }
                               if (trajet != null && trajet.getDateHeureDepart() != null && trajet.getDateHeureRetour() != null) {
                                   dureeMinutes = Duration.between(trajet.getDateHeureDepart(), trajet.getDateHeureRetour()).toMinutes();
                               }
                    %>
                        <div class="card trajet-block">
                            <h3>[Trajet #<%= numeroTrajetGroupe %> : <%= vehicule != null ? safe(vehicule.getReference()) : "V-?" %> (<%= vehicule != null ? vehicule.getNbPlace() : "?" %>p, <%= vehicule != null ? safe(vehicule.getTypeVehicule()) : "-" %>)]</h3>
                            <p>Depart <%= trajet != null ? safe(trajet.getDateHeureDepart()) : "-" %> -> Retour <%= trajet != null ? safe(trajet.getDateHeureRetour()) : "-" %></p>
                            <p>Distance : <%= distanceTotaleTrajet %> km | Duree : <%= dureeMinutes %> min</p>

                            <div class="route-line">
                                <strong>Itineraire :</strong>
                                <span>AEROPORT</span>
                                <% Set<String> hotelsAffiches = new LinkedHashSet<String>();
                                   if (etapes != null && !etapes.isEmpty()) {
                                       for (TrajetEtape etape : etapes) {
                                           if (etape == null) {
                                               continue;
                                           }
                                           Hotel hotel = etape.getHotel();
                                           String hotelNom = hotel != null ? safe(hotel.getNom()) : "HOTEL";
                                           if (hotelsAffiches.contains(hotelNom)) {
                                               continue;
                                           }
                                           hotelsAffiches.add(hotelNom);
                                %>
                                    <span> --<%= etape.getDistanceDepuisPrecedent() %>km--> <%= hotelNom %></span>
                                <%     }
                                   } %>
                            </div>

                            <p class="meta-line">
                                <strong>Reservations :</strong>
                                Voir le bloc <em>Suivi split par reservation</em> pour le detail par vehicule.
                            </p>
                        </div>
                    <%     }
                       } %>
                </div>
            </div>
        <%     }
           } %>
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
            <p class="meta-line">Le restant detaille est visible dans le bloc <strong>Suivi split par reservation</strong>.</p>
        </section>
    </c:if>

    <section>
        <a class="nav-link active" href="<%= ctx %>/assignation/form">Nouvelle assignation</a>
    </section>
</main>
</body>
</html>
