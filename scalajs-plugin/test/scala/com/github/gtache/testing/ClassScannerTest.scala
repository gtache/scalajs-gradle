package com.github.gtache.testing

import java.io.File
import java.net.{URL, URLClassLoader}

import org.junit.Test
import org.scalatools.testing.SubclassFingerprint
import sbt.testing.AnnotatedFingerprint

import scala.annotation.Annotation

class ClassScannerTest {

  @Test
  def testScanner(): Unit = {
    val annFingerprint = new AnnotatedFingerprint {
      override def isModule: Boolean = true

      override def annotationName(): String = "com.github.gtache.testing.ToTest"
    }
    val subFingerprint = new SubclassFingerprint {
      override def isModule: Boolean = true

      override def superClassName(): String = "com.github.gtache.testing.A"
    }
    val fingerprints = (annFingerprint, subFingerprint)
    println(this.getClass().getResource("/resources/A.class").toString)
    val loader = new URLClassLoader(Array(this.getClass.getResource("/resources/")))
    val classes = ClassScanner.parseClasses(loader)
    println(classes.length)
    classes.foreach(c => println(c.getName))
  }
}

class A

class B extends A

class C extends B

@ToTest
class D

class ToTest extends Annotation
