package com.appliedscala.events

import play.api.libs.json.JsValue

trait EventData {
  def action: String
  def json: JsValue
}
