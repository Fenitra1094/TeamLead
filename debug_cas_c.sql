-- DEBUG CAS C: Vérifier les assignations et trajets pour 2026-04-12

-- 1) Vérifier les assignations créées
SELECT 
    'ASSIGNATIONS' as category,
    a.id_assignation,
    a.id_reservation,
    r.idClient,
    r.nbPassager,
    a.quantitePassagersAssignes,
    v.Reference,
    a.date_heure_depart,
    t.id_trajet
FROM dev.Assignation a
LEFT JOIN dev.Reservation r ON a.id_reservation = r.id_reservation
LEFT JOIN dev.Vehicule v ON a.id_vehicule = v.id_vehicule
LEFT JOIN dev.Trajet t ON a.id_trajet = t.id_trajet
WHERE DATE(a.date_heure_depart) = '2026-04-12'
ORDER BY a.date_heure_depart, r.nbPassager DESC, a.id_vehicule;

-- 2) Total passagers par réservation
SELECT 
    'TOTAL_PAR_RESERVATION' as category,
    r.id_reservation,
    r.idClient,
    r.nbPassager as demande,
    COALESCE(SUM(a.quantitePassagersAssignes), 0) as assigne,
    r.nbPassager - COALESCE(SUM(a.quantitePassagersAssignes), 0) as restant
FROM dev.Reservation r
LEFT JOIN dev.Assignation a ON r.id_reservation = a.id_reservation
WHERE DATE(r.DateHeureArrive) = '2026-04-12'
GROUP BY r.id_reservation, r.idClient, r.nbPassager
ORDER BY r.nbPassager DESC;

-- 3) Trajets pour cette date
SELECT 
    'TRAJETS' as category,
    t.id_trajet,
    v.Reference,
    t.date_heure_depart,
    t.date_heure_retour,
    COUNT(a.id_assignation) as nb_assignations
FROM dev.Trajet t
LEFT JOIN dev.Vehicule v ON t.id_vehicule = v.id_vehicule
LEFT JOIN dev.Assignation a ON t.id_trajet = a.id_trajet
WHERE DATE(t.date_assignation) = '2026-04-12'
GROUP BY t.id_trajet, v.Reference, t.date_heure_depart, t.date_heure_retour
ORDER BY t.date_heure_depart, v.Reference;
