-- 2026-03-04 : Initialisation des donnees de test pour staging

-- =========================
-- Schema staging
-- =========================

INSERT INTO staging.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('SOL', 'Hotel Soleil'),
('ATL', 'Hotel Atlas'),
('RIV', 'Hotel Rivage');

INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-03-05 08:30:00', 'STG-001', 2, 2),
('2026-03-05 12:00:00', 'STG-002', 5, 3),
('2026-03-06 09:00:00', 'STG-003', 3, 4);


INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('VH-D-001', 4, 'D'),
('VH-H-001', 7, 'H'),
('VH-ES-001', 5, 'ES'),
('VH-EL-001', 9, 'EL');

INSERT INTO staging.token_expiration (token, expiration) VALUES
('550e8400-e29b-41d4-a716-446655440000', NOW() + INTERVAL '24 hours'),
('6ba7b810-9dad-11d1-80b4-00c04fd430c8', NOW() + INTERVAL '48 hours');

INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '30', 'km', 'Integer');

INSERT INTO staging.Distance (from_hotel, to_hotel, km) VALUES
(1, 2, 12),
(1, 3, 18),
(1, 4, 25),
(2, 3, 7),
(2, 4, 16),
(3, 4, 10);
