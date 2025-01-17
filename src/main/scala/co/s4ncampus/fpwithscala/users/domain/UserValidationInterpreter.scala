package co.s4ncampus.fpwithscala.users.domain

import cats.Applicative
import cats.data.EitherT

class UserValidationInterpreter[F[_]: Applicative](repository: UserRepositoryAlgebra[F])
    extends UserValidationAlgebra[F] {
  def doesNotExist(legalId:String): EitherT[F, UserAlreadyExistsError, Unit] =
    repository.findByLegalId(legalId).map(UserAlreadyExistsError).toLeft(())


}

object UserValidationInterpreter {
  def apply[F[_]: Applicative](repository: UserRepositoryAlgebra[F]) =
    new UserValidationInterpreter[F](repository)
}