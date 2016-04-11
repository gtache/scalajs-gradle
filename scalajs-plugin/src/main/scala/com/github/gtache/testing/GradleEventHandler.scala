package com.github.gtache.testing

import sbt.testing.{Event, EventHandler}


class GradleEventHandler extends EventHandler {
  override def handle(event: Event): Unit = ???
}
