package dev.toniogela.blq.deser

import scodec._

import bits._
import codecs._

// Source of (messy) truth
// https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html

final case class Binlog(events: List[Event])

object Binlog {
  implicit val binlogCodec: Codec[Binlog] = (Event.magicNumber ~> list(Event.eventCodec)).as[Binlog]
}

final case class Event(header: EventHeader, eventData: EventData) {
  def eventType: EventType = header.eventType
}

object Event {
  val magicNumber: Codec[Unit] = "magicNumber" | constant(hex"fe62696e")

  implicit val eventCodec: Codec[Event] = EventHeader.headerCodec.flatPrepend(h => EventData.eventDataCodec(h).hlist)
    .as[Event].withContext("Event")
}

//HEADER

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
  private val eventType: Codec[EventType] = "eventType" | uintL(8).xmapc(EventType.apply)(EventType.unapply)
  private val serverId: Codec[Int]        = "serverId" | int32L
  private val dataLength: Codec[Int]      = "dataLenght" | int32L.xmapc(_ - 19)(_ + 19)
  private val nextPosition: Codec[Int]    = "nextPosition" | int32L
  private val flags: Codec[Short]         = "flags" | short16L

  val headerCodec: Codec[EventHeader] = "header" |
    fixedSizeBytes(19, (timestampMillis :: eventType :: serverId :: dataLength :: nextPosition :: flags))
      .as[EventHeader]
}

//DATA

sealed trait EventData extends Product with Serializable

object EventData {

  def eventDataCodec(header: EventHeader): Codec[EventData] = fixedSizeBytes(
    header.dataLength.toLong,
    header.eventType match {
      case TABLE_MAP => TableMap.codec.upcast[EventData]
      case _         => RawData.codec.upcast[EventData]
    }
  )
}

final case class RawData(raw: Array[Byte]) extends EventData

object RawData {
  val codec: Codec[RawData] = "rawData" | bytes.xmapc(_.toArray)(ByteVector(_)).as[RawData]
}

final case class TableMap(
    tableId: Long,
    databaseName: String,
    tableName: String,
    nOfColumns: Int,
    columnTypes: List[Int],
    nulliness: List[Boolean]
) extends EventData

object TableMap {
  private val tableIdCodec: Codec[Long]            = "tableId" | longL(48)
  private val reservedCodec: Codec[Unit]           = "Reserved for future use" | ignore(16)
  private val dataBaseNameLenghtCodec: Codec[Unit] = "Database Name Lenght" | ignore(8)
  private val dataBaseNameCodec: Codec[String]     = "Database Name" | cstring
  private val tableNameLenghtCodec: Codec[Unit]    = "Table Name lenght" | ignore(8)
  private val tableNameCodec: Codec[String]        = "Table Name" | cstring
  private val numberOfColumnsCodec: Codec[Int]     = "Number of Columns" | vintL

  private def typesCodec(nOfColumns: Int): Codec[List[Int]] = "Column types" | bytes(nOfColumns)
    .xmapc(_.toIndexedSeq.map(_.toInt).toList)(seq => ByteVector(seq.map(_.toByte)))

  private def metadataCodec: Codec[Unit] = "Metadata Block" | vintL.consume(n => ignore(n.toLong * 8))(_ => 0)

  private def nullinessCodec(nOfColumns: Int): Codec[List[Boolean]] = "Nulliness" |
    codecs.bits
      .xmapc(x => x.reverse.take(nOfColumns.toLong).toIndexedSeq.toList)(bools => BitVector.bits(bools.reverse))

  val codec: Codec[TableMap] = "TableMap" |
    (tableIdCodec :: reservedCodec :: dataBaseNameLenghtCodec :: dataBaseNameCodec :: tableNameLenghtCodec ::
      tableNameCodec :: numberOfColumnsCodec.flatPrepend(n => typesCodec(n) :: metadataCodec :: nullinessCodec(n)))
      .as[TableMap]
}

// public TableMapEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
//         TableMapEventData eventData = new TableMapEventData();
//         eventData.setTableId(inputStream.readLong(6));
//         inputStream.skip(3); // 2 bytes reserved for future use + 1 for the length of database name
//         eventData.setDatabase(inputStream.readZeroTerminatedString());
//         inputStream.skip(1); // table name
//         eventData.setTable(inputStream.readZeroTerminatedString());
//         int numberOfColumns = inputStream.readPackedInteger();
//         eventData.setColumnTypes(inputStream.read(numberOfColumns));
//         inputStream.readPackedInteger(); // metadata length
//         eventData.setColumnMetadata(readMetadata(inputStream, eventData.getColumnTypes()));
//         eventData.setColumnNullability(inputStream.readBitSet(numberOfColumns, true));
//         int metadataLength = inputStream.available();
//         TableMapEventMetadata metadata = null;
//         if (metadataLength > 0) {
//             metadata = metadataDeserializer.deserialize(
//                 new ByteArrayInputStream(inputStream.read(metadataLength)),
//                 numericColumnCount(eventData.getColumnTypes())
//             );
//         }
//         eventData.setEventMetadata(metadata);
//         return eventData;
//     }

//     private int numericColumnCount(byte[] types) {
//         int count = 0;
//         for (int i = 0; i < types.length; i++) {
//             switch (ColumnType.byCode(types[i] & 0xff)) {
//                 case TINY:
//                 case SHORT:
//                 case INT24:
//                 case LONG:
//                 case LONGLONG:
//                 case NEWDECIMAL:
//                 case FLOAT:
//                 case DOUBLE:
//                     count++;
//                     break;
//                 default:
//                     break;
//             }
//         }
//         return count;
//     }

//     private int[] readMetadata(ByteArrayInputStream inputStream, byte[] columnTypes) throws IOException {
//         int[] metadata = new int[columnTypes.length];
//         for (int i = 0; i < columnTypes.length; i++) {
//             switch(ColumnType.byCode(columnTypes[i] & 0xFF)) {
//                 case FLOAT:
//                 case DOUBLE:
//                 case BLOB:
//                 case JSON:
//                 case GEOMETRY:
//                     metadata[i] = inputStream.readInteger(1);
//                     break;
//                 case BIT:
//                 case VARCHAR:
//                 case NEWDECIMAL:
//                     metadata[i] = inputStream.readInteger(2);
//                     break;
//                 case SET:
//                 case ENUM:
//                 case STRING:
//                     metadata[i] = bigEndianInteger(inputStream.read(2), 0, 2);
//                     break;
//                 case TIME_V2:
//                 case DATETIME_V2:
//                 case TIMESTAMP_V2:
//                     metadata[i] = inputStream.readInteger(1); // fsp (@see {@link ColumnType})
//                     break;
//                 default:
//                     metadata[i] = 0;
//             }
//         }
//         return metadata;
//     }

//     private static int bigEndianInteger(byte[] bytes, int offset, int length) {
//         int result = 0;
//         for (int i = offset; i < (offset + length); i++) {
//             byte b = bytes[i];
//             result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
//         }
//         return result;
//     }

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
