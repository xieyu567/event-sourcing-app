package services

import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.{EventData, LogRecord}
import dao.LogDao
import model.Tag

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TagEventProducer(logDao: LogDao, readService: ReadService) {
  private def createLogRecord(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action, eventData.json, ZonedDateTime.now())
  }

  private def adjustReadState(event: LogRecord): Future[Seq[Tag]] = {
    readService.adjustState(event).flatMap { _ => readService.getState.map(_.tags) }
  }

  def createTag(text: String, createdBy: UUID): Future[Seq[Tag]] = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = createLogRecord(event)
    logDao.insertLogRecord(record).flatMap { _ =>
      adjustReadState(record)
    }
  }

  def deletedTag(tagId: UUID, deletedBy: UUID): Future[Seq[Tag]] = {
    val event = TagDeleted(tagId, deletedBy)
    val record = createLogRecord(event)
    logDao.insertLogRecord(record).flatMap { _ =>
      adjustReadState(record)
    }
  }
}
