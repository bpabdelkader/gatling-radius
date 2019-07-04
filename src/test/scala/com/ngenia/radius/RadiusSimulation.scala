package com.ngenia.radius

import com.ngenia.radius.Predef._
import com.ngenia.radius.protocol._
import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder

class RadiusSimulation extends Simulation {

  implicit val radiusProtocol: RadiusProtocol = radius
    .host("10.20.30.40")
    .sharedKey("mySharedKey")

  val scn = scenario("Access Request")
    .exec(
      radius("Access Request")
        .username("login001")
        .password("passwd001")
        .properties(
          Map(
            "NAS-Identifier" -> "ATANDT",
            "NAS-IP-Address" -> "1.2.3.4",
            "Calling-Station-Id" -> "33012345678",
            "Called-Station-Id" -> "TESTING",
          ))
        .authenticate())
    .exec(
      radius("Acct Start")
        .username("login001")
        .properties(
          Map(
            "NAS-Identifier" -> "ATANDT",
            "NAS-IP-Address" -> "1.2.3.4",
            "Calling-Station-Id" -> "33012345678",
            "Called-Station-Id" -> "TESTING",
          ))
        .accountStart())
    .exec(
      radius("Interim Update")
        .username("login001")
        .properties(
          Map(
            "NAS-Identifier" -> "ATANDT",
            "NAS-IP-Address" -> "1.2.3.4",
            "Calling-Station-Id" -> "33012345678",
            "Called-Station-Id" -> "TESTING",
          ))
        .interimUpdate())
    .exec(
      radius("Acct Stop")
        .username("login001")
        .properties(
          Map(
            "NAS-Identifier" -> "ATANDT",
            "NAS-IP-Address" -> "1.2.3.4",
            "Calling-Station-Id" -> "33012345678",
            "Called-Station-Id" -> "TESTING",
          ))
        .accountStop()
    )

  setUp(scn.inject(atOnceUsers(1))).protocols(radiusProtocol)
}

object RadiusSimulation {
  def main(args: Array[String]): Unit =
    Gatling.fromMap((new GatlingPropertiesBuilder).simulationClass(classOf[com.ngenia.radius.RadiusSimulation].getName).build)
}