package com.ardoq.addon.maven;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

public class MavenAddonConfiguration extends Configuration {
	
	
	@Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();
	
	
    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }


	
	
	

}
