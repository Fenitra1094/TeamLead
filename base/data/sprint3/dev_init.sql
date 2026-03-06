-- 2026-03-04 : Initialisation des donnees de test pour dev

-- =========================
-- Schema dev
-- =========================

INSERT INTO dev.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('CLB', 'Colbert'),
('NOV', 'Novotel'),
('IBS', 'Ibis');

INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-05 08:00:00', 'CLI-001', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-03-05 09:30:00', 'CLI-002', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-03-06 10:15:00', 'CLI-004', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'));


INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('VH-D-001', 4, 'D'),
('VH-H-001', 7, 'H'),
('VH-ES-001', 5, 'ES'),
('VH-EL-001', 9, 'EL');

INSERT INTO dev.token_expiration (token, expiration) VALUES
('550e8400-e29b-41d4-a716-446655440000', NOW() + INTERVAL '24 hours'),
('6ba7b810-9dad-11d1-80b4-00c04fd430c8', NOW() + INTERVAL '48 hours');

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '30', 'km', 'Integer');


INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 18),
((SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'IBS'), 25);
