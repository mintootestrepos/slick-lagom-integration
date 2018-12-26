package com.slick.init.models

import play.api.libs.json.{Format, Json}

object LMSModels {

  case class SQSMessage(MessageId: String, ReceiptHandle: String, MD5OfBody: String, Body: BorrowerProfileBody, Attributes: String, MessageAttributes: String)

  case class BorrowerProfileBody(borrowerId: String, cfaId: String, loanLimit: Double)

  case class BorrowerProfile(firstName: String, middleName: String, lastName: String, mobileNumber: String, emailAddress: String, password: String, confirmPassword: String)

  case class DepositAmount(access_token: String, userId: String, transactionAmount: String, bankReferenceNo: String, paymentDate: String)

  case class DepositAmountResponse(status: String, code: String, message: String)

  object DepositAmount {
    /**
      * Format for converting user messages to and from JSON.
      *
      * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
      */
    implicit val format: Format[DepositAmount] = Json.format[DepositAmount]
  }

  object DepositAmountResponse {
    /**
      * Format for converting user messages to and from JSON.
      *
      * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
      */
    implicit val format: Format[DepositAmountResponse] = Json.format[DepositAmountResponse]
  }

  object BorrowerProfile {
    /**
      * Format for converting user messages to and from JSON.
      *
      * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
      */
    implicit val format: Format[BorrowerProfile] = Json.format[BorrowerProfile]
  }

}
