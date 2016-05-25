package com.github.gtache.testing

import java.net.{URL, URLClassLoader}

import com.github.gtache.Utils
import org.junit.Assert._
import org.junit.Test
import sbt.testing.{AnnotatedFingerprint, Fingerprint, SubclassFingerprint}

import scala.annotation.StaticAnnotation

class ClassScannerTest {

  val packageName = "com.github.gtache.testing."

  val annFingerprint = new AnnotatedFingerprint {
    override def isModule: Boolean = true

    override def annotationName(): String = packageName + "JavaAnnotation"
  }

  val scalAnnFingerprint = new AnnotatedFingerprint {
    override def isModule: Boolean = false

    override def annotationName(): String = packageName + "ScalaAnnotation"
  }

  val subFingerprint = new SubclassFingerprint {
    override def isModule: Boolean = true

    override def superclassName(): String = packageName + "A"

    override def requireNoArgConstructor(): Boolean = true
  }

  val subFingerprint2 = new SubclassFingerprint {
    override def requireNoArgConstructor(): Boolean = false

    override def isModule: Boolean = true

    override def superclassName(): String = packageName + "G"
  }

  val subFingerprint3 = new SubclassFingerprint {
    override def requireNoArgConstructor(): Boolean = true

    override def isModule: Boolean = true

    override def superclassName(): String = packageName + "K"
  }

  val fingerprints: Array[Fingerprint] = Array(annFingerprint, scalAnnFingerprint, subFingerprint, subFingerprint2, subFingerprint3)
  val test: URL = this.getClass.getResource("../../../../")
  val loader = new URLClassLoader(Array(test))

  val explicitlySpecified: Set[String] = Set("*A*", "*B", "*C").map(Utils.toRegex)
  val excluded = Set("*C", "*H").map(Utils.toRegex)
  val excludedAll: Set[String] = Set(Utils.toRegex("com.*"))
  val explicitlyEmpty: Set[String] = Set(Utils.toRegex("co.*"))
  val all = Set(packageName + "A", packageName + "AB", packageName + "B", packageName + "C", packageName + "D",
    packageName + "E", packageName + "F", packageName + "G", packageName + "H", packageName + "I", packageName + "J",
    packageName + "K", packageName + "L", packageName + "M", packageName + "N")

  @Test
  def testScannerBasic(): Unit = {
    val taskDefs = ClassScanner.scan(loader, fingerprints)

    val nameTasks = taskDefs.map(t => t.fullyQualifiedName())
    val contained = Set(packageName + "A", packageName + "AB", packageName + "B", packageName + "C", packageName + "D", packageName + "F",
      packageName + "G", packageName + "H", packageName + "J", packageName + "K", packageName + "M")
    checkContains(nameTasks.toSet, contained, all)


    val map = nameTasks.zip(taskDefs).toMap
    assertTrue(map.get(packageName + "A").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "AB").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "B").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "C").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "D").get.fingerprint.isInstanceOf[AnnotatedFingerprint])
    assertTrue(map.get(packageName + "F").get.fingerprint.isInstanceOf[AnnotatedFingerprint])
    assertTrue(map.get(packageName + "G").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "H").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "J").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "K").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "M").get.fingerprint.isInstanceOf[AnnotatedFingerprint])
  }

  @Test
  def testScannerExplicitely(): Unit = {
    val taskDefs = ClassScanner.scan(loader, fingerprints, explicitlySpecified)
    val nameTasks = taskDefs.map(t => t.fullyQualifiedName())
    val contained = Set(packageName + "A", packageName + "AB", packageName + "B", packageName + "C")
    checkContains(nameTasks.toSet, contained, all)

    val map = nameTasks.zip(taskDefs).toMap
    assertTrue(map.get(packageName + "A").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "AB").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "B").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "C").get.fingerprint.isInstanceOf[SubclassFingerprint])
  }

  def checkContains(nameTasks: Set[String], contained: Set[String], all: Set[String]): Unit = {
    contained.foreach { s =>
      assertTrue(s + " in " + contained.mkString(" ; "), nameTasks.contains(s))
    }
    all.filterNot(contained).foreach { s =>
      assertFalse(s + " not in " + contained.mkString(" ; "), nameTasks.contains(s))
    }
  }

  @Test
  def testScannerExcluded(): Unit = {
    val taskDefs = ClassScanner.scan(loader, fingerprints, explicitlySpecified, excluded)
    val nameTasks = taskDefs.map(t => t.fullyQualifiedName())
    val contained = Set(packageName + "A", packageName + "AB", packageName + "B")
    checkContains(nameTasks.toSet, contained, all)

    val map = nameTasks.zip(taskDefs).toMap
    assertTrue(map.get(packageName + "A").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "AB").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get(packageName + "B").get.fingerprint.isInstanceOf[SubclassFingerprint])
  }

  @Test
  def testScannerExcludedAll(): Unit = {
    var taskDefs = ClassScanner.scan(loader, fingerprints, explicitlySpecified, excludedAll)
    assertTrue(taskDefs.isEmpty)
    taskDefs = ClassScanner.scan(loader, fingerprints, Set.empty, excludedAll)
    assertTrue(taskDefs.isEmpty)
  }

  @Test
  def testScannerExcludedEmpty(): Unit = {
    val taskDefs = ClassScanner.scan(loader, fingerprints, explicitlyEmpty)
    assertTrue(taskDefs.isEmpty)
  }


}

class A

class B extends A

class C extends B

@JavaAnnotation
class D

class E(s: String) extends A {

}

@JavaAnnotation
class F(s: String) extends A {

}

class G(s: String)

class H(s: String, i: Int) extends G(s)

class I

trait J extends K

trait K

trait L

object AB extends I with J

@ScalaAnnotation
class M

@ScalaAnnotation
object N

class ScalaAnnotation extends StaticAnnotation