-- Drop trigger if it exists to avoid conflicts
DROP TRIGGER IF EXISTS simulate_long_write ON stats;

-- Create or replace the function
CREATE OR REPLACE FUNCTION long_write_delay() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    select pg_sleep(10);
    RETURN NEW;
END;
$$;

-- Create the trigger
CREATE TRIGGER simulate_long_write
    BEFORE INSERT OR UPDATE
    ON stats
    FOR EACH ROW
EXECUTE FUNCTION long_write_delay();