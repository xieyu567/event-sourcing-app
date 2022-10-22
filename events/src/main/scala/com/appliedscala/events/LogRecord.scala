package com.appliedscala.events

import play.api.libs.json.JsValue

import java.time.ZonedDateTime
import java.util.UUID

case class LogRecord(id: UUID, action: String, data: JsValue, timestamp: ZonedDateTime)
