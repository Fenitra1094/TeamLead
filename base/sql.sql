
CREATE TABLE Hotel(
   Id_Hotel COUNTER,
   nom VARCHAR(50),
   PRIMARY KEY(Id_Hotel)
);

CREATE TABLE reservation(
   Id_reservation COUNTER,
   DateHeureArrive DATETIME,
   idClient VARCHAR(50),
   nbPassager INT,
   Id_Hotel INT NOT NULL,
   PRIMARY KEY(Id_reservation),
   FOREIGN KEY(Id_Hotel) REFERENCES Hotel(Id_Hotel)
);
