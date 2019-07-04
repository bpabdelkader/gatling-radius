package com.ngenia.radius.protocol


case object RadiusProtocolBuilderBase {
  def host(host: String) = RadiusProtocolBuilderSharedKey(host) // Step 1
}

case class RadiusProtocolBuilderSharedKey(
    host: String
) {
  def sharedKey(sharedKey: String) = RadiusProtocolBuilder(host, sharedKey) // Step 2
}

case class RadiusProtocolBuilder(
    host:        String,
    sharedKey:   String
) {
  def build(): RadiusProtocol = new RadiusProtocol(host, sharedKey)
}
