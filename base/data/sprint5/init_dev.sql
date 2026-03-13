-- 2026-03-13 : Initialisation des donnees de test pour sprint5 (dev)

INSERT INTO dev.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('PAN', 'Panache'),
('PCH', 'Pachniko'),
('CLB', 'Colbert'),
('NOV', 'Novotel'),
('IBS', 'Ibis'),
('HBS', 'Hibiscus');

INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('VH-D-007', 7, 'D'),
('VH-D-011', 11, 'D'),
('VH-E-012', 12, 'E'),
('VH-E-018', 18, 'E');

INSERT INTO dev.token_expiration (token, expiration) VALUES
('550e8400-e29b-41d4-a716-446655440001', NOW() + INTERVAL '24 hours'),
('6ba7b810-9dad-11d1-80b4-00c04fd430c9', NOW() + INTERVAL '48 hours');

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '30', 'km', 'Integer'),
('temps_attente', '30', 'minutes', 'Integer');

-- Distances (airport -> hotels and between hotels)
INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 18),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 25),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 21),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 5),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 20),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 3),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 14),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 10),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 8),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 11),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 16),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 5),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 7),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 9),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 6),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 4),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 8);

-- Reservations de test Sprint5 (2026-03-20)
-- Fenetre groupe: 04:20 + 30 min = 04:50
-- Groupe attendu: [04:20, 04:45, 04:45, 04:47]
-- heureDepartGroupe attendue: 04:47 (max)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-20 04:20:00', 'CLI-S5-001', 7, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-03-20 04:45:00', 'CLI-S5-002', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-03-20 04:45:00', 'CLI-S5-003', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS')),
('2026-03-20 04:47:00', 'CLI-S5-004', 1, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'));
