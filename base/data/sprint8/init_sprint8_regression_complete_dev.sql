-- Sprint 8 - Pack de regression complet (schema dev)
-- Objectif: couvrir les regles demandees dans la discussion (priorites NA, retours/disponibilites, meme-heure vs ulterieur, depart immediat vs uniforme)

BEGIN;

CREATE SCHEMA IF NOT EXISTS dev;

-- Compatibilite migrations sprint 7/8
ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS quantitePassagersAssignes INT DEFAULT 0;

ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS fromNonAssigneePrecedent BOOLEAN DEFAULT FALSE;

ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS dateHeureDepartEffective TIMESTAMP NULL;

ALTER TABLE IF EXISTS dev.Vehicule
    ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';

-- Reset complet
TRUNCATE TABLE dev.TrajetEtape RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Trajet RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Assignation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.token_expiration RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE dev.Hotel RESTART IDENTITY CASCADE;

-- References
INSERT INTO dev.Hotel (code, nom) VALUES
('AER', 'Aeroport'),
('H1', 'Hotel 1'),
('H2', 'Hotel 2'),
('H3', 'Hotel 3'),
('H4', 'Hotel 4');

INSERT INTO dev.Parametre (code, valeur, unite, typeValeur) VALUES
('Vm', '50', 'km/h', 'Integer'),
('temps_attente', '30', 'minutes', 'Integer');

INSERT INTO dev.Distance (from_hotel, to_hotel, km) VALUES
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), 12),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), 16),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), 20),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='AER'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 24),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), 8),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), 7),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 6),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'), 10),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 9),
((SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'), (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'), 13);

-- ---------------------------------------------------------------------
-- Flotte de test (references explicites par cas)
-- ---------------------------------------------------------------------
INSERT INTO dev.Vehicule (Reference, nbPlace, TypeVehicule, HeureDisponibilite) VALUES
-- C01/C02 (regle earliest retour vs meme-heure plus proche)
('S8-C01-V10', 10, 'D', '23:00:00'),
('S8-C01-V8', 8, 'E', '23:00:00'),
('S8-C02-V10', 10, 'D', '23:00:00'),
('S8-C02-V8', 8, 'E', '23:00:00'),

-- C03/C04/C05 (retour 11:20, depart immediat vs uniforme)
('S8-C03-V6', 6, 'D', '23:00:00'),
('S8-C04-V15', 15, 'D', '23:00:00'),
('S8-C05-V15', 15, 'D', '23:00:00'),
('S8-C05-V9', 9, 'E', '23:00:00'),

-- C06 (retour sans NA ni vol meme-heure)
('S8-C06-VA10', 10, 'D', '23:00:00'),

-- C07/C08 (premiere disponibilite 09:00 et minuit)
('S8-C07-VD9', 9, 'D', '09:00:00'),
('S8-C08-VM8', 8, 'E', '00:00:00'),

-- C09 (retour==vol, priorite vehicule)
('S8-C09-V15', 15, 'D', '23:00:00'),

-- C10 (vol avant vehicule, vehicule entre comme candidat)
('S8-C10-V8', 8, 'D', '23:00:00'),

-- Vehicules bloques pour forcer des non-assignes en amont
('S8-BLK-20', 20, 'D', '23:00:00'),
('S8-BLK-12', 12, 'E', '23:00:00');

-- ---------------------------------------------------------------------
-- Trajets historiques (precondition vehicule retourne)
-- ---------------------------------------------------------------------
INSERT INTO dev.Trajet (id_vehicule, date_heure_depart, date_heure_retour, date_assignation) VALUES
-- C01: retours differents (08:30 vs 08:35)
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C01-V10'), '2026-08-01 07:00:00', '2026-08-01 08:30:00', '2026-08-01'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C01-V8'),  '2026-08-01 07:10:00', '2026-08-01 08:35:00', '2026-08-01'),

