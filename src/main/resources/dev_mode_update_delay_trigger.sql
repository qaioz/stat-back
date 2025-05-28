create function long_write_delay() returns trigger
    language plpgsql
as
$$
BEGIN
    PERFORM pg_sleep(10); -- 10 seconds delay
    RETURN NEW;
END;
$$;

create trigger simulate_long_write
    before insert or update
    on stats
    for each row
execute function long_write_delay();