/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.protocol

case object RadiusProtocolBuilderBase {
  def host(host: String) = RadiusProtocolBuilderSharedKey(host)
}

case class RadiusProtocolBuilderSharedKey(host: String) {
  def sharedKey(sharedKey: String) = RadiusProtocolBuilderReplyTimeout(host, sharedKey)
}

case class RadiusProtocolBuilderReplyTimeout(host: String, sharedKey: String) {
  def replyTimeout(replyTimeout: Int) = RadiusProtocolBuilder(host, sharedKey, replyTimeout) // Step 2
}

case class RadiusProtocolBuilder(host: String, sharedKey: String, replyTimeout: Int) {
  def build(): RadiusProtocol = new RadiusProtocol(host, sharedKey, replyTimeout)
}
