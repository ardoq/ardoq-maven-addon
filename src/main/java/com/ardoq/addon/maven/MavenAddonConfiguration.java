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


    @Valid
    @NotNull
    @JsonProperty
    private ArdoqConfig ardoq = new ArdoqConfig();

    public ArdoqConfig getArdoq() {
        return ardoq;
    }


    public static class ArdoqConfig{
        @NotNull
        @JsonProperty
        private String host;

        @NotNull
        @JsonProperty
        private String protocol;

        public String getHost() {
            return host;
        }

        public String getProtocol() {
            return protocol;
        }

    }



}
