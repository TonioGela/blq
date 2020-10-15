package dev.toniogela.blq

import java.io.File
import com.github.shyiko.mysql.binlog.BinaryLogFileReader
import com.github.shyiko.mysql.binlog.event.{Event, EventHeaderV4, EventType}

object EventsIterator {

  def apply(file: File): Iterator[Event] = {
    val reader: BinaryLogFileReader = new BinaryLogFileReader(file)
    Iterator.continually(reader.readEvent).map(Option(_)).takeWhile(_.isDefined).flatten
  }

  implicit class pimpedIterator(val iterator: Iterator[(Event, Int)]) {
    implicit val typeEquality: Eq[EventType] = Eq.fromUniversalEquals[EventType]

    def filterTypes(types: NonEmptyList[EventType]): Iterator[(Event, Int)] = iterator.filter { case (e, _) =>
      types.contains_(e.getHeader[EventHeaderV4]().getEventType())
    }

    def headers: Iterator[(EventHeaderV4, Int)] = iterator.map { case (e, i) => (e.getHeader[EventHeaderV4](), i) }
  }
}
