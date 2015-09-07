package com.ardoq.addon.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;

import com.ardoq.ArdoqClient;
import com.ardoq.addon.maven.MavenAddonAPI.ImportDefinition;
import com.ardoq.addon.maven.MavenAddonAPI.Mode;
import com.ardoq.addon.maven.MavenAddonConfiguration.ArdoqConfig;
import com.ardoq.mavenImport.ArdoqMavenImport;
import com.ardoq.mavenImport.MavenUtil;
import com.ardoq.mavenImport.ProjectSync;
import com.ardoq.model.Workspace;
import com.ardoq.util.SyncUtil;

public class MavenImportTask implements Callable<String>{

    ArdoqConfig config;
    ImportDefinition imp;
    Map<String, MavenImportTask> runningTasks;
    Mode mode;
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
        mode = Mode.RUNNING;

        ArdoqClient ardoqClient = new ArdoqClient(config.getProtocol()+"://"+config.getHost(),imp.getToken());
        ardoqClient.setOrganization(imp.getOrganization());

        String modelName = "Maven";
        ardoqClient.model().findOrCreate(modelName, ArdoqMavenImport.class.getResourceAsStream("/model.json"));


        String workspace = imp.getWorkspace();
        if(StringUtils.isEmpty(workspace)){
            MavenProject mavenProject = mavenUtil.loadProject(imp.getArtifact());
            workspace = "Maven project "+mavenProject.getName();
        }

        SyncUtil ardoqSync = new SyncUtil(ardoqClient, workspace, modelName);
        ProjectSync projectSync = new ProjectSync(ardoqSync, mavenUtil);

        Workspace workspaceInstance = ardoqSync.getWorkspace();

        String description = "This is an automatically imported workspace, "
                + "based on information from the Maven Project Object Model (POM) with coordinates: ***"+imp.getArtifact()+"***\n"
                + "\n"
                + "> Please don't edit this workspace manually! Changes will be overwritten the next time the import is triggered. If you need more documentation, create a separate workspace and create implicit references into this workspace. \n"
                + "\n"
                + "Import timestamp: "+new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date());
        workspaceInstance.setDescription(description);
        workspaceInstance.setViews(Arrays.asList("processflow","componenttree","tableview","reader","integrations"));
        projectSync.syncProject(imp.getArtifact());
        projectSync.addExclusions(mavenUtil);

        ardoqSync.updateWorkspaceIfDifferent(workspaceInstance);
        ardoqSync.deleteNotSyncedItems();

        runningTasks.remove(key);
        mode = Mode.DONE;
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

}
