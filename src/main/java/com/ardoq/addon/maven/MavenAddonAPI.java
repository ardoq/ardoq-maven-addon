package com.ardoq.addon.maven;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.client.HttpClient;
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
    @Path("/ping")
    public String ping(){
        return "pong";
    }


    @Timed
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public Response test(){
        StreamingOutput stream = new StreamingOutput() {

            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                System.out.println("starting");
                int i=1000;

                Writer writer = new BufferedWriter(new OutputStreamWriter(os));
                while( --i>0){
                    System.out.print("w");
                    writer.write("test sdf asdfasd fasd fas df asdf asd f asdf as df as df asdf as df asd fas df asd fa sdf asd fa sdf asd fa sdf as df asdf a sdf a\n");
                    writer.write(i);
                    writer.flush();
                    os.flush();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("done");
            }
        };
        return Response.ok(stream).build();
    }

    @Timed
    @POST
    @Path("/import")
    public String tags(@QueryParam("artifactId") String artifactId,
                        @QueryParam("token") String token,
                        @QueryParam("organization") String organization) throws Exception{

        System.out.println("artifactId "+artifactId);
        System.out.println("token "+token);
        System.out.println("organization "+organization);

//    	org=piedpiper&token=2854b08d642b43ffb4281b68188bb6fb
        return null;


    }

}
