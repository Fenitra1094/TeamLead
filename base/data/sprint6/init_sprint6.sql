-- 2026-03-17 : Initialisation des donnees de test pour sprint6 (staging)

-- Hotels requis pour les scenarios sprint6
INSERT INTO staging.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('CLB', 'Colbert'),
('NOV', 'Novotel'),
('HBS', 'Hibiscus'),
('PAN', 'Panache');

-- Vehicules (3)
-- V1 : 8 places, Diesel
-- V2 : 4 places, Essence
-- V3 : 12 places, Diesel
INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('V1', 8, 'D'),
('V2', 4, 'E'),
('V3', 12, 'D');

-- Parametres
INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES
('temps_attente', '30', 'minutes', 'Integer'),
('Vm', '30', 'km/h', 'Integer');

-- Distances fictives coherentes
INSERT INTO staging.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), 12),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), 15),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 20),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 10),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), 8),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 9),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 5),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 7),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 6),
((SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 4);

-- Reservations (5) pour date 2026-03-20
INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-20 04:20:00', 'R1-CLI-S6', 7, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB')),
('2026-03-20 04:45:00', 'R2-CLI-S6', 4, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV')),
('2026-03-20 04:45:00', 'R3-CLI-S6', 2, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS')),
('2026-03-20 04:47:00', 'R4-CLI-S6', 1, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN')),
('2026-03-20 05:40:00', 'R5-CLI-S6', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'));
