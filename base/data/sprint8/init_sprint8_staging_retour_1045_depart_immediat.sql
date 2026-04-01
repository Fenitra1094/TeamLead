-- Sprint 8 - STAGING
-- Scenario cible demande:
-- - il existe deja des trajets avant 10:45
-- - des reservations non assignees existent avant 10:45
-- - un vehicule de retour a 10:45 avec capacite 7
-- - reservation non assignee prioritaire = 12 passagers
-- - capacite (7) <= non assignee (12) => vehicule complet et depart immediat a 10:45

BEGIN;

CREATE SCHEMA IF NOT EXISTS staging;

-- Compatibilite colonnes Sprint 7/8
ALTER TABLE IF EXISTS staging.Assignation
    ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

ALTER TABLE IF EXISTS staging.Assignation
    ADD COLUMN IF NOT EXISTS fromNonAssigneePrecedent BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE IF EXISTS staging.Vehicule
    ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';

-- Reinitialisation complete du schema staging (dans l'ordre des dependances)
TRUNCATE TABLE staging.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Hotel RESTART IDENTITY CASCADE;

-- Parametres metier
INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES
('temps_attente', '30', 'minutes', 'Integer'),
('Vm', '30', 'km/h', 'Integer');

-- Hotels
INSERT INTO staging.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('H1', 'Hotel 1'),
('H2', 'Hotel 2'),
('H3', 'Hotel 3'),
('H4', 'Hotel 4');

-- Distances minimales necessaires aux calculs de route
INSERT INTO staging.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H1'), 12),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H2'), 15),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H3'), 18),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H4'), 21),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H2'), 7),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H2'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H3'), 6),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H3'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H4'), 5),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H3'), 9),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H4'), 11),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='H2'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='H4'), 8);

-- Vehicules:
-- - S8-RET-07: vehicule pivot retourne a 10:45 (cas depart immediat attendu)
-- - S8-RET-12: autre vehicule de retour apres, pour le tri/candidature
-- - S8-BLK-* : indisponibles avant 10:45 pour forcer des non assignees en amont
INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule, HeureDisponibilite) VALUES
('S8-RET-07', 7, 'D', '00:00:00'),
('S8-RET-12', 12, 'E', '00:00:00'),
('S8-BLK-15', 15, 'D', '23:00:00'),
('S8-BLK-10', 10, 'E', '23:00:00');

-- Trajets historiques de la journee (precondition "vehicules de retour")
-- Le pivot doit revenir a 10:45 pour declencher votre regle
INSERT INTO staging.Trajet (id_vehicule, date_heure_depart, date_heure_retour, date_assignation) VALUES
((SELECT id_vehicule FROM staging.Vehicule WHERE Reference='S8-RET-07'), '2026-06-10 09:05:00', '2026-06-10 10:45:00', '2026-06-10'),
((SELECT id_vehicule FROM staging.Vehicule WHERE Reference='S8-RET-12'), '2026-06-10 09:15:00', '2026-06-10 10:50:00', '2026-06-10');

-- Reservations avant 10:45 (doivent exister avant le retour du vehicule pivot)
-- On inclut une non assignee majeure a 12 passagers pour valider explicitement 7 <= 12
INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-06-10 09:30:00', 'S8-OLD-NA-004', 4, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H1')),
('2026-06-10 09:34:00', 'S8-OLD-NA-003', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H2')),
('2026-06-10 10:10:00', 'S8-OLD-NA-012', 12, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H3')),
('2026-06-10 10:18:00', 'S8-OLD-NA-005', 5, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H4'));

-- Quelques reservations apres 10:45 pour continuer la simulation si besoin
INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-06-10 10:58:00', 'S8-NEW-006', 6, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H2')),
('2026-06-10 11:05:00', 'S8-NEW-004', 4, (SELECT Id_Hotel FROM staging.Hotel WHERE code='H1'));

COMMIT;

-- =====================================================================
-- Verification apres appel d'assignation sur la date 2026-06-10
-- =====================================================================
-- 1) Controle du cas demande: vehicule pivot S8-RET-07
-- Attendu:
--   - date_heure_depart = 2026-06-10 10:45:00
--   - quantitePassagersAssignes = 7 sur reservation S8-OLD-NA-012 (12 passagers)
--   - fromNonAssigneePrecedent = true
--
-- SELECT v.reference,
--        a.id_assignation,
--        r.idclient,
--        r.nbpassager AS nb_reservation,
--        a.quantitepassagersassignes AS nb_assignes,
--        a.fromnonassigneeprecedent,
--        a.date_heure_depart,
--        a.date_heure_retour
-- FROM staging.assignation a
-- JOIN staging.vehicule v ON v.id_vehicule = a.id_vehicule
-- JOIN staging.reservation r ON r.id_reservation = a.id_reservation
-- WHERE v.reference = 'S8-RET-07'
-- ORDER BY a.id_assignation;
--
-- 2) Verification explicite de l'heure de depart immediate
--
-- SELECT CASE
--          WHEN EXISTS (
--            SELECT 1
--            FROM staging.assignation a
--            JOIN staging.vehicule v ON v.id_vehicule = a.id_vehicule
--            WHERE v.reference = 'S8-RET-07'
--              AND a.date_heure_depart = TIMESTAMP '2026-06-10 10:45:00'
--          ) THEN 'OK_DEPART_IMMEDIAT_10H45'
--          ELSE 'KO_DEPART_IMMEDIAT'
--        END AS resultat;
--
-- 3) Reste non assigne de la reservation 12 passagers (attendu: 5)
-- (suivant le flux exact, le reste peut etre ensuite repris par un autre vehicule)
--
-- SELECT r.idclient,
--        r.nbpassager AS nb_initial,
--        COALESCE(SUM(a.quantitepassagersassignes), 0) AS nb_total_assignes,
--        r.nbpassager - COALESCE(SUM(a.quantitepassagersassignes), 0) AS nb_restant
-- FROM staging.reservation r
-- LEFT JOIN staging.assignation a ON a.id_reservation = r.id_reservation
-- WHERE r.idclient = 'S8-OLD-NA-012'
-- GROUP BY r.idclient, r.nbpassager;