-- Migration V4.0 - 2026-03-09 - init dev: Trajet and TrajetEtape
CREATE TABLE IF NOT EXISTS dev.Trajet (
    id_trajet SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL REFERENCES dev.Vehicule(id_vehicule),
    date_heure_depart TIMESTAMP NOT NULL,
    date_heure_retour TIMESTAMP NOT NULL,
    date_assignation DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS dev.TrajetEtape (
    id_trajet_etape SERIAL PRIMARY KEY,
    id_trajet INT NOT NULL REFERENCES dev.Trajet(id_trajet) ON DELETE CASCADE,
    id_hotel INT NOT NULL REFERENCES dev.Hotel(id_hotel),
    ordre INT NOT NULL,
    distance_depuis_precedent INT NOT NULL
);

ALTER TABLE IF EXISTS dev.Assignation
    ADD COLUMN IF NOT EXISTS Id_Trajet INT NULL;

CREATE INDEX IF NOT EXISTS idx_trajet_vehicule_date ON dev.Trajet(id_vehicule, date_heure_depart, date_heure_retour);
CREATE INDEX IF NOT EXISTS idx_trajet_etape_trajet ON dev.TrajetEtape(id_trajet, ordre);


