package com.ardoq.addon.maven;

import org.apache.http.client.HttpClient;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;

import com.bazaarvoice.dropwizard.webjars.WebJarBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MavenAddonApplication extends Application<MavenAddonConfiguration> {

    @Override
    public void initialize(Bootstrap<MavenAddonConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                                                   new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new AssetsBundle("/html", "/", "index.html", "html"));
        bootstrap.addBundle(new AssetsBundle("/css", "/css","","css"));
        bootstrap.addBundle(new AssetsBundle("/img", "/img","","img"));
        bootstrap.addBundle(new AssetsBundle("/js", "/js","","js"));
        bootstrap.addBundle(new WebJarBundle());
    }

    @Override
    public void run(MavenAddonConfiguration configuration, Environment environment) {
        System.out.println("host: "+configuration.getArdoq().getHost());

        environment.jersey().setUrlPattern( "/api/*" );

        final HttpClient httpClient = new HttpClientBuilder(environment)
                                            .using(configuration.getHttpClientConfiguration())
                                            .build("ardoq maven addon http client");


        MavenAddonAPI api = new MavenAddonAPI(configuration.getArdoq(), httpClient);
        environment.jersey().register(api);

        environment.healthChecks().register("addonhealth",new MavenAddonHealthCheck());


        environment.jersey().register(new LoggingFilter(java.util.logging.Logger.getLogger(LoggingFilter.class.getName()),true));
    }

}