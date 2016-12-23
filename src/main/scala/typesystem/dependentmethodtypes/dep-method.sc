import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class LocalResponse(statusCode: Int)
case class RemoteResponse(message: String)

sealed trait Computation {
  type Response
  val work: Future[Response]
}

case class LocalCompuation(work: Future[LocalResponse]) extends Computation {
  type Response = LocalResponse
}

case class RemoteComputation(work: Future[RemoteResponse]) extends Computation {
  type Response = RemoteResponse
}

object Service {
  def handle(computation: Computation): computation.Response = {
    val duration = Duration(2, SECONDS)
    Await.result(computation.work, duration)
  }
}

Service.handle(LocalCompuation(Future(LocalResponse(0))))
Service.handle(RemoteComputation(Future(RemoteResponse("remote call"))))
