package com.slick.init.impl

import com.loanframe.lfdb.models.LoginTable


class SchedulerImplDao(loginTable: LoginTable) {

  def fetchBorrowerProfile(id: String) = loginTable.findOneById(id)
}
