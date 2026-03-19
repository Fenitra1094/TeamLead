-- Sprint 7 - Reset + donnees de test completes (schema staging)
-- Objectif: couvrir les cas de split, tri DESC reservations, selection vehicule "plus proche",
-- remplissage d'un vehicule entame, report au groupe suivant, tie-break diesel et tie-break random.

BEGIN;

-- 0) Securite schema
CREATE SCHEMA IF NOT EXISTS staging;

-- 1) Securite Sprint 7: colonne de split sur assignation (au cas ou migration faite sur staging uniquement)
ALTER TABLE IF EXISTS staging.Assignation
    ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

-- 2) Reset donnees
TRUNCATE TABLE staging.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Hotel RESTART IDENTITY CASCADE;

-- 3) Donnees de reference
INSERT INTO staging.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('PAN', 'Panache'),
('NOV', 'Novotel'),
('HBS', 'Hibiscus'),
('CLB', 'Colbert');

INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '30', 'km', 'Integer'),
('temps_attente', '30', 'minutes', 'Integer');

INSERT INTO staging.token_expiration (token, expiration) VALUES
('11111111-1111-1111-1111-111111111111', NOW() + INTERVAL '24 hours'),
('22222222-2222-2222-2222-222222222222', NOW() + INTERVAL '48 hours');

-- Distances: graphe dirige complet entre hotels utilises (AER, PAN, NOV, HBS, CLB)
INSERT INTO staging.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), 15),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), 18),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), 25),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), 12),

((SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), 15),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), 3),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), 10),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), 20),

((SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), 18),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), 3),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), 7),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), 16),

((SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), 25),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), 10),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), 7),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), 8),

((SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='AER'), 12),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN'), 20),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV'), 16),
((SELECT Id_Hotel FROM staging.Hotel WHERE code='CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'), 8);

INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('S1-TEST-03D', 10, 'D');

INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-04-15 04:11:00', 'S7-A-CLI-011', 8, (SELECT Id_Hotel FROM staging.Hotel WHERE code='NOV')),
('2026-04-15 04:07:00', 'S7-A-CLI-023', 1, (SELECT Id_Hotel FROM staging.Hotel WHERE code='PAN')),
('2026-04-15 04:18:00', 'S7-A-CLI-005', 3,  (SELECT Id_Hotel FROM staging.Hotel WHERE code='HBS'));
