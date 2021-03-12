package co.s4ncampus.fpwithscala.users

import cats.syntax.all._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import domain.User
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestSpec extends AnyFlatSpec with Matchers {

  implicit val UserEncoder: Encoder[User] = deriveEncoder[User]

  // UserEncoder: Encoder[User] = io.circe.generic.encoding.DerivedAsObjectEncoder$$anon$1@4cf75b7f

  trait UserRepo[F[_]] {
    def find(userId: String): F[Option[User]]
  }

  def service[F[_]](repo: UserRepo[F])(
    implicit F: Effect[F]
  ): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "users" / id =>
      repo.find(id).map {
        case Some(user) => Response(status = Status.Ok).withEntity(user.asJson)
        case None => Response(status = Status.NotFound)
      }
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])
              (implicit ev: EntityDecoder[IO, A]): Boolean = {
    val actualResp = actual.unsafeRunSync()
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty)( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync() == expected
    )
    statusCheck && bodyCheck
  }

  val success: UserRepo[IO] = new UserRepo[IO] {
    def find(id: String): IO[Option[User]] = IO.pure(Some(User(Option(1L), "12344", "Estefany", "Holguin", "EstefanyHolguin@seven4n.coMm", "1234567")))
  }

  val response: IO[Response[IO]] = service[IO](success).orNotFound.run(
    Request(method = Method.GET, uri = uri"/user")
  )

  val expectedJson = Json.fromString("{\"legalId\":\"1246\", \"firstName\":\"Cosito\", \"lastName\":\"Alvarez\", \"email\":\"123@seven4n.com\", \"phone\":\"1234567\"}")


  /**
    * TEST GET
    */
  check[Json](response, Status.Ok, Some(expectedJson))


}
