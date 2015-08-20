package com.ardoq.addon.maven;

import com.codahale.metrics.health.HealthCheck;

public class MavenAddonHealthCheck extends HealthCheck {

    
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
