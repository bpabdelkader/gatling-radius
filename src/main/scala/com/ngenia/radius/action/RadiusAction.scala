package com.ngenia.radius.action


import scala.util.{Failure, Success, Try}

import io.gatling.core.action._
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.commons.util.Clock
import io.gatling.commons.stats._
import io.gatling.commons.validation._

import org.tinyradius.util.RadiusClient
import org.tinyradius.packet._

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

    implicit val implicitSession = session
    implicit val implicitRequestType = requestType

    val start = clock.nowMillis

    val radiusClient = new RadiusClient(
      radiusProtocol.host,
      radiusProtocol.sharedKey
    )

    radiusClient.setSocketTimeout(radiusProtocol.replyTimeout)

    val response: (Option[RadiusPacket], (Status, Option[String])) =
      Try(sendRequest(radiusClient, radiusAttributes)) match {
        case Success(radiusPacket) => (Some(radiusPacket),
          radiusPacket.getPacketType match {
            case RadiusPacket.ACCESS_REJECT => (KO, Some("ACCESS_REJECT"))
            case _ => (OK, None)
          })
        case Failure(e) => (None, (KO, Some(radiusAttributes.requestName + ": " + e.getMessage)))
      }

    radiusClient.close()

    log(start, clock.nowMillis, response._2, radiusAttributes.requestName, session, statsEngine)

    next !
      session
        .set("Acct-Session-Id", sessionId)
        .set("Framed-IP-Address", framedIPAddress(response._1))
  }

  implicit def accessRequest(radiusAttributes: RadiusAttributes)(implicit session: Session): AccessRequest = {

    var accessRequest: AccessRequest = null

    for {
      resolvedUsername <- radiusAttributes.username(session)
      resolvedPassword <- radiusAttributes.password.getOrElse(None).asInstanceOf[Expression[String]](session)
      properties <- resolveProperties(radiusAttributes.properties, session)
    } yield {
      accessRequest = new AccessRequest(resolvedUsername, resolvedPassword)
      for ((k, v) <- properties.asInstanceOf[Map[String, AnyRef]])
        accessRequest.addAttribute(k, v.toString)
    }

    accessRequest.setAuthProtocol(AccessRequest.AUTH_CHAP); // or AUTH_PAP
    accessRequest
  }

  implicit def accountingRequest(radiusAttributes: RadiusAttributes)(implicit requestType: Type, session: Session): AccountingRequest = {

    var accountingRequest: AccountingRequest = null

    for {
      resolvedUsername <- radiusAttributes.username(session)
      properties <- resolveProperties(radiusAttributes.properties, session)
    } yield {
      accountingRequest = new AccountingRequest(
        resolvedUsername,
        requestType match {
          case Type.ACCOUNT_START => 1
          case Type.ACCOUNT_STOP => 2
          case Type.ACCOUNT_INTERIM_UPDATE => 3
        })

      for ((k, v) <- properties.asInstanceOf[Map[String, AnyRef]])
        accountingRequest.addAttribute(k, v.toString)
    }

    for (framedIPAddress <- Option(session.attributes("Framed-IP-Address").asInstanceOf[String]) if framedIPAddress.nonEmpty)
      yield accountingRequest.addAttribute("Framed-IP-Address", framedIPAddress)

    accountingRequest.addAttribute("Acct-Session-Id", sessionId)
    accountingRequest
  }

  private def sendRequest(radiusClient: RadiusClient, radiusAttributes: RadiusAttributes)(implicit requestType: Type, session: Session): RadiusPacket = {
    requestType match {
      case Type.ACCESS_REQUEST => radiusClient.authenticate(radiusAttributes)
      case _ => radiusClient.account(radiusAttributes)
    }
  }

  private def resolveProperties(
                                 properties: Map[String, Expression[Any]],
                                 session: Session
                               ): Validation[Map[String, Any]] =
    properties.foldLeft(Map.empty[String, Any].success) {
      case (resolvedProperties, (key, value)) =>
        for {
          value <- value(session)
          resolvedProperties <- resolvedProperties
        } yield resolvedProperties + (key -> value)

    }

  private def framedIPAddress(radiusPacket: Option[RadiusPacket])(implicit session: Session): String = {
    (for {
      x <- Option(session.attributes.contains("Framed-IP-Address"))
      y <- Option(radiusPacket.isDefined)
    } yield {
      if (x) session.attributes("Framed-IP-Address").asInstanceOf[String]
      else if (y)
        if (radiusPacket.get.getAuthenticator != null) radiusPacket.get.getAttribute("Framed-IP-Address").getAttributeValue else ""
      else ""
    }).get
  }

  private def sessionId(implicit session: Session): String = {
    for (isSessionId <- Option(session.attributes.contains("Acct-Session-Id")) if isSessionId)
      yield session.attributes("Acct-Session-Id").asInstanceOf[String]
    System.currentTimeMillis.toString
  }
}