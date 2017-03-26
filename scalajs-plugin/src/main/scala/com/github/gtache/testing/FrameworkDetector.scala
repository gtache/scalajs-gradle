package com.github.gtache.testing

import com.github.gtache.testing.FrameworkDetector.StoreConsole
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.json._
import org.scalajs.core.tools.linker.backend.ModuleKind
import org.scalajs.core.tools.logging._
import org.scalajs.jsenv._
import org.scalajs.testadapter.ScalaJSFramework

import scala.collection.mutable

/* see https://github.com/scala-js/scala-js/blob/master/sbt-plugin/src/main/scala/scala/scalajs/sbtplugin/FrameworkDetector.scala */
/**
  * A class used to detect all available common TestFramework in a js file
  *
  * @param jsEnv The environment to use
  */
final class FrameworkDetector(jsEnv: JSEnv,
                              moduleKind: ModuleKind = ModuleKind.NoModule, moduleIdentifier: Option[String] = None) {


  val jsGlobalExpr: String = {
    """((typeof global === "object" && global &&
         global["Object"] === Object) ? global : this)"""
  }

  /**
    * Returns a list of instantiated ScalaJSFramework (one for each detected TestFramework). Calls detect()
    *
    * @param frameworks The list of TestFramework to search
    * @param logger     The logger to use
    * @param console    The jsConsole to use
    * @return The list of ScalaJSFramework
    */
  def instantiatedScalaJSFrameworks(frameworks: Seq[TestFramework], logger: Logger, console: JSConsole): List[ScalaJSFramework] = {
    detect(frameworks).map(pair => {
      new ScalaJSFramework(
        pair._2,
        jsEnv.asInstanceOf[ComJSEnv],
        logger,
        console)
    }).toList
  }

  /**
    * Detects the TestFramework in a jsEnv, given a list of them.
    *
    * @param frameworks The frameworks to detect
    * @return A map linking a framework to it's common name
    */
  def detect(frameworks: Seq[TestFramework]): Map[TestFramework, String] = {
    import com.github.gtache.testing.FrameworkDetector.ConsoleFrameworkPrefix
    val data = frameworks.map(_.classNames.toList).toList.toJSON

    val exportsNamespaceExpr =
      makeExportsNamespaceExpr(moduleKind, moduleIdentifier)

    val code = s"""
      (function(exportsNamespace) {
        "use strict";
        /* #2752: if there is no testing framework at all on the classpath,
         * the testing interface will not be there, and therefore the
         * `detectFrameworks` function will not exist. We must therefore be
         * careful when selecting it.
         */
        var namespace = exportsNamespace;
        namespace = namespace.org || {};
        namespace = namespace.scalajs || {};
        namespace = namespace.testinterface || {};
        namespace = namespace.internal || {};
        var detectFrameworksFun = namespace.detectFrameworks || (function(data) {
          var results = [];
          for (var i = 0; i < data.length; ++i)
            results.push(void 0);
          return results;
        });
        var data = ${jsonToString(data)};
        var results = detectFrameworksFun(data);
        for (var i = 0; i < results.length; ++i) {
          console.log("$ConsoleFrameworkPrefix" + (results[i] || ""));
        }
      })($exportsNamespaceExpr);
    """

    val vf = new MemVirtualJSFile("frameworkDetector.js").withContent(code)
    val console = new StoreConsole

    val runner = jsEnv.jsRunner(vf)
    runner.run(NullLogger, console)

    // Filter jsDependencies unexpected output
    val results = console.buf collect {
      case s if s.startsWith(ConsoleFrameworkPrefix) =>
        s.stripPrefix(ConsoleFrameworkPrefix)
    }

    assert(results.size == frameworks.size)

    (frameworks zip results).filter(_._2.nonEmpty).toMap
  }

  def makeExportsNamespaceExpr(moduleKind: ModuleKind,
                               moduleIdentifier: Option[String]): String = {
    moduleKind match {
      case ModuleKind.NoModule =>
        jsGlobalExpr

      case ModuleKind.CommonJSModule =>
        import org.scalajs.core.ir.Utils.escapeJS
        val moduleIdent = moduleIdentifier.getOrElse {
          throw new IllegalArgumentException(
            "The module identifier must be specified for CommonJS modules")
        }
        s"""require("${escapeJS(moduleIdent)}")"""
    }
  }


}

object FrameworkDetector {

  private val ConsoleFrameworkPrefix = "@scalajs-test-framework-detector:"

  private class StoreConsole extends JSConsole {
    val buf: mutable.Buffer[String] = mutable.Buffer.empty[String]

    def log(msg: Any): Unit = buf += msg.toString
  }


}
