package co.s4ncampus.fpwithscala.users.domain

import cats.data._
import cats.Monad

class UserService[F[_]](repository: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]) {

  def create(user: User)(implicit M: Monad[F]): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user.legalId)
      saved <- EitherT.liftF(repository.create(user))
    } yield saved

  def get(legalId: String): OptionT[F, User] = repository.findByLegalId(legalId)

  def update(legalId: String, user: User): F[Boolean] =
    repository.updateUser(legalId, user)

  def delete(legalId: String): F[Boolean] =
    repository.delUser(legalId)

  def list(): F[List[User]] = repository.listUsers()

}

object UserService {
  def apply[F[_]](
                   repositoryAlgebra: UserRepositoryAlgebra[F],
                   validationAlgebra: UserValidationAlgebra[F],
                 ): UserService[F] =
    new UserService[F](repositoryAlgebra, validationAlgebra)
}