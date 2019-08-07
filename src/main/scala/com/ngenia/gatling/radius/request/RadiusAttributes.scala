/*
 * Gatling-Radius Plugin
 * Created on 24/06/2019
 *
 * @author Bilal Pierre ABDELKADER
 * @version: 1.0.6
 */
package com.ngenia.gatling.radius.request

import io.gatling.core.session.Expression

case class RadiusAttributes(
                             requestName: String,
                             username: Expression[String],
                             password: Option[Expression[String]],
                             properties: Map[String, Expression[Any]] = Map.empty
                           )