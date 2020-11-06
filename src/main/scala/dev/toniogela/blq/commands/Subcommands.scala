/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */

package dev.toniogela.blq.commands

import java.io.File

import com.monovore.decline.Opts._
import com.monovore.decline.{Command, Opts}
import dev.toniogela.blq.cli.Args._
import dev.toniogela.blq.cli.Options._
import dev.toniogela.blq.service.EventsIterator
import dev.toniogela.blq.service.EventsIterator._

object Subcommands {

  private val tableMapsOpts: Opts[Unit] = (argument[File]("binlogFile"), tableMode).mapN((binlog, mode) =>
    EventsIterator(binlog).tableMaps.map(tmap =>
      mode match {
        case Schemas => tmap.getDatabase()
        case Tables  => tmap.getTable()
        case Both    => s"${tmap.getDatabase()}.${tmap.getTable()}"
      }
    ).toSet.foreach(println)
  )

  val tableMaps: Opts[Unit] = Opts
    .subcommand(Command("tables", "Prints a summary of the table_map informations in the binlog file")(tableMapsOpts))
}
