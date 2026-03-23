-- Sprint 7.1: Ajout de l'heure de disponibilite des vehicules
-- Objectif: stocker une heure explicite de disponibilite sans casser les logiques existantes
-- Valeur par defaut 00:00 pour conserver le comportement historique

ALTER TABLE IF EXISTS dev.Vehicule
ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';

ALTER TABLE IF EXISTS staging.Vehicule
ADD COLUMN IF NOT EXISTS HeureDisponibilite TIME NOT NULL DEFAULT '00:00:00';
