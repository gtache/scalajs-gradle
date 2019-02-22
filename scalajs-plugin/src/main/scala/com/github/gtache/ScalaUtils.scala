package com.github.gtache

import java.io.File
import java.net.{URL, URLClassLoader}
import java.nio.file.{AccessDeniedException, Files}
import java.util

import com.github.gtache.Scalajsld.{MIN_OUTPUT, OUTPUT}
import com.github.gtache.testing.TestFramework
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.FileCollection
import org.scalajs.core.ir.Utils.escapeJS
import org.scalajs.core.tools.io.{FileVirtualJSFile, MemVirtualJSFile}
import org.scalajs.core.tools.jsdep.ResolvedJSDependency
import org.scalajs.core.tools.linker.backend.ModuleKind
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.{PhantomJSEnv, PhantomJettyClassLoader}
import org.scalajs.jsenv.rhino.RhinoJSEnv

import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConverters, mutable}

object ScalaUtils {
  val JETTY_SERVER_VERSION: String = "8.1.16.v20140903"
  val JETTY_WEBSOCKET_VERSION: String = "8.1.16.v20140903"

  val CPSeparator: String = File.pathSeparator
  val JAVA_OPT: String = "javaOpt"

  //LogLevel
  val ERROR_LVL: String = "error"
  val WARN_LVL: String = "warn"
  val INFO_LVL: String = "info"
  val DEBUG_LVL: String = "debug"

  //Envs
  val RHINO: String = "rhino"
  val PHANTOM: String = "phantom"
  val JSENV: String = "jsEnv"
  val JSDOM: String = "jsDom"

  //All parameters for generated js file
  val JS_REL_DIR: String = File.separator + "js" + File.separator
  val EXT: String = ".js"
  val FULLOPT_SUFFIX: String = "_fullopt" + EXT
  val FASTOPT_SUFFIX: String = "_fastopt" + EXT
  val TEST_SUFFIX: String = "_test"
  val NOOPT_TEST_SUFFIX: String = TEST_SUFFIX + EXT
  val FULLOPT_TEST_SUFFIX: String = TEST_SUFFIX + FULLOPT_SUFFIX
  val FASTOPT_TEST_SUFFIX: String = TEST_SUFFIX + FASTOPT_SUFFIX

  val RUN_FAST: String = "runFast"
  val RUN_FULL: String = "runFull"
  val RUN_NOOPT: String = "runNoOpt"

  //Module
  val COMMONJS_MODULE: String = "commonJSModule"
  val ES_MODULE: String = "esModule"
  val USE_MAIN_MODULE: String = "useMainModuleInit"

  val TEST_FRAMEWORKS: String = "testFrameworks"

  private var graph: TaskExecutionGraph = _

  def stripRight(str: String, suffix: String): String = {
    str.stripSuffix(suffix)
  }

  /**
    * Resolves the level of logging, depending on the project properties
    *
    * @param project  The project with the property to check
    * @param property The property used on the switch
    * @return The level of logging (default : Debug)
    */
  def resolveLogLevel(project: Project, property: String, base: Level): Level = {
    if (project.hasProperty(property)) {
      val level = project.property(property).asInstanceOf[String].toLowerCase
      if (level == ERROR_LVL) {
        Level.Error
      } else if (level == WARN_LVL) {
        Level.Warn
      } else if (level == INFO_LVL) {
        Level.Info
      } else if (level == DEBUG_LVL) {
        Level.Debug
      } else {
        project.getLogger.warn("Unknown log level : " + project.property(property) + ". Assuming " + base)
        base
      }
    } else base
  }

  /**
    * Resolves the environment to use, depending on the project properties
    *
    * @param project The project with properties to check
    * @return The environment to use (Default : Node)
    */
  def resolveEnv(project: Project): JSEnv = {
    if (project.hasProperty(JSENV)) {
      project.property(JSENV) match {
        case e: JSEnv => e
        case x =>
          project.getLogger.error("JSEnv not of type JSEnv ; was " + x.getClass)
          null
      }
    } else if (project.hasProperty(RHINO)) {
      //TODO will be deprecated
      new RhinoJSEnv(Scalajsld.getOptions.semantics, false)
    } else if (project.hasProperty(PHANTOM)) {
      val phantomConfig = project.getConfigurations.getByName("phantomJetty")
      val jars: ArrayBuffer[URL] = ArrayBuffer()
      phantomConfig.iterator().forEachRemaining((t: File) => {
        if (t.getAbsolutePath.contains("jetty")) {
          jars.append(t.toURI.toURL)
        }
      })
      val loader: PhantomJettyClassLoader = new PhantomJettyClassLoader(new URLClassLoader(jars.toArray), project.getBuildscript.getClassLoader)

      val config = PhantomJSEnv.Config()
        .withExecutable("phantomjs")
        .withArgs(List.empty)
        .withEnv(Map.empty)
        .withAutoExit(true)
        .withJettyClassLoader(loader)

      new PhantomJSEnv(config)
    } else if (project.hasProperty(JSDOM)) {
      val config = JSDOMNodeJSEnv.Config()
        .withExecutable("node")
        .withArgs(List.empty)
        .withEnv(Map.empty)

      new JSDOMNodeJSEnv(config)
    } else {
      val config = NodeJSEnv.Config()
        .withExecutable("node")
        .withArgs(List.empty)
        .withEnv(Map.empty)
        .withSourceMap(true)

      new NodeJSEnv(config)
    }
  }

