package no.tripletex.amager;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MavenAddonAPI {
	private static final Logger logger = LoggerFactory.getLogger(MavenAddonAPI.class);
	
	HttpClient httpClient;
	
	public MavenAddonAPI(HttpClient httpClient){
		this.httpClient = httpClient;
	}

	
	@Timed
	@GET
	@Path("/generate")
	public String tags(@PathParam("artifactId") String artifactId) throws Exception{
	}
	
}
