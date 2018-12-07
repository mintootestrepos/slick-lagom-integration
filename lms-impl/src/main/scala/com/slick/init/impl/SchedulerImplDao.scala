package com.slick.init.impl

import com.loanframe.lfdb.models._


class SchedulerImplDao(loginTable: LoginTable) {

  def fetchBorrowerProfile(id: String) = loginTable.findOneById(id)
}
