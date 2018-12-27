package com.slick.init.models

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, NotAcceptable}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable

object LMSModels {

  case class SQSMessage(MessageId: String, ReceiptHandle: String, MD5OfBody: String, Body: BorrowerProfileBody, Attributes: String, MessageAttributes: String)

  case class BorrowerProfileBody(borrowerId: String, cfaId: String, loanLimit: Double)

  case class BorrowerProfile(firstName: String, middleName: String, lastName: String, mobileNumber: String, emailAddress: String, password: String, confirmPassword: String)

  case class DepositAmount(access_token: String, userId: String, transactionAmount: String, bankReferenceNo: String, paymentDate: String)

  case class DepositAmountResponse(status: String, code: String, message: String)

  class PlainTextSerializer extends NegotiatedSerializer[String, ByteString] {
    override val protocol = MessageProtocol(Some("text/plain"))

    override def serialize(message: String): ByteString = ByteString.fromString(message, "utf-8")
  }

  class PlainTextDeserializer extends NegotiatedDeserializer[String, ByteString] {

    def deserialize(bytes: ByteString) = bytes.decodeString("utf-8")
  }

  class LMSMessageSerializer extends MessageSerializer[String, ByteString] {
    override def serializerForRequest: NegotiatedSerializer[String, ByteString] = new PlainTextSerializer

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[String, ByteString] = new PlainTextDeserializer

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[String, ByteString] = {
      acceptedMessageProtocols match {
        case Nil => new PlainTextSerializer
        case protocols => protocols.collectFirst {
          case MessageProtocol(Some("text/plain" | "text/*" | "*/*" | "*"), _, _) => new PlainTextSerializer
        }.getOrElse {
          throw NotAcceptable(acceptedMessageProtocols, MessageProtocol(Some("text/plain")))
        }
      }
    }
  }

  case class ProcessPayment(value: String)

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

  object ProcessPayment {
    implicit val format: PlainTextSerializer = new PlainTextSerializer
  }

}
