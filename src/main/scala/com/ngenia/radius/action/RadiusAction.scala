package com.ngenia.radius.action

import scala.util.{Failure, Success}

import io.gatling.core.action._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.commons.util.Clock

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

  override def name: String = "RADIUS"

  override def execute(session: Session): Unit = {

    implicit val iSession = session

    val start = clock.nowMillis

    val future = Future {
      client.RadiusClient.sendRequest(radiusProtocol, requestType, radiusAttributes)
    }

    future.onComplete {
      case Success(response) => {
        log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)
        next !
          session
            .set("Acct-Session-Id", RadiusUtils.sessionId)
            .set("Framed-IP-Address", RadiusUtils.framedIPAddress(response._1))
      }
      case Failure(e) => e.printStackTrace
    }
  }
}