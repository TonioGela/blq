package dev.toniogela.blq.deser

import java.nio.file.Paths
import java.nio.file.Files
import scodec._
import bits._
import scodec.Attempt.Failure
import scodec.Attempt.Successful
import java.lang.System

object AnotherTest extends App {

  val file: BitVector = BitVector(Files.readAllBytes(Paths.get("")))

  // val codec = constant(hex"fe62696e") ~> list(EventHeader.headerCodec.flatZip(h => ignore(h.dataLength.toLong * 8)))
  // codec.decodeValue(file) match {
  //   case Failure(cause)   => println(cause)
  //   case Successful(list) => list.map(_._1).zipWithIndex.map(_.swap).foreach { case (i, e) =>
  //       println(s"[$i] ${e.eventType}")
  //     }
  // }

  val init: Long = System.currentTimeMillis
  Binlog.binlogCodec.decodeValue(file) match {
    case Failure(err)  => println(s"ERROR: $err")
    case Successful(_) =>
      val time: Long = System.currentTimeMillis
      println(s"${(time - init)} ms DESERIALIZED!")
  }

}
