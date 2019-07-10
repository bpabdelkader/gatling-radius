package com.ngenia.radius.action

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.Action
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.config.GatlingConfiguration

import com.ngenia.radius.request._
import com.ngenia.radius.protocol.{RadiusComponents, RadiusProtocol}

case class RadiusActionBuilder(requestType: Type, radiusAttributes: RadiusAttributes, configuration: GatlingConfiguration) extends ActionBuilder {

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry): RadiusComponents =
    protocolComponentsRegistry.components(RadiusProtocol.RadiusProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action =
    RadiusAction(requestType, radiusAttributes, components(ctx.protocolComponentsRegistry).radiusProtocol, ctx.coreComponents.clock, ctx.coreComponents.statsEngine, next)

}