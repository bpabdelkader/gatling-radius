package com.ngenia.radius.request


import io.gatling.core.session.Expression


case class RadiusAttributes(
  requestName:  String,
  username:     Expression[String],
  password:     Option[Expression[String]],
  properties:   Map[String, Expression[Any]] = Map.empty
)