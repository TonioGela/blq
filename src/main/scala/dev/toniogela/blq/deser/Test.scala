package dev.toniogela.blq.deser

import java.nio.file.Paths
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import scodec._
import bits._
import java.nio.file.Path

object Test extends App {

  val output = new BufferedOutputStream(new FileOutputStream("/Users/agelameris/Desktop/shrinked"))
  output.write(hex"fe62696e".toArray)

  val files: Array[Path] = Paths.get("/Users/agelameris/Desktop/faulty").toFile.listFiles().map(_.toPath()).sorted

  var formatDescriptionAlreadyWritten = false

  def asByteArray(event: Event): Array[Byte] = Event.eventCodec.encode(event).require.toByteArray

  files.foreach { file =>
    // List(Paths.get("/Users/agelameris/Desktop/faulty/dbmd02_gcp.001614")).foreach { file =>
    println(s"Reading $file")
    val iterator = NewEventsIterator(file).zipWithIndex
    while (iterator.hasNext) {
      val (event, _) = iterator.next()
      event.eventType match {
        case FORMAT_DESCRIPTION => if (!formatDescriptionAlreadyWritten) output.write(asByteArray(event))
        case QUERY              => output.write(asByteArray(event))
        case TABLE_MAP          => if (iterator.hasNext) {
            val (contentEvent, _) = iterator.next()
            contentEvent.eventType match {
              case EXT_DELETE_ROWS | DELETE_ROWS | PRE_GA_DELETE_ROWS | EXT_UPDATE_ROWS | UPDATE_ROWS |
                  PRE_GA_UPDATE_ROWS | EXT_WRITE_ROWS | WRITE_ROWS | PRE_GA_WRITE_ROWS =>
                output.write(asByteArray(event))
                output.write(asByteArray(contentEvent))
              case _ => ()
            }
          }
        case _                  => ()
      }
    }
  }

  output.flush()
  output.close()
}
