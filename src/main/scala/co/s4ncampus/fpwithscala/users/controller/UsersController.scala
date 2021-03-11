package co.s4ncampus.fpwithscala.users.controller

import co.s4ncampus.fpwithscala.users.domain._
import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import co.s4ncampus.fpwithscala.users.domain.User
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

class UsersController[F[_] : Sync] extends Http4sDsl[F] {

  implicit val userDecoder: EntityDecoder[F, User] = jsonOf

  private def createUser(userService: UserService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        val action = for {
          user <- req.as[User]
          result <- userService.create(user).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(UserAlreadyExistsError(existing)) => Conflict(s"The user with legal id ${existing.legalId} already exists")
        }
    }

  private def getUser(userService: UserService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / legalId =>
        userService.get(legalId).value.flatMap {
          case Some(usr) => Ok(usr.asJson)
          case None => NotFound()
        }
    }

  private def deleteUser(userService: UserService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case DELETE -> Root / legalId =>
        val action = userService.delete(legalId).value
        action.flatMap {
          case Right(saved) => saved match {
            case 0 => NotFound("No se encontro")
            case 1 => Ok("Filas eliminadas 1")
            case x => Conflict(s"${x}")
          }
          case Left(err) => Conflict(s"The user with legal id ${err} already exists")
        }
    }

  def endpoints(userService: UserService[F]): HttpRoutes[F] = {
    //To convine routes use the function `<+>`
    createUser(userService) <+> getUser(userService) <+> deleteUser(userService)
  }

}

object UsersController {
  def endpoints[F[_] : Sync](userService: UserService[F]): HttpRoutes[F] =
    new UsersController[F].endpoints(userService)
}