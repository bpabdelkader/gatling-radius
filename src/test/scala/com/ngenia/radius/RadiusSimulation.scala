package com.ngenia.radius

import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.core.config.GatlingPropertiesBuilder

import com.ngenia.radius.Predef._
import com.ngenia.radius.protocol._

class RadiusSimulation extends Simulation {

  implicit val radiusProtocol: RadiusProtocol = radius
    .host("10.20.30.40")
    .sharedKey("mySharedKey")
    .replyTimeout(1000) // replyTimeout in ms

  val radiusProperties: Map[String, Expression[Any]] = Map(
    "NAS-Identifier" -> "${NAS-Identifier}",
    "NAS-IP-Address" -> "${NAS-IP-Address}",
    "Calling-Station-Id" -> "${Calling-Station-Id}",
    "Called-Station-Id" -> "${Called-Station-Id}"
  )

  /*
   * Loading feeder files in memory uses a lot of heap, expect a 5-to-10-times ratio with the file size.
   * This is due to JVM’s internal UTF-16 char encoding and object headers overhead.
   * It's better to create custom Iterator =>
   *  val loginFeeder =
    (for (i <- 300001 until 300010)
      yield Map(
        "username" -> s"login$i",
        "password" -> s"passwd$i"
      )).circular
   */
  val dataFeeder = csv("data/dataFeeder.csv").circular

  val scn = scenario("Radius Scenario")
    .feed(dataFeeder)
    .exec(
      radius("Access Request")
        .username("${username}")
        .password("${password}")
        .properties(radiusProperties)
        .authenticate())
    .exec(
      radius("Acct Start")
        .username("${username}")
        .properties(radiusProperties)
        .accountStart())
    .exec(
      radius("Interim Update")
        .username("${username}")
        .properties(radiusProperties)
        .interimUpdate())
    .exec(
      radius("Acct Stop")
        .username("${username}")
        .properties(radiusProperties)
        .accountStop())

  setUp(
    scn.inject(
      constantUsersPerSec(1000) during (15)
    )
  ).protocols(radiusProtocol)
}

object RadiusSimulation {
  def main(args: Array[String]): Unit =
    Gatling.fromMap((new GatlingPropertiesBuilder).simulationClass(classOf[com.ngenia.radius.RadiusSimulation].getName).build)
}