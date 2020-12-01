/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */

package dev.toniogela.blq.commands

import scala.Console._

import com.monovore.decline.Opts
import dev.toniogela.blq.cli.Options._
import dev.toniogela.blq.service.EventsIterator
import dev.toniogela.blq.service.EventsIterator._

object MainCommand {

  val command: Opts[Unit] = (binlogFile, eventTypes.orNone, eventRange.orNone, headers.orFalse, printMode)
    .mapN { (binlog, types, range, header, print) =>
      val events = EventsIterator(binlog).zipWithIndex

      val slicedEvents = range.fold(events) { case (a, b) => events.slice(a, b + 1) }

      val filteredEvents = types.fold(slicedEvents)(slicedEvents.just)

      val reReFilteredEvents = if (header) filteredEvents.headers else filteredEvents

      def number(n: Int) = s"$GREEN[$RED$n$GREEN]$RESET"

      print match {
        case Numbers => reReFilteredEvents.foreach { case (e, i) => println(s"${number(i)} $e") }
        case Count   => println(reReFilteredEvents.size)
        case Default => reReFilteredEvents.foreach(println)
      }

    }
}
