ALTER TABLE IF EXISTS dev.Hotel
ADD COLUMN IF NOT EXISTS code VARCHAR(50);


CREATE TABLE IF NOT EXISTS dev.Distance (
    Id_Distance SERIAL,
    from_hotel INT NOT NULL,
    to_hotel INT NOT NULL,
    km INT NOT NULL,
    PRIMARY KEY (Id_Distance),
    FOREIGN KEY (from_hotel) REFERENCES dev.Hotel(Id_Hotel),
    FOREIGN KEY (to_hotel) REFERENCES dev.Hotel(Id_Hotel)
);

CREATE TABLE IF NOT EXISTS dev.Parametre (
    code VARCHAR(50),
    valeur VARCHAR(50),
    unite VARCHAR(20),
    typeValeur VARCHAR(20),
    PRIMARY KEY (code)
);


CREATE TABLE IF NOT EXISTS dev.Assignation (
    Id_Assignation SERIAL,
    Id_Reservation INT NOT NULL,
    Id_Vehicule INT NOT NULL,
    date_heure_depart TIMESTAMP,
    date_heure_retour TIMESTAMP,
    PRIMARY KEY (Id_Assignation),
    FOREIGN KEY (Id_Reservation) REFERENCES dev.reservation(Id_reservation),
    FOREIGN KEY (Id_Vehicule) REFERENCES dev.Vehicule(Id_Vehicule)
);

CREATE INDEX idx_assignation_vehicule_date
ON dev.Assignation (Id_Vehicule, date_heure_depart, date_heure_retour);
