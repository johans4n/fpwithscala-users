package co.s4ncampus.fpwithscala.users.infraestructure.repository

import co.s4ncampus.fpwithscala.users.domain._

import cats.data._
import cats.syntax.all._
import doobie._
import doobie.implicits._
import cats.effect.Bracket

private object UserSQL {

  def insert(user: User): Update0 =
    sql"""
    INSERT INTO USERS (LEGAL_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE)
    VALUES (${user.legalId}, ${user.firstName}, ${user.lastName}, ${user.email}, ${user.phone})
  """.update

  def selectByLegalId(legalId: String): Query0[User] = sql"""
    SELECT ID, LEGAL_ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE
    FROM USERS
    WHERE LEGAL_ID = $legalId
  """.query[User]

  def updateQuery(user: User): Update0 =
    sql"""
      UPDATE USERS SET LEGAL_ID = ${user.legalId}, FIRST_NAME = ${user.firstName}, LAST_NAME = ${user.lastName}, EMAIL = ${user.email}, PHONE = ${user.phone}
      WHERE LEGAL_ID = ${user.legalId}
    """.update

  def deleteUser(legalId: String): Update0 =
    sql""" DELETE FROM USERS WHERE LEGAL_ID = $legalId
  """.update

}

class DoobieUserRepositoryInterpreter[F[_] : Bracket[?[_], Throwable]](val xa: Transactor[F])
  extends UserRepositoryAlgebra[F] {

  import UserSQL._

  def create(user: User): F[User] =
    insert(user).withUniqueGeneratedKeys[Long]("ID").map(id => user.copy(id = id.some)).transact(xa)

  def findByLegalId(legalId: String): OptionT[F, User] = OptionT(selectByLegalId(legalId).option.transact(xa))

  def getUser(legalId: String): F[User] =
    selectByLegalId(legalId).option.map(usr => usr.get).transact(xa)

  def updateUser(user: User): F[Boolean] = updateQuery(user).run.transact(xa).map(_ == 1)

  def delUser(legalId: String): F[Boolean] = deleteUser(legalId).run.transact(xa).map(_ == 1)


}

object DoobieUserRepositoryInterpreter {
  def apply[F[_] : Bracket[?[_], Throwable]](xa: Transactor[F]): DoobieUserRepositoryInterpreter[F] =
    new DoobieUserRepositoryInterpreter[F](xa)
}