  /**
    * Returns the minimal ResolvedDependency Seq, which is composed of only the generated js file.
    *
    * @param project The project
    * @return the (mutable) seq of only one element
    */
  def getMinimalDependencySeq(project: Project): Seq[ResolvedJSDependency] = {
    val file = new FileVirtualJSFile(project.file(resolvePath(project)))
    val fileD = ResolvedJSDependency.minimal(file)
    val configurations = resolveConfigurationLibs(project)
    val dependencySeq = ArrayBuffer[ResolvedJSDependency]()
    dependencySeq.append(fileD)
    if (configurations != null) {
      dependencySeq.append(configurations)
    }
    dependencySeq.toSeq
  }


  def resolveConfigurationLibs(project: Project): ResolvedJSDependency = {
    val javaSystemProperties = resolveJavaSystemProperties(project)
    if (javaSystemProperties.isEmpty) {
      null
    } else {
      val formattedProps = javaSystemProperties.map({ p =>
        "\"" + escapeJS(p._1) + "\": \"" + escapeJS(p._2) + "\""
      })
      formattedProps.foreach(project.getLogger.warn(_))
      val code =
        "var __ScalaJSEnv = (typeof __ScalaJSEnv === \"object\" && __ScalaJSEnv) ? __ScalaJSEnv : {};\n" +
          "__ScalaJSEnv.javaSystemProperties = {" + formattedProps.mkString(", ") + "};\n"

      ResolvedJSDependency.minimal(new MemVirtualJSFile("setJavaSystemProperties.js").withContent(code))
    }
  }

  def resolveJavaSystemProperties(project: Project): Map[String, String] = {
    if (project.hasProperty(JAVA_OPT)) {
      val options = mutable.Map[String, String]()
      val javaOpt = project.property(JAVA_OPT).asInstanceOf[String]
      val beginning = javaOpt.substring(0, 2)
      if (beginning == "-D") {
        val pairs = javaOpt.substring(2, javaOpt.length()).split(CPSeparator)
        pairs.foreach({ pair =>
          val keyV = pair.split("=")
          if (keyV.length != 2) {
            project.getLogger.error("javaOpt can only be \"-D<key1>=<value2>" + CPSeparator + "<key2>=<value2>...\", but received " + project.property(JAVA_OPT))
            throw new IllegalArgumentException()
          } else {
            options.put(keyV(0), keyV(1))
          }
        })
        options.foreach(pair => project.getLogger.warn(pair._1 + "->" + pair._2))
        options.toMap
      } else {
        project.getLogger.error("javaOpt can only be \"-D<key1>=<value2>" + CPSeparator + "<key2>=<value2>...\", but received " + project.property(JAVA_OPT))
        throw new IllegalArgumentException()
      }
    } else Map.empty
  }

  /**
    * Resolves the path of the file to run, depending on full, fast or no optimization
    *
    * @return The path of the file
    */
  def resolvePath(project: Project): String = {
    val buildPath = project.getBuildDir.getAbsolutePath
    val jsPath = buildPath + JS_REL_DIR
    val baseFilename = jsPath + project.getName
    if (graph == null) {
      project.getLogger.warn("TaskGraph not ready yet : Possible error when computing output file\n" +
        "Run with TestJS explicitly to be sure it works")
    }
    //Performs suboptimal check if TaskExecutionGraph not ready
    val hasTest = if (graph != null) graph.hasTask(":TestJS") else isTaskInStartParameter(project, "testjs")
    if (project.hasProperty(MIN_OUTPUT)) {
      project.file(project.property(MIN_OUTPUT)).getAbsolutePath
    } else if (project.hasProperty(OUTPUT)) {
      project.file(project.property(OUTPUT)).getAbsolutePath
    } else if (project.hasProperty(RUN_FULL)) {
      if (hasTest) {
        project.file(baseFilename + FULLOPT_TEST_SUFFIX).getAbsolutePath
      } else {
        project.file(baseFilename + FULLOPT_SUFFIX).getAbsolutePath
      }
    } else if (project.hasProperty(RUN_NOOPT)) {
      if (hasTest) {
        project.file(baseFilename + NOOPT_TEST_SUFFIX).getAbsolutePath
      } else {
        project.file(baseFilename + EXT).getAbsolutePath
      }
    } else {
      if (hasTest) {
        project.file(baseFilename + FASTOPT_TEST_SUFFIX).getAbsolutePath
      } else {
        project.file(baseFilename + FASTOPT_SUFFIX).getAbsolutePath
      }
    }
  }

