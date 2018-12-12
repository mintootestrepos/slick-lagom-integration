package com.slick.init.impl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.loanframe.lfdb.models.LoginTable
import com.slick.init.api.{LMSService, SlickExampleLMSService}
import com.softwaremill.macwire._
import play.api.db.{ConnectionPool, HikariCPConnectionPool}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class SlickExampleAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator

  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) with LagomDevModeComponents {

  }

  override def describeService = Some(readDescriptor[SlickExampleLMSServiceImpl])
}

abstract class SlickExampleApp(context: LagomApplicationContext) extends LagomApplication(context)
  with SlickPersistenceComponents with AhcWSComponents {

  override implicit lazy val actorSystem: ActorSystem = ActorSystem("LMSActorSystem")

  override lazy val materializer: ActorMaterializer = ActorMaterializer()
  override lazy val lagomServer = serverFor[SlickExampleLMSService](wire[SlickExampleLMSServiceImpl])
  lazy val externalService = serviceClient.implement[LMSService]

  override def connectionPool: ConnectionPool = new HikariCPConnectionPool(environment)

  override def jsonSerializerRegistry: JsonSerializerRegistry = new JsonSerializerRegistry {
    override def serializers: immutable.Seq[JsonSerializer[_]] = Vector.empty
  }

  val loginTable = wire[LoginTable]

  wire[SlickExampleScheduler]

}
