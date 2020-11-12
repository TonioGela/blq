/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */

package dev.toniogela.blq.commands

import java.io.File

import com.github.shyiko.mysql.binlog.event.EventType._
import com.github.shyiko.mysql.binlog.event.{EventHeaderV4, QueryEventData, TableMapEventData}
import com.monovore.decline.Opts._
import com.monovore.decline.{Command, Opts}
import dev.toniogela.blq.cli.Args._
import dev.toniogela.blq.cli.Options._
import dev.toniogela.blq.service.EventsIterator
import dev.toniogela.blq.service.EventsIterator._
import scala.collection.mutable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import cats.data.Validated.Valid

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

  private val statOpts: Opts[Unit] = arguments[File]("binlogFile").map { files =>
    val firstTimestamp: AtomicReference[Long]                 = new AtomicReference(0)
    val lastTimestamp: AtomicReference[Long]                  = new AtomicReference(0)
    val numberOfDDLs: AtomicReference[Long]                   = new AtomicReference(0)
    val infos: mutable.Map[(String, String), (Int, Int, Int)] = mutable.Map[(String, String), (Int, Int, Int)]()

    val events = files.toList.iterator.flatMap(EventsIterator(_))
    while (events.hasNext) {
      val event  = events.next()
      val header = event.getHeader[EventHeaderV4]()

      val timestamp = header.getTimestamp()
      firstTimestamp.compareAndSet(0, timestamp)
      lastTimestamp.set(timestamp)

      header.getEventType() match {
        case QUERY     => if (event.getData[QueryEventData]().getSql() =!= "BEGIN") numberOfDDLs.getAndUpdate(_ + 1)
        case TABLE_MAP =>
          val tmap  = event.getData[TableMapEventData]()
          val table = (tmap.getDatabase(), tmap.getTable())
          if (events.hasNext) {
            val innerEvent = events.next()
            innerEvent.getHeader[EventHeaderV4]().getEventType() match {
              case EXT_WRITE_ROWS | WRITE_ROWS | PRE_GA_WRITE_ROWS    =>
                val (i, d, u) = infos.getOrElse(table, (0, 0, 0))
                infos += table -> ((i + 1, d, u))
              case EXT_DELETE_ROWS | DELETE_ROWS | PRE_GA_DELETE_ROWS =>
                val (i, d, u) = infos.getOrElse(table, (0, 0, 0))
                infos += table -> ((i, d + 1, u))
              case EXT_UPDATE_ROWS | UPDATE_ROWS | PRE_GA_UPDATE_ROWS =>
                val (i, d, u) = infos.getOrElse(table, (0, 0, 0))
                infos += table -> ((i, d, u + 1))
              case _                                                  => ()
            }
          }
        case _         => ()
      }
    }

    val (databaseLenght, tableLenght, insertLenght, deleteLenght, updateLenght) = {
      val infosList = infos.toList
      val db        = infosList.map { case ((db, _), _) => db }.map(_.length).max
      val table     = infosList.map { case ((_, t), _) => t }.map(_.length).max
      val w         = infosList.map { case (_, (w, _, _)) => w }.map(_.toString.length).max
      val d         = infosList.map { case (_, (_, d, _)) => d }.map(_.toString.length).max
      val u         = infosList.map { case (_, (_, _, u)) => u }.map(_.toString.length).max
      (
        db.max("database".length),
        table.max("table".length),
        w.max("# insert".length),
        d.max("# delete".length),
        u.max("# update".length)
      )
    }

    val header = s"│ ${"database".padTo(databaseLenght, ' ')} │ ${"table".padTo(tableLenght, ' ')} │ " +
      s"${"# insert".padTo(insertLenght, ' ')} │ ${"# delete".padTo(deleteLenght, ' ')} │ ${"# update".padTo(updateLenght, ' ')} │"

    val separatorGeneric = "─" * (header.length - 2)
    val top              = s"┌$separatorGeneric┐"
    val middle           = s"├$separatorGeneric┤"
    val bottom           = s"└$separatorGeneric┘"

    val from: String = Instant.ofEpochMilli(firstTimestamp.get()).atZone(ZoneId.systemDefault())
      .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss OOOO '['VV']'"))

    val to: String = Instant.ofEpochMilli(lastTimestamp.get()).atZone(ZoneId.systemDefault())
      .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss OOOO '['VV']'"))

    println(top)
    println(s"│ SUMMARY".padTo(header.length() - 1, ' ') + "│")
    println(middle)
    println(s"│ From: $from".padTo(header.length() - 1, ' ') + "│")
    println(s"│ To:   $to".padTo(header.length() - 1, ' ') + "│")
    println(s"│ Number of DDLs: $numberOfDDLs".padTo(header.length() - 1, ' ') + "│")

    println(middle)

    println(header)
    println(middle)

    infos.toList.sortBy { case ((db, _), _) => db }.foreach { case ((db, t), (i, d, u)) =>
      val dbS: String = db.padTo(databaseLenght, ' ')
      val tS: String  = t.padTo(tableLenght, ' ')
      val iS: String  = i.toString.padTo(insertLenght, ' ')
      val dS: String  = d.toString.padTo(deleteLenght, ' ')
      val uS: String  = u.toString.padTo(updateLenght, ' ')
      val row: String = s"│ $dbS │ $tS │ $iS │ $dS │ $uS │"
      println(row)
    }

    println(bottom)
  }

  val stats: Opts[Unit] = Opts
    .subcommand(Command("stats", "Prints the statistics of the passed binlog files")(statOpts))
}
