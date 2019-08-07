/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.protocol

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

object RadiusProtocol {

  val RadiusProtocolKey: ProtocolKey[RadiusProtocol, RadiusComponents] = new ProtocolKey[RadiusProtocol, RadiusComponents] {

    override def protocolClass: Class[Protocol] = classOf[RadiusProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultProtocolValue(configuration: GatlingConfiguration): RadiusProtocol =
      throw new IllegalStateException("Can't provide a default value for RadiusProtocol")

    override def newComponents(coreComponents: CoreComponents): RadiusProtocol => RadiusComponents = {
      radiusProtocol => RadiusComponents(radiusProtocol)
    }
  }
}

case class RadiusProtocol(
                           host: String,
                           sharedKey: String,
                           replyTimeout: Int,
                         ) extends Protocol {

  type Components = RadiusComponents
}
