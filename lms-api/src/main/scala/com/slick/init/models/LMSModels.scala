package com.slick.init.models

import play.api.libs.json.{Format, Json}

object LMSModels {

  case class SQSMessage(MessageId: String, ReceiptHandle: String, MD5OfBody: String, Body: BorrowerProfileBody, Attributes: String, MessageAttributes: String)

  case class BorrowerProfileBody(borrowerId: String, cfaId: String, loanLimit: Double)

  case class BorrowerProfile(firstName: String, middleName: String, lastName: String, mobileNumber: String, emailAddress: String, password: String, confirmPassword: String)

  object BorrowerProfile {
    /**
      * Format for converting user messages to and from JSON.
      *
      * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
      */
    implicit val format: Format[BorrowerProfile] = Json.format[BorrowerProfile]
  }

}
