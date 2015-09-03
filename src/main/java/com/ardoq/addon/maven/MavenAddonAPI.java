package com.ardoq.addon.maven;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ardoq.addon.maven.MavenAddonConfiguration.ArdoqConfig;
import com.codahale.metrics.annotation.Timed;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MavenAddonAPI {
    private static final Logger logger = LoggerFactory.getLogger(MavenAddonAPI.class);

    Map<String,MavenImportTask> runningTasks = new ConcurrentHashMap<String,MavenImportTask>();
    ExecutorService fixedPool = Executors.newFixedThreadPool(10);

    ArdoqConfig config;
    HttpClient httpClient;

    public MavenAddonAPI(ArdoqConfig config,HttpClient httpClient){
        this.config = config;
        this.httpClient = httpClient;
    }

    @Timed
    @GET
    @Path("/ping")
    public String ping(){
        return "pong";
    }

    @Timed
    @POST
    @Path("/import")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String startImport(ImportDefinition imp){

        if(StringUtils.isEmpty(imp.getToken())){ throw new IllegalArgumentException("token is required");}
        if(StringUtils.isEmpty(imp.getArtifact())){ throw new IllegalArgumentException("aritfact is required");}

        MavenImportTask task = new MavenImportTask(config,imp,runningTasks);
        fixedPool.submit(task);
        logger.info("starting maven import of " + task.getKey());
        return "\""+task.getKey()+"\""; // enclosing in " to serve as JSON
    }


    @Timed
    @GET
    @Path("/status/{importKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Status importStatus(@PathParam("importKey") String importKey){

        Status status = new Status();
        MavenImportTask task = runningTasks.get(importKey);

        if(task==null){
            status.setMode(Mode.DONE);
            return status;
        }

        status.setMode(task.getMode());
        task.emptyOutputBufferInto(status.getOutputBuffer());
        return status;
    }



    public static class ImportDefinition{
        private String artifact;
        private String organization;
        private String workspace;
        private String token;
        private String repoURL;
        private String repoUsername;
        private String repoPassword;

        public String getArtifact() {
            return artifact;
        }
        public void setArtifact(String artifact) {
            this.artifact = artifact;
        }
        public String getOrganization() {
            return organization;
        }
        public void setOrganization(String organization) {
            this.organization = organization;
        }
        public String getWorkspace() {
            return workspace;
        }
        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        public String getRepoURL() {
            return repoURL;
        }
        public void setRepoURL(String repoURL) {
            this.repoURL = repoURL;
        }
        public String getRepoUsername() {
            return repoUsername;
        }
        public void setRepoUsername(String repoUsername) {
            this.repoUsername = repoUsername;
        }
        public String getRepoPassword() {
            return repoPassword;
        }
        public void setRepoPassword(String repoPassword) {
            this.repoPassword = repoPassword;
        }

    }

    public enum Mode { PENDING, RUNNING, DONE};
    public static class Status {
        private Mode mode;
        private List<String> outputBuffer = new LinkedList<String>();
        public Mode getMode() {
            return mode;
        }
        public void setMode(Mode mode) {
            this.mode = mode;
        }
        public List<String> getOutputBuffer() {
            return outputBuffer;
        }
        public void setOutputBuffer(List<String> outputBuffer) {
            this.outputBuffer = outputBuffer;
        }
    }


    @Timed
    @POST
    @Path("/import2")
    @Produces(MediaType.TEXT_PLAIN)
    public Response tags(@QueryParam("artifactId") String artifactId,
                        @QueryParam("token") String token,
                        @QueryParam("organization") String organization) throws Exception{

        System.out.println("artifactId "+artifactId);
        System.out.println("token "+token);
        System.out.println("organization "+organization);

        StreamingOutput stream = new StreamingOutput() {

            public void write(OutputStream os) throws IOException, WebApplicationException {
                System.out.println("starting");
                int i=500;

                Writer writer = new BufferedWriter(new OutputStreamWriter(os));
                while( --i>0){
                    System.out.print("w");
                    writer.write("test sdf asdfasd fasd fas df asdf asd f asdf as df as df asdf as df asd fas df asd fa sdf asd fa sdf asd fa sdf as df asdf a sdf a\n<br/>");
                    writer.write(i);
                    writer.flush();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("done");
                os.flush();
                os.close();
            }
        };
        return Response.ok(stream).build();


    }

}
