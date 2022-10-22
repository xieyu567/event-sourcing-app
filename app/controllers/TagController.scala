package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.UserAuthAction
import services.{ReadService, TagEventProducer}

import java.util.UUID
import scala.concurrent.Future

class TagController(components: ControllerComponents, tagEventProducer: TagEventProducer,
                    userAuthAction: UserAuthAction, readService: ReadService)
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

  def createTag(): Action[AnyContent] = userAuthAction.async { implicit request =>
    createTagForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        tagEventProducer.createTag(data.text, request.user.userId).map { tags =>
          Ok(Json.toJson(tags))
        }
      }
    )
  }

  def deleteTag(): Action[AnyContent] = userAuthAction.async { implicit request =>
    deleteTagForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        tagEventProducer.deletedTag(data.id, request.user.userId).map { tags =>
          Ok(Json.toJson(tags))
        }
      }
    )
  }

  def getTags: Action[AnyContent] = Action.async { implicit request =>
    readService.getState.map { state =>
      Ok(Json.toJson(state.tags))
    }
  }
}
