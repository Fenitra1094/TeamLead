create database reservation_voiture;
\c reservation_voiture


CREATE SCHEMA IF NOT EXISTS dev;

CREATE TABLE IF NOT EXISTS dev.Hotel(
   Id_Hotel SERIAL,
   nom VARCHAR(50) NOT NULL,
   PRIMARY KEY(Id_Hotel)
);

CREATE TABLE IF NOT EXISTS dev.reservation(
   Id_reservation SERIAL,
   DateHeureArrive TIMESTAMP,
   idClient VARCHAR(50),
   nbPassager INT,
   Id_Hotel INT NOT NULL,
   PRIMARY KEY(Id_reservation),
   FOREIGN KEY(Id_Hotel) REFERENCES dev.Hotel(Id_Hotel)
);
