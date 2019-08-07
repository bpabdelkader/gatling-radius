/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.client

import io.gatling.core.session.Session
import org.tinyradius.packet.RadiusPacket

import scala.util.{Failure, Success, Try}

object RadiusUtils {

  def framedIPAddress()(implicit session: Session): String =
    (for (x <- Option(session.attributes.contains("Framed-IP-Address")))
      yield
        if (x) session.attributes("Framed-IP-Address").asInstanceOf[String]
        else "").get

  def framedIPAddress(radiusPacket: Option[RadiusPacket])(implicit session: Session): String = {
    Try {
      (for {
        x <- Option(session.attributes.contains("Framed-IP-Address"))
        y <- Option(radiusPacket.isDefined)
      } yield {
        if (x) session.attributes("Framed-IP-Address").asInstanceOf[String]
        else if (y)
          if ((radiusPacket.get.getAuthenticator != null) && (radiusPacket.get.getPacketType != RadiusPacket.ACCESS_REJECT))
            radiusPacket.get.getAttribute("Framed-IP-Address").getAttributeValue
          else ""
        else ""
      }).get
    } match {
      case Success(framedIPAddress) => framedIPAddress
      case Failure(e) => ""
    }
  }

  def sessionId(implicit session: Session): String = {
    (for (x <- Option(session.attributes.contains("Acct-Session-Id")))
      yield if (x) session.attributes("Acct-Session-Id").asInstanceOf[String] else System.currentTimeMillis.toString
      ).get
  }
}
