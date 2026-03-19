--schema dev
BEGIN;

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES 
('temps_attente', '30', 'minutes', 'Integer'), 
('Vm', '30', 'km/h', 'Integer');

INSERT INTO dev.Hotel (code, nom) SELECT 'AER', 'Aeroport' WHERE NOT EXISTS (SELECT 1 FROM dev.Hotel WHERE code = 'AER');
INSERT INTO dev.Hotel (code, nom) SELECT 'CLB', 'Colbert' WHERE NOT EXISTS (SELECT 1 FROM dev.Hotel WHERE code = 'CLB');
INSERT INTO dev.Hotel (code, nom) SELECT 'NOV', 'Novotel' WHERE NOT EXISTS (SELECT 1 FROM dev.Hotel WHERE code = 'NOV');
INSERT INTO dev.Hotel (code, nom) SELECT 'HBS', 'Hibiscus' WHERE NOT EXISTS (SELECT 1 FROM dev.Hotel WHERE code = 'HBS');
INSERT INTO dev.Hotel (code, nom) SELECT 'PAN', 'Panache' WHERE NOT EXISTS (SELECT 1 FROM dev.Hotel WHERE code = 'PAN');

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), 12
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 15
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 20
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 10
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'AER'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), 8
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 9
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 5
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), 7
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 6
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV'))
);

INSERT INTO dev.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'), (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'), 4
WHERE NOT EXISTS (
    SELECT 1 FROM dev.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS'))
);

-- Jeu A : v1=8, v2=3 ; r1=6, r2=4, r3=3
-- Jeu B : v1=8, v2=3 ; r1=6, r4=5, r2=4, r3=3
-- Jeu C : r=23 avec vehicules 13, 8, 6

INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('S7-A-V1', 8, 'D'),
('S7-A-V2', 3, 'E'),
('S7-B-V1', 8, 'D'),
('S7-B-V2', 3, 'E'),
('S7-C-V1', 13, 'D'),
('S7-C-V2', 8, 'E'),
('S7-C-V3', 6, 'E');

INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
-- Cas A (date 2026-06-01)
('2026-06-01 04:20:00', 'S7-A-R1', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-06-01 04:25:00', 'S7-A-R2', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-06-01 04:28:00', 'S7-A-R3', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS')),

-- Cas B (date 2026-06-02)
('2026-06-02 05:00:00', 'S7-B-R1', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB')),
('2026-06-02 05:03:00', 'S7-B-R4', 5, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'PAN')),
('2026-06-02 05:06:00', 'S7-B-R2', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'NOV')),
('2026-06-02 05:09:00', 'S7-B-R3', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'HBS')),

-- Cas C (date 2026-06-03)
('2026-06-03 06:00:00', 'S7-C-R23', 23, (SELECT Id_Hotel FROM dev.Hotel WHERE code = 'CLB'));

COMMIT;




--schema staging
BEGIN;

INSERT INTO staging.Parametre (code, valeur, unite, typeValeur) VALUES 
('temps_attente', '30', 'minutes', 'Integer'), 
('Vm', '30', 'km/h', 'Integer');

INSERT INTO staging.Hotel (code, nom) SELECT 'AER', 'Aeroport' WHERE NOT EXISTS (SELECT 1 FROM staging.Hotel WHERE code = 'AER');
INSERT INTO staging.Hotel (code, nom) SELECT 'CLB', 'Colbert' WHERE NOT EXISTS (SELECT 1 FROM staging.Hotel WHERE code = 'CLB');
INSERT INTO staging.Hotel (code, nom) SELECT 'NOV', 'Novotel' WHERE NOT EXISTS (SELECT 1 FROM staging.Hotel WHERE code = 'NOV');
INSERT INTO staging.Hotel (code, nom) SELECT 'HBS', 'Hibiscus' WHERE NOT EXISTS (SELECT 1 FROM staging.Hotel WHERE code = 'HBS');
INSERT INTO staging.Hotel (code, nom) SELECT 'PAN', 'Panache' WHERE NOT EXISTS (SELECT 1 FROM staging.Hotel WHERE code = 'PAN');

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), 12
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), 15
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 20
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 10
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'AER'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), 8
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 9
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 5
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), 7
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 6
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV'))
);

INSERT INTO staging.Distance (from_hotel, to_hotel, km)
SELECT (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'), (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'), 4
WHERE NOT EXISTS (
    SELECT 1 FROM staging.Distance
    WHERE (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN'))
       OR (from_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN') AND to_hotel = (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS'))
);

-- Jeu A : v1=8, v2=3 ; r1=6, r2=4, r3=3
-- Jeu B : v1=8, v2=3 ; r1=6, r4=5, r2=4, r3=3
-- Jeu C : r=23 avec vehicules 13, 8, 6

INSERT INTO staging.Vehicule (Reference, nbPlace, TypeVehicule) VALUES
('S7-A-V1', 8, 'D'),
('S7-A-V2', 3, 'E'),
('S7-B-V1', 8, 'D'),
('S7-B-V2', 3, 'E'),
('S7-C-V1', 13, 'D'),
('S7-C-V2', 8, 'E'),
('S7-C-V3', 6, 'E');

INSERT INTO staging.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
-- Cas A (date 2026-06-01)
('2026-06-01 04:20:00', 'S7-A-R1', 6, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB')),
('2026-06-01 04:25:00', 'S7-A-R2', 4, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV')),
('2026-06-01 04:28:00', 'S7-A-R3', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS')),

-- Cas B (date 2026-06-02)
('2026-06-02 05:00:00', 'S7-B-R1', 6, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB')),
('2026-06-02 05:03:00', 'S7-B-R4', 5, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'PAN')),
('2026-06-02 05:06:00', 'S7-B-R2', 4, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'NOV')),
('2026-06-02 05:09:00', 'S7-B-R3', 3, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'HBS')),

-- Cas C (date 2026-06-03)
('2026-06-03 06:00:00', 'S7-C-R23', 23, (SELECT Id_Hotel FROM staging.Hotel WHERE code = 'CLB'));

COMMIT;
