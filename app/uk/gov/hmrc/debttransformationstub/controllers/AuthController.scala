/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.debttransformationstub.controllers

import play.api.Environment
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.debttransformationstub.utils.AuthCredential
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future
import scala.io.Source

class AuthController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) with BaseController {

  def getAccessToken() = Action(parse.tolerantFormUrlEncoded).async { implicit request =>
    Future successful Accepted(
      Source.fromFile(environment.getFile("conf/resources/data/auth/bearer-token.json")).mkString
    )
  }

}
