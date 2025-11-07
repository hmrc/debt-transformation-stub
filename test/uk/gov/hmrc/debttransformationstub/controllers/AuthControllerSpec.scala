package uk.gov.hmrc.debttransformationstub.controllers

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.BaseOneServerPerSuite
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import play.api.test.{ DefaultAwaitTimeout, Helpers }
import play.api.{ Application, Environment }
import uk.gov.hmrc.http.HeaderCarrier

class AuthControllerSpec
    extends AnyFreeSpecLike with BaseOneServerPerSuite with DefaultAwaitTimeout with GuiceOneServerPerSuite
    with ScalaFutures with IntegrationPatience {

  val cc: ControllerComponents = Helpers.stubControllerComponents()
  val env: Environment = app.injector.instanceOf[Environment]
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .build()

  "getAccessToken" - {

    val validBody = Map(
      "client_id"     -> "stub-client-id",
      "client_secret" -> "client_secret1",
      "grant_type"    -> "grant_type1"
    )

    val baseClientCall = client
      .url(s"http://localhost:$port/prweb/PRRestService/oauth2/v1/token")

    "should succeed when the content header type is correct and post body is correct" in {
      val result = baseClientCall
        .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .post(validBody)
        .futureValue

      result.body shouldBe
        """
          |{
          |  "access_token": "non-scheduled-pega-access-token",
          |  "token_type":"bearer",
          |  "expires_in": 3600
          |}
          |""".stripMargin.trim

      result.status shouldBe 202
    }

    "should fail when the content header type is not correct and post body is correct" in {
      val result = baseClientCall
        .withHttpHeaders("Content-Type" -> "application/json")
        .post(validBody)
        .futureValue

      result.body shouldBe
        """
          |{"statusCode":415,"message":"Expecting application/x-www-form-urlencoded body"}
          |""".stripMargin.trim

      result.status shouldBe 415
    }

  }
}
