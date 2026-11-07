ALTER TABLE bookings ADD COLUMN IF NOT EXISTS pnr VARCHAR(10);

UPDATE bookings
SET pnr = LPAD(id::text, 10, '0')
WHERE pnr IS NULL;

ALTER TABLE bookings ALTER COLUMN pnr SET NOT NULL;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_bookings_pnr') THEN
        ALTER TABLE bookings ADD CONSTRAINT uk_bookings_pnr UNIQUE (pnr);
    END IF;
END $$;
