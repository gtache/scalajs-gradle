import java.net.URLClassLoader

import com.github.gtache.testing.ClassScanner
import org.junit.Test
import org.junit.Assert._
import sbt.testing.Fingerprint
import sbt.testing.SubclassFingerprint
import sbt.testing.AnnotatedFingerprint

import scala.annotation.Annotation

class ClassScannerTest {

  @Test
  def testScanner(): Unit = {
    val annFingerprint = new AnnotatedFingerprint {
      override def isModule: Boolean = true

      override def annotationName(): String = "ToTest"
    }
    val subFingerprint = new SubclassFingerprint {
      override def isModule: Boolean = true

      override def superclassName(): String = "A"

      override def requireNoArgConstructor(): Boolean = true
    }
    val fingerprints : Array[Fingerprint] = Array(annFingerprint, subFingerprint)
    val loader = new URLClassLoader(Array(this.getClass.getResource(".")))
    val classes = ClassScanner.parseClasses(loader)
    val taskDefs = ClassScanner.scan(loader,fingerprints)
    val nameTasks = taskDefs.map(t => t.fullyQualifiedName())
    assertTrue(nameTasks.contains("A"))
    assertTrue(nameTasks.contains("B"))
    assertTrue(nameTasks.contains("C"))
    assertTrue(nameTasks.contains("D"))
  }
}

class A

class B extends A

class C extends B

@ToTest
class D

class ToTest extends Annotation
