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

    // Ordre de traitement: d'abord l'ordre de premiere apparition dans les assignations,
    // puis les reservations restantes selon l'ordre des groupes (deja triees metier),
    // puis les non assignees et le reste en secours.
    List<Integer> reservationsSplitOrdreTraitement = new ArrayList<Integer>();
    Set<Integer> idsDejaPrisOrdreTraitement = new LinkedHashSet<Integer>();

    for (Assignation assignation : assignations) {
        if (assignation == null) {
            continue;
        }
        Reservation reservation = assignation.getReservation();
        int idReservation = reservation != null ? reservation.getIdReservation() : assignation.getIdReservation();
        if (idReservation <= 0 || !reservationsSplitIds.contains(idReservation)) {
            continue;
        }
        if (idsDejaPrisOrdreTraitement.add(idReservation)) {
            reservationsSplitOrdreTraitement.add(idReservation);
        }
    }

    for (GroupeTemps groupe : groupes) {
        if (groupe == null || groupe.getReservations() == null) {
            continue;
        }
        for (Reservation reservation : groupe.getReservations()) {
            if (reservation == null) {
                continue;
            }
            int idReservation = reservation.getIdReservation();
            if (idReservation <= 0 || !reservationsSplitIds.contains(idReservation)) {
                continue;
            }
            if (idsDejaPrisOrdreTraitement.add(idReservation)) {
                reservationsSplitOrdreTraitement.add(idReservation);
            }
        }
    }

    for (Reservation reservation : nonAssignees) {
        if (reservation == null) {
            continue;
        }
        int idReservation = reservation.getIdReservation();
        if (idReservation <= 0 || !reservationsSplitIds.contains(idReservation)) {
            continue;
        }
        if (idsDejaPrisOrdreTraitement.add(idReservation)) {
            reservationsSplitOrdreTraitement.add(idReservation);
        }
    }

    for (Integer idReservation : reservationsSplitTriees) {
        if (idReservation != null && idsDejaPrisOrdreTraitement.add(idReservation)) {
            reservationsSplitOrdreTraitement.add(idReservation);
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
        <h2>Suivi split par reservation (ordre de traitement)</h2>
        <% if (reservationsSplitOrdreTraitement.isEmpty()) { %>
            <div class="alert warning">Aucune reservation a afficher pour l'ordre de traitement.</div>
        <% } else { %>
            <table class="split-table">
                <thead>
                <tr>
                    <th>Ordre</th>
                    <th>Reservation</th>
                    <th>Hotel</th>
                    <th>Demande</th>
                    <th>Assignations partielles (vehicule - passagers)</th>
                    <th>Restant</th>
                    <th>Statut</th>
                </tr>
                </thead>
                <tbody>
                <% int ordreTraitement = 0;
                   Map<Integer, Integer> utilisationCumuleeParVehicule = new LinkedHashMap<Integer, Integer>();
                   for (Integer idReservation : reservationsSplitOrdreTraitement) {
                       if (idReservation == null) {
                           continue;
                       }
                       ordreTraitement++;

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
                        <td>#<%= ordreTraitement %></td>
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
                                           int capaciteVehicule = Math.max(0, asInt(detail.get("capaciteVehicule")));
                                           int utilisationAvant = Math.max(0, utilisationCumuleeParVehicule.getOrDefault(idVehicule, 0));
                                           int utilisationApres = utilisationAvant + passagersDetail;
                                           utilisationCumuleeParVehicule.put(idVehicule, utilisationApres);
                                           int placesRestantesVehicule = Math.max(0, capaciteVehicule - utilisationApres);
                                    %>
                                        <li>
                                            <strong><%= safe(labelVehicule) %></strong> : <%= passagersDetail %>p embarques
                                            <% if (placesRestantesVehicule > 0) { %>
                                                <span class="meta-line">(reste <%= placesRestantesVehicule %>p)</span>
                                            <% } %>
                                        </li>
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

               // Reports effectivement transmis depuis le groupe precedent vers le groupe courant.
               Map<Integer, Integer> reportsVersGroupeCourant = new LinkedHashMap<Integer, Integer>();

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

                   Set<Integer> reportsEnEntreeIds = new LinkedHashSet<Integer>();
                   Map<Integer, Integer> reportsEnEntreeParReservation = new LinkedHashMap<Integer, Integer>();
                   int passagersReportesEnEntree = 0;
                   for (Map.Entry<Integer, Integer> entry : reportsVersGroupeCourant.entrySet()) {
                       Integer idReservation = entry.getKey();
                       int restant = Math.max(0, entry.getValue());
                       if (idReservation == null || restant <= 0) {
                           continue;
                       }
                       reportsEnEntreeIds.add(idReservation);
                       reportsEnEntreeParReservation.put(idReservation, restant);
                       passagersReportesEnEntree += restant;
                   }

                   Set<Integer> lotIds = new LinkedHashSet<Integer>(reportsEnEntreeIds);
                   lotIds.addAll(reservationsGroupeIds);

                   List<Assignation> assignationsGroupe = new ArrayList<Assignation>();
                   Map<Integer, Integer> passagersAssignesDansGroupe = new LinkedHashMap<Integer, Integer>();
                   for (Assignation assignation : assignations) {
                       if (assignation == null || assignation.getReservation() == null) {
                           continue;
                       }

                       int idReservation = assignation.getReservation().getIdReservation();
                       if (!lotIds.contains(idReservation)) {
                           continue;
                       }

                       LocalDateTime dateTraitement = toMinute(assignation.getDateHeureDepart());
                       boolean traiteDansCeGroupe = isInInterval(dateTraitement, debutIntervalle, finIntervalle);
                       if (!traiteDansCeGroupe) {
                           continue;
                       }

                       assignationsGroupe.add(assignation);
                       int pax = Math.max(0, asInt(assignation.getQuantitePassagersAssignes()));
                       if (pax <= 0 && assignation.getReservation() != null) {
                           pax = Math.max(0, assignation.getReservation().getNbPassager());
                       }
                       passagersAssignesDansGroupe.put(idReservation, passagersAssignesDansGroupe.getOrDefault(idReservation, 0) + pax);
                   }

                   LocalDateTime heureDepartOperationnelle = debutIntervalle;
                   for (Assignation assignation : assignationsGroupe) {
                       if (assignation == null || assignation.getDateHeureDepart() == null) {
                           continue;
                       }
                       LocalDateTime departAssigne = toMinute(assignation.getDateHeureDepart());
                       if (departAssigne == null) {
                           continue;
                       }
                       if (heureDepartOperationnelle == null || departAssigne.isAfter(heureDepartOperationnelle)) {
                           heureDepartOperationnelle = departAssigne;
                       }
                   }

                   Map<Integer, Integer> reportsPourGroupeSuivant = new LinkedHashMap<Integer, Integer>();
                   for (Integer idReservation : lotIds) {
                       if (idReservation == null) {
                           continue;
                       }

                       int reportEntree = Math.max(0, reportsVersGroupeCourant.getOrDefault(idReservation, 0));
                       int demandeNativeGroupe = 0;
                       if (reservationsGroupeIds.contains(idReservation)) {
                           demandeNativeGroupe = Math.max(0, passagersDemandesParReservation.getOrDefault(idReservation, 0));
                       }

                       int totalATraiterDansCeGroupe = reportEntree + demandeNativeGroupe;
                       int assignesGroupe = Math.max(0, passagersAssignesDansGroupe.getOrDefault(idReservation, 0));
                       int restantApresGroupe = Math.max(0, totalATraiterDansCeGroupe - assignesGroupe);
                       if (restantApresGroupe > 0) {
                           reportsPourGroupeSuivant.put(idReservation, restantApresGroupe);
                       }
                   }
                   reportsVersGroupeCourant = reportsPourGroupeSuivant;

                   Set<Integer> reservationsAssigneesLot = new LinkedHashSet<Integer>();
                   for (Map.Entry<Integer, Integer> entry : passagersAssignesDansGroupe.entrySet()) {
                       if (entry.getKey() != null && Math.max(0, entry.getValue()) > 0) {
                           reservationsAssigneesLot.add(entry.getKey());
                       }
                   }

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
                <h3>Groupe #<%= numeroGroupe %> @ <%= safe(heureDepartOperationnelle) %></h3>
                <p class="meta-line">
                    Intervalle: <%= safe(debutIntervalle) %> -> <%= safe(finIntervalle) %>
                    | Depart operationnel: <%= safe(heureDepartOperationnelle) %>
                    | Duree: <%= attenteMinutes %> min
                    | Reservations: <%= nbReservations %>
                    | Assignees: <%= nbAssignees %>
                    | Non assignees: <%= nbNonAssignees %>
                </p>
                <p>Reportees en entree : <%= reportsEnEntreeIds.size() %> reservation(s) / <%= passagersReportesEnEntree %> passager(s)</p>
                <% if (!reportsEnEntreeIds.isEmpty()) { %>
                    <div class="alert warning">
                        Ce groupe traite des reservations reportees du groupe precedent.
                        <br>
                        <strong>Details reports :</strong>
                        <% boolean firstReported = true;
                           for (Integer idReport : reportsEnEntreeIds) {
                               if (idReport == null) {
                                   continue;
                               }
                               Reservation reservationReportee = reservationsParId.get(idReport);
                               int paxReportes = Math.max(0, reportsEnEntreeParReservation.getOrDefault(idReport, 0));
                               String hotelReport = "-";
                               String clientReport = "-";
                               if (reservationReportee != null) {
                                   if (reservationReportee.getHotel() != null) {
                                       hotelReport = safe(reservationReportee.getHotel().getNom());
                                   }
                                   clientReport = safe(reservationReportee.getIdClient());
                               }
                               if (!firstReported) {
                                   out.print(", ");
                               }
                               out.print("R" + idReport + " (" + paxReportes + "p, " + hotelReport + ", " + clientReport + ")");
                               firstReported = false;
                           }
                        %>
                    </div>
                <% } %>

                <% if (reservationsGroupe.isEmpty() && reportsEnEntreeIds.isEmpty()) { %>
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
                        <%
                           Set<Integer> reservationIdsAffichees = new LinkedHashSet<Integer>();
                           reservationIdsAffichees.addAll(reservationsGroupeIds);
                           reservationIdsAffichees.addAll(reportsEnEntreeIds);

                           List<Integer> reservationIdsAfficheesTriees = new ArrayList<Integer>(reservationIdsAffichees);
                           final Map<Integer, Integer> passagersDemandesRef = passagersDemandesParReservation;
                           final Map<Integer, Integer> reportsEnEntreeRef = reportsEnEntreeParReservation;
                           Collections.sort(reservationIdsAfficheesTriees, new java.util.Comparator<Integer>() {
                               @Override
                               public int compare(Integer id1, Integer id2) {
                                   int p1 = reportsEnEntreeRef.containsKey(id1)
                                           ? Math.max(0, reportsEnEntreeRef.get(id1))
                                           : Math.max(0, passagersDemandesRef.getOrDefault(id1, 0));
                                   int p2 = reportsEnEntreeRef.containsKey(id2)
                                           ? Math.max(0, reportsEnEntreeRef.get(id2))
                                           : Math.max(0, passagersDemandesRef.getOrDefault(id2, 0));

                                   if (p1 != p2) {
                                       return Integer.compare(p2, p1);
                                   }
                                   return Integer.compare(id1, id2);
                               }
                           });

                           for (Integer idReservationAffichee : reservationIdsAfficheesTriees) {
                               if (idReservationAffichee == null || idReservationAffichee <= 0) {
                                   continue;
                               }
                               Reservation reservation = reservationsParId.get(idReservationAffichee);
                               boolean estReportEntree = reportsEnEntreeIds.contains(idReservationAffichee);
                               boolean assignee = reservationsAssigneesLot.contains(idReservationAffichee);
                               int passagersAffiches = estReportEntree
                                       ? Math.max(0, reportsEnEntreeParReservation.getOrDefault(idReservationAffichee, 0))
                                       : Math.max(0, passagersDemandesParReservation.getOrDefault(idReservationAffichee,
                                               reservation != null ? reservation.getNbPassager() : 0));
                               String etat = estReportEntree
                                       ? (assignee ? "Reportee (entree groupe) - Assignee" : "Reportee (entree groupe) - Non assignee")
                                       : (assignee ? "Assignee" : "Reportee / Non assignee");
                        %>
                            <tr>
                                <td><%= idReservationAffichee %></td>
                                <td><%= (reservation != null && reservation.getHotel() != null) ? safe(reservation.getHotel().getNom()) : "-" %></td>
                                <td><%= passagersAffiches %></td>
                                <td><%= etat %></td>
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
                                <% if (assignationsTrajet == null || assignationsTrajet.isEmpty()) { %>
                                    Aucune reservation rattachee.
                                <% } else {
                                       Map<Integer, Integer> passagersParReservationTrajet = new LinkedHashMap<Integer, Integer>();
                                       Map<Integer, Reservation> reservationParIdTrajet = new LinkedHashMap<Integer, Reservation>();
                                       for (Assignation a : assignationsTrajet) {
                                           if (a == null) {
                                               continue;
                                           }
                                           Reservation r = a.getReservation();
                                           int idReservation = r != null ? r.getIdReservation() : a.getIdReservation();
                                           if (idReservation <= 0) {
                                               continue;
                                           }

                                           int passagersAssignesTrajet = a.getQuantitePassagersAssignes();
                                           if (passagersAssignesTrajet <= 0 && r != null) {
                                               passagersAssignesTrajet = Math.max(0, r.getNbPassager());
                                           }

                                           passagersParReservationTrajet.put(
                                                   idReservation,
                                                   passagersParReservationTrajet.getOrDefault(idReservation, 0) + Math.max(0, passagersAssignesTrajet)
                                           );
                                           if (r != null) {
                                               reservationParIdTrajet.putIfAbsent(idReservation, r);
                                           }
                                       }

                                       List<String> resumeReservationsTrajet = new ArrayList<String>();
                                       for (Map.Entry<Integer, Integer> e : passagersParReservationTrajet.entrySet()) {
                                           Integer idRes = e.getKey();
                                           int pax = Math.max(0, e.getValue());
                                           Reservation r = reservationParIdTrajet.get(idRes);
                                           String client = (r != null && r.getIdClient() != null) ? String.valueOf(r.getIdClient()) : null;
                                           String libelle = "R" + idRes + " (" + pax + "p)";
                                           if (client != null && !client.trim().isEmpty()) {
                                               libelle += " - " + client;
                                           }
                                           resumeReservationsTrajet.add(libelle);
                                       }
                                %>
                                    <%= safe(String.join(" ; ", resumeReservationsTrajet)) %>
                                <% } %>
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


    <section class="card">
        <h2>Reservations par vehicule</h2>
        <c:if test="${empty reservationsParVehicule}">
            <div class="alert warning">Aucune réservation n'a été faite par un véhicule.</div>
        </c:if>
        <c:if test="${not empty reservationsParVehicule}">
            <table>
                <thead>
                <tr>
                    <th>Véhicule</th>
                    <th>Réservations (ID)</th>
                    <th>Clients</th>
                </tr>
                </thead>
                <tbody>
                <% Map<Integer, List<Reservation>> reservationsParVehicule = (Map<Integer, List<Reservation>>) request.getAttribute("reservationsParVehicule");
                   Set<Integer> vehiculesAffiches = reservationsParVehicule != null ? reservationsParVehicule.keySet() : new LinkedHashSet<>();
                   for (Integer idVehicule : vehiculesAffiches) {
                       List<Reservation> reservations = reservationsParVehicule.get(idVehicule);
                       if (reservations == null || reservations.isEmpty()) continue;
                       StringBuilder ids = new StringBuilder();
                       StringBuilder clients = new StringBuilder();
                       for (int i = 0; i < reservations.size(); i++) {
                           Reservation r = reservations.get(i);
                           if (r == null) continue;
                           if (i > 0) { ids.append(", "); clients.append(", "); }
                           ids.append("R").append(r.getIdReservation());
                           clients.append(r.getIdClient() != null ? r.getIdClient() : "-");
                       }
                %>
                    <tr>
                        <td>V<%= idVehicule %></td>
                        <td><%= ids.toString() %></td>
                        <td><%= clients.toString() %></td>
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
