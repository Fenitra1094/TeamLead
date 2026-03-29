-- Sprint 8: Support de la priorite des non assignes precedents
-- Objectif:
--  - tracer l'origine des passagers embarques (precedent vs nouveau)
--  - tracer l'heure de depart effective utilisee
-- Compatibilite sprint7:
--  - colonnes ajoutees en IF NOT EXISTS
--  - valeurs par defaut pour ne pas casser les donnees existantes

ALTER TABLE IF EXISTS dev.Assignation
ADD COLUMN IF NOT EXISTS fromNonAssigneePrecedent BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE IF EXISTS staging.Assignation
ADD COLUMN IF NOT EXISTS fromNonAssigneePrecedent BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN dev.Assignation.fromNonAssigneePrecedent IS
'Sprint 8: TRUE si l''assignation embarque des passagers non assignes du groupe precedent.';

COMMENT ON COLUMN staging.Assignation.fromNonAssigneePrecedent IS
'Sprint 8: TRUE si l''assignation embarque des passagers non assignes du groupe precedent.';
