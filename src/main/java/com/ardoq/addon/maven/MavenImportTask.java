package com.ardoq.addon.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ardoq.addon.maven.MavenAddonAPI.ImportDefinition;
import com.ardoq.addon.maven.MavenAddonAPI.Mode;
import com.ardoq.addon.maven.MavenAddonConfiguration.ArdoqConfig;
import com.ardoq.mavenImport.ArdoqMavenImport;
import com.ardoq.mavenImport.MavenUtil;


public class MavenImportTask implements Callable<String>{

    Logger logger = LoggerFactory.getLogger(MavenImportTask.class);

    ArdoqConfig config;
    ImportDefinition imp;
    Map<String, MavenImportTask> runningTasks;
    Mode mode;
    String workspaceID;
    String componentID;
    MavenUtil mavenUtil;
    LogBuffer logBuffer = new LogBuffer();

    public MavenImportTask(ArdoqConfig config, ImportDefinition imp, Map<String, MavenImportTask> runningTasks) {
        this.config = config;
        this.imp = imp;
        this.runningTasks = runningTasks;


        String[] filteredScopes = new String[]{"test","provided"};

        if("test".equals(imp.getScope())){
            // when user specifies test scope - don't filter out any scopes - note that the test scopes can take a looong time..
            filteredScopes = new String[]{};
        }

        mavenUtil = new MavenUtil(new PrintStream(logBuffer),filteredScopes);

        mode = Mode.PENDING;
        String key = getKey();
        runningTasks.put(key,this);
    }


    public void addRepository(String url, String username, String password){
        mavenUtil.addRepository(url, username, password);
    }


    public String call() throws Exception {
        String key = getKey();
        try {
            mode = Mode.RUNNING;

            ArdoqMavenImport ardoqMavenImport = new ArdoqMavenImport(config.getProtocol()+"://"+config.getHost(),imp.getWorkspace(), imp.getOrganization(), imp.getToken());

            List<String> artifactList = new LinkedList<String>();
            for(String s:imp.getArtifact().split("[, ]")){
                if(s.trim().length()>0){
                    artifactList.add(s);
                }
            }
            List<String> projectIDs = ardoqMavenImport.startImport(artifactList,mavenUtil);
            componentID = projectIDs.get(0);
            workspaceID = ardoqMavenImport.getWorkspaceID();

            mode = Mode.DONE;
        } catch (Exception e) {
            mode = Mode.ERROR;
            logger.error("Error importing",e);
            logBuffer.write(e.getMessage().getBytes());
        }
        return key;
    }


    public void emptyOutputBufferInto(List<String> target){
        logBuffer.emptyOutputBufferInto(target);
    }


    public String getKey() {
        String key = imp.getOrganization()+"-"+imp.getArtifact();
        return key;
    }


    public Mode getMode() {
        return mode;
    }


    public static class LogBuffer extends OutputStream{
        ConcurrentLinkedQueue<String> outputBuffer = new ConcurrentLinkedQueue<String>();
        StringBuffer currentLine = new StringBuffer();

        @Override
        public void write(int b) throws IOException {
            char ch = (char)b;
            System.out.print(ch);
            if('\n'==ch){
                synchronized(outputBuffer) {
                    outputBuffer.add(currentLine.toString());
                    currentLine = new StringBuffer();
                }
            }else{
                currentLine.append(ch);
            }
        }

        public void emptyOutputBufferInto(List<String> target){
            synchronized(outputBuffer) {
                while(outputBuffer.size()>0){
                    target.add(outputBuffer.remove());
                }
            }
        }

    }


    public String getWorkspaceID() {
        return this.workspaceID;
    }

    public void setWorkspaceID(String workspaceID) {
        this.workspaceID = workspaceID;
    }

    public String getComponentID() {
        return this.componentID;
    }

    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

}
