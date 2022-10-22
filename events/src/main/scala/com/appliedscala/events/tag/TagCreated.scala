package com.appliedscala.events.tag

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

import java.util.UUID

case class TagCreated(id: UUID, text: String, createdBy: UUID) extends EventData {
  override def action: String = TagCreated.actionName

  override def json: JsValue = Json.writes[TagCreated].writes(this)
}

object TagCreated {
  val actionName = "tag-created"
  implicit val reads: Reads[TagCreated] = Json.reads[TagCreated]
}