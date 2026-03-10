package com.cousin.repository;

import com.cousin.model.Trajet;
import com.cousin.model.TrajetEtape;
import com.cousin.model.Vehicule;
import com.cousin.model.Hotel;
import com.cousin.util.DbConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrajetRepository {

    /**
     * Insere un trajet et retourne l'id genere.
     */
    public int insertTrajet(Trajet trajet, Connection connection) throws SQLException {
        String sql = "INSERT INTO Trajet(Id_Vehicule, date_heure_depart, date_heure_retour, date_assignation) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, trajet.getIdVehicule());
            st.setTimestamp(2, Timestamp.valueOf(trajet.getDateHeureDepart()));
            st.setTimestamp(3, Timestamp.valueOf(trajet.getDateHeureRetour()));
            st.setDate(4, Date.valueOf(trajet.getDateAssignation()));
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    trajet.setIdTrajet(id);
                    return id;
                }
            }
        }

        throw new SQLException("Impossible de recuperer l'id du trajet insere");
    }

    /**
     * Insere une etape de trajet (avec connexion existante).
     */
    public void insertTrajetEtape(TrajetEtape etape, Connection connection) throws SQLException {
        String sql = "INSERT INTO TrajetEtape(Id_Trajet, Id_Hotel, ordre, distance_depuis_precedent) VALUES (?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, etape.getIdTrajet());
            st.setInt(2, etape.getIdHotel());
            st.setInt(3, etape.getOrdre());
            st.setInt(4, etape.getDistanceDepuisPrecedent());
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    etape.setIdTrajetEtape(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Supprime tous les trajets et etapes pour une date donnee (dans une transaction).
     */
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

    /**
     * Retourne la liste des trajets pour une date donnee avec vehicule et etapes chargees.
     */
    public List<Trajet> findTrajetsByDate(LocalDate date) throws SQLException {
        List<Trajet> trajets = new ArrayList<>();

        String sql = "SELECT Id_Trajet, Id_Vehicule, date_heure_depart, date_heure_retour, date_assignation " +
                     "FROM Trajet WHERE date_assignation = ? ORDER BY Id_Trajet";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setDate(1, Date.valueOf(date));

            try (ResultSet rs = st.executeQuery()) {
                HotelRepository hrepo = new HotelRepository();
                Map<Integer, Hotel> hotelMap = new HashMap<>();
                for (Hotel h : hrepo.findAll()) {
                    hotelMap.put(h.getIdHotel(), h);
                }

                VehiculeRepository vrepo = new VehiculeRepository();

                while (rs.next()) {
                    Trajet t = new Trajet();
                    t.setIdTrajet(rs.getInt("Id_Trajet"));
                    t.setIdVehicule(rs.getInt("Id_Vehicule"));
                    Timestamp tsd = rs.getTimestamp("date_heure_depart");
                    if (tsd != null) t.setDateHeureDepart(tsd.toLocalDateTime());
                    Timestamp tsr = rs.getTimestamp("date_heure_retour");
                    if (tsr != null) t.setDateHeureRetour(tsr.toLocalDateTime());
                    java.sql.Date dassign = rs.getDate("date_assignation");
                    if (dassign != null) t.setDateAssignation(dassign.toLocalDate());

                    try {
                        Vehicule v = vrepo.findById(t.getIdVehicule());
                        t.setVehicule(v);
                    } catch (Exception ex) {
                        // ignore
                    }

                    List<TrajetEtape> etapes = new ArrayList<>();
                    String sqlEt = "SELECT Id_Trajet_Etape, Id_Hotel, ordre, distance_depuis_precedent " +
                                   "FROM TrajetEtape WHERE Id_Trajet = ? ORDER BY ordre";
                    try (PreparedStatement st2 = connection.prepareStatement(sqlEt)) {
                        st2.setInt(1, t.getIdTrajet());
                        try (ResultSet rs2 = st2.executeQuery()) {
                            while (rs2.next()) {
                                TrajetEtape e = new TrajetEtape();
                                e.setIdTrajetEtape(rs2.getInt("Id_Trajet_Etape"));
                                e.setIdTrajet(t.getIdTrajet());
                                int idHotel = rs2.getInt("Id_Hotel");
                                e.setIdHotel(idHotel);
                                e.setOrdre(rs2.getInt("ordre"));
                                e.setDistanceDepuisPrecedent(rs2.getInt("distance_depuis_precedent"));

                                Hotel h = hotelMap.get(idHotel);
                                if (h != null) {
                                    e.setHotel(h);
                                } else {
                                    Hotel hh = new Hotel();
                                    hh.setIdHotel(idHotel);
                                    e.setHotel(hh);
                                }

                                etapes.add(e);
                            }
                        }
                    }

                    t.setEtapes(etapes);
                    trajets.add(t);
                }
            }
        }

        return trajets;
    }
}