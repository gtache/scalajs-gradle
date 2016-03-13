/**
 * Created by Guillaume on 09.03.2016.
 */

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;

public class ScalajsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final File sjsirDir = project.file("sjsir/");
        final File jsDir = project.file("js/");
        final File jsFile = project.file(jsDir + project.getName() + ".js");
        final File jsExecFile = project.file(jsDir + project.getName() + "_exec.js");

        TaskContainer tasks = project.getTasks();

        CleanAllTask cleanAll = tasks.create("CleanAll", CleanAllTask.class);
        cleanAll.dependsOn("clean");
        cleanAll.setToDelete(project.files(sjsirDir, jsDir));

        CreateDirsTask createDirs = tasks.create("CreateDirs", CreateDirsTask.class);
        createDirs.setToCreate(project.files(sjsirDir, jsDir));

        FastOptJSTask fastOptJS = tasks.create("FastOptJS", FastOptJSTask.class);
        fastOptJS.dependsOn("compileScala");
        fastOptJS.setSrcDir(sourceSets.main.runtimeClasspath);
        fastOptJS.setDestFile(jsFile);

        CopyJSTask copyJS = tasks.create("CopyJS", CopyJSTask.class);
        copyJS.dependsOn("FastOptJS");
        copyJS.from(jsFile);
        copyJS.into(jsExecFile);

        AddMethExecTask addMethExec = tasks.create("AddMethExec", AddMethExecTask.class);
        addMethExec.dependsOn("CopyJS");
        addMethExec.setSrcFile(jsExecFile);

        RunJSTask runJS = tasks.create("RunJS", RunJSTask.class);
        runJS.dependsOn("AddMethExec");
        runJS.setToExec(jsExecFile);
    }
}
