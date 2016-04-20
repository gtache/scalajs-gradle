package com.github.gtache.testing

import java.lang.annotation.Annotation
import java.net.{URL, URLClassLoader}
import java.nio.file.Paths

import sbt.testing.{AnnotatedFingerprint, Fingerprint, SubclassFingerprint, TaskDef}

import scala.collection.mutable.ArrayBuffer

object ClassScanner {

  def scan(classL: URLClassLoader, fingerprints: Array[Fingerprint]): Array[TaskDef] = {

    def checkSuperclasses(c: Class[_], sF: SubclassFingerprint): Boolean = {

      def checkName(c: Class[_], fName: String): Boolean = {
        c.getName == fName || c.getSimpleName == fName || c.getCanonicalName == fName
      }

      val fName = sF.superclassName()
      if (checkName(c,fName)){
        return true
      }
      var sC = c.getSuperclass
      while (sC != null) {
        if (checkName(sC, fName)) {
          return true
        } else {
          sC = sC.getSuperclass
        }
      }
      false
    }

    val classes = parseClasses(classL)
    val buffer = ArrayBuffer[TaskDef]()
    classes.foreach(c => {
      fingerprints.foreach {
        case aF: AnnotatedFingerprint => {
          try {
            if (c.isAnnotationPresent(Class.forName(aF.annotationName()).asInstanceOf[Class[_ <: Annotation]])) {
              buffer += new TaskDef(c.getCanonicalName, aF, false, Array.empty)
            }
          } catch {
            case e: ClassNotFoundException => {
              Console.err.println("Class not found for annotation : " + aF.annotationName())
            }
          }
        }
        case sF: SubclassFingerprint => {
          if (checkSuperclasses(c, sF)) {
            if (!sF.requireNoArgConstructor || (sF.requireNoArgConstructor && c.getConstructor() != null)) {
              buffer += new TaskDef(c.getCanonicalName, sF, false, Array.empty)
            }
          }
        }
        case _ => throw new IllegalArgumentException("Unsupported Fingerprint type")
      }
    })
    buffer.toArray
  }

  def parseClasses(classL: URLClassLoader): Array[Class[_]] = {
    val buffer = ArrayBuffer.empty[Class[_]]
    def parseClasses(url: URL): Array[Class[_]] = {
      val f = Paths.get(url.toURI).toFile
      val packageName = {
        if (url != classL.getURLs()(0)) {
          classL.getURLs()(0).toURI.relativize(url.toURI).toString.replace('/','.')
        } else {
          ""
        }
      }
      if (f.isDirectory) {

        /*
        f.listFiles().flatMap(file => {
          if (!file.isDirectory && file.getName.endsWith(".class")) {
            val path = file.getPath
            classL.loadClass(path.substring(0, path.indexOf('.')))
          } else if (file.isDirectory) {
            parseClasses(file.toURI.toURL)
          } else {
            Array.empty[Class[_]]
          }
        })
        */
        val buffer = ArrayBuffer.empty[Class[_]]
        f.listFiles().foreach(file => {
          if (!file.isDirectory && file.getName.endsWith(".class")) {
            val name = file.getName
            buffer += classL.loadClass(packageName+name.substring(0, name.indexOf('.')))
          } else if (file.isDirectory) {
            parseClasses(file.toURI.toURL).foreach(c => {
              buffer += c
            })
          }
        })
        buffer.toArray
      } else {
        if (f.getName.endsWith(".class")) {
          val name = f.getName
          Array(classL.loadClass(packageName+name.substring(0, name.indexOf('.'))))
        } else {
          Array.empty[Class[_]]
        }
      }
    }

    classL.getURLs.foreach(url => {
      val f = Paths.get(url.toURI).toFile
      if (!f.isDirectory && f.getName.endsWith(".class")) {
        val name = f.getName
        buffer += classL.loadClass(name.substring(0, name.indexOf('.')))
      } else if (f.isDirectory) {
        parseClasses(url).foreach(c => {
          buffer += c
        })
      }
    })
    buffer.toArray

    /*
    classL.getURLs.flatMap(url => {
      val f = Paths.get(url.toURI).toFile
      if (!f.isDirectory && f.getName.endsWith(".class")) {
        val path = f.getPath
        classL.loadClass(path.substring(0, path.indexOf('.')))
      } else if (f.isDirectory) {
        parseClasses(url)
      } else {
        Array.empty
      }
    })
    */
  }
}


