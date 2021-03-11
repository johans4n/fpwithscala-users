package co.s4ncampus.fpwithscala.users.domain

import cats.data.OptionT

trait UserRepositoryAlgebra[F[_]] {
  def create(user: User): F[User]

  def getUser(legalId: String): F[User]

  def delUser(legalId: String):F[Boolean]

  def findByLegalId(legalId: String): OptionT[F, User]

  def updateUser(user: User): F[Boolean]
}