/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.protocol

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class RadiusComponents(radiusProtocol: RadiusProtocol) extends ProtocolComponents {
  override def onStart: Session => Session = ProtocolComponents.NoopOnStart

  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit
}