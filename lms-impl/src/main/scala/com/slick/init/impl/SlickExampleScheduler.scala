package com.slick.init.impl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.gson.Gson
import com.loanframe.lfdb.models.LoginTable
import com.slick.init.api.LMSService
import com.slick.init.models.LMSModels.BorrowerProfile
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class LMSMessage(msg: String, message: String)

class SlickExampleScheduler @Inject()(lmsService: LMSService, configuration: Configuration)(implicit ec: ExecutionContext) {
  val borrowerProfile = BorrowerProfile("John", "", "Becker", "+919686880498", "john.becker+29c0e4eec5614ef4a85d20320497c06a@gmail.com",
    "29c0e4eec5614ef4a85d20320497c06a", "29c0e4eec5614ef4a85d20320497c06a")
  val gson = new Gson()
  val concurrency = Runtime.getRuntime.availableProcessors() * 10

  implicit val timeout: Timeout = 3.minute
  implicit val system: ActorSystem = ActorSystem("LMSActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val schedulerImplDao = new SchedulerImplDao(LoginTable)

  def hitLMSAPI = {

    val borrower = schedulerImplDao.fetchBorrowerProfile("SomeID")
    println(s"================> $borrower")
  }

  system.scheduler.schedule(2.seconds, 2.seconds)(hitLMSAPI)

}
