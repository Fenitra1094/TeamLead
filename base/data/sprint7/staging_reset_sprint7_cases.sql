-- Sprint 7 - Reset + donnees de test completes (schema dev)
-- Objectif: couvrir les cas de split, tri DESC reservations, selection vehicule "plus proche",
-- remplissage d'un vehicule entame, report au groupe suivant, tie-break diesel et tie-break random.

BEGIN;

-- 0) Securite schema
CREATE SCHEMA IF NOT EXISTS dev;

-- 1) Securite Sprint 7: colonne de split sur assignation (au cas ou migration faite sur dev uniquement)
ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

-- 2) Reset donnees
TRUNCATE TABLE dev.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Hotel RESTART IDENTITY CASCADE;

-- 3) Donnees de reference
INSERT INTO dev.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('PAN', 'Panache'),
('NOV', 'Novotel'),
('HBS', 'Hibiscus'),
('CLB', 'Colbert');

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '30', 'km', 'Integer'),
('temps_attente', '30', 'minutes', 'Integer');

INSERT INTO dev.token_expiration (token, expiration) VALUES
('11111111-1111-1111-1111-111111111111', NOW() + INTERVAL '24 hours'),
('22222222-2222-2222-2222-222222222222', NOW() + INTERVAL '48 hours');

-- Distances: graphe dirige complet entre hotels utilises (AER, PAN, NOV, HBS, CLB)
INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), 18),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), 25),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), 12),

((SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), 3),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), 10),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), 20),

((SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), 18),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), 3),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), 7),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), 16),

((SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), 25),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), 10),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), 7),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), 8),

((SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN'), 20),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'), 16),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'), 8);

-- Flotte Sprint 7 (volontairement variee)
-- Notes:
-- - 8D + 8E + 8E-B pour tester diesel puis random en cas d'egalite stricte.
-- - Pas de vehicule 5 places pour forcer le cas restant=5 vers vehicule 6 (plus proche que 8).
INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('S7-VH-18E', 18, 'E'),
('S7-VH-13D', 13, 'D'),
('S7-VH-12E', 12, 'E'),
('S7-VH-10D', 10, 'D'),
('S7-VH-08D', 8, 'D'),
('S7-VH-08E-A', 8, 'E'),
('S7-VH-08E-B', 8, 'E'),
('S7-VH-06E', 6, 'E'),
('S7-VH-03D', 3, 'D');


INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('S1-TEST-03D', 10, 'D');



-- 4) Cas de test reservations
-- Important: executer l'assignation date par date pour observer chaque scenario.

-- =====================================================================
-- CAS A - Split d'une grosse reservation + choix "plus proche" pour le reste
-- Date: 2026-04-10 (meme regroupement 04:00-04:30)
-- Couvre:
--   * split d'une reservation > capacite max vehicule
--   * tri reservations DESC (insertion volontairement non triee)
--   * sur reste=5, priorite vehicule 6 (plus proche) plutot que 8
-- =====================================================================
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-10 04:11:00', 'S7-A-CLI-011', 11, (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV')),
('2026-04-10 04:07:00', 'S7-A-CLI-023', 23, (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN')),
('2026-04-10 04:18:00', 'S7-A-CLI-005', 5,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'));


INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-15 04:11:00', 'S7-A-CLI-011', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV')),
('2026-04-15 04:07:00', 'S7-A-CLI-023', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN')),
('2026-04-15 04:18:00', 'S7-A-CLI-005', 3,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'));



-- =====================================================================
-- CAS B - Remplissage d'un vehicule entame avec reservations "du bas"
-- Date: 2026-04-11 (meme regroupement 04:00-04:30)
-- Couvre:
--   * reservation prioritaire la plus grande
--   * vehicule entame puis remplissage avec reservations suivantes compatibles
--   * verification "deja traitee"
-- =====================================================================
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-11 04:06:00', 'S7-B-CLI-002', 2,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB')),
('2026-04-11 04:04:00', 'S7-B-CLI-016', 16, (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN')),
('2026-04-11 04:16:00', 'S7-B-CLI-009', 9,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV')),
('2026-04-11 04:21:00', 'S7-B-CLI-004', 4,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'));

-- =====================================================================
-- CAS C - Report au regroupement suivant (meme date)
-- Date: 2026-04-12
-- Groupe 1: 04:00-04:30 -> demande volontairement > capacite totale
-- Groupe 2: 11:00-11:30 -> doit reprendre les reports + nouvelles reservations
-- =====================================================================
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-12 04:05:00', 'S7-C-CLI-095', 95, (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN')),
('2026-04-12 11:10:00', 'S7-C-CLI-003', 3,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV')),
('2026-04-12 11:14:00', 'S7-C-CLI-006', 6,  (SELECT Id_Hotel FROM dev.Hotel WHERE code='CLB'));

-- =====================================================================
-- CAS D - Tie-break diesel (capacites identiques, ecart identique)
-- Date: 2026-04-13
-- Reservation 8 passagers: candidats 8D / 8E / 8E-B -> diesel prioritaire
-- =====================================================================
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-13 04:03:00', 'S7-D-CLI-008', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='HBS'));

-- =====================================================================
-- CAS E - Tie-break random sur egalite stricte
-- Date: 2026-04-14
-- Idee:
--   1) une reservation 8 consomme 8D (diesel)
--   2) seconde reservation 8: choix aleatoire entre 8E-A et 8E-B
-- =====================================================================
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-14 04:01:00', 'S7-E-CLI-008-A', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='PAN')),
('2026-04-14 04:12:00', 'S7-E-CLI-008-B', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='NOV'));

COMMIT;

-- =====================================================================
-- REQUETES DE CONTROLE (a lancer apres chaque execution de l'algo par date)
-- =====================================================================
-- Exemple date cible: 2026-04-10
-- SELECT * FROM dev.assignation a
-- WHERE DATE(a.date_heure_depart) = '2026-04-10'
-- ORDER BY a.id_vehicule, a.id_reservation, a.id_assignation;
--
-- SELECT r.id_reservation, r.idclient, r.nbpassager
-- FROM dev.reservation r
-- WHERE DATE(r.dateheurearrive) = '2026-04-10'
-- ORDER BY r.nbpassager DESC, r.id_reservation;
--
-- SELECT t.id_trajet, t.id_vehicule, t.date_heure_depart, t.date_heure_retour, t.date_assignation
-- FROM dev.trajet t
-- WHERE t.date_assignation = '2026-04-10'
-- ORDER BY t.id_vehicule, t.id_trajet;