  /**
    * Checks if there is a task to be executed (explicitely specified)
    *
    * @param project The project whose startparameters we want to use
    * @param task    The name task to be checked
    */
  def isTaskInStartParameter(project: Project, task: String): Boolean = {
    val tasks = JavaConverters.asScalaBuffer(project.getGradle.getStartParameter.getTaskNames).map(x => x.toLowerCase())
    tasks.contains(task.toLowerCase())
  }

  /**
    * Prepares the TaskExecutionGraph for the given project
    *
    * @param project The gradle project
    */
  def prepareGraph(project: Project): Unit = {
    graph = null //Need to reset, as if we are using a daemon it will be saved
    project.getGradle.getTaskGraph.whenReady(x => graph = x)
  }

  /**
    * Returns the list of custom TestFrameworks added by the user
    *
    * @param project The project
    * @return A list of TestFramework
    */
  def resolveTestFrameworks(project: Project): List[TestFramework] = {
    if (project.hasProperty(TEST_FRAMEWORKS)) {
      val frameworksName = JavaConverters.asScalaBuffer(project.property(TEST_FRAMEWORKS).asInstanceOf[util.List[String]])
      frameworksName.map({ f =>
        new TestFramework(Array(f): _*)
      }).toList
    } else {
      List.empty
    }
  }

  def resolveModuleKind(project: Project): ModuleKind = {
    if (project.hasProperty(COMMONJS_MODULE)) {
      ModuleKind.CommonJSModule
    } else if (project.hasProperty(ES_MODULE)) {
      ModuleKind.ESModule
    } else {
      ModuleKind.NoModule
    }
  }

  /**
    * Deletes a file, if it is a folder, deletes it recursively.
    *
    * @param file The file to delete
    */
  def deleteRecursive(file: File): Unit = {
    if (file.exists()) {
      if (file.isDirectory) {
        file.listFiles().foreach({ f => deleteRecursive(f) })
      }
      try {
        if (Files.isWritable(file.toPath)) {
          if (!(file.isDirectory && file.listFiles().length > 0)) {
            Files.deleteIfExists(file.toPath)
          }
        }
      }
      catch {
        case e: AccessDeniedException =>
        case e: Exception => throw e
        //Files.isWritable doesnt work 100% apparently => can't use project.delete, throws exception sometimes
      }
    }
  }

  /**
    * Returns all the files in a directory recursively
    *
    * @param file The file to print
    */
  def listRecursive(file: File): List[String] = {
    val files = ArrayBuffer[String]()
    if (file.isDirectory) {
      file.listFiles().foreach({ f =>
        files.appendAll(listRecursive(f))
      })
    } else {
      files.append(file.getName)
    }
    files.toList
  }

  /**
    * Returns all the files in a FileCollection recursively
    *
    * @param fileC   The FileCollection
    * @param project For logging (printing)
    */
  def listRecursiveC(fileC: FileCollection, project: Project): List[String] = {
    val files = ArrayBuffer[String]()
    fileC.getFiles.forEach({ f =>
      files.appendAll(listRecursive(f))
    })
    files.toList
  }

  def listClasspathC(fileC: FileCollection, ext: String = "") {
    val ret = ArrayBuffer[(String, String)]()
    fileC.getFiles.forEach({ f =>
      ret.appendAll(listClasspath(f, "", ext))
    })
    ret.toList
  }

  def listClasspath(file: File, pckge: String = "", ext: String = ""): List[(String, String)] = {
    val ret = ArrayBuffer[(String, String)]()
    if (file.isDirectory) {
      file.listFiles().foreach({ f =>
        if (f.isFile && f.getAbsolutePath.endsWith(ext)) {
          ret.append((pckge + "." + f.getName, f.getAbsolutePath))
        } else if (f.isDirectory) {
          ret.appendAll(listClasspath(f, pckge + "." + f.getName, ext))
        }
      })
    } else {
      if (file.isFile && file.getAbsolutePath.endsWith(ext)) {
        ret.append((pckge + "." + file.getName, file.getAbsolutePath))
      }
    }
    ret.toList
  }

  /**
    * Takes a wildcard and returns a regex corresponding to it
    * Example : com.github.* => com\\.github\\..*
    *
    * @param s The string to transform
    * @return The regex
    */
  def toRegex(s: String): String = {
    s.map({ c =>
      if (c == '*') {
        ".*"
      } else if (c == '?') {
        "."
      } else if (c == '.' ||
        c == '[' || c == ']' ||
        c == '(' || c == ')' ||
        c == '^' || c == '$' ||
        c == '|' || c == '\\') {
        "\\" + c
      } else {
        c.toString
      }
    }).mkString("")
  }

}
