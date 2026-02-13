CREATE TABLE IF NOT EXISTS dev.Vehicule (
    Id_Vehicule SERIAL,
    Reference VARCHAR(50) NOT NULL,
    nbPlace INT,
    TypeVehicule VARCHAR(50),
    PRIMARY KEY(Id_Vehicule)
);