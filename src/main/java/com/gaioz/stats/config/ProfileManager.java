package com.gaioz.stats.config;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileManager implements EnvironmentAware {

    private static Environment environment;

    @Override
    public void setEnvironment(Environment env) {
        ProfileManager.environment = env;
    }

    public static boolean isDevProfileActive() {
        if (environment == null) return false;
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("dev")) {
                return true;
            }
        }
        return false;
    }
}
