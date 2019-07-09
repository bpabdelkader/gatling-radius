package com.ngenia.radius.action

import io.gatling.core.action._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.commons.stats._
import io.gatling.core.util.NameGen

trait RadiusLogging extends ChainableAction with NameGen {

  def log(start: Long, end: Long, status: (Status, Option[String]), requestName: String, session: Session, statsEngine: StatsEngine) =
    statsEngine.logResponse(session, requestName, start, end, status._1, None, status._2)
}