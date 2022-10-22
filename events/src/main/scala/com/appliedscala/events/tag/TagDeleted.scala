package com.appliedscala.events.tag

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

import java.util.UUID

case class TagDeleted(id: UUID, deletedBy: UUID) extends EventData {
  override def action: String = TagDeleted.actionName

  override def json: JsValue = Json.writes[TagDeleted].writes(this)
}

object TagDeleted {
  val actionName = "tag-deleted"
  implicit val reads: Reads[TagDeleted] = Json.reads[TagDeleted]
}
