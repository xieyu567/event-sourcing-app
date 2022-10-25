package services

import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.LogRecord

import messaging.IMessageProcessingRegistry

import java.util.UUID

class TagEventProducer(registry: IMessageProcessingRegistry) {
  private val producer = registry.createProducer("tags")

  def createTag(text: String, createdBy: UUID): Unit = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = LogRecord.fromEvent(event)
    producer.send(record.encode)
  }

  def deletedTag(tagId: UUID, deletedBy: UUID): Unit = {
    val event = TagDeleted(tagId, deletedBy)
    val record = LogRecord.fromEvent(event)
    producer.send(record.encode)
  }
}
