-- Migration V4.0 - 2026-03-09 - init staging: Trajet and TrajetEtape
CREATE TABLE IF NOT EXISTS Trajet (
    id_trajet SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL REFERENCES Vehicule(id_vehicule),
    date_heure_depart TIMESTAMP NOT NULL,
    date_heure_retour TIMESTAMP NOT NULL,
    date_assignation DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS TrajetEtape (
    id_trajet_etape SERIAL PRIMARY KEY,
    id_trajet INT NOT NULL REFERENCES Trajet(id_trajet) ON DELETE CASCADE,
    id_hotel INT NOT NULL REFERENCES Hotel(id_hotel),
    ordre INT NOT NULL,
    distance_depuis_precedent INT NOT NULL
);

ALTER TABLE IF EXISTS staging.Assignation
    ADD COLUMN IF NOT EXISTS Id_Trajet INT NULL;

CREATE INDEX IF NOT EXISTS idx_trajet_vehicule_date ON Trajet(id_vehicule, date_heure_depart, date_heure_retour);
CREATE INDEX IF NOT EXISTS idx_trajet_etape_trajet ON TrajetEtape(id_trajet, ordre);




