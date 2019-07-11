package com.ngenia.radius

import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder

import com.ngenia.radius.Predef._
import com.ngenia.radius.protocol._

class RadiusSimulation extends Simulation {

  implicit val radiusProtocol: RadiusProtocol = radius
    .host("10.20.30.40")
    .sharedKey("mySharedKey")
    .replyTimeout(1000) // replyTimeout in ms

  val scn = scenario("Access Request")
    .feed(csv("data/dataFeeder.csv").circular)
    .exec(
      radius("Access Request")
        .username("${username}")
        .password("${password}")
        .properties(
          Map(
            "NAS-Identifier" -> "${NAS-Identifier}",
            "NAS-IP-Address" -> "${NAS-IP-Address}",
            "Calling-Station-Id" -> "${Calling-Station-Id}",
            "Called-Station-Id" -> "${Called-Station-Id}",
          ))
        .authenticate())
    .exec(
      radius("Acct Start")
        .username("${username}")
        .properties(
          Map(
            "NAS-Identifier" -> "${NAS-Identifier}",
            "NAS-IP-Address" -> "${NAS-IP-Address}",
            "Calling-Station-Id" -> "${Calling-Station-Id}",
            "Called-Station-Id" -> "${Called-Station-Id}",
          ))
        .accountStart())
    .exec(
      radius("Interim Update")
        .username("${username}")
        .properties(
          Map(
            "NAS-Identifier" -> "${NAS-Identifier}",
            "NAS-IP-Address" -> "${NAS-IP-Address}",
            "Calling-Station-Id" -> "${Calling-Station-Id}",
            "Called-Station-Id" -> "${Called-Station-Id}",
          ))
        .interimUpdate())
    .exec(
      radius("Acct Stop")
        .username("${username}")
        .properties(
          Map(
            "NAS-Identifier" -> "${NAS-Identifier}",
            "NAS-IP-Address" -> "${NAS-IP-Address}",
            "Calling-Station-Id" -> "${Calling-Station-Id}",
            "Called-Station-Id" -> "${Called-Station-Id}",
          ))
        .accountStop())

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(radiusProtocol)
}

object RadiusSimulation {
  def main(args: Array[String]): Unit =
    Gatling.fromMap((new GatlingPropertiesBuilder).simulationClass(classOf[com.ngenia.radius.RadiusSimulation].getName).build)
}