package com.slick.init.impl

import com.slick.init.lfdb.LoginTable

class SchedulerImplDao(loginTable: LoginTable) {

  def fetchBorrowerProfile(id: String) = loginTable.findOneById(id)
}
