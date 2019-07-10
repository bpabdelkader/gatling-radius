package com.ngenia.radius.dsl

import io.gatling.core.Predef.{Session, configuration}
import io.gatling.core.session.Expression
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration

import com.ngenia.radius.action.RadiusActionBuilder
import com.ngenia.radius.request._

case class RadiusDslBuilderBase(requestName: String) {
  def username(username: Expression[String]): RadiusDslBuilderPassword = RadiusDslBuilderPassword(configuration, requestName, username)
}

case class RadiusDslBuilderPassword(configuration: GatlingConfiguration, requestName: String, username: Expression[String]) {
  // Password is optional for Accounting requests
  def password(password: Expression[String]): RadiusDslBuilderProperties = RadiusDslBuilderProperties(configuration, requestName, username, password)

  def properties(properties: Map[String, Expression[Any]]) = RadiusDslBuilder(configuration, RadiusAttributes(requestName, username, None, properties))
}

case class RadiusDslBuilderProperties(configuration: GatlingConfiguration, requestName: String, username: Expression[String], password: Expression[String]) {
  def properties(properties: Map[String, Expression[Any]]) = RadiusDslBuilder(configuration, RadiusAttributes(requestName, username, Some(password), properties))
}

case class RadiusDslBuilder(onfiguration: GatlingConfiguration, radiusAttributes: RadiusAttributes) {
  def authenticate(): ActionBuilder = radiusActionBuilder(Type.ACCESS_REQUEST, radiusAttributes, configuration)

  def accountStart(): ActionBuilder = radiusActionBuilder(Type.ACCOUNT_START, radiusAttributes, configuration)

  def interimUpdate(): ActionBuilder = radiusActionBuilder(Type.ACCOUNT_INTERIM_UPDATE, radiusAttributes, configuration)

  def accountStop(): ActionBuilder = radiusActionBuilder(Type.ACCOUNT_STOP, radiusAttributes, configuration)

  private def radiusActionBuilder(requestType: Type, radiusAttributes: RadiusAttributes, configuration: GatlingConfiguration): ActionBuilder = {
    RadiusActionBuilder.apply(requestType, radiusAttributes, configuration)
  }
}