-- C02: retours meme heure (08:30)
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C02-V10'), '2026-08-02 07:00:00', '2026-08-02 08:30:00', '2026-08-02'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C02-V8'),  '2026-08-02 07:05:00', '2026-08-02 08:30:00', '2026-08-02'),

-- C03/C04/C05
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C03-V6'),  '2026-08-03 09:50:00', '2026-08-03 11:20:00', '2026-08-03'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C04-V15'), '2026-08-04 09:45:00', '2026-08-04 11:20:00', '2026-08-04'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C05-V15'), '2026-08-05 09:40:00', '2026-08-05 11:20:00', '2026-08-05'),
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C05-V9'),  '2026-08-05 10:00:00', '2026-08-05 11:25:00', '2026-08-05'),

-- C06
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C06-VA10'),'2026-08-06 08:30:00', '2026-08-06 10:00:00', '2026-08-06'),

-- C09
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C09-V15'), '2026-08-09 09:50:00', '2026-08-09 11:20:00', '2026-08-09'),

-- C10 (vehicule revient apres vol)
((SELECT id_vehicule FROM dev.Vehicule WHERE Reference='S8-C10-V8'),  '2026-08-10 07:30:00', '2026-08-10 09:10:00', '2026-08-10');

-- ---------------------------------------------------------------------
-- Reservations par cas
-- ---------------------------------------------------------------------

-- C01: RNA multiples trie DESC + retour le plus tot prioritaire
-- Attendu metier: RNA 8 va d'abord sur V10 (retour 08:30) et non V8 (08:35)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-01 08:00:00', 'S8-C01-RNA-8', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1')),
('2026-08-01 08:02:00', 'S8-C01-RNA-3', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'));

-- C02: RNA=8 + retours meme heure => plus proche capacite (V8)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-02 08:00:00', 'S8-C02-RNA-8', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'));

-- C03: NA seulement, complet => depart immediat heure retour
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-03 11:00:00', 'S8-C03-RNA-6', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'));

-- C04: NA + vol meme heure retour, complet => depart immediat heure retour
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-04 11:00:00', 'S8-C04-RNA-8', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3')),
('2026-08-04 11:20:00', 'S8-C04-VOL-7', 7, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'));

-- C05: incomplet a 11:20, complete plus tard (11:22) + autres entrees => depart uniforme (pas immediat)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-05 11:00:00', 'S8-C05-RNA-8', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1')),
('2026-08-05 11:20:00', 'S8-C05-VOL-MH-2', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2')),
('2026-08-05 11:22:00', 'S8-C05-VOL-LATE-5', 5, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3')),
('2026-08-05 11:24:00', 'S8-C05-NORM-4', 4, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H4'));

-- C06: aucune NA et aucun vol a 10:00, vehicule retour declenche un regroupement
-- (reservation hors meme-heure, dans fenetre, pour observer l'entree sans declenchement par vol)
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-06 10:10:00', 'S8-C06-VOL-3', 3, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'));

-- C07: premiere disponibilite 09:00 prioritaire sur vol 09:10
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-07 09:10:00', 'S8-C07-VOL-7', 7, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'));

-- C08: disponibilite a minuit declenche regroupement avant vol 00:15
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-08 00:15:00', 'S8-C08-VOL-6', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2'));

-- C09: retour==vol (11:20), priorite vehicule + NA
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-09 11:00:00', 'S8-C09-RNA-8', 8, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3')),
('2026-08-09 11:20:00', 'S8-C09-VOL-7', 7, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H1'));

-- C10: vol arrive avant vehicule retour, regroupement par vol, vehicule entre candidat si dans fenetre
INSERT INTO dev.reservation (DateHeureArrive, idClient, nbPassager, Id_Hotel) VALUES
('2026-08-10 09:00:00', 'S8-C10-VOL-6', 6, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H2')),
('2026-08-10 09:12:00', 'S8-C10-VOL-2', 2, (SELECT Id_Hotel FROM dev.Hotel WHERE code='H3'));

COMMIT;

-- =====================================================================
-- Mode d'execution recommande
-- =====================================================================
-- 1) Charger ce script.
-- 2) Lancer l'assignation pour chaque date:
--    2026-08-01 a 2026-08-10 (selon vos endpoints/services)
-- 3) Verifier avec les requetes ci-dessous.

