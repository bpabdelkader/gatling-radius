/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.protocol

case object RadiusProtocolBuilderBase {
  def host(host: String, authPort: Option[Int] = None, acctPort: Option[Int] = None) = RadiusProtocolBuilderSharedKey(host, authPort, acctPort)
}

case class RadiusProtocolBuilderSharedKey(host: String, authPort: Option[Int], acctPort: Option[Int]) {
  def sharedKey(sharedKey: String) = RadiusProtocolBuilderReplyTimeout(host, authPort, acctPort, sharedKey)
}

case class RadiusProtocolBuilderReplyTimeout(host: String, authPort: Option[Int], acctPort: Option[Int], sharedKey: String) {
  def replyTimeout(replyTimeout: Int) = RadiusProtocolBuilder(host, authPort, acctPort, sharedKey, replyTimeout) // Step 2
}

case class RadiusProtocolBuilder(host: String, authPort: Option[Int], acctPort: Option[Int], sharedKey: String, replyTimeout: Int) {
  def build(): RadiusProtocol = new RadiusProtocol(host, authPort, acctPort, sharedKey, replyTimeout)
}
