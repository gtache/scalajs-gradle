package com.github.gtache.testing

import sbt.testing._


class GradleEvent extends Event{
  override def fullyQualifiedName(): String = ???

  override def throwable(): OptionalThrowable = ???

  override def status(): Status = ???

  override def selector(): Selector = ???

  override def fingerprint(): Fingerprint = ???

  override def duration(): Long = ???
}
