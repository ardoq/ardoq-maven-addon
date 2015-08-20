package com.ardoq.addon.maven;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.bazaarvoice.dropwizard.assets.AssetsBundleConfiguration;
import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MavenAddonConfiguration extends Configuration implements AssetsBundleConfiguration{
	
	
	@Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();
	
    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();


    @Valid
    @NotNull
    @JsonProperty
    private final AssetsConfiguration assets = new AssetsConfiguration();

    
    public AssetsConfiguration getAssetsConfiguration() {
        return assets;
    }
	
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }
    
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }


	
	
	

}
