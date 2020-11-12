package dev.toniogela.blq.deser

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Path

import scodec.bits.BitVector

import NewEventsIterator._
import Event._
import EventHeader.headerCodec
import scodec.Attempt.Failure
import scodec.Attempt.Successful
import scodec.Attempt
import java.lang.AutoCloseable

class NewEventsIterator(val filePath: Path) extends Iterator[Event] with AutoCloseable {

  private val stream: BufferedInputStream = new BufferedInputStream(new FileInputStream(filePath.toFile))
  magicNumber.decode(stream.read(4)).or_("Not a valid binary log")

  private def deser: Attempt[Event] = for {
    header <- headerCodec.decodeValue(stream.read(19))
    data = stream.readArray(header.dataLength)
  } yield Event(header, RawData(data))

  private var nextEvent: Attempt[Event] = deser

  override def hasNext: Boolean = stream.available > 0 && deser.isSuccessful

  override def next(): Event = {
    val eventToEmit = nextEvent
    nextEvent = deser
    eventToEmit.require
  }

  override def close(): Unit = stream.close()

}

object NewEventsIterator {

  def apply(filePath: Path): NewEventsIterator = new NewEventsIterator(filePath)

  implicit class BufferedInputStreamOps(private val stream: BufferedInputStream) extends AnyVal {

    def read(n: Int): BitVector = BitVector(readArray(n))

    def readArray(n: Int): Array[Byte] = {
      val array = Array.ofDim[Byte](n)
      stream.read(array)
      array
    }
  }

  implicit class AttemptOps[A](private val attempt: Attempt[A]) extends AnyVal {

    def or_(message: String): Unit = attempt match {
      case Failure(_)    => throw new Exception(message)
      case Successful(_) => ()
    }

    def or(message: String): A = attempt match {
      case Failure(_)    => throw new Exception(message)
      case Successful(v) => v
    }
  }
}
