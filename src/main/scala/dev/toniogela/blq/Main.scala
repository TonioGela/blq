/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */
package dev.toniogela.blq

import com.monovore.decline._
import dev.toniogela.blq.cli.Options._
import dev.toniogela.blq.EventsIterator._
import scala.Console._

object Main
    extends CommandApp(
      name = "bq",
      header = "Prints binlog files content optionally applying filter. Filters do stack and apply to count too.",
      main = (binlogFile, eventTypes.orNone, eventRange.orNone, printMode).mapN { (binlog, types, range, print) =>
        val events           = EventsIterator(binlog).zipWithIndex
        val filteredEvents   = types.fold(events)(events.filterTypes)
        val reFilteredEvents = range.fold(filteredEvents) { case (a, b) => filteredEvents.slice(a, b + 1) }
        def number(n: Int)   = s"$GREEN[$RED$n$GREEN]$RESET"
        print match {
          case Numbers => reFilteredEvents.foreach { case (e, i) => println(s"${number(i)} $e") }
          case Count   => println(reFilteredEvents.size)
          case Default => reFilteredEvents.foreach(println)
        }
      }
    )
