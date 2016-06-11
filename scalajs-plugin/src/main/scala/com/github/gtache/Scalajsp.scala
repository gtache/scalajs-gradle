package com.github.gtache

import java.io.{Console => _, _}
import java.util.zip.ZipFile

import org.scalajs.core.ir.Printers.{IRTreePrinter, InfoPrinter}
import org.scalajs.core.ir.ScalaJSVersions
import org.scalajs.core.tools.io._

import scala.collection.immutable.Seq

/**
  * Object used to translate sjsir files to something readable
  */
object Scalajsp {

  private val stdout =
    new BufferedWriter(new OutputStreamWriter(Console.out, "UTF-8"))

  /**
    * Returns the default options
    *
    * @return the default options
    */
  def defaultOptions: Options = {
    Options()
  }

  /**
    * Executes scalajsp with the given Options
    *
    * @param givenOptions The options to use
    */
  def execute(givenOptions: Options): Unit = {
    for {
      fileName <- givenOptions.fileNames
    } {
      val vfile = givenOptions.jar map { jar =>
        readFromJar(jar, fileName)
      } getOrElse {
        readFromFile(fileName)
      }

      displayFileContent(vfile, givenOptions)
    }
  }

  private def displayFileContent(vfile: VirtualScalaJSIRFile,
                                 opts: Options): Unit = {
    if (opts.infos)
      new InfoPrinter(stdout).print(vfile.info)
    else
      new IRTreePrinter(stdout).printTopLevelTree(vfile.tree)

    stdout.flush()
  }

  private def readFromFile(fileName: String) = {
    val file = new File(fileName)

    if (!file.exists)
      fail(s"No such file: $fileName")
    else if (!file.canRead)
      fail(s"Unable to read file: $fileName")
    else
      FileVirtualScalaJSIRFile(file)
  }

  private def fail(msg: String) = {
    Console.err.println(msg)
    sys.exit(1)
  }

  private def readFromJar(jar: File, name: String) = {
    val jarFile =
      try {
        new ZipFile(jar)
      }
      catch {
        case _: FileNotFoundException => fail(s"No such JAR: $jar")
      }
    try {
      val entry = jarFile.getEntry(name)
      if (entry == null)
        fail(s"No such file in jar: $name")
      else {
        val name = jarFile.getName + "#" + entry.getName
        val content =
          IO.readInputStreamToByteArray(jarFile.getInputStream(entry))
        new MemVirtualSerializedScalaJSIRFile(name).withContent(content)
      }
    } finally {
      jarFile.close()
    }
  }

  /**
    * Prints the supported Scala.js IR versions
    */
  def printSupported(): Unit = {
    import ScalaJSVersions._
    println(s"Emitted Scala.js IR version is: $binaryEmitted")
    println("Supported Scala.js IR versions are")
    binarySupported.foreach(v => println(s"* $v"))
  }

  /**
    * Options used to run Scalajsp
    *
    * @param infos     If we only want the infos about the file and not its content
    * @param jar       If the file to be read is in a jar
    * @param fileNames The files to read
    */
  case class Options(infos: Boolean = false,
                     jar: Option[File] = None,
                     fileNames: Seq[String] = Seq.empty) {

    /**
      * Returns a new Options instance with the given infos value
      *
      * @param newInfos The new infos value
      * @return A new Options
      */
    def withInfos(newInfos: Boolean): Options = {
      this.copy(infos = newInfos)
    }

    /**
      * Returns a new Options instance with the given jar value
      *
      * @param newJar The new jar value
      * @return A new Options
      */
    def withJar(newJar: Option[File]): Options = {
      this.copy(jar = newJar)
    }

    /**
      * Returns a new Options instance with the given fileNames value
      *
      * @param newFilenames The new Filenames value
      * @return A new Options
      */
    def withFileNames(newFilenames: Seq[String]): Options = {
      this.copy(fileNames = newFilenames)
    }

  }

}