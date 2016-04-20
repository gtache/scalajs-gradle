package com.github.gtache.testing

import java.net.URLClassLoader

import org.junit.Assert._
import org.junit.Test
import sbt.testing.{AnnotatedFingerprint, Fingerprint, SubclassFingerprint}

class ClassScannerTest {

  @Test
  def testScanner(): Unit = {
    val annFingerprint = new AnnotatedFingerprint {
      override def isModule: Boolean = true

      override def annotationName(): String = "com.github.gtache.testing.ToTest"
    }

    val subFingerprint = new SubclassFingerprint {
      override def isModule: Boolean = true

      override def superclassName(): String = "com.github.gtache.testing.A"

      override def requireNoArgConstructor(): Boolean = true
    }

    val subFingerprint2 = new SubclassFingerprint {
      override def requireNoArgConstructor(): Boolean = false

      override def isModule: Boolean = true

      override def superclassName(): String = "com.github.gtache.testing.G"
    }

    val fingerprints: Array[Fingerprint] = Array(annFingerprint, subFingerprint, subFingerprint2)
    val loader = new URLClassLoader(Array(this.getClass.getResource("../../../../")))
    val taskDefs = ClassScanner.scan(loader, fingerprints)

    val nameTasks = taskDefs.map(t => t.fullyQualifiedName())
    assertTrue(nameTasks.contains("com.github.gtache.testing.A"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.B"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.C"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.D"))
    assertFalse(nameTasks.contains("com.github.gtache.testing.E"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.F"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.G"))
    assertTrue(nameTasks.contains("com.github.gtache.testing.H"))
    assertFalse(nameTasks.contains("com.github.gtache.testing.I"))

    val map = nameTasks.zip(taskDefs).toMap
    assertTrue(map.get("com.github.gtache.testing.A").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get("com.github.gtache.testing.B").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get("com.github.gtache.testing.C").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get("com.github.gtache.testing.D").get.fingerprint.isInstanceOf[AnnotatedFingerprint])
    assertTrue(map.get("com.github.gtache.testing.F").get.fingerprint.isInstanceOf[AnnotatedFingerprint])
    assertTrue(map.get("com.github.gtache.testing.G").get.fingerprint.isInstanceOf[SubclassFingerprint])
    assertTrue(map.get("com.github.gtache.testing.H").get.fingerprint.isInstanceOf[SubclassFingerprint])
  }
}

class A

class B extends A

class C extends B

@ToTest
class D

class E(s : String) extends A {

}

@ToTest
class F(s : String) extends A {

}

class G(s : String)

class H(s : String, i : Int) extends G(s)

class I