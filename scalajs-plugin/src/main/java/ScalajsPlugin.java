/**
 * Created by Guillaume on 09.03.2016.
 */

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.scala.ScalaCompile;

public class ScalajsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();
        CleanAllTask cleanAll = (CleanAllTask) tasks.create("CleanAll", CleanAllTask.class).dependsOn("clean");
        //cleanAll.setToDelete()
        CreateDirsTask createDirs = (CreateDirsTask) tasks.create("CreateDirs", CreateDirsTask.class);
        //createDirs.setToCreate(files(sjsirDir,jsDir))
        FastOptJSTask fastOptJS = (FastOptJSTask) tasks.create("FastOptJS", FastOptJSTask.class).dependsOn("compileScala");
        //fastOptJS.setSrcDir();
        //fastOptJS.setDestFile();
        CopyJSTask copyJS = (CopyJSTask) tasks.create("CopyJS", CopyJSTask.class).dependsOn("FastOptJS");
        //copyJS.from()
        //copyJS.into()
        AddMethExecTask addMethExec = (AddMethExecTask) tasks.create("AddMethExec", AddMethExecTask.class).dependsOn("CopyJS");
        //addMethExec.setClassname();
        //addMethExec.setMethname();
        //addMethExec.setSrcfile();
        RunJSTask runJS = (RunJSTask) tasks.create("RunJS", RunJSTask.class).dependsOn("AddMethExec");
        //runJS.setToExec(project.file(jsFile));
    }
}
