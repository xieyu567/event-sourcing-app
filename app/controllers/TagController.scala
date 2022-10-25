package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.UserAuthAction
import services.{ReadService, TagEventProducer}

import java.util.UUID

class TagController(
    components: ControllerComponents,
    tagEventProducer: TagEventProducer,
    userAuthAction: UserAuthAction,
    readService: ReadService)
    extends AbstractController(components) {

  import util.ThreadPools.CPU

  case class CreateTagData(text: String)

  case class DeleteTagData(id: UUID)

  private val createTagForm = Form {
    mapping("text" -> nonEmptyText)(CreateTagData.apply)(CreateTagData.unapply)
  }

  private val deleteTagForm = Form {
    mapping("id" -> uuid)(DeleteTagData.apply)(DeleteTagData.unapply)
  }

  def createTag(): Action[AnyContent] = userAuthAction { implicit request =>
    createTagForm
      .bindFromRequest()
      .fold(_ => BadRequest, data => {
        tagEventProducer.createTag(data.text, request.user.userId)
        Ok
      })
  }

  def deleteTag(): Action[AnyContent] = userAuthAction { implicit request =>
    deleteTagForm
      .bindFromRequest()
      .fold(_ => BadRequest, data => {
        tagEventProducer.deletedTag(data.id, request.user.userId)
        Ok
      })
  }

  def getTags: Action[AnyContent] = Action.async { implicit request =>
    readService.getState.map { state =>
      Ok(Json.toJson(state.tags))
    }
  }
}
