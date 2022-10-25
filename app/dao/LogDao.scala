package dao

import com.appliedscala.events.LogRecord
import play.api.libs.json.Json
import scalikejdbc._

import java.util.UUID
import scala.concurrent.Future

class LogDao {

  import util.ThreadPools.IO

  def insertLogRecord(event: LogRecord): Future[Unit] = Future {
    NamedDB(Symbol("eventstore")).localTx { implicit session =>
      val jsonStr = event.data.toString()
      sql"""insert into logs(record_id, action_name, event_data, timestamp)
           |values(${event.id}, ${event.action}, $jsonStr, ${event.timestamp})
           |""".stripMargin.update().apply()
    }
  }

  def getLogRecords: Future[Seq[LogRecord]] = Future {
    NamedDB(Symbol("eventstore")).readOnly { implicit session =>
      sql"""select * from logs order by timestamp""".map(rs2LogRecord).list().apply()
    }
  }

  private def rs2LogRecord(rs: WrappedResultSet): LogRecord = {
    LogRecord(
      UUID.fromString(rs.string("record_id")),
      rs.string("action_name"),
      Json.parse(rs.string("event_data")),
      rs.dateTime("timestamp"))
  }
}
