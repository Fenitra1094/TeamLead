-- Sprint 7: Données de test pour le split de réservations
-- Cas de test obligatoires pour validation du split intelligent

-- ========================================
-- CAS 1: v1=8, v2=3 ; r1=6, r2=4, r3=3
-- Attente: r1 entièrement sur v1 (6p), r2 split sur v1 (2p restant) + v2 (2p), r3 sur v2 (1p restant)
-- ========================================

-- CAS 2: v1=8, v2=3 ; r1=6, r4=5, r2=4, r3=3
-- Attente: r1 (6p) sur v1, r4 split sur v1 (2p) + v2 (3p), r2/r3 reportés ou split selon regroupement
-- Ordre traitement DESC: r4(5) -> r1(6) -> r2(4) -> r3(3)

-- CAS 3: r=23 avec vehicules 13, 8, 6
-- Attente: validation du tri "plus proche" (écart = capacité - passagersRestants)
-- - 23 -> v13 (20 places restantes, réservé normalement)
-- - ou si on teste un split: 23p sur v13 (13) + v8 (8) + v6 (2 restant)

-- Note: Ces données doivent être intégrées avec les clients et hôtels existants
-- Pour le MVP, on peut créer des enregistrements minimalistes directement dans les INSERT

-- Exemple d'insertion (à adapter selon le schéma réel):
-- INSERT INTO dev.Reservation(date_heure_arrive, id_client, nb_passager, id_hotel) VALUES ...
-- INSERT INTO dev.Vehicule(Reference, nbPlace, TypeVehicule) VALUES ...
