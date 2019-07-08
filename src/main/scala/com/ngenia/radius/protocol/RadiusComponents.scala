package com.ngenia.radius.protocol

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class RadiusComponents(radiusProtocol: RadiusProtocol) extends ProtocolComponents {
  override def onStart: Session => Session = ProtocolComponents.NoopOnStart

  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit
}