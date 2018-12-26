package com.slick.init.api

import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.slick.init.models.LMSModels.{DepositAmount, DepositAmountResponse}

trait LMSService extends Service {

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("lms-service")
      .withCalls(
        restCall(Method.POST, "/v1/loanApplication/depositAmount", depositAmount)
      ).withAutoAcl(true)
    // @formatter:on
  }

  def depositAmount: ServiceCall[DepositAmount, DepositAmountResponse]
}
