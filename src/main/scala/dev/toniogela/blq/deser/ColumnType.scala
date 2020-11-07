package dev.toniogela.blq.deser

sealed trait ColumnType

case object Decimal      extends ColumnType
case object Tiny         extends ColumnType
case object Short        extends ColumnType
case object Long         extends ColumnType
case object Float        extends ColumnType
case object Double       extends ColumnType
case object Null         extends ColumnType
case object Timestamp    extends ColumnType
case object Longlong     extends ColumnType
case object Int24        extends ColumnType
case object Date         extends ColumnType
case object Time         extends ColumnType
case object Datetime     extends ColumnType
case object Year         extends ColumnType
case object Newdate      extends ColumnType
case object Varchar      extends ColumnType
case object Bit          extends ColumnType
case object Timestamp_v2 extends ColumnType
case object Datetime_v2  extends ColumnType
case object Time_v2      extends ColumnType
case object Json         extends ColumnType
case object Newdecimal   extends ColumnType
case object Enum         extends ColumnType
case object Set          extends ColumnType
case object Tiny_blob    extends ColumnType
case object Medium_blob  extends ColumnType
case object Long_blob    extends ColumnType
case object Blob         extends ColumnType
case object Var_string   extends ColumnType
case object String       extends ColumnType
case object Geometry     extends ColumnType
case object Invalid      extends ColumnType //TODO remove

object ColumnType {

  def apply(typeCode: Int): ColumnType = typeCode match {
    case 0   => Decimal
    case 1   => Tiny
    case 2   => Short
    case 3   => Long
    case 4   => Float
    case 5   => Double
    case 6   => Null
    case 7   => Timestamp
    case 8   => Longlong
    case 9   => Int24
    case 10  => Date
    case 11  => Time
    case 12  => Datetime
    case 13  => Year
    case 14  => Newdate
    case 15  => Varchar
    case 16  => Bit
    case 17  => Timestamp_v2
    case 18  => Datetime_v2
    case 19  => Time_v2
    case 245 => Json
    case 246 => Newdecimal
    case 247 => Enum
    case 248 => Set
    case 249 => Tiny_blob
    case 250 => Medium_blob
    case 251 => Long_blob
    case 252 => Blob
    case 253 => Var_string
    case 254 => String
    case 255 => Geometry
    case _   => Invalid //TODO remove
  }
}
