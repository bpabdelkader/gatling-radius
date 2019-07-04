package com.ngenia.radius.dsl

import com.ngenia.radius.protocol._
import io.gatling.core.config.GatlingConfiguration


trait RadiusDsl {

  def radius(implicit configuration: GatlingConfiguration) = RadiusProtocolBuilderBase

  def radius(requestName: String) = RadiusDslBuilderBase(requestName)

  implicit def radiusProtocolBuilder2RadiusProtocol(builder: RadiusProtocolBuilder): RadiusProtocol = builder.build()
}
