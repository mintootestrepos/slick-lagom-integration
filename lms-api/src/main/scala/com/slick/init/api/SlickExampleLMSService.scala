package com.slick.init.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

trait SlickExampleLMSService extends Service {

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("lf-lms-service")
      .withCalls(
        restCall(Method.GET, "/lf-lms/health", checkHealth)
      )
      .withAutoAcl(true)
    // @formatter:on
  }

  def checkHealth: ServiceCall[NotUsed, String]
}