package util

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object ThreadPools {
  implicit val IO: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val CPU: ExecutionContext = ExecutionContext.Implicits.global
}
