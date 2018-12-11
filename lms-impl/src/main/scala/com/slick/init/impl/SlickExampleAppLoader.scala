package com.slick.init.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.slick.init.lfdb.Logins
import play.api.Play
import play.api.db.HikariCPComponents
import play.api.db.slick.DatabaseConfigProvider
import slick.basic.BasicProfile
import slick.jdbc.PostgresProfile

import scala.collection.immutable
//import com.loanframe.lfdb.models.{ConfigTable, LoginTable}
import com.slick.init.api.{LMSService, SlickExampleLMSService}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class SlickExampleAppLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) {
    override def serviceLocator: ServiceLocator = NoServiceLocator

  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = new SlickExampleApp(context) with LagomDevModeComponents {
  }

  override def describeService = Some(readDescriptor[SlickExampleLMSServiceImpl])
}

abstract class SlickExampleApp(context: LagomApplicationContext)
  extends LagomApplication(context)
    with SlickPersistenceComponents
    with HikariCPComponents
    with JdbcPersistenceComponents
    with AhcWSComponents {

  override def jsonSerializerRegistry: JsonSerializerRegistry = new JsonSerializerRegistry {
    override def serializers: immutable.Seq[JsonSerializer[_]] = Vector.empty
  }

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[SlickExampleLMSService](wire[SlickExampleLMSServiceImpl])

  //Bind the external service in ServiceModule.
  lazy val externalService = serviceClient.implement[LMSService]
  implicit val app = Play.current
  // don't use an object and let the Slick DB be inject in constructor

  lazy val dbConfig = DatabaseConfigProvider.get[PostgresProfile]
  //  wire[ConfigTable]
  lazy val loginTable = wire[Logins]
  // inject ConfigTable, ActorSystem and Materializer here
  wire[SlickExampleScheduler](loginTable)


}
