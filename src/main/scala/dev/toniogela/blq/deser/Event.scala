package dev.toniogela.blq.deser

import scodec._

import bits._
import codecs._

// Source of (messy) truth
// https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html

final case class Event(header: EventHeader, rawEventData: Array[Byte])

object Event {

  def rawBytes(dataLength: Int): Codec[Array[Byte]] = "rawEventData" | bytes(dataLength).xmapc(_.toArray)(ByteVector(_))

  val eventCodec: Codec[Event] = "event" | EventHeader.headerCodec.flatZipHList(h => rawBytes(h.dataLength)).as[Event]
}

final case class EventHeader(
    timestampMillis: Int,
    eventType: EventType,
    serverId: Int,
    dataLength: Int,
    nextPosition: Int,
    flags: Short
)

object EventHeader {
  private val timestampMillis: Codec[Int] = "timestampMillis" | int32L.xmapc(_ * 1000)(_ / 1000)
  private val eventType: Codec[EventType] = "eventType" | shortL(8).xmapc(EventType.apply)(EventType.unapply)
  private val serverId: Codec[Int]        = "serverId" | int32L
  private val dataLength: Codec[Int]      = "dataLenght" | int32L.xmapc(_ - 19)(_ + 19)
  private val nextPosition: Codec[Int]    = "nextPosition" | int32L
  private val flags: Codec[Short]         = "flags" | short16L

  val headerCodec: Codec[EventHeader] = "header" |
    (timestampMillis :: eventType :: serverId :: dataLength :: nextPosition :: flags).as[EventHeader]
}

sealed trait EventData

final case class FormatDescription(content: String) extends EventData

object BinlogReader {
  val crcLength: Int     = 0
  val magic: Array[Byte] = Array(0xFE, 0x62, 0x69, 0x6E).map(_.toByte)

}

// object EventDataCodecs {

//   case class RawQueryEvent(threadId: Int, executionTime: Int, errorCode: Int, databaseName: String, query: String)

//   val statusVariableBlockCodec: Codec[Unit] = int16L.consume(size => ignore(size * 8))(_ => 0)

//   val rawQueryEventCodec = "queryEvent" |
//     (("threadId" | int32L) ::
//       ("executionTime" | int32L) :: ignore(8) ::
//       ("errorCode" | int16L) ::
//       ("status variable block" | statusVariableBlockCodec) ::
//       ("databaseName" | cstring) ::
//       ("query" | utf8)).complete.as[RawQueryEvent]

//   case class RawTableMapEvent(
//       tableId: Long,
//       databaseName: String,
//       tableName: String,
//       nOfColumns: Int,
//       columnTypes: List[Int],
//       nulliness: List[Boolean]
//   )

//   val firstTableMapCodec = "tmapHead" |
//     ("tableId" |
//       longL(48) ::
//       ("Reserved for future use" | ignore(16)) ::
//       ("Database name lenght" | ignore(8)) ::
//       ("Database name" | cstring) ::
//       ("Table name lenght" | ignore(8)) ::
//       ("Table name" | cstring))

//   val packedInfosCodec = "# Columns" | vintL.flatPrepend { nOfColumns =>
//     ("Column types" | typesCodec(nOfColumns)) :: ("Metadata Block" | metadataCodec) ::
//       ("Nulliness" | nullinessCodec(nOfColumns))
//   }

//   def typesCodec(nOfColumns: Int): Codec[List[Int]] = bytes(nOfColumns)
//     .xmapc(_.toIndexedSeq.map(_.toInt).toList)(seq => ByteVector(seq.map(_.toByte)))

//   def metadataCodec: Codec[Unit] = vintL.consume(size => ignore(size * 8))(_ => 0)

//   def nullinessCodec(nOfColumns: Int): Codec[List[Boolean]] = codecs.bits
//     .xmapc(x => x.reverse.take(nOfColumns).toIndexedSeq.toList)(bools => BitVector.bits(bools.reverse))

//   val rawTableMapCodec: Codec[RawTableMapEvent] = "tableMapEvent" | (firstTableMapCodec ::: packedInfosCodec)
//     .as[RawTableMapEvent]

//   case class TableMapEvent(
//       tableId: Long,
//       databaseName: String,
//       tableName: String,
//       nOfColumns: Int,
//       columnTypes: List[ColumnType],
//       nulliness: List[Boolean]
//   )

//   object TableMapEvent {

//     def from(rtamp: RawTableMapEvent): TableMapEvent = TableMapEvent(
//       rtamp.tableId,
//       rtamp.databaseName,
//       rtamp.tableName,
//       rtamp.nOfColumns,
//       rtamp.columnTypes.map(ColumnType(_)),
//       rtamp.nulliness
//     )
//   }
// }
