/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.client

import com.ngenia.gatling.radius.protocol.RadiusProtocol
import com.ngenia.gatling.radius.request._
import io.gatling.commons.stats._
import io.gatling.commons.validation._
import io.gatling.core.session.{Expression, Session}
import org.tinyradius.packet._
import org.tinyradius.util.RadiusClient

import scala.util.{Failure, Success, Try}

object RadiusClient {

  def sendRequest(radiusProtocol: RadiusProtocol, requestType: Type, radiusAttributes: RadiusAttributes)(implicit session: Session): (Option[RadiusPacket], (Status, Option[String])) = {

    val radiusClient = new RadiusClient(
      radiusProtocol.host,
      radiusProtocol.sharedKey)

    radiusClient.setSocketTimeout(radiusProtocol.replyTimeout)

    val response: (Option[RadiusPacket], (Status, Option[String])) =
      Try(
        requestType match {
          case Type.ACCESS_REQUEST => radiusClient.authenticate(radiusAttributes)
          case _ => radiusClient.account(accountingRequest(radiusAttributes, requestType))
        }
      ) match {
        case Success(radiusPacket) => (Some(radiusPacket),
          radiusPacket.getPacketType match {
            case RadiusPacket.ACCESS_REJECT => (KO, Some("ACCESS_REJECT"))
            case _ => (OK, None)
          })
        case Failure(e) => (None, (KO, Some(radiusAttributes.requestName + ": " + e.getMessage)))
      }

    radiusClient.close()

    response
  }

  private def resolveProperties(properties: Map[String, Expression[Any]])(implicit session: Session): Validation[Map[String, Any]] = {
    properties.foldLeft(Map.empty[String, Any].success) {
      case (resolvedProperties, (key, value)) =>
        for {
          value <- value(session)
          resolvedProperties <- resolvedProperties
        } yield resolvedProperties + (key -> value)

    }
  }

  private implicit def accessRequest(radiusAttributes: RadiusAttributes)(implicit session: Session): AccessRequest = {
    var accessRequest: AccessRequest = null

    for {
      resolvedUsername <- radiusAttributes.username(session)
      resolvedPassword <- radiusAttributes.password.getOrElse(None).asInstanceOf[Expression[String]](session)
      properties <- resolveProperties(radiusAttributes.properties)
    } yield {
      accessRequest = new AccessRequest(resolvedUsername, resolvedPassword)
      for ((k, v) <- properties.asInstanceOf[Map[String, AnyRef]])
        accessRequest.addAttribute(k, v.toString)
    }

    accessRequest.setAuthProtocol(AccessRequest.AUTH_CHAP); // or AUTH_PAP
    accessRequest
  }

  private implicit def accountingRequest(radiusAttributes: RadiusAttributes, requestType: Type)(implicit session: Session): AccountingRequest = {
    var accountingRequest: AccountingRequest = null

    for {
      resolvedUsername <- radiusAttributes.username(session)
      properties <- resolveProperties(radiusAttributes.properties)
    } yield {
      accountingRequest = new AccountingRequest(
        resolvedUsername,
        requestType match {
          case Type.ACCESS_REQUEST => 0
          case Type.ACCOUNT_START => 1
          case Type.ACCOUNT_STOP => 2
          case Type.ACCOUNT_INTERIM_UPDATE => 3
        })

      for ((k, v) <- properties.asInstanceOf[Map[String, AnyRef]])
        accountingRequest.addAttribute(k, v.toString)
    }

    for (x <- Option(RadiusUtils.framedIPAddress)) yield if (x != "") accountingRequest.addAttribute("Framed-IP-Address", x)

    accountingRequest.addAttribute("Acct-Session-Id", RadiusUtils.sessionId)

    accountingRequest
  }
}