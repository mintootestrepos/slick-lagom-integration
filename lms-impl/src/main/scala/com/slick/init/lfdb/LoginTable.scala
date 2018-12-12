package com.slick.init.lfdb

import java.sql.Timestamp

import com.loanframe.lfdb.contract.LoginAuthType.LoginAuthType
import com.loanframe.lfdb.contract.RoleType.RoleType
import com.loanframe.lfdb.contract.{LoginAuthType, RoleType, UserProfile}
import com.loanframe.lfdb.util.Functions._
import javax.inject.Inject
import play.api.libs.Codecs
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

case class Login(id: String, email: String, password: String, authCode: String, expire: Timestamp, profileComplete: Boolean,
                 createTime: Timestamp, updateTime: Timestamp, firstName: String, lastName: String, mobile: String, authType: LoginAuthType,
                 token: Option[String], passwordResetKey: Option[String] = None, status: Boolean = false, otpCode: String = "",
                 role: RoleType = RoleType.Borrower, appPushId: Option[String], webPushId: Option[String], agentId: Option[String])

class LoginTable @Inject()(val driver: JdbcProfile, val db: Database) {

  import driver.api._

  val logins: TableQuery[LoginMapping] = TableQuery[LoginMapping]

  implicit val loginAuthTypeMapper = MappedColumnType.base[LoginAuthType, String](
    e => e.toString,
    s => LoginAuthType.withName(s)
  )

  implicit val loanTypeMapper = MappedColumnType.base[RoleType, String](e => e.toString, s => RoleType.withName(s))

  def findOneById(id: String): Option[Login] = sync(db.run(logins.filter(_.id === id).result.headOption))

  def findBorrowerByEmail(email: String): Option[Login] = sync(db.run(logins.filter(u => u.email === email.toLowerCase
    && u.role === RoleType.Borrower).result.headOption))

  def findActiveIdByEmailAndPassword(email: String, password: String): Option[Login] = sync(db.run(logins.filter(u => u.email === email.toLowerCase
    && u.password === password && u.status === true).result.headOption))

  def findOneIdByEmailAndPassword(email: String, password: String): Future[Option[Login]] = db.run(logins.filter(u => u.email === email.toLowerCase
    && u.password === password).result.headOption)

  def findActiveIdByAuthCode(auth: String): Option[String] = sync(db.run((for (l <- logins.filter(u => u.authcode === auth
    && u.status === true)) yield l.email).result.headOption))

  def findActiveIdWithRoleByAuthCode(auth: String, role: RoleType): Option[String] = sync(db.run((for (l <- logins.filter(u => u.authcode === auth
    && u.status === true && u.role === role)) yield l.email).result.headOption))

  def findIdWithRoleByAuthCode(auth: String, role: RoleType): Option[String] = sync(db.run((for (l <- logins.filter(u => u.authcode === auth
    && u.role === role)) yield l.email).result.headOption))

  def findActiveIdWithAdminRoleAuthCode(auth: String): Option[String] = sync(db.run((for (l <- logins.filter(u => u.authcode === auth && u.status === true
    && (u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager || u.role === RoleType.CreditAnalyst
    || u.role === RoleType.SalesManager))) yield l.email).result.headOption))

  def findActiveAdminByEmail(email: String): Option[Login] = sync(db.run(logins.filter(u => u.email === email.toLowerCase && u.status === true
    && (u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager || u.role === RoleType.CreditAnalyst
    || u.role === RoleType.SalesManager)).result.headOption))

  def findActiveAdminByAuthCode(auth: String): Option[Login] = sync(db.run(logins.filter(u => u.authcode === auth && u.status === true
    && (u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager || u.role === RoleType.CreditAnalyst
    || u.role === RoleType.SalesManager)).result.headOption))

  def findActiveIdWithRoleOrAdminRoleAuthCode(auth: String, role: RoleType): Option[String] = sync(db.run((for (l <- logins.filter(u => u.authcode === auth
    && u.status === true && (u.role === role || u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager
    || u.role === RoleType.CreditAnalyst || u.role === RoleType.SalesManager))) yield l.email).result.headOption))

  def findActiveAdminOrUserWithRoleByEmail(email: String, role: RoleType): Option[Login] = sync(db.run(logins.filter(u => u.email === email.toLowerCase
    && u.status === true && (u.role === role || u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager
    || u.role === RoleType.CreditAnalyst || u.role === RoleType.SalesManager)).result.headOption))

  def forgotPassword(email: String): Boolean = {
    val query = for (l <- logins.filter(_.email.toLowerCase === email.toLowerCase)) yield l.passwordResetKey
    sync(db.run(query.update(Some(newUUID())))) == 1
  }

