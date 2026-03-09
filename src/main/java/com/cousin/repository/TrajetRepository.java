package com.cousin.repository;

import com.cousin.model.Trajet;
import com.cousin.model.TrajetEtape;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;

public class TrajetRepository {

    public void deleteTrajetsByDate(LocalDate date, Connection connection) throws SQLException {
        String deleteEtapes = "DELETE FROM TrajetEtape WHERE Id_Trajet IN (SELECT Id_Trajet FROM Trajet WHERE date_assignation = ?)";
        String deleteTrajets = "DELETE FROM Trajet WHERE date_assignation = ?";

        try (PreparedStatement deleteEtapesStmt = connection.prepareStatement(deleteEtapes);
             PreparedStatement deleteTrajetsStmt = connection.prepareStatement(deleteTrajets)) {
            deleteEtapesStmt.setDate(1, Date.valueOf(date));
            deleteEtapesStmt.executeUpdate();

            deleteTrajetsStmt.setDate(1, Date.valueOf(date));
            deleteTrajetsStmt.executeUpdate();
        }
    }

}
