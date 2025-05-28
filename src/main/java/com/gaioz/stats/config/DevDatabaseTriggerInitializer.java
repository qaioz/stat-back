package com.gaioz.stats.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile("dev") // Only run in dev mode
@RequiredArgsConstructor
public class DevDatabaseTriggerInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void init() throws Exception {
        var script = new ClassPathResource("dev_mode_update_delay_trigger.sql");
        ScriptUtils.executeSqlScript(dataSource.getConnection(), script);
    }
}
