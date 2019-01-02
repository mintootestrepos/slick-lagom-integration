package com.slick.init.impl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.gson.Gson
import com.loanframe.lfdb.models._
import com.slick.init.api.LMSService
import com.slick.init.models.LMSModels.{BorrowerProfile, DepositAmount}
import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class LMSMessage(msg: String, message: String)

class SlickExampleScheduler @Inject()(lmsService: LMSService, system: ActorSystem, materializer: ActorMaterializer, loginTable: LoginTable, configuration: Configuration, borrowersTable: BorrowersTable,
                                      lmsMessageTracker: LmsMsgTracker, trancheRecords: TrancheRecords, channelFinancingAgreementTable: ChannelFinancingAgreementTable,
                                      partnerTable: IntroducerTable, lenderTable: LenderTable, lenderRepayTable: LenderRepayTable)(implicit ec: ExecutionContext) {

  val borrowerProfile = DepositAmount( "ad3b29a5-ccf9-4ab3-976a-3271345ad3c6", "0fa91c33-892f-474f-a2ce-8226724bfa5e", "9876987.0", "12345", "20/00/2018")

  val gson = new Gson()
  val concurrency = Runtime.getRuntime.availableProcessors() * 10

  implicit val timeout: Timeout = 3.minute

  val schedulerImplDao = new SchedulerImplDao(loginTable)

  def hitLMSAPI = {

    println("=============>1")

    val borrower = lmsService.depositAmount.invoke(borrowerProfile)
    borrower.map(println)
    println(s"================> $borrower")
  }

  system.scheduler.schedule(2.seconds, 2.seconds) {
    println("=============>")
    hitLMSAPI
  }

}