  def setResetPasswordKey(email: String): Option[Login] = findOneByEmail(email) match {
    case Some(user) =>
      val updateUser = user.copy(passwordResetKey = Some(newUUID()))
      sync(db.run(logins.filter(_.id === user.id).update(updateUser)))
      Some(updateUser)
    case _ => None
  }

  /**
    * find one login by email
    *
    * @param email email of user
    * @return
    */
  def findOneByEmail(email: String): Option[Login] = sync(db.run(logins.filter(_.email === email.toLowerCase).result.headOption))

  def registerUser(login: Login): Try[Login] = try sync(db.run(logins += login)) match {
    case 1 => Success(login)
    case _ => Failure(new Exception("Registration failed."))
  }
  catch {
    case ex: Exception => ex.printStackTrace()
      if (ex.getMessage.contains("duplicate key value")) Failure(new Exception("User id already exists."))
      else Failure(new Exception(ex.getMessage))
  }

  def addInTransaction(login: Login): DBIO[Boolean] = (logins += login).map(_ == 1)

  def setPassword(password: String, passwordKey: String): Boolean = {
    val query = for (l <- logins.filter(_.passwordResetKey === passwordKey)) yield l.password
    sync(db.run(query.update(password))) match {
      case 1 =>
        val resetKey = for (l <- logins.filter(_.passwordResetKey === passwordKey)) yield l.passwordResetKey
        sync(db.run(resetKey.update(None)))
        true
      case _ => false
    }
  }

  /**
    * activate login
    *
    * @param email
    * @param otp
    * @return
    */
  def activate(email: String, otp: String): Boolean = {
    val query = for (l <- logins.filter(u => u.email.toLowerCase === email.toLowerCase && u.otpCode === otp)) yield l.status
    sync(db.run(query.update(true))) == 1
  }

  def activateInTransaction(email: String, otp: String): DBIO[Boolean] = (for (l <- logins
    .filter(u => u.email.toLowerCase === email.toLowerCase && u.otpCode === otp)) yield l.status).update(true).map(_ == 1)

  def activateInTransactionById(id: String): DBIO[Boolean] = (for (l <- logins.filter(u => u.id === id)) yield l.status).update(true).map(_ == 1)

  def generateNewOtp(emailId: String, newMobile: String): Option[Login] = findActiveIdByEmail(emailId) match {
    case Some(user) =>
      val updatedProfile = user.copy(otpCode = newOTP())
      sync(db.run(logins.filter(_.id === user.id).update(updatedProfile)))
      Some(updatedProfile)
    case None => None
  }

  /**
    * find one active login by email
    *
    * @param email email of user
    * @return
    */
  def findActiveIdByEmail(email: String): Option[Login] = sync(db.run(logins.filter(u => u.email === email.toLowerCase && u.status === true).result.headOption))

  def changeProfile(user: Login, emailId: String, profileInfo: UserProfile): Option[UserProfile] =
    profileInfo.password match {
      case Some(newPassword) =>
        val encryptedPass = Codecs.sha1(newPassword)
        val updatedProfile = user.copy(email = profileInfo.email, password = encryptedPass)
        sync(db.run(logins.filter(_.id === user.id).update(updatedProfile)))
        Some(UserProfile(updatedProfile.email, None))
      case None =>
        val updatedProfile = user.copy(email = profileInfo.email)
        sync(db.run(logins.filter(_.id === user.id).update(updatedProfile)))
        Some(UserProfile(updatedProfile.email, None))
    }

  def changeProfileInTransaction(currentLogin: Login, emailId: String, profileInfo: UserProfile): DBIO[Boolean] =
    profileInfo.password match {
      case Some(newPassword) =>
        val encryptedPass = Codecs.sha1(newPassword)
        val updatedProfile = currentLogin.copy(email = profileInfo.email, password = encryptedPass)
        logins.filter(_.id === currentLogin.id).update(updatedProfile).map(_ == 1)
      case None =>
        val updatedProfile = currentLogin.copy(email = profileInfo.email)
        logins.filter(_.id === currentLogin.id).update(updatedProfile).map(_ == 1)
    }

  def updateLoginObject(updatedLogin: Login): Option[Login] = {
    sync(db.run(logins.filter(_.id === updatedLogin.id).update(updatedLogin)))
    Some(updatedLogin)
  }

  /**
    *
    * @param updatedLogin updatedLogin
    * @return
    */
  def updateLogin(updatedLogin: Login): DBIO[Boolean] = logins.filter(_.id === updatedLogin.id).update(updatedLogin).map(_ == 1)

