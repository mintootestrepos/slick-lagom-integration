package com.slick.init.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.slick.init.api.SlickExampleLMSService
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future

class SlickExampleLMSServiceImpl extends SlickExampleLMSService {

  private final val log: Logger = LoggerFactory.getLogger(classOf[SlickExampleLMSServiceImpl])

  override def checkHealth = ServiceCall { _ =>
    println("The LMS integrator is up and running...")
    Future.successful(ResponseHeader.Ok.toString)
  }
}
