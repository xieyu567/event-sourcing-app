import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.routing.Router
import com.softwaremill.macwire._
import _root_.controllers._
import dao._
import messaging.MessageLogRegistry
import play.api.mvc.DefaultControllerComponents
import scalikejdbc.config.DBs
import security.{UserAuthAction, UserAwareAction}
import services._
import router.Routes

import scala.concurrent.Future

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EvolutionsComponents
    with DBComponents
    with HikariCPComponents
    with AssetsComponents {

  override lazy val controllerComponents = wire[DefaultControllerComponents]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val maybeRouter: Option[Router] = Option(router)

  override lazy val httpErrorHandler = wire[ProdErrorHandler]
  override lazy val httpFilters = Seq()

  lazy val mainController: MainController = wire[MainController]
  lazy val authController: AuthController = wire[AuthController]
  lazy val tagController: TagController = wire[TagController]

  lazy val sessionDao: SessionDao = wire[SessionDao]
  lazy val userDao: UserDao = wire[UserDao]
  lazy val logDao: LogDao = wire[LogDao]
  lazy val inMemoryReadDao: InMemoryReadDao = wire[InMemoryReadDao]

  lazy val userService: UserService = wire[UserService]
  lazy val authService: AuthService = wire[AuthService]
  lazy val userAuthAction: UserAuthAction = wire[UserAuthAction]
  lazy val userAwareAction: UserAwareAction = wire[UserAwareAction]
  lazy val readService: ReadService = wire[ReadService]
  lazy val clientBroadcastService: ClientBroadcastService = wire[ClientBroadcastService]

  lazy val tagEventProducer: TagEventProducer = wire[TagEventProducer]
  lazy val messageRegistry: MessageLogRegistry = wire[MessageLogRegistry]
  lazy val tagEventConsumer: TagEventConsumer = wire[TagEventConsumer]
  lazy val logRecordConsumer: LogRecordConsumer = wire[LogRecordConsumer]
  lazy val consumerAggregator: ConsumerAggregator = wire[ConsumerAggregator]

  override lazy val dynamicEvolutions = new DynamicEvolutions

  applicationLifecycle.addStopHook { () =>
    DBs.closeAll()
    messageRegistry.shutdown()
    Future.successful(())
  }

  val onStart: Unit = {
    DBs.setupAll()
    val evolutions = applicationEvolutions
    if (evolutions.upToDate) {
      readService.init()
    }
  }
}
