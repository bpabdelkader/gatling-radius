/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.dsl

import com.ngenia.gatling.radius.protocol._
import io.gatling.core.config.GatlingConfiguration

trait RadiusDsl {

  def radius(implicit configuration: GatlingConfiguration) = RadiusProtocolBuilderBase

  def radius(requestName: String) = RadiusDslBuilderBase(requestName)

  implicit def radiusProtocolBuilder2RadiusProtocol(builder: RadiusProtocolBuilder): RadiusProtocol = builder.build()
}