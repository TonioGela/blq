/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */
package dev.toniogela.blq

import com.github.shyiko.mysql.binlog.BinaryLogFileReader
import com.github.shyiko.mysql.binlog.event.{Event, EventType}
import com.monovore.decline.Argument
import java.io.File
import scala.util.Try

object EventIterator {

  def apply(file: File): Iterator[Event] = {
    val reader: BinaryLogFileReader = new BinaryLogFileReader(file)
    Iterator.continually(reader.readEvent).map(Option(_)).takeWhile(_.isDefined).flatten
  }
}

object Args {

  implicit val fileReader: Argument[File] = new Argument[File] {
    override def defaultMetavar: String = "filePath"

    override def read(fileString: String): ValidatedNel[String, File] = Validated.validNel(new File(fileString))
      .ensure(NonEmptyList.one(s"The file $fileString does not exist!"))(_.exists)
  }

  implicit val typeFilter: Argument[EventType] = new Argument[EventType] {

    override def defaultMetavar: String = "type"

    override def read(eventType: String): ValidatedNel[String, EventType] = Validated
      .fromTry(Try(EventType.valueOf(eventType)))
      .leftMap(_ => NonEmptyList.one(s"$eventType is not a valid eventType. Use one of:\n$eventTypes"))
  }

  private val eventTypes: String =
    "START_V3, QUERY, STOP, ROTATE, INTVAR, LOAD, SLAVE, CREATE_FILE, APPEND_BLOCK, EXEC_LOAD," +
      "DELETE_FILE, NEW_LOAD, RAND, USER_VAR, FORMAT_DESCRIPTION, XID, BEGIN_LOAD_QUERY," +
      "EXECUTE_LOAD_QUERY, TABLE_MAP, PRE_GA_WRITE_ROWS, PRE_GA_UPDATE_ROWS, PRE_GA_DELETE_ROWS," +
      "WRITE_ROWS, UPDATE_ROWS, DELETE_ROWS, INCIDENT, HEARTBEAT, IGNORABLE, ROWS_QUERY, EXT_WRITE_ROWS," +
      "EXT_UPDATE_ROWS, EXT_DELETE_ROWS, GTID, ANONYMOUS_GTID, PREVIOUS_GTIDS, TRANSACTION_CONTEXT," +
      "VIEW_CHANGE, XA_PREPARE"
}
