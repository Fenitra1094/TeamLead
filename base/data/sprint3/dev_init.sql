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
('2026-03-05 08:00:00', 'CLI-001', 2, 5);
('2026-03-05 09:30:00', 'CLI-002', 4, 6),
('2026-03-05 11:00:00', 'CLI-003', 6, 4),
('2026-03-06 10:15:00', 'CLI-004', 3, 7);

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
(4, 5, 12),
(1, 3, 18),
(1, 4, 25),
(2, 3, 7),
(2, 4, 16),
(3, 4, 10);
