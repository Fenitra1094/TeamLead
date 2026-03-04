CREATE SCHEMA IF NOT EXISTS staging;

ALTER TABLE IF EXISTS staging.Hotel
ADD COLUMN IF NOT EXISTS code VARCHAR(50);

CREATE TABLE IF NOT EXISTS staging.Vehicule (
	Id_Vehicule SERIAL,
	Reference VARCHAR(50) NOT NULL,
	nbPlace INT,
	TypeVehicule VARCHAR(50),
	PRIMARY KEY (Id_Vehicule)
);

CREATE TABLE IF NOT EXISTS staging.token_expiration(
   id SERIAL,
   token VARCHAR(255) NOT NULL UNIQUE,
   expiration TIMESTAMP NOT NULL,
   PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS staging.Distance (
	Id_Distance SERIAL,
	from_hotel INT NOT NULL,
	to_hotel INT NOT NULL,
	km INT NOT NULL,
	PRIMARY KEY (Id_Distance),
	FOREIGN KEY (from_hotel) REFERENCES staging.Hotel(Id_Hotel),
	FOREIGN KEY (to_hotel) REFERENCES staging.Hotel(Id_Hotel)
);

CREATE TABLE IF NOT EXISTS staging.Parametre (
	code VARCHAR(50),
	valeur VARCHAR(50),
	unite VARCHAR(20),
	typeValeur VARCHAR(20),
	PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS staging.Assignation (
	Id_Assignation SERIAL,
	Id_Reservation INT NOT NULL,
	Id_Vehicule INT NOT NULL,
	date_heure_depart TIMESTAMP,
	date_heure_retour TIMESTAMP,
	PRIMARY KEY (Id_Assignation),
	FOREIGN KEY (Id_Reservation) REFERENCES staging.reservation(Id_reservation),
	FOREIGN KEY (Id_Vehicule) REFERENCES staging.Vehicule(Id_Vehicule)
);

CREATE INDEX IF NOT EXISTS idx_assignation_vehicule_date
ON staging.Assignation (Id_Vehicule, date_heure_depart, date_heure_retour);
