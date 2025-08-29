/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.models

/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

import play.api.libs.json.{ Format, Json, OFormat }
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequest._

import java.time.LocalDate

case class CdcsCreateCaseRequest(
  plan: CdcsCreateCaseRequestPlan,
  customer: CdcsCreateCaseRequestCustomer,
  taxAccount: CdcsCreateCaseRequestTaxAccount,
  upstreamErrors: List[CdcsUpstreamErrors]
)

object CdcsCreateCaseRequest {

  implicit val format: OFormat[CdcsCreateCaseRequest] = Json.format[CdcsCreateCaseRequest]

  case class CdcsCreateCaseRequestPlan(
    frequency: String,
    payments: List[CdcsCreateCaseRequestPayment],
    instalments: List[CdcsCreateCaseRequestInstalment],
    instalmentStartDate: LocalDate,
    instalmentAmount: BigInt,
    numberOfInstalments: Int,
    planInterest: BigInt,
    totalInterest: BigInt,
    totalDebtIncInt: BigInt,
    interestAccrued: BigInt,
    ttpArrangementId: Option[String],
    AAId: Option[String],
    duration: Option[Int],
    initialPaymentMethod: Option[String],
    initialPaymentReference: Option[String],
    totalLiabilityAmount: Option[BigInt],
    initialPaymentAmount: Option[BigInt],
    initialPaymentDate: Option[LocalDate],
    paymentPlanType: Option[String],
    collectionInfo: Option[List[CdcsCreateCaseRequestCollectionInfo]]
  )

  object CdcsCreateCaseRequestPlan {
    implicit val format: OFormat[CdcsCreateCaseRequestPlan] = Json.format[CdcsCreateCaseRequestPlan]
  }

  case class CdcsCreateCaseRequestInstalment(
    debtItemChargeId: String,
    dueDate: LocalDate,
    amountDue: BigInt,
    expectedPayment: BigInt,
    instalmentNumber: Int,
    instalmentInterestAccrued: BigInt,
    instalmentBalance: BigInt
  )

  object CdcsCreateCaseRequestInstalment {
    implicit val format: OFormat[CdcsCreateCaseRequestInstalment] = Json.format[CdcsCreateCaseRequestInstalment]

  }

  case class CdcsCreateCaseRequestPayment(
    paymentMethod: Option[String],
    paymentReference: Option[String]
  )

  object CdcsCreateCaseRequestPayment {
    implicit val format: OFormat[CdcsCreateCaseRequestPayment] = Json.format[CdcsCreateCaseRequestPayment]
  }

  case class CdcsCreateCaseRequestCollectionInfo(
    initialCollection: Option[CdcsCreateCaseRequestCollection],
    regularCollections: List[CdcsCreateCaseRequestCollection]
  )

  object CdcsCreateCaseRequestCollectionInfo {
    implicit val format: OFormat[CdcsCreateCaseRequestCollectionInfo] = Json.format[CdcsCreateCaseRequestCollectionInfo]

  }

  case class CdcsCreateCaseRequestCollection(
    amountDue: BigInt,
    paymentDueDate: LocalDate
  )

  object CdcsCreateCaseRequestCollection {
    implicit val format: OFormat[CdcsCreateCaseRequestCollection] = Json.format[CdcsCreateCaseRequestCollection]
  }

  case class CdcsCreateCaseRequestUpstreamError(
    sourceSystem: String,
    upstreamErrorCode: Option[String],
    upstreamErrorDescription: Option[String]
  )

  object CdcsCreateCaseRequestUpstreamError {

    implicit val format: OFormat[CdcsCreateCaseRequestUpstreamError] = Json.format[CdcsCreateCaseRequestUpstreamError]
  }

  case class CdcsUpstreamErrors(
    code: String,
    description: String
  )

  object CdcsUpstreamErrors {

    implicit val format: OFormat[CdcsUpstreamErrors] = Json.format[CdcsUpstreamErrors]
  }

  case class CdcsCreateCaseRequestCustomer(individual: CdcsCreateCaseRequestIndividual)

  object CdcsCreateCaseRequestCustomer {
    implicit val format: OFormat[CdcsCreateCaseRequestCustomer] = Json.format[CdcsCreateCaseRequestCustomer]
  }

