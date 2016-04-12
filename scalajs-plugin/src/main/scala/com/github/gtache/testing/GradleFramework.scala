package com.github.gtache.testing

import sbt.testing.{Fingerprint, Framework, Runner}


class GradleFramework extends Framework {
  override def name(): String = "scalajs"

  override def fingerprints(): Array[Fingerprint] = ???

  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner = ???

  override def slaveRunner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader, send: (String) => Unit): Runner = ???
}