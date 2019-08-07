/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.action

import com.ngenia.gatling.radius.protocol.{RadiusComponents, RadiusProtocol}
import com.ngenia.gatling.radius.request._
import com.ngenia.radius.action.RadiusAction
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.ProtocolComponentsRegistry
import io.gatling.core.structure.ScenarioContext

case class RadiusActionBuilder(requestType: Type, radiusAttributes: RadiusAttributes, configuration: GatlingConfiguration) extends ActionBuilder {

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry): RadiusComponents =
    protocolComponentsRegistry.components(RadiusProtocol.RadiusProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action =
    RadiusAction(requestType, radiusAttributes, components(ctx.protocolComponentsRegistry).radiusProtocol, ctx.coreComponents.clock, ctx.coreComponents.statsEngine, next)

}