  case class CdcsCreateCaseRequestCustomerAddress(
    addressType: Int,
    addressLine1: String,
    addressLine2: Option[String],
    addressLine3: Option[String],
    addressLine4: Option[String],
    postCode: Option[String],
    contactDetails: Option[CdcsCreateCaseRequestContactDetails]
  )

  object CdcsCreateCaseRequestCustomerAddress {

    implicit val format: OFormat[CdcsCreateCaseRequestCustomerAddress] =
      Json.format[CdcsCreateCaseRequestCustomerAddress]
  }

  case class CdcsCreateCaseRequestContactDetails(
    telephoneNumber: String,
    email: String
  )

  object CdcsCreateCaseRequestContactDetails {
    implicit val format: OFormat[CdcsCreateCaseRequestContactDetails] = Json.format[CdcsCreateCaseRequestContactDetails]
  }

  case class CdcsCreateCaseRequestIdentification(
    idType: String,
    idValue: String
  )

  object CdcsCreateCaseRequestIdentification {
    implicit val format: OFormat[CdcsCreateCaseRequestIdentification] = Json.format[CdcsCreateCaseRequestIdentification]
  }

  case class CdcsCreateCaseRequestTaxAccount(
    regimeType: Int,
    taxAccountDetails: Option[CdcsCreateCaseRequestTaxAccountDetails],
    debtItems: List[CdcsCreateCaseRequestDebtItem]
  )

  object CdcsCreateCaseRequestTaxAccount {

    implicit val format: OFormat[CdcsCreateCaseRequestTaxAccount] = Json.format[CdcsCreateCaseRequestTaxAccount]
  }

  case class CdcsCreateCaseRequestDebtItem(
    chargeReference: String,
    amount: BigDecimal,
    charges: List[CdcsCreateCaseRequestCharge],
    parentChargeReference: Option[String]
  )

  object CdcsCreateCaseRequestDebtItem {
    implicit val format: OFormat[CdcsCreateCaseRequestDebtItem] = Json.format[CdcsCreateCaseRequestDebtItem]

  }

  case class CdcsCreateCaseRequestCharge(
    mainTrans: String,
    subTrans: String,
    outstandingAmount: BigInt,
    dueDate: String,
    source: Option[String],
    parentMainTrans: Option[String],
    periodFrom: Option[String],
    periodTo: Option[String],
    interestStartDate: Option[String]
  )

  object CdcsCreateCaseRequestCharge {
    implicit val format: OFormat[CdcsCreateCaseRequestCharge] = Json.format[CdcsCreateCaseRequestCharge]
  }

  case class CdcsCreateCaseRequestTaxAccountDetails(districtNumber: Option[String])

  object CdcsCreateCaseRequestTaxAccountDetails {
    implicit val format: OFormat[CdcsCreateCaseRequestTaxAccountDetails] =
      Json.format[CdcsCreateCaseRequestTaxAccountDetails]

  }
}

case class CdcsCreateCaseRequestIndividual(
  lastName: CdcsCreateCaseRequestLastName,
  customerAddresses: List[CdcsCreateCaseRequestCustomerAddress],
  identifications: List[CdcsCreateCaseRequestIdentification],
  title: Option[CdcsCreateCaseRequestTitle],
  initials: Option[CdcsCreateCaseRequestInitials],
  firstName: Option[CdcsCreateCaseRequestFirstName],
  middleName: Option[CdcsCreateCaseRequestMiddleName],
  dateOfBirth: Option[CdcsCreateCaseRequestDateOfBirth]
)

object CdcsCreateCaseRequestIndividual {

  implicit val format: OFormat[CdcsCreateCaseRequestIndividual] = Json.format[CdcsCreateCaseRequestIndividual]
}

final case class CdcsCreateCaseRequestLastName(value: String)
object CdcsCreateCaseRequestLastName {
  def empty: CdcsCreateCaseRequestLastName = CdcsCreateCaseRequestLastName("")
  implicit val format: Format[CdcsCreateCaseRequestLastName] = Json.valueFormat[CdcsCreateCaseRequestLastName]
}
