package com.gaioz.stats.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Profile("dev") // Only run in dev mode
@RequiredArgsConstructor
@Slf4j
public class DevDatabaseTriggerInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void init() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            log.info("Initializing dev mode database triggers...");
            
            // Drop trigger if exists
            statement.execute("DROP TRIGGER IF EXISTS simulate_long_write ON stats");
            
            // Create function
            String functionSql = """
                    CREATE OR REPLACE FUNCTION long_write_delay()
                    RETURNS trigger
                    LANGUAGE plpgsql
                    AS $$
                    BEGIN
                        PERFORM pg_sleep(5);
                        RETURN NEW;
                    END;
                    $$
                    """;
            statement.execute(functionSql);
            
            // Create trigger
            String triggerSql = """
                CREATE TRIGGER simulate_long_write
                    BEFORE INSERT OR UPDATE
                    ON stats
                    FOR EACH ROW
                    EXECUTE FUNCTION long_write_delay()
                """;
            statement.execute(triggerSql);
            
            log.info("Dev mode database triggers initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize dev mode database triggers", e);
            throw e;
        }
    }
}