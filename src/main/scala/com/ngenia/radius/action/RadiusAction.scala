package com.ngenia.radius.action

import akka.pattern.after
import akka.actor.ActorSystem
import akka.japi.Option.Some

import scala.util.{Failure, Success}
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import io.gatling.core.action._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.commons.util.Clock
import io.gatling.commons.stats.{KO, OK, Status}

import com.ngenia.radius.client
import com.ngenia.radius.client.RadiusUtils
import com.ngenia.radius.protocol.RadiusProtocol
import com.ngenia.radius.request._

case class RadiusAction(
                         requestType: Type,
                         radiusAttributes: RadiusAttributes,
                         radiusProtocol: RadiusProtocol,
                         clock: Clock,
                         statsEngine: StatsEngine,
                         next: Action
                       ) extends RadiusLogging {

  override def name: String = "Radius"

  override def execute(session: Session): Unit = {

    implicit val iSession = session

    val start = clock.nowMillis

    /*
     * Blocking Calls
     */
    val response = client.RadiusClient.sendRequest(radiusProtocol, requestType, radiusAttributes)
    log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)
    next !
      session
        .set("Acct-Session-Id", RadiusUtils.sessionId)
        .set("Framed-IP-Address", RadiusUtils.framedIPAddress(response._1))

    /*
     * Non-Blocking calls
     */
    /*implicit val system = ActorSystem("theSystem")
    implicit val timeout = 100 milliseconds

    lazy val future = Future {
      client.RadiusClient.sendRequest(radiusProtocol, requestType, radiusAttributes)
    }

    var result: (Status, Option[String]) = null
    future withTimeout new TimeoutException("Future timed out after " + timeout) onComplete {
      case Success(response) => {
        log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)
        system.terminate()
        next ! session
      }
      case Failure(e) => {
        log(start, clock.nowMillis, (KO, Some(e.getMessage)), radiusAttributes.requestName, session, statsEngine)
        system.terminate()
        next ! session
      }
    }*/
  }

  /*implicit class FutureExtensions[T](f: Future[T]) {
    def withTimeout(timeout: => Throwable)(implicit duration: FiniteDuration, system: ActorSystem): Future[T] = {
      Future firstCompletedOf Seq(f, after(duration, system.scheduler)(Future.failed(timeout)))
    }
  }*/

}