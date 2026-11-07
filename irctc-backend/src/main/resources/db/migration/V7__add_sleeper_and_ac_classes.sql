ALTER TABLE trains ADD COLUMN sleeper_seats INTEGER;
ALTER TABLE trains ADD COLUMN ac_seats INTEGER;
ALTER TABLE trains ADD COLUMN sleeper_fare NUMERIC(10, 2);
ALTER TABLE trains ADD COLUMN ac_fare NUMERIC(10, 2);

WITH existing_usage AS (
    SELECT a.train_id, MAX(GREATEST(t.total_seats - a.available_seats, 0)) AS max_booked
    FROM train_seat_availability a
    JOIN trains t ON t.id = a.train_id
    GROUP BY a.train_id
)
UPDATE trains t
SET sleeper_seats = GREATEST((t.total_seats + 1) / 2,
                             COALESCE((SELECT max_booked FROM existing_usage u WHERE u.train_id = t.id), 0)),
    ac_seats = t.total_seats - GREATEST((t.total_seats + 1) / 2,
                                        COALESCE((SELECT max_booked FROM existing_usage u WHERE u.train_id = t.id), 0)),
    sleeper_fare = COALESCE(t.fare_per_seat, 500.00),
    ac_fare = COALESCE(t.fare_per_seat, 500.00) * 2
;

ALTER TABLE trains ALTER COLUMN sleeper_seats SET NOT NULL;
ALTER TABLE trains ALTER COLUMN ac_seats SET NOT NULL;
ALTER TABLE trains ALTER COLUMN sleeper_fare SET NOT NULL;
ALTER TABLE trains ALTER COLUMN ac_fare SET NOT NULL;
ALTER TABLE trains ADD CONSTRAINT ck_trains_class_capacity
    CHECK (sleeper_seats >= 0 AND ac_seats >= 0 AND sleeper_seats + ac_seats = total_seats);
ALTER TABLE trains ADD CONSTRAINT ck_trains_class_fares_positive
    CHECK (sleeper_fare > 0 AND ac_fare > 0);

CREATE TEMPORARY TABLE legacy_train_availability ON COMMIT DROP AS
    SELECT id, train_id, travel_date, available_seats FROM train_seat_availability;

ALTER TABLE train_seat_availability ADD COLUMN travel_class VARCHAR(16) NOT NULL DEFAULT 'SLEEPER';
ALTER TABLE train_seat_availability DROP CONSTRAINT IF EXISTS uk_seat_availability_train_date;

UPDATE train_seat_availability a
SET available_seats = GREATEST(t.sleeper_seats - GREATEST(t.total_seats - old.available_seats, 0), 0)
FROM trains t, legacy_train_availability old
WHERE old.id = a.id AND old.train_id = t.id;

INSERT INTO train_seat_availability (train_id, travel_date, travel_class, available_seats)
SELECT t.id, old.travel_date, 'AC',
       GREATEST(t.ac_seats - GREATEST(t.total_seats - old.available_seats - t.sleeper_seats, 0), 0)
FROM trains t
JOIN legacy_train_availability old ON old.train_id = t.id;

ALTER TABLE train_seat_availability ADD CONSTRAINT uk_seat_availability_train_date_class
    UNIQUE (train_id, travel_date, travel_class);
ALTER TABLE train_seat_availability ADD CONSTRAINT ck_seat_availability_class
    CHECK (travel_class IN ('SLEEPER', 'AC'));

ALTER TABLE bookings ADD COLUMN travel_class VARCHAR(16) NOT NULL DEFAULT 'SLEEPER';
ALTER TABLE bookings ADD CONSTRAINT ck_bookings_travel_class
    CHECK (travel_class IN ('SLEEPER', 'AC'));
