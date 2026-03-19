-- Sprint 7: Ajouter support des assignations partielles (split de réservations)
-- Ajoute un champ quantitePassagersAssignes pour tracker le nombre de passagers effectivement assignés
-- Permet de supporter les divisions/splits de réservations sur plusieurs véhicules

ALTER TABLE dev.Assignation 
ADD COLUMN quantitePassagersAssignes INT DEFAULT 0;

-- Commentaire explicatif
COMMENT ON COLUMN dev.Assignation.quantitePassagersAssignes IS 
'Sprint 7: Nombre de passagers effectivement assignés à ce véhicule pour cette réservation. 
Support des assignations partielles (ex: réservation 8p split sur V1 (6p) + V2 (2p))';
