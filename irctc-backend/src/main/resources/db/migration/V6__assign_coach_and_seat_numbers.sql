ALTER TABLE booking_passengers ADD COLUMN IF NOT EXISTS coach_number VARCHAR(8);
ALTER TABLE booking_passengers ADD COLUMN IF NOT EXISTS seat_number INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_booking_passengers_seat_positive') THEN
        ALTER TABLE booking_passengers ADD CONSTRAINT ck_booking_passengers_seat_positive
            CHECK (seat_number IS NULL OR seat_number > 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_booking_passenger_seat') THEN
        ALTER TABLE booking_passengers ADD CONSTRAINT uk_booking_passenger_seat
            UNIQUE (booking_id, coach_number, seat_number);
    END IF;
END $$;
