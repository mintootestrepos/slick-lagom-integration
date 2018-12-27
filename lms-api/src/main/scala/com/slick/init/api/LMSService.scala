package com.slick.init.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.slick.init.models.LMSModels.{DepositAmount, DepositAmountResponse, ProcessPayment}

trait LMSService extends Service {

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("lms-service")
      .withCalls(
        restCall(Method.POST, "/api/v1/loanApplication/depositAmount", depositAmount),
        restCall(Method.GET, "/public/earlyPartialRepayment?id&paymentDate", earlyPartialRepayment _)
      ).withAutoAcl(true)
    // @formatter:on
  }

  def depositAmount: ServiceCall[DepositAmount, DepositAmountResponse]
  def earlyPartialRepayment(id: String, paymentDate: String): ServiceCall[NotUsed, String]
}
