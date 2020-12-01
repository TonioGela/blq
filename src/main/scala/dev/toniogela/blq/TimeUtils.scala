package dev.toniogela.blq

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}

import scala.util.Try

object TimeUtils {

  val DEFAULT_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
  val DEFAULT_ZONE: ZoneId              = ZoneId.of("Europe/Rome")

  def yesterdayMidnight: ZonedDateTime = LocalDate.now().minusDays(1).atStartOfDay().atZone(DEFAULT_ZONE)
  def yesterdayMidnightS: String       = yesterdayMidnight.format(DEFAULT_FORMAT)

  def todayMidnight: ZonedDateTime = LocalDate.now().atStartOfDay().atZone(DEFAULT_ZONE)
  def todayMidnightS: String       = todayMidnight.format(DEFAULT_FORMAT)

  def toDateTime(epoch: Long): ZonedDateTime = Instant.ofEpochMilli(epoch).atZone(DEFAULT_ZONE)

  def toDateTime(date: String): Either[Throwable, ZonedDateTime] = Try(ZonedDateTime.parse(date, DEFAULT_FORMAT))
    .toEither

  def toDateString(date: ZonedDateTime): String = date.format(DEFAULT_FORMAT)
  def toDateString(epoch: Long): String         = toDateString(toDateTime(epoch))

}