-- =====================================================================
-- Requetes de verification
-- =====================================================================

-- A) Vue globale des assignations Sprint 8 pack
-- SELECT DATE(a.date_heure_depart) AS d, v.reference, r.idclient,
--        r.nbpassager AS nb_resa,
--        a.quantitepassagersassignes AS qte_assignee,
--        a.fromnonassigneeprecedent,
--        a.date_heure_depart, a.date_heure_retour
-- FROM dev.assignation a
-- JOIN dev.vehicule v ON v.id_vehicule = a.id_vehicule
-- JOIN dev.reservation r ON r.id_reservation = a.id_reservation
-- WHERE DATE(a.date_heure_depart) BETWEEN '2026-08-01' AND '2026-08-10'
-- ORDER BY d, a.date_heure_depart, v.reference, a.id_assignation;

-- B) C01/C02: verifier regle 8 passagers (earliest vs plus proche meme-heure)
-- SELECT DATE(a.date_heure_depart) AS d, v.reference, r.idclient,
--        a.quantitepassagersassignes, a.date_heure_depart
-- FROM dev.assignation a
-- JOIN dev.vehicule v ON v.id_vehicule = a.id_vehicule
-- JOIN dev.reservation r ON r.id_reservation = a.id_reservation
-- WHERE r.idclient IN ('S8-C01-RNA-8', 'S8-C02-RNA-8')
-- ORDER BY d, a.id_assignation;

-- C) C03/C04/C09: verifier depart immediat a l'heure de retour
-- SELECT DATE(a.date_heure_depart) AS d, v.reference,
--        MIN(a.date_heure_depart) AS depart_effectif,
--        CASE DATE(a.date_heure_depart)
--            WHEN DATE '2026-08-03' THEN TIMESTAMP '2026-08-03 11:20:00'
--            WHEN DATE '2026-08-04' THEN TIMESTAMP '2026-08-04 11:20:00'
--            WHEN DATE '2026-08-09' THEN TIMESTAMP '2026-08-09 11:20:00'
--        END AS depart_attendu
-- FROM dev.assignation a
-- JOIN dev.vehicule v ON v.id_vehicule = a.id_vehicule
-- WHERE v.reference IN ('S8-C03-V6', 'S8-C04-V15', 'S8-C09-V15')
-- GROUP BY DATE(a.date_heure_depart), v.reference
-- ORDER BY d, v.reference;

-- D) C05: verifier depart uniforme (pas bloque a 11:20 si entrees ulterieures)
-- SELECT v.reference,
--        MIN(a.date_heure_depart) AS depart_min,
--        MAX(a.date_heure_depart) AS depart_max
-- FROM dev.assignation a
-- JOIN dev.vehicule v ON v.id_vehicule = a.id_vehicule
-- WHERE DATE(a.date_heure_depart) = '2026-08-05'
-- GROUP BY v.reference
-- ORDER BY v.reference;

-- E) C07/C08: verifier priorite premiere disponibilite (09:00 et 00:00)
-- SELECT DATE(a.date_heure_depart) AS d, v.reference, r.idclient,
--        a.date_heure_depart, a.quantitepassagersassignes
-- FROM dev.assignation a
-- JOIN dev.vehicule v ON v.id_vehicule = a.id_vehicule
-- JOIN dev.reservation r ON r.id_reservation = a.id_reservation
-- WHERE r.idclient IN ('S8-C07-VOL-7', 'S8-C08-VOL-6')
-- ORDER BY d, a.id_assignation;
