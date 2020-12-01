/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */

package dev.toniogela.blq.commands

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.atomic.AtomicReference

import scala.collection.mutable
import scala.collection.parallel.CollectionConverters._

import com.github.shyiko.mysql.binlog.event.EventType._
import com.github.shyiko.mysql.binlog.event.{EventHeaderV4, QueryEventData, TableMapEventData}
import com.monovore.decline.Opts._
import com.monovore.decline.{Argument, Command, Opts}
import dev.toniogela.blq.TimeUtils
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

  private val statOpts: Opts[Unit] = arguments[File]("binlogFile").map { files =>
    val firstTimestamp: AtomicReference[Long]                 = new AtomicReference(0)
    val lastTimestamp: AtomicReference[Long]                  = new AtomicReference(0)
    val numberOfDDLs: AtomicReference[Long]                   = new AtomicReference(0)
    val infos: mutable.Map[(String, String), (Int, Int, Int)] = mutable.Map[(String, String), (Int, Int, Int)]()

    files.toList.par.map { file =>
      val events = EventsIterator(file)
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

  implicit private val alignTableReader: Argument[(String, String, ZonedDateTime, ZonedDateTime)] =
    new Argument[(String, String, ZonedDateTime, ZonedDateTime)] {
      override def defaultMetavar: String = "align-table"

      val errorMessage =
        s"the align-table flag should have this structure: <schema.table@2020-10-21T00:56:04+02:00[Europe/Berlin]>"

      override def read(
          string: String
      ): cats.data.ValidatedNel[String, (String, String, ZonedDateTime, ZonedDateTime)] = Validated
        .condNel(string.count(_ === '@') === 1, string.split('@'), errorMessage)
        .andThen { case Array(tableInfo, dateInfo) =>
          Validated.condNel(dateInfo.count(_ === '~') === 1, dateInfo.split('~'), errorMessage).map {
            case Array(start, end) => (tableInfo, start, end)
          }
        }.andThen { case (tableInfo, startdate, endDate) =>
          val dates = for {
            start <- TimeUtils.toDateTime(startdate)
            end   <- TimeUtils.toDateTime(endDate)
          } yield (start, end)

          Validated.fromEither(dates).leftMap(_ => NonEmptyList.one(errorMessage)).map { case (x, y) =>
            (tableInfo, x, y)
          }
        }.andThen { case (string, start, end) =>
          Validated.condNel(string.count(_ === '.') === 1, string.split('.'), errorMessage)
            .map { case Array(schema, name) => (schema, name, start, end) }
        }
    }

  private val alignTableOpts: Opts[Unit] =
    (argument[(String, String, ZonedDateTime, ZonedDateTime)]("align-table"), argument[File]("binlogFile")).mapN {
      (maybeAlignTable, binlog) =>
        val (_, _, startDate, endDate) = maybeAlignTable
        val filter                     = (x: ZonedDateTime) => x.isAfter(startDate) && (x.isBefore(endDate) || x.isEqual(endDate))

        EventsIterator(binlog).filter { event =>
          val dateTime: ZonedDateTime = Instant.ofEpochMilli(event.getHeader[EventHeaderV4].getTimestamp)
            .atZone(TimeUtils.DEFAULT_ZONE)
          filter(dateTime)
        }.foreach(println)
    }

  val alignTable: Opts[Unit] = Opts.subcommand(Command("align", "timeFilter")(alignTableOpts))

}
