package no.tripletex.amager;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import no.tripletex.amager.docker.DockerRegistryClient;
import no.tripletex.amager.ec2.EC2Client;
import no.tripletex.amager.ec2.EC2HealthCheck;

import org.apache.http.client.HttpClient;

import com.bazaarvoice.dropwizard.webjars.WebJarBundle;

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