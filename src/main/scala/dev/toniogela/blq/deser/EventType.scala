package dev.toniogela.blq.deser

sealed trait EventType

case object UNKNOWN            extends EventType
case object START_V3           extends EventType
case object QUERY              extends EventType
case object STOP               extends EventType
case object ROTATE             extends EventType
case object INTVAR             extends EventType
case object LOAD               extends EventType
case object SLAVE              extends EventType
case object CREATE_FILE        extends EventType
case object APPEND_BLOCK       extends EventType
case object EXEC_LOAD          extends EventType
case object DELETE_FILE        extends EventType
case object NEW_LOAD           extends EventType
case object RAND               extends EventType
case object USER_VAR           extends EventType
case object FORMAT_DESCRIPTION extends EventType
case object XID                extends EventType
case object BEGIN_LOAD_QUERY   extends EventType
case object EXECUTE_LOAD_QUERY extends EventType
case object TABLE_MAP          extends EventType
case object PRE_GA_WRITE_ROWS  extends EventType
case object PRE_GA_UPDATE_ROWS extends EventType
case object PRE_GA_DELETE_ROWS extends EventType
case object WRITE_ROWS         extends EventType
case object UPDATE_ROWS        extends EventType
case object DELETE_ROWS        extends EventType
case object INCIDENT           extends EventType
case object HEARTBEAT_LOG      extends EventType
case object IGNORABLE_LOG      extends EventType
case object ROWS_QUERY_LOG     extends EventType
case object EXT_WRITE_ROWS     extends EventType
case object EXT_UPDATE_ROWS    extends EventType
case object EXT_DELETE_ROWS    extends EventType
case object GTID_LOG           extends EventType
case object ANONYMOUS_GTID_LOG extends EventType
case object PREVIOUS_GTIDS_LOG extends EventType

object EventType {

  def apply(eventCode: Short): EventType = eventCode match {
    case 1  => START_V3
    case 2  => QUERY
    case 3  => STOP
    case 4  => ROTATE
    case 5  => INTVAR
    case 6  => LOAD
    case 7  => SLAVE
    case 8  => CREATE_FILE
    case 9  => APPEND_BLOCK
    case 10 => EXEC_LOAD
    case 11 => DELETE_FILE
    case 12 => NEW_LOAD
    case 13 => RAND
    case 14 => USER_VAR
    case 15 => FORMAT_DESCRIPTION
    case 16 => XID
    case 17 => BEGIN_LOAD_QUERY
    case 18 => EXECUTE_LOAD_QUERY
    case 19 => TABLE_MAP
    case 20 => PRE_GA_WRITE_ROWS
    case 21 => PRE_GA_UPDATE_ROWS
    case 22 => PRE_GA_DELETE_ROWS
    case 23 => WRITE_ROWS
    case 24 => UPDATE_ROWS
    case 25 => DELETE_ROWS
    case 26 => INCIDENT
    case 27 => HEARTBEAT_LOG
    case 28 => IGNORABLE_LOG
    case 29 => ROWS_QUERY_LOG
    case 30 => EXT_WRITE_ROWS
    case 31 => EXT_UPDATE_ROWS
    case 32 => EXT_DELETE_ROWS
    case 33 => GTID_LOG
    case 34 => ANONYMOUS_GTID_LOG
    case 35 => PREVIOUS_GTIDS_LOG
    case _  => UNKNOWN
  }

  def unapply(eventType: EventType): Short = eventType match {
    case UNKNOWN            => 0
    case START_V3           => 1
    case QUERY              => 2
    case STOP               => 3
    case ROTATE             => 4
    case INTVAR             => 5
    case LOAD               => 6
    case SLAVE              => 7
    case CREATE_FILE        => 8
    case APPEND_BLOCK       => 9
    case EXEC_LOAD          => 10
    case DELETE_FILE        => 11
    case NEW_LOAD           => 12
    case RAND               => 13
    case USER_VAR           => 14
    case FORMAT_DESCRIPTION => 15
    case XID                => 16
    case BEGIN_LOAD_QUERY   => 17
    case EXECUTE_LOAD_QUERY => 18
    case TABLE_MAP          => 19
    case PRE_GA_WRITE_ROWS  => 20
    case PRE_GA_UPDATE_ROWS => 21
    case PRE_GA_DELETE_ROWS => 22
    case WRITE_ROWS         => 23
    case UPDATE_ROWS        => 24
    case DELETE_ROWS        => 25
    case INCIDENT           => 26
    case HEARTBEAT_LOG      => 27
    case IGNORABLE_LOG      => 28
    case ROWS_QUERY_LOG     => 29
    case EXT_WRITE_ROWS     => 30
    case EXT_UPDATE_ROWS    => 31
    case EXT_DELETE_ROWS    => 32
    case GTID_LOG           => 33
    case ANONYMOUS_GTID_LOG => 34
    case PREVIOUS_GTIDS_LOG => 35
  }

}
