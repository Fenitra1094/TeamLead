-- Insert test data for Hotel and Reservation
-- Sprint 1 - Test data for staging environment

-- Insert Hotels
INSERT INTO staging.hotel (nom) VALUES 
    ('Hotel Ivandry'),
    ('Hotel Carlton'),
    ('Hotel Louvre'),
    ('Hotel Colbert');

-- Insert Test Reservations
INSERT INTO staging.reservation (idclient, nbpassager, dateheureArrive, id_hotel) VALUES
    ('0001', 3, '2026-02-10 14:30:00'::timestamp, 1),
    ('0002', 2, '2026-02-10 15:45:00'::timestamp, 2),
    ('0003', 5, '2026-02-11 10:00:00'::timestamp, 3),
    ('0004', 1, '2026-02-11 16:30:00'::timestamp, 4);
