-- Sprint 8 - Seed de donnees (schema dev)
-- Objectif: couvrir les 4 cas metier "vehicule revenu + priorite non assignes precedents"
--
-- Cas 1: nonAssignes=10, capacite=6 -> depart immediat
-- Cas 2: nonAssignes=3, capacite=6, nouveaux=3 dans fenetre -> depart anticipe (vehicule plein)
-- Cas 3: nonAssignes=2, capacite=6, aucun nouveau -> depart fin fenetre
-- Cas 4: chevauchement regroupement normal + vehicule revenu

BEGIN;

CREATE SCHEMA IF NOT EXISTS dev;

-- Compatibilite migrations sprint 7 / 8
ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS fromNonAssigneePrecedent BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS dateHeureDepartEffective TIMESTAMP NULL;

ALTER TABLE IF EXISTS dev.Vehicule
    ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';

-- Reset complet donnees fonctionnelles
TRUNCATE TABLE dev.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Hotel RESTART IDENTITY CASCADE;

-- Donnees de reference
INSERT INTO dev.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('H1', 'Hotel 1'),
('H2', 'Hotel 2'),
('H3', 'Hotel 3'),
('H4', 'Hotel 4');

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '50', 'km/h', 'Integer'),
('temps_attente', '30', 'minutes', 'Integer');

INSERT INTO dev.token_expiration (token, expiration) VALUES
('88888888-1111-1111-1111-111111111111', NOW() + INTERVAL '24 hours'),
('88888888-2222-2222-2222-222222222222', NOW() + INTERVAL '48 hours');

-- Distances minimales
INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), 20),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), 30),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), 40),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 50),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 15);

-- Flotte de test Sprint 8
-- V-C1..V-C4 = vehicules cibles des 4 cas (capacite 6)
-- V-BLK-* = vehicules "bloquants" indisponibles au debut pour forcer des non assignes precedents
INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule, HeureDisponibilite) VALUES
('S8-C1-V6', 6, 'D', '00:00:00'),
('S8-C2-V6', 6, 'E', '00:00:00'),
('S8-C3-V6', 6, 'D', '00:00:00'),
('S8-C4-V6', 6, 'E', '00:00:00'),
('S8-BLK-12', 12, 'D', '23:00:00'),
('S8-BLK-9', 9, 'E', '23:00:00');

-- ---------------------------------------------------------------------
-- Precondition "vehicule revenu" : trajet historique termine pour chaque cas
-- ---------------------------------------------------------------------
INSERT INTO dev.Trajet (id_vehicule, date_heure_depart, date_heure_retour, date_assignation) VALUES
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C1-V6'), '2026-05-01 06:30:00', '2026-05-01 08:00:00', '2026-05-01'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C2-V6'), '2026-05-02 08:40:00', '2026-05-02 10:00:00', '2026-05-02'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C3-V6'), '2026-05-03 09:45:00', '2026-05-03 11:00:00', '2026-05-03'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C4-V6'), '2026-05-04 10:15:00', '2026-05-04 11:45:00', '2026-05-04');

-- ---------------------------------------------------------------------
-- CAS 1: nonAssignes=10, capacite=6 -> depart immediat (heureRetourVehicule)
-- Date cible: 2026-05-01
-- ---------------------------------------------------------------------
-- Lot precedent non assigne (a forcer via absence de capacite disponible avant retour)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-05-01 07:10:00', 'S8-C1-NA-010', 10, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'));

-- ---------------------------------------------------------------------
-- CAS 2: nonAssignes=3, capacite=6, nouveaux=3 dans fenetre -> depart anticipe
-- Date cible: 2026-05-02
-- Fenetre attendue: [10:00, 10:30]
-- ---------------------------------------------------------------------
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
-- Non assignes precedents
('2026-05-02 09:05:00', 'S8-C2-NA-003', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2')),
-- Nouveaux dans fenetre de retour
('2026-05-02 10:05:00', 'S8-C2-NW-002', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2')),
('2026-05-02 10:12:00', 'S8-C2-NW-001', 1, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'));

-- ---------------------------------------------------------------------
-- CAS 3: nonAssignes=2, capacite=6, aucun nouveau -> depart fin fenetre
-- Date cible: 2026-05-03
-- Fenetre attendue: [11:00, 11:30]
-- ---------------------------------------------------------------------
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-05-03 10:10:00', 'S8-C3-NA-002', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'));

-- ---------------------------------------------------------------------
-- CAS 4: chevauchement regroupement normal + vehicule revenu
-- Date cible: 2026-05-04
-- Exemple: regroupement normal 12:00-12:30 et retour vehicule a 11:45
-- ---------------------------------------------------------------------
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
-- Non assignes precedents
('2026-05-04 11:20:00', 'S8-C4-NA-004', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4')),
-- Reservations du regroupement normal qui chevauchent la fenetre retour
('2026-05-04 12:00:00', 'S8-C4-NW-002', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1')),
('2026-05-04 12:10:00', 'S8-C4-NW-003', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2')),
('2026-05-04 12:24:00', 'S8-C4-NW-001', 1, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'));

COMMIT;

-- ---------------------------------------------------------------------
-- Requetes de verification (a executer apres /assignation/assigner?date=...)
-- ---------------------------------------------------------------------
-- 1) Synthese assignations sprint 8
-- SELECT a.id_assignation, a.id_reservation, a.id_vehicule,
--        a.quantitepassagersassignes, a.fromnonassigneeprecedent,
--        a.dateheuredeparteffective, a.date_heure_depart, a.date_heure_retour
-- FROM dev.assignation a
-- ORDER BY a.id_assignation;
--
-- 2) Reservations par date de cas
-- SELECT DATE(r.dateheurearrive) AS d, r.id_reservation, r.idclient, r.nbpassager
-- FROM dev.reservation r
-- WHERE DATE(r.dateheurearrive) BETWEEN '2026-05-01' AND '2026-05-04'
-- ORDER BY d, r.dateheurearrive, r.id_reservation;
--
-- 3) Trajets retour vehicule (precondition)
-- SELECT t.id_trajet, t.id_vehicule, t.date_heure_depart, t.date_heure_retour, t.date_assignation
-- FROM dev.trajet t
-- WHERE t.date_assignation BETWEEN '2026-05-01' AND '2026-05-04'
-- ORDER BY t.date_assignation, t.id_vehicule;
