package com.slick.init.api

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.slick.init.models.LMSModels.BorrowerProfile

trait LMSService extends Service {

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("lms-service")
      .withCalls(
        restCall(Method.POST, "/v1/borrower/stepOne", createBorrowerProfile)
      ).withAutoAcl(true)
    // @formatter:on
  }

  def createBorrowerProfile: ServiceCall[BorrowerProfile, Done]
}