  def getCreditManagers: Seq[Login] = sync(db.run(logins
    .filter(u => u.role === RoleType.AdminCreditManager || u.role === RoleType.CreditManager
      || u.role === RoleType.CreditAnalyst || u.role === RoleType.SalesManager || u.role === RoleType.SalesTeam)
    .sortBy(s => (s.firstName.asc, s.lastName.asc)).result))

  def getRelationshipManagers: Seq[Login] = sync(db.run(logins
    .filter(u => u.role === RoleType.SalesManager || u.role === RoleType.SalesTeam)
    .sortBy(s => (s.firstName.asc, s.lastName.asc)).result))

  def setAppPushId(userId: String, appPushId: String): Boolean = {
    val query = for (l <- logins.filter(_.id === userId)) yield l.appPushId
    sync(db.run(query.update(Some(appPushId)))) == 1
  }

  def setWebPushId(userId: String, webPushId: String): Boolean = {
    val query = for (l <- logins.filter(_.id === userId)) yield l.webPushId
    sync(db.run(query.update(Some(webPushId)))) == 1
  }

  def findOneLFUserLoginByFirstNameAndLastName(name: String): Option[Login] = sync(db.run(logins.filter(x => (x.firstName ++ x.lastName) === name
    && (x.role === RoleType.AdminCreditManager || x.role === RoleType.CreditManager || x.role === RoleType.CreditAnalyst || x.role === RoleType.SalesManager
    || x.role === RoleType.SalesTeam)).result)).headOption

  def findAllLFUserLoginByFirstNameAndLastName(names: Seq[String]): Option[Login] = sync(db.run(logins.filter(x => ((x.firstName ++ x.lastName) inSetBind names)
    && (x.role === RoleType.AdminCreditManager || x.role === RoleType.CreditManager || x.role === RoleType.CreditAnalyst || x.role === RoleType.SalesManager
    || x.role === RoleType.SalesTeam)).result)).headOption

  def findAllLFUsersByFirstNameAndLastName(names: Seq[String]): Seq[Login] =
    sync(db.run(logins.filter(x => ((x.firstName ++ x.lastName) inSetBind names) && (x.role === RoleType.AdminCreditManager
      || x.role === RoleType.CreditManager || x.role === RoleType.CreditAnalyst || x.role === RoleType.SalesManager || x.role === RoleType.SalesTeam)).result))

  /**
    * update login status to either true or false
    *
    * @param loginId
    * @return
    */
  def updateLoginStatus(loginId: String): DBIO[Boolean] = {
    val query = for (l <- logins.filter(_.id === loginId)) yield l.status
    query.update(false).map(_ == 1)
  }

  /**
    * check if email is existing
    *
    * @param email
    * @return
    */
  def isEmailExisting(email: String): Boolean = sync(db.run(logins.filter(u => u.email.toLowerCase === email.toLowerCase).exists.result))

  def findByRole(role: RoleType): Seq[Login] = sync(db.run(logins.filter(l => l.role === role).result))

  def findOnePushIdByCaseManager(caseManager: String): Option[(String, String)] = {
    val query = sql"""select id, app_push_id from logins where role = 'SalesTeam' and first_name || last_name = $caseManager;""".as[(String, String)]
    sync(db.run(query)).headOption
  }

  def deleteInTransaction(loginId: String): DBIO[Boolean] = logins.filter(x => x.id === loginId).delete.map(_ == 1)

  class LoginMapping(tag: Tag) extends Table[Login](tag, "logins") {
    def * : ProvenShape[Login] = (id, email, password, authcode, expire, profileComplete, createTime, updateTime, firstName,
      lastName, mobile, authType, token, passwordResetKey, status, otpCode, role, appPushId, webPushId, agentId) <> (Login.tupled, Login.unapply _)

    def id = column[String]("id", O.PrimaryKey)

    def email = column[String]("email")

    def password = column[String]("password")

    def authcode = column[String]("authcode")

    def expire = column[Timestamp]("expire")

    def profileComplete = column[Boolean]("profile_complete")

    def createTime = column[Timestamp]("create_time")

    def updateTime = column[Timestamp]("update_time")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def mobile = column[String]("mobile")

    def authType = column[LoginAuthType]("auth_type")

    def token = column[Option[String]]("token")

    def passwordResetKey = column[Option[String]]("password_reset_key")

    def status = column[Boolean]("status")

    def otpCode = column[String]("otp_code")

    def role = column[RoleType]("role")

    def appPushId = column[Option[String]]("app_push_id")

    def webPushId = column[Option[String]]("web_push_id")

    def agentId = column[Option[String]]("agent_id")
  }

}
