package com.ardoq.addon.maven;

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
import javax.ws.rs.core.MediaType;

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

        if(StringUtils.isNoneEmpty(imp.getRepoURL())) {
            task.addRepository(imp.getRepoURL(), imp.getRepoUsername(), imp.getRepoPassword());
        }

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
        private String scope;
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
        public String getScope() {
            return scope;
        }
        public void setScope(String scope) {
            this.scope = scope;
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


}
