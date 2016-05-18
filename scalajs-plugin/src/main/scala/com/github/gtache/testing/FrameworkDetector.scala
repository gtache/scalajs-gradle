package com.github.gtache.testing

import com.github.gtache.testing.FrameworkDetector.StoreConsole
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.json._
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
final class FrameworkDetector(jsEnv: JSEnv) {


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
    import FrameworkDetector.ConsoleFrameworkPrefix
    val data = frameworks.map(_.classNames.toList).toList.toJSON

    val code =
      s"""
      var data = ${jsonToString(data)};
      function frameworkExists(name) {
        var parts = name.split(".");
        var obj = $jsGlobalExpr;
        for (var i = 0; i < parts.length; ++i) {
          obj = obj[parts[i]];
          if (obj === void 0)
            return false;
        }
        return true;
      }
      for (var i = 0; i < data.length; ++i) {
        var gotOne = false;
        for (var j = 0; j < data[i].length; ++j) {
          if (frameworkExists(data[i][j])) {
            console.log("$ConsoleFrameworkPrefix" + data[i][j]);
            gotOne = true;
            break;
          }
        }
        if (!gotOne) {
          // print an empty line with prefix to zip afterwards
          console.log("$ConsoleFrameworkPrefix");
        }
      }
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


}

object FrameworkDetector {

  private val ConsoleFrameworkPrefix = "@scalajs-test-framework-detector:"

  private class StoreConsole extends JSConsole {
    val buf = mutable.Buffer.empty[String]

    def log(msg: Any): Unit = buf += msg.toString
  }


}
