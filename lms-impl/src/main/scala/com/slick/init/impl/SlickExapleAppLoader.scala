package com.slick.init.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.slick.init.api.{LMSService, SlickExampleLMSService}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class SlickExapleAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator
  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) with LagomDevModeComponents {

  }

  override def describeService = Some(readDescriptor[SlickExampleLMSServiceImpl])
}

abstract class SlickExampleApp(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[SlickExampleLMSService](wire[SlickExampleLMSServiceImpl])

  //Bind the external service in ServiceModule.
  lazy val externalService = serviceClient.implement[LMSService]

  wire[SlickExampleScheduler]

}
