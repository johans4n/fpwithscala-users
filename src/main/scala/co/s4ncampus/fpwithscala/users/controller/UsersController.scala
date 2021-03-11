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
        userService.delete(legalId).flatMap {
          case false => NotFound()
          case true => Ok("Columnas eliminadas: 1")
        }
    }

  private def updateUser(userService: UserService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req@PUT -> Root =>
        val action = for {
          user <- req.as[User]
          result <- userService.update(user)
        } yield (user, result)

        action.flatMap {
          case (user, true) => userService.get(user.legalId).value.flatMap {
            case Some(usr) => Ok(usr.asJson)
            case None => NotFound()
          }
          case (_, false) => NotFound()
        }
    }


  def endpoints(userService: UserService[F]): HttpRoutes[F] = {
    //To convine routes use the function `<+>`
    createUser(userService) <+> getUser(userService) <+> updateUser(userService) <+> deleteUser(userService)
  }

}

object UsersController {
  def endpoints[F[_] : Sync](userService: UserService[F]): HttpRoutes[F] =
    new UsersController[F].endpoints(userService)
}