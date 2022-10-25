package security

import model.User
import play.api.Logger
import play.api.mvc._
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

case class UserAwareRequest[A](user: Option[User], request: Request[A])
    extends WrappedRequest[A](request)

class UserAwareAction(
    authService: AuthService,
    ec: ExecutionContext,
    playBodyParsers: PlayBodyParsers)
    extends ActionBuilder[UserAwareRequest, AnyContent] {

  val log: Logger = Logger(this.getClass)
  override implicit val executionContext: ExecutionContext = ec
  override def parser: BodyParser[AnyContent] = playBodyParsers.defaultBodyParser

  def invokeBlock[A](
      request: Request[A],
      block: UserAwareRequest[A] => Future[Result]): Future[Result] = {
    authService.checkCookie(request).flatMap { maybeUser =>
      block(UserAwareRequest(maybeUser, request))
    }
  }
}
