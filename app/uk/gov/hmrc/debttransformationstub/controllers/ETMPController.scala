package uk.gov.hmrc.debttransformationstub.controllers

import play.api.Environment
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.io.Source

class ETMPController @Inject()(environment: Environment,
                               cc: ControllerComponents)
  extends BackendController(cc) {
  private val getFinancialsCasePath = "conf/resources/data/etmp/getFinancials/"

  def getFinancials(idType: String, idNumber: String,
                    regimeType: String, dateFrom: String,
                    dateTo: String, onlyOpenItems: String,
                    includeLocks: String, calculateAccruedInterest: String,
                    customerPaymentInformation: String): Action[AnyContent] = Action { request =>
    environment.getExistingFile(s"$getFinancialsCasePath$idNumber.json") match {
      case Some(file) =>
        Ok(Source.fromFile(file).mkString)
      case _ =>
        NotFound("file not found") // TODO - use error message from Get financials spec
    }

  }

}
