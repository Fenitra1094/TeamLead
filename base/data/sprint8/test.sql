BEGIN;

-- Securite schema
CREATE SCHEMA IF NOT EXISTS staging;

-- Securite sprint 7 (si migration pas encore appliquee)
ALTER TABLE IF EXISTS staging.Assignation
ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

ALTER TABLE IF EXISTS staging.Vehicule
ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';

-- Reset complet des donnees fonctionnelles
TRUNCATE TABLE staging.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE staging.Hotel RESTART IDENTITY CASCADE;

-- Hotels du scenario
INSERT INTO staging.Hotel (code, nom) VALUES
('AER', 'aeroport'),
('H1', 'hotel1'),
('H2', 'hotel2');

-- Parametres du scenario
-- temps_attente est utilise dans votre code
-- Vm est conserve pour compatibilite avec vos scripts existants
INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES
('temps_attente', '30', 'minutes', 'Integer'),
('Vm', '50', 'km/h', 'Integer'),
('vitesse_moyenne', '50', 'km/h', 'Integer');

-- Vehicules du scenario
-- TypeVehicule: D = diesel, E = essence
INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule, HeureDisponibilite) VALUES
('vehicule1', 5, 'D', '09:00:00'),
('vehicule2', 5, 'E', '09:00:00'),
('vehicule3', 12, 'D', '00:00:00'),
('vehicule4', 9, 'D', '09:00:00'),
('vehicule5', 12, 'E', '13:00:00');

-- Distances du scenario
INSERT INTO staging.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'),
(SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1'), 90),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'),
(SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H2'), 35),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1'),
(SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H2'), 60);

-- Reservations du scenario (19/03/2026)
INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-19 09:00:00', 'Client1', 7, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
('2026-03-19 08:00:00', 'Client2', 20, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H2')),
('2026-03-19 09:10:00', 'Client3', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
('2026-03-19 09:15:00', 'Client4', 10, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
('2026-03-19 09:20:00', 'Client5', 5, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
('2026-03-19 13:30:00', 'Client6', 12, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1'));

-- Optionnel: tokens de test
INSERT INTO staging.token_expiration (token, expiration) VALUES
('11111111-1111-1111-1111-111111111111', NOW() + INTERVAL '24 hours'),
('22222222-2222-2222-2222-222222222222', NOW() + INTERVAL '48 hours');

COMMIT;


9h - 9h30 
depart 9h24

vehicule :
INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule, HeureDisponibilite) VALUES
V1 => ('vehicule3', 12, 'D', '00:00:00'), * => retour 13:00
- V2 => ('vehicule4', 9, 'D', '09:00:00'), * => retour 13:06
- V3 => ('vehicule1', 5, 'D', '09:00:00'), => retour 9h24 => retour 13:00
-   V4 => ('vehicule2', 5, 'E', '09:00:00'), => retour 13:00

en ordre :
- RN1 => ('2026-03-19 08:00:00', 'Client2', 8, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H2')), NA *
R2 => ('2026-03-19 09:15:00', 'Client4', 10, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')), *
- R3 => ('2026-03-19 09:00:00', 'Client1', 7, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
- R4 => ('2026-03-19 09:20:00', 'Client5', 5, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')),
- R5 => ('2026-03-19 09:10:00', 'Client3', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1')), *

RN1 8 => V2 9 => 1 places => R5 1
V3 5 => R5 2 => 3 places => R4 3
V4  5 => R4 2 => 3 places => R3 3
V1 12 => R3 4 => 8 places => R2 8
R2 => 2 places restantes non assignées 

R2 8 => V4 9 => 1 places => R3 1

V1 5 => R3 2 => 3 places => R5 3
V2  5 => R5 2 => 3 places => R1 3
V3 12 => R1 4 => 8 places => R4 8
R4 => 2 places restantes non assignées 



9h20 depart




('2026-03-19 13:30:00', 'Client6', 12, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'H1'));
