-- ============================================
-- Migration V1.2 - Réinitialisation des données de test
-- Date: 2026-02-09
-- Sprint: 1
-- Description: Supprime les données et réinsère les données de test
-- ============================================

-- Désactiver les contraintes de clé étrangère temporairement
SET session_replication_role = 'replica';

-- Supprimer toutes les réservations
DELETE FROM staging.reservation;

-- Supprimer tous les hôtels
DELETE FROM staging.hotel;

-- Réactiver les contraintes
SET session_replication_role = 'origin';

-- Réinitialiser les séquences (ID auto-increment)
ALTER SEQUENCE staging.hotel_id_hotel_seq RESTART WITH 1;
ALTER SEQUENCE staging.reservation_id_reservation_seq RESTART WITH 1;

-- Insérer les hôtels de test
INSERT INTO staging.hotel (nom) VALUES 
    ('Hotel Ivandry'),
    ('Hotel Carlton'),
    ('Hotel Louvre'),
    ('Hotel Colbert');

-- Insérer les réservations de test
INSERT INTO staging.reservation (idclient, nbpassager, dateheureArrive, id_hotel) VALUES
    ('0001', 3, '2026-02-10 14:30:00'::timestamp, 1),
    ('0002', 2, '2026-02-10 15:45:00'::timestamp, 2),
    ('0003', 5, '2026-02-11 10:00:00'::timestamp, 3),
    ('0004', 1, '2026-02-11 16:30:00'::timestamp, 4);

-- Vérifier les données
SELECT '=== HOTELS ===' as Info;
SELECT * FROM staging.hotel;

SELECT '=== RESERVATIONS ===' as Info;
SELECT * FROM staging.reservation;

SELECT '✓ Réinitialisation des données terminée avec succès!' as Status;
