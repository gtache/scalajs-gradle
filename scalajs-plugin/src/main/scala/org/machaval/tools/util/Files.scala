package org.machaval.tools.util

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.net.URL
import java.nio.file.Paths

object Files {
  def url2file(url: URL): File = Paths.get(url.toURI).toFile
  def nameAndExtension(fullName: String): (String, Option[String]) = Strings.split(fullName, '.')

  def write(file: File, content: String): Unit = {
    file.getParentFile.mkdirs()
    val writer: BufferedWriter = new BufferedWriter(new FileWriter(file))
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }

  def writeBytes(file: File, content: Array[Byte]): Unit = {
    java.nio.file.Files.write(Paths.get(file.toURI), content)
  }

  def file(directory: File, segments: String*): File = fileSeq(directory, segments)

  @scala.annotation.tailrec
  def fileSeq(directory: File, segments: Seq[String]): File = {
    if (segments.isEmpty) {
      directory
    } else {
      fileSeq(new File(directory, segments.head), segments.tail)
    }
  }
}
