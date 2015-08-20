package com.ardoq.addon.maven;

import org.apache.http.client.HttpClient;

import com.bazaarvoice.dropwizard.webjars.WebJarBundle;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MavenAddonApplication extends Application<MavenAddonConfiguration> {

    @Override
    public void initialize(Bootstrap<MavenAddonConfiguration> bootstrap) {
    	bootstrap.addBundle(new AssetsBundle("/html", "/", "index.html", "html"));
    	bootstrap.addBundle(new AssetsBundle("/css", "/css","","css"));
    	bootstrap.addBundle(new AssetsBundle("/js", "/js","","js"));
    	bootstrap.addBundle(new WebJarBundle());
    }

    @Override
    public void run(MavenAddonConfiguration configuration, Environment environment) {
    	
    	environment.jersey().setUrlPattern( "/api/*" );
    	
    	final HttpClient httpClient = new HttpClientBuilder(environment)
    									.using(configuration.getHttpClientConfiguration())
    									.build("ardoqHttpClient");
    	
    	
    	MavenAddonAPI api = new MavenAddonAPI(httpClient);
    	environment.jersey().register(api);
    	
    	environment.healthChecks().register("ec2",new MavenAddonHealthCheck());
    }

}