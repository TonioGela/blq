/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */
package dev.toniogela.blq.cli

import java.io.File
import com.monovore.decline.Opts
import com.monovore.decline.Opts._
import dev.toniogela.blq.cli.Args._
import com.github.shyiko.mysql.binlog.event.EventType
import com.monovore.decline.Visibility

object Options {
  val binlogFile: Opts[File] = argument[File]("binlogFile")

  val eventTypes: Opts[NonEmptyList[EventType]] =
    options[EventType]("filter", "Filter events according to event type. Can be repeated.")

  private val head: Opts[(Int, Int)] = option[Int]("head", "Prints just the first n  matching events.")
    .validate(s"head number should be > 0")(_ > 0).map(x => (0, x - 1))

  private val rangeError = "range parameter should be in the shape <n:m> with n <= m"

  private val range: Opts[(Int, Int)] =
    option[String]("range", "Prints just the matching events in the given inclusive range.", metavar = "n:m")
      .mapValidated(s => Validated.condNel(s.contains(':'), s.split(":"), rangeError)).mapValidated {
        case Array(a, b) => a.toIntOption.product(b.toIntOption).toValidNel(rangeError)
      }.validate(rangeError) { case (a, b) => a <= b }

  val eventRange: Opts[(Int, Int)]    = head.orElse(range)

  sealed trait PrettyPrint
  case object Default extends PrettyPrint
  case object Numbers extends PrettyPrint
  case object Count   extends PrettyPrint

  val printMode: Opts[PrettyPrint] = (flag("count", help = "Count events matching filters.").as(Count))
    .orElse(flag("with-numbers", help = "Show event numbers").as(Numbers)).withDefault(Default)

  val headers: Opts[Unit] = flag("header", help = "Prints just the headers.")

  val version: Opts[Unit] = flag("version", "Prints version number and exits.", "v", Visibility.Partial)
    .map(_ => println("0.1.0-SNAPSHOT"))

}
