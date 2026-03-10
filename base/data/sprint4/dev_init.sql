-- 2026-03-10 : Initialisation des donnees de test pour sprint4 (dev)

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
('Vm', '30', 'km', 'Integer');

-- Distances (airport -> hotels and between hotels)
INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 18),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 25),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 15),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH'), 5),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 20),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 8),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 20);

-- Reservations : same date, multiple vols/times to test grouping and routing
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-20 09:30:00', 'CLI-001', 11, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-03-20 09:30:00', 'CLI-002', 5, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-03-20 09:30:00', 'CLI-003', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS')),
('2026-03-20 14:00:00', 'CLI-004', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN')),
('2026-03-20 14:00:00', 'CLI-005', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PCH')),
('2026-03-20 16:45:00', 'CLI-006', 7, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-03-20 16:45:00', 'CLI-007', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-03-20 18:00:00', 'CLI-008', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'));

