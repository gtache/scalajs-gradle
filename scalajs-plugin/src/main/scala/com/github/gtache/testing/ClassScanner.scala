package com.github.gtache.testing

import java.io.File
import java.lang.annotation.Annotation
import java.net.{URL, URLClassLoader}
import java.nio.file.Paths

import sbt.testing._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.runtime.universe._

object ClassScanner {

  /**
    * Finds all classes contained in an URLClassLoader which match to a fingerprint, or only those specified in explicitelySpecified
    * minus the ones in excluded
    *
    * @param classL               The URLClassLoader
    * @param fingerprints         The fingerprints to
    * @param explicitelySpecified A set of String to use as regex
    * @param excluded             A set of String to use as regex
    * @return The TaskDefs found by the scan
    */
  def scan(classL: URLClassLoader, fingerprints: Array[Fingerprint], explicitelySpecified: Set[String] = Set.empty, excluded: Set[String] = Set.empty): Array[TaskDef] = {
    
    def checkSuperclasses(c: Class[_], sF: SubclassFingerprint): Boolean = {

      def checkName(c: Class[_], fName: String): Boolean = {
        c.getName == fName || c.getSimpleName == fName || c.getCanonicalName == fName
      }


      def checkRec(c: Class[_], fName: String): Boolean = {
        if (checkName(c, fName)) {
          true
        } else {
          var sC = c.getSuperclass
          while (sC != null) {
            if (checkRec(sC, fName)) {
              return true
            } else {
              sC = sC.getSuperclass
            }
          }
          c.getInterfaces.foreach(interf => {
            if (checkRec(interf, fName)) {
              return true
            }
          })
          false
        }
      }

      checkRec(c, sF.superclassName())
    }

    val objSuffix = "$"
    val classes = parseClasses(classL, explicitelySpecified, excluded)
    val buffer = ArrayBuffer[TaskDef]()
    classes.foreach(c => {
      fingerprints.foreach {
        case aF: AnnotatedFingerprint => {
          try {
            val mirror = runtimeMirror(classL)
            val symb = mirror.classSymbol(c)
            val annotations = symb.annotations
            if ((c.isAnnotationPresent(Class.forName(aF.annotationName(), false, classL).asInstanceOf[Class[_ <: Annotation]])
              || annotations.exists(a => a.tree.tpe.toString == aF.annotationName()))
              && (aF.isModule || (!aF.isModule && !c.getName.endsWith(objSuffix)))) {
              buffer += new TaskDef(c.getName.stripSuffix(objSuffix), aF, explicitelySpecified.nonEmpty, Array(new SuiteSelector))
            }
          } catch {
            case e: ClassNotFoundException => {
              Console.err.println("Class not found for annotation : " + aF.annotationName())
            }
          }
        }
        case sF: SubclassFingerprint => {
          if (checkSuperclasses(c, sF)) {
            if (!sF.requireNoArgConstructor || c.isInterface || (sF.requireNoArgConstructor && checkZeroArgsConstructor(c))
              && (sF.isModule || (!sF.isModule && !c.getName.endsWith(objSuffix)))) {
              buffer += new TaskDef(c.getName.stripSuffix(objSuffix), sF, explicitelySpecified.nonEmpty, Array(new SuiteSelector))
            }
          }
        }
        case _ => throw new IllegalArgumentException("Unsupported Fingerprint type")
      }
    })
    buffer.toArray.distinct
  }

  /**
    * Checks if the given class has a constructor with zero arguments
    *
    * @param c The class
    * @return true or false
    */
  def checkZeroArgsConstructor(c: Class[_]): Boolean = {
    c.getDeclaredConstructors.foreach(cons => {
      if (cons.getParameterCount == 0) {
        return true
      }
    })
    false
  }

  /**
    * Finds all classes in a URLClassLoader, or only those specified by explicitelySpecified
    * minus the ones in excluded
    *
    * @param classL               The URLClassLoader
    * @param explicitelySpecified A set of String to use as regex
    * @param excluded             A set of String to use as regex
    * @return the classes
    */
  def parseClasses(classL: URLClassLoader, explicitelySpecified: Set[String] = Set.empty, excluded: Set[String] = Set.empty): Array[Class[_]] = {

    val URIPathSep = '/'
    val extSep = '.'
    val ext = extSep + "class"

    def checkSpecific(name: String): Boolean = {
      !excluded.exists(s => s.r.pattern.matcher(name).matches()) &&
        (explicitelySpecified.isEmpty || explicitelySpecified.exists(s => s.r.pattern.matcher(name).matches()))
    }

    def checkAndAddFile(file: File, buffer: ArrayBuffer[Class[_]], meth: () => Unit, packageName: String = ""): Unit = {
      if (!file.isDirectory && file.getName.endsWith(ext)) {
        val fileName = file.getName
        val name = packageName + fileName.substring(0, fileName.indexOf(extSep))
        if (checkSpecific(name)) {
          buffer += classL.loadClass(name)
        }
      } else if (file.isDirectory) {
        meth()
      }
    }
    def parseClasses(url: URL, idx: Int, explicitelySpecified: Set[String] = Set.empty, excluded: Set[String] = Set.empty): Array[Class[_]] = {
      val f = Paths.get(url.toURI).toFile
      val packageName = {
        if (url != classL.getURLs()(idx)) {
          classL.getURLs()(idx).toURI.relativize(url.toURI).toString.replace(URIPathSep, extSep)
        } else {
          ""
        }
      }
      if (f.isDirectory) {
        val buffer = ArrayBuffer.empty[Class[_]]
        f.listFiles().foreach(file => {
          checkAndAddFile(file, buffer, () => parseClasses(file.toURI.toURL, idx, explicitelySpecified, excluded).foreach(c => {
            buffer += c
          }), packageName)
        })
        buffer.toArray
      } else {
        if (f.getName.endsWith(ext)) {
          val fileName = f.getName
          val name = fileName.substring(0, fileName.indexOf(extSep))
          if (checkSpecific(name)) {
            Array(classL.loadClass(packageName + name.substring(0, name.indexOf(extSep))))
          } else {
            Array.empty[Class[_]]
          }
        } else {
          Array.empty[Class[_]]
        }
      }
    }

    val buffer = ArrayBuffer.empty[Class[_]]
    classL.getURLs.zipWithIndex.foreach(url => {
      val f = Paths.get(url._1.toURI).toFile
      checkAndAddFile(f, buffer, () => parseClasses(url._1, url._2, explicitelySpecified, excluded).foreach(c => {
        buffer += c
      }))
    })
    buffer.toArray
  }
}


