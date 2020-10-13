package dev.toniogela.blq

import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.Opts._
import dev.toniogela.blq.Args._
import java.io.File
import com.github.shyiko.mysql.binlog.event.EventType
import com.github.shyiko.mysql.binlog.event.EventHeaderV4

object Main
    extends CommandApp(
      name = "bq",
      header = "Prints binlog files content optionally applying filter. Filters do stack and apply to count too.",
      main = (
        argument[File]("binlogFile"),
        options[EventType]("filter", "Filter events according to event type. Can be repeated.").orNone,
        option[Int]("head", "Reads just the first n events.", metavar = "n").orNone,
        flag("count", help = "Count events matching filters.").map(_ => true).withDefault(false)
      ).mapN { (binlog, types, head, count) =>
        val events          = EventIterator(binlog)
        val filteredEvents  = types
          .fold(events)(ts => events.filter(e => ts.toList contains e.getHeader[EventHeaderV4].getEventType))
        val truncatedEvents = head.fold(filteredEvents)(filteredEvents.take(_))
        if (count) println(truncatedEvents.count(_ => true)) else truncatedEvents.foreach(println)
      }
    )
