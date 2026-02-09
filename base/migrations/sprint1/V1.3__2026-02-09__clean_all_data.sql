-- ============================================
-- Migration V1.3 - Nettoyage Complet
-- Date: 2026-02-09
-- Sprint: 1
-- Description: Supprime TOUTES les données (dangereux!)
-- ============================================

-- Avertissement
DO $$
BEGIN
    RAISE NOTICE 'WARNING: Suppression de TOUTES les donnees du schema staging!';
END $$;

-- Désactiver les contraintes de clé étrangère temporairement
SET session_replication_role = 'replica';

-- Supprimer toutes les réservations
DELETE FROM staging.reservation;

-- Supprimer tous les hôtels
DELETE FROM staging.hotel;

-- Réactiver les contraintes
SET session_replication_role = 'origin';

-- Réinitialiser les séquences à 1
ALTER SEQUENCE staging.hotel_id_hotel_seq RESTART WITH 1;
ALTER SEQUENCE staging.reservation_id_reservation_seq RESTART WITH 1;

-- Vérifier que tout est vide
SELECT 'Vérification du nettoyage...' as Info;
SELECT COUNT(*) as NbHotels FROM staging.hotel;
SELECT COUNT(*) as NbReservations FROM staging.reservation;

SELECT '✓ Nettoyage complet terminé!' as Status;
