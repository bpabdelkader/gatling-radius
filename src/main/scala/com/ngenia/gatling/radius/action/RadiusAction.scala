/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.radius.action

import akka.actor.ActorSystem
import akka.japi.Option.Some
import akka.pattern.after
import com.ngenia.gatling.radius.action.RadiusLogging
import com.ngenia.gatling.radius.client
import com.ngenia.gatling.radius.client.RadiusUtils
import com.ngenia.gatling.radius.protocol.RadiusProtocol
import com.ngenia.gatling.radius.request._
import io.gatling.commons.stats.{KO, Status}
import io.gatling.commons.util.Clock
import io.gatling.core.action._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import org.tinyradius.packet.RadiusPacket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

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
    implicit val system = ActorSystem("theSystem")
    implicit val timeout = radiusProtocol.replyTimeout milliseconds

    val start = clock.nowMillis

    /*
     * Blocking Calls
     */
    /*val response = client.RadiusClient.sendRequest(radiusClient, requestType, radiusAttributes)
    log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)
    next !
      session
        .set("Acct-Session-Id", RadiusUtils.sessionId)
        .set("Framed-IP-Address", RadiusUtils.framedIPAddress(response._1))*/

    /*
     * Non-Blocking calls
     */
    Future {
      client.RadiusClient.sendRequest(radiusProtocol, requestType, radiusAttributes)
    } withTimeout new TimeoutException("Future timed out after " + timeout) onComplete {
      case Success(response) => next(system, start, response)
      case Failure(e) => next(system, start, (None, (KO, Some(e.getMessage))))
    }
  }

  private def next(system: ActorSystem,
                   start: Long,
                   response: (Option[RadiusPacket], (Status, Option[String]))
                  )(implicit session: Session): Unit = {
    log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)
    system.terminate()
    next ! session
      .set("Acct-Session-Id", RadiusUtils.sessionId)
      .set("Framed-IP-Address", RadiusUtils.framedIPAddress(response._1))
  }

  implicit class FutureExtensions[T](f: Future[T]) {
    def withTimeout(timeout: => Throwable)(implicit duration: FiniteDuration, system: ActorSystem): Future[T] = {
      Future firstCompletedOf Seq(f, after(duration, system.scheduler)(Future.failed(timeout)))
    }
  }

}