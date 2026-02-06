CREATE SCHEMA IF NOT EXISTS staging;

CREATE TABLE IF NOT EXISTS staging.Hotel(
   Id_Hotel SERIAL,
   nom VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_Hotel)
);

CREATE TABLE IF NOT EXISTS staging.reservation(
   Id_reservation SERIAL,
   DateHeureArrive TIMESTAMP,
   idClient VARCHAR(50),
   nbPassager INT,
   Id_Hotel INT NOT NULL,
   PRIMARY KEY(Id_reservation),
   FOREIGN KEY(Id_Hotel) REFERENCES staging.Hotel(Id_Hotel)
);
